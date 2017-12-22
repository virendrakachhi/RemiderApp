package com.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Toast;

import com.attendanceapp.utils.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class SelectLocationActivity extends Activity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyC2v7hipps8kR8cklOiMO0Jrf6opMea6GI";
    private static final String LOG_TAG = SelectLocationActivity.class.getSimpleName();

    // Google Map
    private GoogleMap googleMap;
    MarkerOptions markerOptions;
    LatLng latLng;
    AutoCompleteTextView autoCompleteTextView;
    GPSTracker gpsTracker;
    ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        // searchEditText = (EditText) findViewById(R.id.searchBox);
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = autoCompleteTextView.getText().toString().trim();
                if (str.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter text before search", Toast.LENGTH_SHORT).show();
                    return;
                }
                new GeocoderTask().execute(str);
            }
        });

        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item_map_location));
        autoCompleteTextView.setOnItemClickListener(this);

        gpsTracker = new GPSTracker(SelectLocationActivity.this);

        try {
            // Loading map
            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        new GeocoderTask().execute(str);
    }


    /**
     * function to load map. If map is not created it will create it for you
     */
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
                return;
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);


//
//            CameraUpdate center= CameraUpdateFactory.newLatLng(coordinate);
//            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
//            googleMap.moveCamera(center);
//            googleMap.animateCamera(zoom);

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    double lat = latLng.latitude;
                    double lng = latLng.longitude;

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_LATITUDE, lat);
                    intent.putExtra(EXTRA_LONGITUDE, lng);

                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    LatLng latLng = marker.getPosition();

                    double lat = latLng.latitude;
                    double lng = latLng.longitude;

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_LATITUDE, lat);
                    intent.putExtra(EXTRA_LONGITUDE, lng);

                    setResult(RESULT_OK, intent);
                    finish();

                    return true;
                }
            });

            if (gpsTracker.canGetLocation()) {
                Location location = gpsTracker.getLocation();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng markerLoc = new LatLng(latitude, longitude);
                final CameraPosition cameraPosition = new CameraPosition.Builder().target(markerLoc).zoom(16).bearing(0).tilt(30).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initializeMap();
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            try {

                if (addresses == null || addresses.size() == 0) {
                    Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
                }

                // Clears all the existing markers on the map
                googleMap.clear();

                // Adding Markers on Google Map for each matching address
                for (int i = 0; i < addresses.size(); i++) {

                    Address address = addresses.get(i);

                    // Creating an instance of GeoPoint, to display in Google Map
                    latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    String addressText = String.format("%s, %s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                            address.getCountryName());

                    markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(addressText);

                    googleMap.addMarker(markerOptions);

                    // Locate the first location
                    if (i == 0)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            //sb.append("&components=country:"+countryCode);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}


//
//package com.attendanceapp;
//
//import android.app.Activity;
//        import android.content.Intent;
//        import android.os.Bundle;
//        import android.view.Window;
//        import android.view.WindowManager;
//        import android.widget.Toast;
//
//        import com.google.android.gms.maps.GoogleMap;
//        import com.google.android.gms.maps.MapFragment;
//        import com.google.android.gms.maps.model.LatLng;
//
//
//public class SelectLocationActivity extends Activity {
//
//    public static final String EXTRA_LATITUDE = "extra_latitude";
//    public static final String EXTRA_LONGITUDE = "extra_longitude";
//
//    // Google Map
//    private GoogleMap googleMap;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_select_location);
//
//        try {
//            // Loading map
//            initializeMap();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * function to load map. If map is not created it will create it for you
//     */
//    private void initializeMap() {
//        if (googleMap == null) {
//            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
//
//            // check if map is created successfully or not
//            if (googleMap == null) {
//                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            googleMap.setMyLocationEnabled(true);
//            googleMap.getUiSettings().setZoomControlsEnabled(true);
//            googleMap.getUiSettings().setZoomGesturesEnabled(true);
//            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                @Override
//                public void onMapClick(LatLng latLng) {
//                    double lat = latLng.latitude;
//                    double lng = latLng.longitude;
//
//                    Intent intent = new Intent();
//                    intent.putExtra(EXTRA_LATITUDE, lat);
//                    intent.putExtra(EXTRA_LONGITUDE, lng);
//
//                    setResult(RESULT_OK, intent);
//                    finish();
//                }
//            });
//
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        initializeMap();
//    }
//}
//
//
//