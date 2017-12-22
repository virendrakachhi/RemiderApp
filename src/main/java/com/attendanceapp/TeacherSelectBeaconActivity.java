package com.attendanceapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
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

import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.WebUtils;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;


public class TeacherSelectBeaconActivity extends Activity {

    public static final String RESPONSE_SELECTED_BEACONS = "RESPONSE_SELECTED_BEACONS";
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static final String TAG = TeacherSelectBeaconActivity.class.getSimpleName();

    ListView beaconListView;
    Button save;
    TextView toolbar;

    TreeSet<Integer> selectIndexSet = new TreeSet<>();
    List<Beacon> beaconList = new ArrayList<>();
    ListAdapter listAdapter;
    private BeaconManager beaconManager;
    SparseBooleanArray sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_select_beacon);

        toolbar = (TextView) findViewById(R.id.header);
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringBuilder stringBuilder = new StringBuilder();

                if (sp == null || sp.size() < 1) {
                    Toast.makeText(getApplicationContext(), "Please select beacon before save", Toast.LENGTH_LONG).show();

                } else {
                    for (int i = 0; i < sp.size(); i++) {
                        if (sp.valueAt(i)) {
                            stringBuilder.append(beaconList.get(sp.keyAt(i)).getMacAddress());
                            if (i < sp.size() - 1) {
                                stringBuilder.append(",");
                            }
                        }
                    }

                    Toast.makeText(getApplicationContext(), "Selected beacons: " + stringBuilder.toString(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.putExtra(RESPONSE_SELECTED_BEACONS, stringBuilder.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        beaconListView = (ListView) findViewById(R.id.beaconListView);
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
//                        getActionBar().setSubtitle("Found beacons: " + beacons.size());
                        beaconList.clear();
                        beaconList.addAll(beacons);
                        Toast.makeText(TeacherSelectBeaconActivity.this, "Found beacons: " + beacons.size(), Toast.LENGTH_LONG).show();
                        setAdapter();
                    }
                });
            }
        });

//        TODO temp function, delete before send to client
//        createTempList();
//        setAdapter();

        updateBeaconList();
    }

//    private void createTempList() {
//        for (int i = 1; i <= 20; i++) {
//            beaconList.add(new Beacon("B9407F30-F5F8-466E-AFF9-25556B57FE6D", "Name " + i, "mac_address_" + i, i, i, i, i));
//        }
//    }

    private void setAdapter() {
        listAdapter = new ListAdapter(TeacherSelectBeaconActivity.this, beaconList);
        beaconListView.setAdapter(listAdapter);
    }

    private class ViewHolder {
        TextView beaconName, beaconMacAddress;
        LinearLayout group;
    }

    private class ListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        //        private Context context;
        private List<Beacon> beaconList;

        public ListAdapter(Context context, List<Beacon> beaconList) {
            this.beaconList = beaconList;
//            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return beaconList.size();
        }

        @Override
        public Beacon getItem(int position) {
            return beaconList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_beacon, null, false);
                holder = new ViewHolder();

                holder.group = (LinearLayout) view.findViewById(R.id.group);
                holder.beaconName = (TextView) view.findViewById(R.id.beacon_name);
                holder.beaconMacAddress = (TextView) view.findViewById(R.id.beacon_mac);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Beacon beacon = beaconList.get(position);
            holder.group.setBackgroundColor(getResources().getColor(selectIndexSet.contains(position) ? R.color.beacon_list_back : R.color.white));
            holder.beaconName.setText(beacon.getName());
            holder.beaconMacAddress.setText(beacon.getMacAddress());

            return view;
        }
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    private void updateBeaconList() {
        new AsyncTask<Void,Void,String>(){

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return WebUtils.getBeaconsJson();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s == null) {

                } else {
                    beaconList = DataUtils.getBeaconListFromJson(s);
                    if (beaconList.size() == 0) {
                        Toast.makeText(TeacherSelectBeaconActivity.this, "Please purchase beacons", Toast.LENGTH_LONG).show();
                    } else {
                        setAdapter();
                    }
                }

            }

        }.execute();
    }


    @Override protected void onDestroy() {
        beaconManager.disconnect();

        super.onDestroy();
    }

    @Override protected void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        super.onStop();
    }


    @Override protected void onStart() {
        super.onStart();

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

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                toolbar.setText("Bluetooth not enabled");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void connectToService() {
        toolbar.setText("Scanning...");
        beaconList.clear();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(TeacherSelectBeaconActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }

}
