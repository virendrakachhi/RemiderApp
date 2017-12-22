package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.adapters.HCModuleDayWeekMonthYearListAdapter;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jagdeep.singh on 1/22/2016.
 */
public class HcModuleShowClientTotalHoursActivity extends Activity implements View.OnClickListener {

    TextView dayTxt, weekTxt, monthTxt, yearTxt;
    ListView totalhoursList;
    Context context;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;

//    List<Attendance> stringArrayList = new ArrayList<>();
//    List<Attendance> absentArrayList = new ArrayList<>();

    Button downloadButton;
    String locationId;
    LinearLayout timeSelectorHeader, heading;

    List<HCModuleDayWeekMonthYearViewBean> scheduleArrayList = new ArrayList<>();

    HCModuleDayWeekMonthYearListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_show_client_total_hours_activity);

        // list_item_hc_module_day_week_month_year
        // totalhoursList
        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();

        context = getApplicationContext();
        locationId = getIntent().getStringExtra("LocationID");

        timeSelectorHeader = (LinearLayout) findViewById(R.id.timeSelectorHeader);
        timeSelectorHeader.setVisibility(View.GONE);


        totalhoursList = (ListView) findViewById(R.id.totalhoursList);

        downloadButton = (Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this);


        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);


        // Setting Inittial Colors
        scheduleArrayList.clear();
//        updateData();
        updateDataAsync();
//        HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();
//        for (int i = 0; i < 7; i++) {
//            obj.date = "Day " + i;
//            obj.heading = "Service By Employee " + i;
//            obj.scheduleDetails = 3 + i * 2 + "";
//            scheduleArrayList.add(obj);
//        }

//        setAdapter();


