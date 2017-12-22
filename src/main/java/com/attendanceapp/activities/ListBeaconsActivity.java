package com.attendanceapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.R;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

//import android.support.v7.app.AppCompatActivity;

/**
 * Displays list of found beacons sorted by RSSI.
 * Starts new activity with selected beacon if activity was provided.
 *
 * @author wiktor.gworek@estimote.com (Wiktor Gworek)
 */
public class ListBeaconsActivity extends Activity {

    private static final String TAG = ListBeaconsActivity.class.getSimpleName();
    public static final String RESPONSE_SELECTED_BEACONS = "RESPONSE_SELECTED_BEACONS";
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    public static final String EXTRA_BEACONS = "EXTRA_BEACONS";

    TreeSet<Integer> selectIndexSet = new TreeSet<>();
    TreeSet<Integer> editSelectIndexSet = new TreeSet<>();

    private BeaconManager beaconManager;
    private BeaconListAdapter editAdapter;
    private BeaconListAdapter adapter;
    private Toolbar toolbar;
    SparseBooleanArray sp;
    SparseBooleanArray editsp = new SparseBooleanArray();
    Button save;
    List<Beacon> beaconList = new ArrayList<>();
    List<Beacon> editbeaconList = new ArrayList<>();
    ListView beaconListView;
    ListView editBeaconListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Configure device list.
        beaconListView = (ListView) findViewById(R.id.device_list);
        editBeaconListView = (ListView) findViewById(R.id.selected_device_list);

        adapter = new BeaconListAdapter(this);
        editAdapter = new BeaconListAdapter(this);

        beaconListView.setAdapter(adapter);
        editBeaconListView.setAdapter(editAdapter);

        beaconListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sp = beaconListView.getCheckedItemPositions();

                ViewHolder viewHolder = (ViewHolder) view.getTag();

                if (selectIndexSet.contains(position)) {
                    selectIndexSet.remove(position);
                    viewHolder.group.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    selectIndexSet.add(position);
                    viewHolder.group.setBackgroundColor(getResources().getColor(R.color.beacon_list_back));
                }

            }
        });

        editBeaconListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editsp = editBeaconListView.getCheckedItemPositions();

                ViewHolder viewHolder = (ViewHolder) view.getTag();

                if (editSelectIndexSet.contains(position)) {
                    editSelectIndexSet.remove(position);
                    viewHolder.group.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    editSelectIndexSet.add(position);
                    viewHolder.group.setBackgroundColor(getResources().getColor(R.color.beacon_list_back));
                }

            }
        });

        // Configure BeaconManager.
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        toolbar.setSubtitle("Found beacons: " + beacons.size());

                        beaconList.clear();
                        beaconList.addAll(beacons);

                        adapter.replaceWith(beacons);

                        // TODO temp
