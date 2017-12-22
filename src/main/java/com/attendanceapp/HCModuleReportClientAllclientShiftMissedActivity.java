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
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Attendance;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.WebUtils;

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
public class HCModuleReportClientAllclientShiftMissedActivity extends Activity implements View.OnClickListener {
    TextView dayTxt, weekTxt, monthTxt, yearTxt, headingText;
    ListView listView;
    LinearLayout timeSelectorHeader, heading;
    Button downloadButton;
    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    String locationID, empID, managerID;
    Context context;
    List<Attendance> stringArrayList = new ArrayList<>();
    List<Attendance> absentArrayList = new ArrayList<>();
    //    List<HCModuleDayWeekMonthYearViewBean> scheduleList = new LinkedList<>();
    ListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_check_attendance);
        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();

        listView = (ListView) findViewById(R.id.absentListView);
        locationID = getIntent().getExtras().getString("LocationID");
        managerID = getIntent().getExtras().getString("UserID");
        empID = getIntent().getExtras().getString("EmpID");
        updateData();


//        headingText=(TextView) findViewById(R.id.headingText);
//        headingText.setText("Following Shifts Missed");
//        getAttendance();
//        timeSelectorHeader=(LinearLayout) findViewById(R.id.timeSelectorHeader);
//        timeSelectorHeader.setVisibility(View.GONE);
//
//        heading=(LinearLayout) findViewById(R.id.heading);
//        heading.setVisibility(View.VISIBLE);
//
        downloadButton = (Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this);
//
//        totalhoursList = (ListView) findViewById(R.id.totalhoursList);


        // Setting Inittial Colors
//        scheduleList.clear();
//        HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();
//        for (int i = 0; i < 7; i++) {
//
//            obj.heading = "Employee " + i;
//            obj.date = "Day " + i;
//            obj.scheduleDetails = "10:30 am to 6:00 pm ";
//            scheduleArrayList.add(obj);
//        }

//        setAdapter();


    }


    private void updateData() {
        new AsyncTask<Void, Void, String>() {

            ProgressDialog alertDialog = new ProgressDialog(HCModuleReportClientAllclientShiftMissedActivity.this);

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<String, String>();
                // class_id, email
//                map.put("user_id",ManagerHCDashboardActivity.user.getUserId());
//                map.put("employee_id",managerID);
                map.put("location_id", locationID);
                try {
                    return new WebUtils().post(AppConstants.URL_GET_SHIFT_MISSED, map);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String s) {
                alertDialog.dismiss();
                if (s != null) {
                    stringArrayList = DataUtils.getShiftMissed(s);

                    absentArrayList.clear();
                    for (Attendance attendance : stringArrayList) {
                        if (attendance.isPresent()) {
                            absentArrayList.add(attendance);
                        }
                    }

                    setAdapter();
                    if (absentArrayList.size() == 0) {
                        findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);
                    }
                }

            }
        }.execute();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.downloadButton:

                Toast.makeText(HCModuleReportClientAllclientShiftMissedActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
                txtExportReport();

                break;
        }
    }

    private void txtExportReport() {
//        hideButtonLayout();
//        exportReportLayout.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, String>() {
            String classId = locationID;
            ProgressDialog dialog = new ProgressDialog(HCModuleReportClientAllclientShiftMissedActivity.this);

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
                hm.put("location_id", locationID);
                try {
                    return new WebUtils().post(AppConstants.URL_EXPORT_HC_REPORT, hm);
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

                        new AlertDialog.Builder(HCModuleReportClientAllclientShiftMissedActivity.this)
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

        listAdapter = new ListAdapter(getApplicationContext(), absentArrayList);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    private class ListAdapter extends BaseAdapter {
        List<Attendance> messagesStringArrayList;
        LayoutInflater inflater;
        Context context;

        public ListAdapter(Context context, List<Attendance> messagesStringArrayList) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.messagesStringArrayList = messagesStringArrayList;
        }


        @Override
        public int getCount() {
            return messagesStringArrayList.size();
        }

        @Override
        public Attendance getItem(int position) {
            return messagesStringArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView dateTextView, nameTextView, txtStartEndTime;
            ImageButton deleteImageButton;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_absent, null, false);
                holder = new ViewHolder();

                holder.dateTextView = (TextView) view.findViewById(R.id.dateTextView);
                holder.nameTextView = (TextView) view.findViewById(R.id.dateTextView1);
                holder.txtStartEndTime = (TextView) view.findViewById(R.id.txt_start_end_time);
                holder.deleteImageButton = (ImageButton) view.findViewById(R.id.deleteImageButton);
                holder.deleteImageButton.setVisibility(View.GONE);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.dateTextView.setText(messagesStringArrayList.get(position).getCreateDate());
            holder.nameTextView.setVisibility(View.VISIBLE);
            holder.nameTextView.setText(messagesStringArrayList.get(position).getEmployeeEmail());
            holder.txtStartEndTime.setText(messagesStringArrayList.get(position).getStartTime().substring(0,5) + " "+ messagesStringArrayList.get(position).getStartTime().substring(9,11) + " - " + messagesStringArrayList.get(position).getEndTime().substring(0,5) + " "+ messagesStringArrayList.get(position).getEndTime().substring(9,11));
            return view;
        }
    }

