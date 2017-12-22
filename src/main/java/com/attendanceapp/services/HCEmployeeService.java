package com.attendanceapp.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.AppGlobals;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.WebUtils;
import com.attendanceapp.webserviceCommunicator.WebServiceHandler;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class HCEmployeeService extends Service {
    String locationID, managerID, userId;
    boolean status = false;
    private static ArrayList<Boolean> attendenceSubmitArry = new ArrayList<Boolean>();
    GPSTracker gpsTracker;
    List<HCModuleDayWeekMonthYearViewBean> scheduleList;
    String locationSetup, locationType;

    float meters;

    public HCEmployeeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
//        new CountDownTimer(Long.MAX_VALUE, 1000 * 60 * 15  ) {
        new CountDownTimer(Long.MAX_VALUE, 1000 * 60 * 1) {
            public void onTick(long millisUntilFinished) {

                SharedPreferences preferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

                String userData = preferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);
                if (userData != null) {
                    final User emp = new Gson().fromJson(userData, User.class);

                    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    @SuppressWarnings("deprecation") List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                    Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    String packageName = componentInfo.getPackageName();
                    if (emp != null) {
                        if (intent != null) {
                            Bundle extras = intent.getExtras();
                            if (extras == null) {
                                Log.d("Service", "null");
                                return;
                            } else {
                                if (emp.getUserRoles().get(0).getRole() == UserRole.EmployeeHC.getRole()) {
                                    Log.d("Service", "not null");

                                    locationID = extras.getString("LocationId");
                                    managerID = extras.getString("ManagerId");

                                    Double Longitude = extras.getDouble("Longitude");
                                    Double Latitude = extras.getDouble("Latitude");

                                    if (extras.getString("LocationType") != null) {
                                        locationType = extras.getString("LocationType");
                                    }

                                    if (extras.getString("LocationSetup") != null) {
                                        locationSetup = extras.getString("LocationSetup");
                                    }

                                    //gpsTracker = new GPSTracker(getApplicationContext());

                                    //if(gpsTracker.canGetLocation()) {
                                    if (AppGlobals.mCurrentLocation != null) {

                                        double longitude = 0.0, latitude = 0.0;

                                        // Location location = gpsTracker.getUpdateLocation();

                                        Location location = AppGlobals.mCurrentLocation;

                                        if (location != null) {
                                            Location currentLocation = new Location("currentLocation");
                                            currentLocation.setLatitude(location.getLatitude());
                                            currentLocation.setLongitude(location.getLongitude());

                                            Location scheduledLocation = new Location("scheduledLocation");
                                            scheduledLocation.setLatitude(Latitude);
                                            scheduledLocation.setLongitude(Longitude);

                                            Log.e("current lat", location.getLatitude() + "<<>>" + location.getLongitude());

                                            Log.e("scheduledLocation lat", Latitude + "<<>>" + Longitude);

                                            userId = emp.getUserId();
                                            meters = scheduledLocation.distanceTo(currentLocation);

                                            Toast.makeText(getApplicationContext(), "You are   " + Float.toString(meters) + "  meters away from your scheduled location. ", Toast.LENGTH_LONG).show();

                                            getScheduling(meters);
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Please enable your GPS to update your Attendance", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            public void onFinish() {
            }

        }.start();

        return START_STICKY;
    }

    void matchDates(float m) {
        try {
            Calendar cal = Calendar.getInstance();
            Date date = new Date();
            for (int i = 0; i < scheduleList.size(); i++) {
                String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
                int checkDate = modifiedDate.compareTo(scheduleList.get(i).date);
//            int checkDate2 = modifiedDate.compareTo(scheduleList.get(i).endDate);
//            Toast.makeText(getApplicationContext(),scheduleList.get(i).date+"\n"+modifiedDate+"\n"+Integer.toString(checkDate),Toast.LENGTH_LONG).show();
                if (checkDate == 0) {
                    SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss aa");
//                sdfTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                    String time = sdfTime.format(date);
//    long time = date.getTime();
                    String startTime = scheduleList.get(i).startTime;
                    String endTime = scheduleList.get(i).endTime;
                    if (userId.matches(scheduleList.get(i).employeeID)) {

                        if ((m < 20.0f && m >= 0.f)) {

//                    SimpleDateFormat startTime2 = new SimpleDateFormat("h:mm:ss a");
//                    SimpleDateFormat endTime2 = new SimpleDateFormat("h:mm:ss a");

//                    String strTimeStart = "4:30:10 p";
//                    String strTimEnd = "5:30:10 p";

                            try {
                                String dd = sdfTime.format(new Date());
                                Date ddd = sdfTime.parse(dd);
                                Date date1 = sdfTime.parse(startTime);
                                Date date2 = sdfTime.parse(endTime);

//                        Toast.makeText(getApplicationContext(),ddd.toString()+"\n"+date1.toString()+"\n"+date2.toString(),Toast.LENGTH_LONG).show();


//                        if (ddd.after(date1) || ddd.equals(date1)) {
                                Log.v("The date1 is >>>>", "smallercasnklcwknkw");
                                if (ddd.after(date1) && ddd.before(date2)) {

                                    new AsyncAddAttendenceByBeacon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, scheduleList.get(i).locationID,
                                            scheduleList.get(i).managerID, scheduleList.get(i).employeeID,
                                            scheduleList.get(i).startDate, "P", scheduleList.get(i).scheduleID);

                                    if (!attendenceSubmitArry.get(i)) {
                                        updateAttendance("P", modifiedDate, scheduleList.get(i).scheduleID, i);
                                    }
                                    if (!status)
                                        sendNotificationToManager(time, scheduleList.get(i).scheduleID);


                                    Log.v("The date1 is >>>>", "smaller");
                                    break;
                                }
//                            else
//                            {
//                                updateAttendance("A",modifiedDate);
//                            }

//                        }


                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


//                    Toast.makeText(getApplicationContext(),"Attendance Updated",Toast.LENGTH_LONG).show();


                        } else {

                            try {
                                String dd = sdfTime.format(new java.util.Date());
                                Date ddd = sdfTime.parse(dd);
                                Date date1 = sdfTime.parse(startTime);
                                Date date2 = sdfTime.parse(endTime);

//                        Toast.makeText(getApplicationContext(),ddd.toString()+"\n"+date1.toString()+"\n"+date2.toString(),Toast.LENGTH_LONG).show();


//                        if (ddd.after(date1) || ddd.equals(date1)) {
                                if (ddd.before(date2) || ddd.equals(date2)) {


                                    updateAttendance("A", modifiedDate, scheduleList.get(i).scheduleID, i);
//                                if(!status)
//                                    sendNotificationToManager(time,scheduleList.get(i).scheduleID);
                                    new AsyncAddAttendenceByBeacon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, scheduleList.get(i).locationID,
                                            scheduleList.get(i).managerID, scheduleList.get(i).employeeID,
                                            scheduleList.get(i).startDate, "A", scheduleList.get(i).scheduleID);

                                    Log.v("The date1 is >>>>", "smaller");
                                    break;
                                }
//                        }


                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }

                    }


//                Toast.makeText(getApplicationContext(),time+"\n"+startTime,Toast.LENGTH_LONG).show();
//                int checkTime = time.compareTo(startTime);
//                int checkTime2 = time.compareTo(endTime);
//                if(checkTime<=0 && checkTime2 >= 0 )
//                {
//        Toast.makeText(getApplicationContext(),"Time Match Found",Toast.LENGTH_LONG).show();
//
//                }
//                else
//                {
//
////sendMessage("Late by 30 Minutes");
//                }

//                break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//get Connected Wifi Name

    public String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);


//        manager.getConnectionInfo()
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }


// get scheduling


    private void getScheduling(final float m) {
        new AsyncTask<Void, Void, String>() {
//            private ProgressDialog progressDialog = new ProgressDialog(EmployeeHCDashboardActivity.this);

            @Override
            protected void onPreExecute() {
//                progressDialog.setMessage("Please wait...");
//                progressDialog.setCancelable(false);
//                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                scheduleList = new LinkedList<>();

                hm.put("location_id", locationID);
//                hm.put("location_id","80");
//                hm.put("role", String.valueOf(userRole.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_EMPLOYEE_SCHEDULE, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
//                progressDialog.dismiss();
                if (result != null) {
                    try {
                        JSONObject jsn = new JSONObject(result);
                        if (jsn.getString("Message").matches("Records Found successfully")) {
                            JSONArray jsnObj = jsn.getJSONArray("Data");
                            for (int i = 0; i < jsnObj.length(); i++) {
                                HCModuleDayWeekMonthYearViewBean bean = new HCModuleDayWeekMonthYearViewBean();
//                                bean.startDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("startDate");
//                                bean.endDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("endDate");
                                bean.date = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("date");

                                bean.startTime = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("begin_time");
                                bean.endTime = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("end_time");
                                bean.scheduleID = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("id");
                                bean.employeeID = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("employ_id");
                                bean.managerID = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("user_id");
                                if (i >= attendenceSubmitArry.size()) {
                                    attendenceSubmitArry.add(false);
                                }
                                bean.locationID = jsnObj.getJSONObject(i).getJSONObject("Location").getString("id");
                                bean.locationName = jsnObj.getJSONObject(i).getJSONObject("Location").getString("locationName");
                                scheduleList.add(bean);

                            }
                            matchDates(m);
                            return;
                        } else {
                            scheduleList.add(new HCModuleDayWeekMonthYearViewBean());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        }.execute();

    }


    //send notification to Manager

    private void sendNotificationToManager(String time, String scheID) {
//        user = userUtils.getUserFromSharedPrefs();
        final String message = time;
        if (message.length() < 1) {
            return;
        }
        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("current_time", message);
//        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("location_id", locationID);
        stringStringHashMap.put("schedule_id", scheID);
        stringStringHashMap.put("teacher_id", userId);
        stringStringHashMap.put("user_id", managerID);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(AppConstants.URL_SEND_NOTIFICATION_TO_MANAGER, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    status = true;
//                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
//                    String currentDateandTime = sdf.format(new Date());
//                    HCModuleNotifications obj = new HCModuleNotifications();
//                    obj.message = message;
//                    obj.messageDate = currentDateandTime;
////                    obj.senderName = user.getUsername();
////                   obj.receiverName = "reciever";
//                    hcModuleNotificationsArrayList.add(obj);
//                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();


    }


    private void updateAttendance(String attend, String scheduleDate, String scheID, final int i) {
//        user = userUtils.getUserFromSharedPrefs();

        final String message = attend;
        if (message.length() < 1) {
            return;
        }
        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("attend", message);
//        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("location_id", locationID);
        stringStringHashMap.put("schedule_date", scheduleDate);
        stringStringHashMap.put("employee_id", userId);
        stringStringHashMap.put("employee_schedule_id", scheID);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(AppConstants.URL_UPDATE_ATTENDANCE_OF_EMP, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.v("Result", "result=== " + result);
                    try {

                        JSONObject object = new JSONObject(result);
                        String message = object.optString("Message");
                        if (message.equalsIgnoreCase("Attendance Already Updated") || message.equalsIgnoreCase("Attendance Updated")) {
                            attendenceSubmitArry.set(i, true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
//                    String currentDateandTime = sdf.format(new Date());
//                    HCModuleNotifications obj = new HCModuleNotifications();
//                    obj.message = message;
//                    obj.messageDate = currentDateandTime;
////                    obj.senderName = user.getUsername();
////                   obj.receiverName = "reciever";
//                    hcModuleNotificationsArrayList.add(obj);
//                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();


    }

    /*new AsyncAddAttendenceByBeacon().execute(scheduleList.get(schedulePosition).locationID,
            scheduleList.get(schedulePosition).managerID, scheduleList.get(schedulePosition).employeeID,
            scheduleList.get(schedulePosition).startDate, "P", scheduleList.get(schedulePosition).scheduleID);*/

    private class AsyncAddAttendenceByBeacon extends AsyncTask<String, String, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //        location_id, user_id, employee_id, startDate (yyyy-mm-dd) ,attends
        @Override
        protected String doInBackground(String... strings) {
//            2016-04-12 04:04:58
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String currentDateandTime = sdf.format(new Date());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("location_id", strings[0]));
            nameValuePairs.add(new BasicNameValuePair("user_id", strings[1]));
            nameValuePairs.add(new BasicNameValuePair("employee_id", strings[2]));
            nameValuePairs.add(new BasicNameValuePair("startDate", strings[3]));
            nameValuePairs.add(new BasicNameValuePair("attend", strings[4]));
            nameValuePairs.add(new BasicNameValuePair("employee_schedule_id", strings[5]));
            nameValuePairs.add(new BasicNameValuePair("datetime", currentDateandTime));

            response = new WebServiceHandler().webServiceCall(nameValuePairs, AppConstants.URL_ADD_ATTENDENCE_BY_BEACON);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
