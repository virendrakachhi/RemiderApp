package com.attendanceapp.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.attendanceapp.AppConstants;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.WebUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class AppMonitorService extends Service {

    public AppMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new CountDownTimer(Long.MAX_VALUE, 1000 * 60) {
            public void onTick(long millisUntilFinished) {

                SharedPreferences preferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
                String userData = preferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);

                if (userData != null) {
                    final User user = new Gson().fromJson(userData, User.class);

                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    @SuppressWarnings("deprecation") List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                    Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    String packageName = componentInfo.getPackageName();

//                    Toast.makeText(AppMonitorService.this, "" + packageName, Toast.LENGTH_SHORT).show();

                    if (!packageName.contains("com.attendanceapp")) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                HashMap<String, String> map = new HashMap<>();

                                map.put("user_id", user.getUserId());
                                map.put("email", user.getEmail());
                                try {
                                    new WebUtils().post(AppConstants.URL_SEND_NOTIFICATION_IF_USING_MOBILE, map);
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