//    private void getAttendance() {
//        new AsyncTask<Void, Void, String>() {
////            private ProgressDialog progressDialog = new ProgressDialog(EmployeeHCDashboardActivity.this);
//
//            @Override
//            protected void onPreExecute() {
////                progressDialog.setMessage("Please wait...");
////                progressDialog.setCancelable(false);
////                progressDialog.show();
//            }
//
//            @Override
//            protected String doInBackground(Void... params) {
//                HashMap<String, String> hm = new HashMap<>();
//
////                hm.put("location_id",locationID);
////                hm.put("user_id",managerID);
////                hm.put("employee_id",empID);
//
//                hm.put("location_id","84");
//                hm.put("user_id","200");
//                hm.put("employee_id","202");
//
//
////                hm.put("location_id","80");
////                hm.put("role", String.valueOf(userRole.getRole()));
//                try {
//                    return new WebUtils().post(AppConstants.URL_GET_ATTENDANCE_OF_EMP, hm);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
////                progressDialog.dismiss();
//                if (result != null) {
//                    try {
//
//                        JSONObject jsn = new JSONObject(result);
//                        if(jsn.getString("Message").matches("Attendance Saved"))
//                        {
//                            JSONArray jsnObj = jsn.getJSONArray("Data");
//                            for(int i=0; i<jsnObj.length(); i++)
//                            {
//                                HCModuleDayWeekMonthYearViewBean bean = new HCModuleDayWeekMonthYearViewBean();
//                                bean.attendance = jsnObj.getJSONObject(i).getJSONObject("EmployeeAttendance").getString("attend");
//                                bean.empName = jsnObj.getJSONObject(i).getJSONObject("User").getString("username");
//                                bean.date = jsnObj.getJSONObject(i).getJSONObject("EmployeeAttendance").getString("modified");
//                                  scheduleList.add(bean);
//
//                            }}
//                        else
//                        {
//                            scheduleList.add(new HCModuleDayWeekMonthYearViewBean());
//                        }
//                        setAdapter();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//            }
//        }.execute();
//
//    }


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
            mBuilder = new NotificationCompat.Builder(HCModuleReportClientAllclientShiftMissedActivity.this);
            mBuilder.setContentTitle("Download")
                    .setContentText("Download in progress").setSmallIcon(R.drawable.ic_launcher);

            //            fileName = Environment.getExternalStorageDirectory().toString() + "/Notify/" + System.currentTimeMillis() + ".csv";
            File externalStorage = Environment.getExternalStorageDirectory();
            File notifyDir = new File(externalStorage, "Notify");

            if (!notifyDir.exists() && !notifyDir.isDirectory()) {
                boolean created = notifyDir.mkdir();
                if (!created) {
                    Toast.makeText(HCModuleReportClientAllclientShiftMissedActivity.this, "Unable to create directory to new file", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(HCModuleReportClientAllclientShiftMissedActivity.this, "Unable to create new file", Toast.LENGTH_LONG).show();
                }
            }
        }


        ProgressDialog pDialog;

        public void showCustomDialog() {

            pDialog = new ProgressDialog(HCModuleReportClientAllclientShiftMissedActivity.this);
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
            PendingIntent pIntent = PendingIntent.getActivity(HCModuleReportClientAllclientShiftMissedActivity.this, 0, intent, 0);
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
                Toast.makeText(HCModuleReportClientAllclientShiftMissedActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(HCModuleReportClientAllclientShiftMissedActivity.this, "Error in saving file!", Toast.LENGTH_LONG).show();
            }
        }

    }


    public void gotoBack(View view) {
        super.onBackPressed();
    }

}






/*
package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.attendanceapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class HCModuleReportClientAllclientShiftMissedActivity extends Activity {

    private Spinner employeeSpinner;
    private Button showReportData ;
    private ListView list;

    List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_report_show_clientallclientshiftmissed_activity);

        employeeSpinner = (Spinner) findViewById(R.id.employeeSpinner);

        showReportData= (Button) findViewById(R.id.showReportData);
        list = (ListView) findViewById(R.id.list);




    }


}
*/
