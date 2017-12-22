package com.attendanceapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;

import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.WebUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

public class SendLocationService extends Service {
    GPSTracker gpsTracker;
    User user;

    @Override
        public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsTracker = new GPSTracker(getApplicationContext());
        SharedPreferences preferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        String userData = preferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);
        if (userData == null) {
            stopSelf();
        }
        user = new Gson().fromJson(userData, User.class);

        new CountDownTimer(Long.MAX_VALUE, 1000 * 60) {

            public void onTick(long millisUntilFinished) {
                if (gpsTracker.canGetLocation()) {
                    Location location = gpsTracker.getLocation();

                    if (location != null) {
                        final double latitude = location.getLatitude();
                        final double longitude = location.getLongitude();

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                HashMap<String, String> map = new HashMap<>();
                                // user_id, longitude, latitude
                                map.put("user_id", String.valueOf(user.getUserId()));
                                map.put("latitude", String.valueOf(latitude));
                                map.put("longitude", String.valueOf(longitude));
                                try {
                                    new WebUtils().post(AppConstants.URL_SEND_LOCATION_UPDATE, map);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }.execute();

                    }
                }
            }

            public void onFinish() {
            }
        }.start();

        return START_STICKY;
    }
}