//        setAdapter();
    }

    @Override
    public void onClick(View v) {
//
        switch (v.getId()) {


            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;

//
//            case R.id.daytxt:
//                processDayButton();
//
//                break;
//
//            case R.id.weektxt:
//                processWeekButton();
//
//                break;
//
//            case R.id.monthtxt:
//                processMonthButton();
//
//                break;
//
//            case R.id.yeartxt:
//                processYearButton();
//
//                break;
//
            case R.id.downloadButton:

                Toast.makeText(HcModuleShowClientTotalHoursActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();

                txtExportReport();
                break;
//
//
//
//
        }
//
    }

    private void txtExportReport() {
//        hideButtonLayout();
//        exportReportLayout.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, String>() {
            String classId = locationId;
            ProgressDialog dialog = new ProgressDialog(HcModuleShowClientTotalHoursActivity.this);

            @Override
            protected void onPreExecute() {
                dialog.setTitle("Creating Report");
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
//                hm.put("user_id", classEventCompany.getTeacherId());
//                hm.put("class_id", classId);
                hm.put("location_id", locationId);
                hm.put("export_records", "yes");

                try {
                    return new WebUtils().post(AppConstants.URL_GET_ATTENDENCE_BY_BEACON, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                dialog.dismiss();
                if (result != null) {
                    try {
                        final JSONObject object = new JSONObject(result);

                        if (object.has("Error")) {
//                            makeToast(object.getString("Error"));
                            return;
                        }

                        // {"Message":"Csv successfully saved","link":"http:\/\/abdevs.com\/attendance\/file.csv"}
                        final String fileName = object.getString("link");

                        new AlertDialog.Builder(HcModuleShowClientTotalHoursActivity.this)
                                .setTitle("Report Created")
                                .setMessage("Click download button to save")
                                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


//                                        new DownloadFileFromURL().execute("http://182.71.22.43/attendance/file.csv");
                                        // starting new Async Task
                                        new DownloadFileFromURL().execute("http://182.71.22.43/attendance/" + fileName);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();

                    } catch (JSONException e) {
//                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
//                        makeToast("Error in getting data");
                    }
                } else {
//                    makeToast("Please check internet connection");
                }
            }
        }.execute();
    }


    private void setAdapter() {

        listAdapter = new HCModuleDayWeekMonthYearListAdapter(HcModuleShowClientTotalHoursActivity.this, scheduleArrayList);
        totalhoursList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }


    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        //       String fileName;
        File fileToSave;
        int count = 1;


        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showCustomDialog();

            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(HcModuleShowClientTotalHoursActivity.this);
            mBuilder.setContentTitle("Download")
                    .setContentText("Download in progress").setSmallIcon(R.drawable.ic_launcher);

            //            fileName = Environment.getExternalStorageDirectory().toString() + "/Notify/" + System.currentTimeMillis() + ".csv";
            File externalStorage = Environment.getExternalStorageDirectory();
            File notifyDir = new File(externalStorage, "Notify");

            if (!notifyDir.exists() && !notifyDir.isDirectory()) {
                boolean created = notifyDir.mkdir();
                if (!created) {
                    Toast.makeText(HcModuleShowClientTotalHoursActivity.this, "Unable to create directory to new file", Toast.LENGTH_LONG).show();
                    return;
                }
            }
//            mBuilder.setProgress(100, 0, false);
//
//            mNotifyManager.notify(count, mBuilder.build());

//            fileName = Environment.getDataDirectory().toString() + "/Notify/" + System.currentTimeMillis() + ".csv";
            fileToSave = new File(notifyDir, "report_" + System.currentTimeMillis() + ".csv");

            if (!fileToSave.exists()) {
                boolean created = false;
                try {
                    created = fileToSave.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!created) {
                    Toast.makeText(HcModuleShowClientTotalHoursActivity.this, "Unable to create new file", Toast.LENGTH_LONG).show();
                }
            }
        }


        ProgressDialog pDialog;

        public void showCustomDialog() {

            pDialog = new ProgressDialog(HcModuleShowClientTotalHoursActivity.this);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            pDialog.show();
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//// Update progress
//            mBuilder.setProgress(100, values[0], false);
//            mNotifyManager.notify(count, mBuilder.build());
//            super.onProgressUpdate(values);
//        }


        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lengthOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                OutputStream output = new FileOutputStream(fileToSave);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    try {
// Sleep for 5 seconds
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        Log.d("TAG", "sleep failure");
                    }
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
            count = Integer.parseInt(progress[0]);
            mBuilder.setProgress(100, count, false);

            mNotifyManager.notify(0, mBuilder.build());
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            pDialog.dismiss();
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            // set your audio path
            intent.setDataAndType(Uri.fromFile(fileToSave), "text/*");
            PendingIntent pIntent = PendingIntent.getActivity(HcModuleShowClientTotalHoursActivity.this, 0, intent, 0);
            mBuilder.setContentTitle("Done.");
            mBuilder.setContentText("Download complete")


                    // Removes the progress bar
                    .setProgress(0, 0, false).setAutoCancel(true)
                    .setContentIntent(pIntent).build();
            ;
            mNotifyManager.notify(0, mBuilder.build());
//
//            mBuilder.setContentText("Download complete");
//// Removes the progress bar
//            mBuilder.setProgress(0, 0, false);
//            mNotifyManager.notify(0, mBuilder.build());
            if (fileToSave.exists()) {
                Toast.makeText(HcModuleShowClientTotalHoursActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(HcModuleShowClientTotalHoursActivity.this, "Error in saving file!", Toast.LENGTH_LONG).show();
            }
        }

    }


    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

//            getattendance_by_beacon
//            param:employee_id,location_id,employee_schedule_id

            @Override
            protected String doInBackground(Void... params) {
                //                    Toast.makeText(getApplicationContext(),"Inside If",Toast.LENGTH_LONG).show();
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationId);

                try {
                    return new WebUtils().post(AppConstants.URL_GET_ATTENDENCE_BY_BEACON, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    try {
                        JSONObject jsn = new JSONObject(result);
                        /*"employee_data":[

                        {
                            "employee_id":"637",
                                "employee_username":"popup",
                                "employee_email":"popup@gmail.com",
                                "hours_spent":"00:12:57"
                        }

                        ]*/

                        JSONArray jsnArry = jsn.getJSONArray("employee_data");

                        for (int i = 0; i < jsnArry.length(); i++) {
                            HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();

                            obj.heading = "Service By Employee: " + jsnArry.getJSONObject(i).getString("employee_username");
                            obj.date = "Total Hours: ";
                            obj.scheduleDetails = jsnArry.getJSONObject(i).getString("hours_spent");
                            scheduleArrayList.add(obj);
                        }

                        setAdapter();
                        if (scheduleArrayList.size() == 0) {
                            findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);
                        }

                        /*JSONArray jsnArry = jsn.getJSONArray("Data");
                        if (jsn.getString("Message").matches("Success")) {
                            for (int i = 0; i < jsnArry.length(); i++) {
                                HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();

                                obj.heading = "Service By Employee: " + jsnArry.getJSONObject(i).getString("employee_username");
                                obj.date = "Total Hours: ";
                                obj.scheduleDetails = jsnArry.getJSONObject(i).getString("hours_spent");
                                scheduleArrayList.add(obj);
                            }

                            setAdapter();
                            if (scheduleArrayList.size() == 0) {
                                findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);
                            }
                        }*/


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                }


            }
        }.execute();

    }

    public void gotoBack(View view) {


        super.onBackPressed();
    }


    // in OnResume
    @Override
    protected void onResume() {
        super.onResume();

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());

    }

// On Back Pressed


    @Override
    public void onBackPressed() {
        if (navigationLayout.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
            navigationLayout.setAnimation(textAnimation);
            navigationLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }


}