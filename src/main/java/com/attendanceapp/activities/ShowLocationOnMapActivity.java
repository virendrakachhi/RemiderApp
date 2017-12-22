package com.attendanceapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.attendanceapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowLocationOnMapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_LAT = "EXTRA_LAT";
    public static final String EXTRA_LNG = "EXTRA_LNG";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";

    private double latitude, longitude;
    private String title = "";

//    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
//    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
//    private static final String OUT_JSON = "/json";
//    private static final String API_KEY = "AIzaSyCo-jx1ybX5ZqNzwYht2BNROJDhQu27P5I";

    // Google Map
    private GoogleMap googleMap;
    MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location_on_map);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(EXTRA_LAT, 0);
        longitude = intent.getDoubleExtra(EXTRA_LNG, 0);
        title = intent.getStringExtra(EXTRA_TITLE);

    }


    @Override
    public void onMapReady(GoogleMap map) {
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(sydney).title(title));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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


//            CameraUpdate center= CameraUpdateFactory.newLatLng(coordinate);
//            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
//            googleMap.moveCamera(center);
//            googleMap.animateCamera(zoom);


            LatLng markerLoc = new LatLng(latitude, longitude);
            final CameraPosition cameraPosition = new CameraPosition.Builder().target(markerLoc).zoom(16).bearing(0).tilt(30).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }
    }
}
