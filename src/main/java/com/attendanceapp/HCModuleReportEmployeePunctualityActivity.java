package com.attendanceapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.adapters.HCModuleDayWeekMonthYearListAdapter;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;
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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jagdeep.singh on 1/22/2016.
 */
public class HCModuleReportEmployeePunctualityActivity extends AppCompatActivity implements View.OnClickListener {
    ListView totalhoursList;
    LinearLayout timeSelectorHeader;
    Button downloadButton;
    Context context;
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

        context = getApplicationContext();


        timeSelectorHeader=(LinearLayout) findViewById(R.id.timeSelectorHeader);
        timeSelectorHeader.setVisibility(View.GONE);


        downloadButton=(Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this);

        totalhoursList = (ListView) findViewById(R.id.totalhoursList);
getAttendance();

        // Setting Inittial Colors
//        scheduleArrayList.clear();
//
//        for (int i = 0; i < 7; i++) {
//            HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();
//            obj.heading = "Employee " + i;
//            obj.date = "Late by " + i+" minutes";
//            obj.scheduleDetails = "Arraival time 10:40 am " +"\n"+"Scheduled Time 6:10 pm ";
//            scheduleArrayList.add(obj);
//        }

//        setAdapter();


    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.downloadButton:

                Toast.makeText(HCModuleReportEmployeePunctualityActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
                txtExportReport();

                break;


        }
    }
    public void gotoBack(View view) {
        super.onBackPressed();
    }
    private void txtExportReport() {
//        hideButtonLayout();
//        exportReportLayout.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, String>() {
            String classId = "84";
            ProgressDialog dialog = new ProgressDialog(HCModuleReportEmployeePunctualityActivity.this);

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
                hm.put("class_id", classId);

                try {
                    return new WebUtils().post(AppConstants.URL_EXPORT_REPORT, hm);
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

                        new AlertDialog.Builder(HCModuleReportEmployeePunctualityActivity.this)
                                .setTitle("Report Created")
                                .setMessage("Click download button to save")
                                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // starting new Async Task
                                        new DownloadFileFromURL().execute("http://abdevs.com/attendance/" + fileName);
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

        listAdapter = new HCModuleDayWeekMonthYearListAdapter(HCModuleReportEmployeePunctualityActivity.this, scheduleArrayList);
        totalhoursList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }


    private void getAttendance() {
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

//                hm.put("location_id",locationID);
//                hm.put("user_id",managerID);
//                hm.put("employee_id",empID);

                hm.put("location_id","84");
                hm.put("user_id","200");
                hm.put("employee_id","202");


//                hm.put("location_id","80");
//                hm.put("role", String.valueOf(userRole.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_ATTENDANCE_OF_EMP, hm);
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
                        if(jsn.getString("Message").matches("Attendance Saved"))
                        {
                            JSONArray jsnObj = jsn.getJSONArray("Data");
                            for(int i=0; i<jsnObj.length(); i++)
                            {
                                HCModuleDayWeekMonthYearViewBean bean = new HCModuleDayWeekMonthYearViewBean();
                                bean.attendance = jsnObj.getJSONObject(i).getJSONObject("EmployeeAttendance").getString("attend");
                                bean.empName = jsnObj.getJSONObject(i).getJSONObject("User").getString("username");
                                bean.date = jsnObj.getJSONObject(i).getJSONObject("EmployeeAttendance").getString("modified");
                                scheduleArrayList.add(bean);

                            }}
                        else
                        {
                            scheduleArrayList.add(new HCModuleDayWeekMonthYearViewBean());
                        }
                        setAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        }.execute();

    }




    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        //       String fileName;
        File fileToSave;


        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showCustomDialog();

//            fileName = Environment.getExternalStorageDirectory().toString() + "/Notify/" + System.currentTimeMillis() + ".csv";
            File externalStorage = Environment.getExternalStorageDirectory();
            File notifyDir = new File(externalStorage, "Notify");

            if (!notifyDir.exists() && !notifyDir.isDirectory()) {
                boolean created = notifyDir.mkdir();
                if (!created) {
                    Toast.makeText(HCModuleReportEmployeePunctualityActivity.this, "Unable to create directory to new file", Toast.LENGTH_LONG).show();
                    return;
                }
            }
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
                    Toast.makeText(HCModuleReportEmployeePunctualityActivity.this, "Unable to create new file", Toast.LENGTH_LONG).show();
                }
            }
        }



        ProgressDialog pDialog;

        public void showCustomDialog() {

            pDialog = new ProgressDialog(HCModuleReportEmployeePunctualityActivity.this);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            pDialog.show();
        }


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


    }


}