//                        createTempList();

                    }
                });
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

            }

            @Override
            public void onExitedRegion(Region region) {

            }
        });

        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(saveListener);

        List<Beacon> arrayListExtra = getIntent().getParcelableArrayListExtra(EXTRA_BEACONS);

        if (arrayListExtra != null && arrayListExtra.size() > 0) {
            editbeaconList.addAll(arrayListExtra);
            editAdapter.replaceWith(editbeaconList);
            findViewById(R.id.selected_devices_layout).setVisibility(View.VISIBLE);
        }
    }

    View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONArray jsonArray = new JSONArray();

            if ((sp == null || sp.size() < 1) && (editsp == null || editsp.size() < 1)) {
                Toast.makeText(getApplicationContext(), "Please select beacon before save", Toast.LENGTH_LONG).show();

            } else {
                for (int i = 0; i < sp.size(); i++) {
                    if (sp.valueAt(i)) {
                        Beacon positionBeacon = beaconList.get(sp.keyAt(i));

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("proximityUUID", positionBeacon.getProximityUUID() != null ? positionBeacon.getProximityUUID() : "");
                            jsonObject.put("name", positionBeacon.getName() != null ? positionBeacon.getName() : "");
                            jsonObject.put("macAddress", positionBeacon.getMacAddress() != null ? positionBeacon.getMacAddress() : "");
                            jsonObject.put("major", positionBeacon.getMajor() != 0 ? positionBeacon.getMajor() : "");
                            jsonObject.put("minor", positionBeacon.getMinor() != 0 ? positionBeacon.getMinor() : "");
                            jsonObject.put("measuredPower", positionBeacon.getMeasuredPower() != 0 ? positionBeacon.getMeasuredPower() : "");
                            jsonObject.put("rssi", positionBeacon.getRssi());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonArray.put(jsonObject);
                    }
                }
                if (editsp != null) {
                    for (int i = 0; i < editsp.size(); i++) {
                        if (editsp != null && editsp.valueAt(i)) {
                            Beacon positionBeacon = editbeaconList.get(editsp.keyAt(i));

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("proximityUUID", positionBeacon.getProximityUUID() != null ? positionBeacon.getProximityUUID() : "");
                                jsonObject.put("name", positionBeacon.getName() != null ? positionBeacon.getName() : "");
                                jsonObject.put("macAddress", positionBeacon.getMacAddress() != null ? positionBeacon.getMacAddress() : "");
                                jsonObject.put("major", positionBeacon.getMajor() != 0 ? positionBeacon.getMajor() : "");
                                jsonObject.put("minor", positionBeacon.getMinor() != 0 ? positionBeacon.getMinor() : "");
                                jsonObject.put("measuredPower", positionBeacon.getMeasuredPower() != 0 ? positionBeacon.getMeasuredPower() : "");
                                jsonObject.put("rssi", positionBeacon.getRssi());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            jsonArray.put(jsonObject);
                        }
                    }
                }

                System.out.println("jsonArray = " + jsonArray);

                Intent intent = new Intent();
                intent.putExtra(RESPONSE_SELECTED_BEACONS, jsonArray.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO temp
//        createTempList();

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }

    @Override
    protected void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                toolbar.setSubtitle("Bluetooth not enabled");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        toolbar.setSubtitle("Scanning...");
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(ListBeaconsActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }


    private void createTempList() {
        for (int i = 1; i <= 20; i++) {
            beaconList.add(new Beacon("B9407F30-F5F8-466E-AFF9-25556B57FE6D", "Name " + i, "mac_address_" + i, i, i, i, i));
        }
        adapter.replaceWith(beaconList);
    }

    class BeaconListAdapter extends BaseAdapter {

        private ArrayList<Beacon> beacons;
        private LayoutInflater inflater;

        public BeaconListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            this.beacons = new ArrayList<>();
        }

        public void replaceWith(Collection<Beacon> newBeacons) {
            this.beacons.clear();
            this.beacons.addAll(newBeacons);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return beacons.size();
        }

        @Override
        public Beacon getItem(int position) {
            return beacons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = inflateIfRequired(view);
            bind(getItem(position), view, position);
            return view;
        }

        private void bind(Beacon beacon, View view, int position) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.macTextView.setText(String.format("MAC: %s (%.2fm)", beacon.getMacAddress(), Utils.computeAccuracy(beacon)));
            holder.majorTextView.setText("Major: " + beacon.getMajor());
            holder.minorTextView.setText("Minor: " + beacon.getMinor());
            holder.measuredPowerTextView.setText("MPower: " + beacon.getMeasuredPower());
            holder.rssiTextView.setText("RSSI: " + beacon.getRssi());
            holder.group.setBackgroundColor(getResources().getColor(selectIndexSet.contains(position) ? R.color.beacon_list_back : R.color.white));
        }

        @SuppressLint("InflateParams")
        private View inflateIfRequired(View view) {
            if (view == null) {
                view = inflater.inflate(R.layout.beacon_item, null);
                view.setTag(new ViewHolder(view));
            }
            return view;
        }

    }

    public class ViewHolder {
        final TextView macTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView measuredPowerTextView;
        final TextView rssiTextView;
        public final LinearLayout group;

        ViewHolder(View view) {
            macTextView = (TextView) view.findViewWithTag("mac");
            majorTextView = (TextView) view.findViewWithTag("major");
            minorTextView = (TextView) view.findViewWithTag("minor");
            measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
            rssiTextView = (TextView) view.findViewWithTag("rssi");
            group = (LinearLayout) view.findViewWithTag("group");
        }
    }

}
