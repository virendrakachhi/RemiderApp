package com.attendanceapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.attendanceapp.AppConstants;
import com.attendanceapp.models.HCBeaconEmployee;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.WebUtils;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BeaconMonitorService extends Service {
    private static final String TAG = BeaconMonitorService.class.getSimpleName();

    User user;
    private BeaconManager beaconManager;
    SharedPreferences preferences;
    private ArrayList<Beacon> beaconList = new ArrayList<>();

    ArrayList<HCBeaconEmployee> arrayListBeacon = new ArrayList<HCBeaconEmployee>();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        preferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        String userData = preferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);

        try {
            Bundle extras = intent.getExtras();
            arrayListBeacon = (ArrayList<HCBeaconEmployee>) extras.getSerializable("BeaconList");
            System.out.print("");
        } catch (Exception e){
            e.printStackTrace();
        }

        if (userData == null) {
            stopSelf();

        } else {
            // Configure BeaconManager.
            beaconManager = new BeaconManager(this);
            // TODO enable before send
            // Check if device supports Bluetooth Low Energy.
            if (beaconManager.hasBluetooth() && beaconManager.isBluetoothEnabled()) {
                connectToService();
            } else {
                stopSelf();
            }

            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                    System.out.println();
//                    sendPresence(beacons);
                }
            });

            beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
                @Override
                public void onEnteredRegion(Region region, List<Beacon> list) {
                    List<Beacon> beaconListToSend = new ArrayList<Beacon>();
                    if (arrayListBeacon.size() >0){
                        for (int x = 0; x < arrayListBeacon.size(); x++){
                            HCBeaconEmployee beaconEmployee = arrayListBeacon.get(x);
                            for (int i = 0; i < list.size(); i++) {
                                Beacon beacon = list.get(i);
                                if (Integer.parseInt(beaconEmployee.getBeaconMajor()) == beacon.getMajor() &&
                                        Integer.parseInt(beaconEmployee.getBeaconMinor()) == beacon.getMinor()){
                                    beaconListToSend.add(beacon);
                                }
                            }
                        }
                        if (beaconListToSend.size() > 0){
                            sendPresence(beaconListToSend);
                        }
                    }
                }

                @Override
                public void onExitedRegion(Region region) {
                    /*if (arrayListBeacon.size() >0){

                    }*/
                    sendPresence(Collections.<Beacon>emptyList());
                }
            });

            if (arrayListBeacon.size() > 0){
                try {
                    beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS_REGION);
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
//        TODO disable before send
//        createTempList();
//        sendPresence(beaconList);

        return START_STICKY;
    }

    private void createTempList() {
        int i = 1;
        beaconList.add(new Beacon("B9407F30-F5F8-466E-AFF9-25556B57FE6D", "Name " + i, "mac_address_" + i, i, i, i, i));
        i++;
        beaconList.add(new Beacon("B9407F30-F5F8-466E-AFF9-25556B57FE6D", "Name " + i, "mac_address_" + i, i, i, i, i));
        i++;
        beaconList.add(new Beacon("B9407F30-F5F8-466E-AFF9-25556B57FE6D", "Name " + i, "mac_address_" + i, i, i, i, i));
    }

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", "b9407f30-f5f8-466e-aff9-25556b57fe6d", null, null);
    private void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();

                    stopSelf();
                }
            }
        });
    }

    private void sendPresence(final List<Beacon> beacons) {
        String userData = preferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);

        if (userData == null) {
            stopSelf();
        } else {
            user = new Gson().fromJson(userData, User.class);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {

                    HashMap<String, String> map = new HashMap<>();
                    map.put("user_id",user.getUserId());

                    StringBuilder builder = new StringBuilder();
                    Iterator<Beacon> iterator = beacons.iterator();
                    while (iterator.hasNext()) {
                        builder.append(iterator.next().getMacAddress());
                        if (iterator.hasNext()) {
                            builder.append(',');
                        }
                    }
                    map.put("macAddress",builder.toString());

                    try {

                        new WebUtils().post(AppConstants.URL_SEND_PRESENCE_USING_BEACONS, map);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute();
        }


    }

    @Override
    public void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

}
