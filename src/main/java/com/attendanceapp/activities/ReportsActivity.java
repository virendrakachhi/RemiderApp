package com.attendanceapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.adapters.Reports_AbsenteeExpendableListAdapter;
import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.TeacherClass;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.PageHeader;
import com.attendanceapp.utils.UserUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class ReportsActivity extends Activity implements OnClickListener {

    public static final String EXTRA_CLASS_INDEX = "EXTRA_CLASS_INDEX";
    private static final String TAG = ReportsActivity.class.getSimpleName();
    PageHeader pageHeader;

    TextView txtAbsenteeReport, txtRunCustomReport, txtExportReport;
    LinearLayout buttonsLayout, absenteeLayout, customReportLayout, exportReportLayout;
    Button calenderButton;

    ExpandableListView listView;
    Reports_AbsenteeExpendableListAdapter expendableListAdapter;
    TreeMap<String, List<Attendance>> classAttendanceMap = new TreeMap<>();

    TeacherClass teacherClass;
    UserUtils userUtils;

    final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    ListView customReportListView;
    AbsentListAdapter customReportListAdapter;
    ArrayList<Attendance> customReportAttendanceList = new ArrayList<>();

    private boolean showEditButton, showName, showNameDate, hideDeleteButton = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_choice);

        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        absenteeLayout = (LinearLayout) findViewById(R.id.absenteeLayout);
        customReportLayout = (LinearLayout) findViewById(R.id.customReportLayout);
        exportReportLayout = (LinearLayout) findViewById(R.id.exportReportLayout);

        listView = (ExpandableListView) findViewById(R.id.absenteeListView);

        txtAbsenteeReport = (TextView) findViewById(R.id.txtAbsenteeReport);
        txtRunCustomReport = (TextView) findViewById(R.id.txtRunCustomReport);
        txtExportReport = (TextView) findViewById(R.id.txtExportReport);

        calenderButton = (Button) findViewById(R.id.calenderButton);
        customReportListView = (ListView) findViewById(R.id.list);

        txtAbsenteeReport.setOnClickListener(this);
        txtRunCustomReport.setOnClickListener(this);
        txtExportReport.setOnClickListener(this);
        calenderButton.setOnClickListener(this);

        pageHeader = new PageHeader(this, true, false, PageHeader.LeftButtonOption.BACK);
        pageHeader.getTitle().setText("Reports");
        pageHeader.getLeftButton().setOnClickListener(this);

        userUtils = new UserUtils(this);
        Intent intent = getIntent();
        Teacher teacher = userUtils.getUserWithDataFromSharedPrefs(Teacher.class);
        teacherClass = teacher.getTeacherClassList().get(intent.getIntExtra(EXTRA_CLASS_INDEX, 0));

        customReportListAdapter = new AbsentListAdapter(this, customReportAttendanceList);
        customReportListView.setAdapter(customReportListAdapter);

        expendableListAdapter = new Reports_AbsenteeExpendableListAdapter(this, classAttendanceMap);
        listView.setAdapter(expendableListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtAbsenteeReport:
                txtAbsenteeReport();
                break;
            case R.id.txtRunCustomReport:
                txtRunCustomReport();
                break;
            case R.id.txtExportReport:
                txtExportReport();
                break;
            case R.id.leftButton:
                onBackPressed();
                break;
            case R.id.calenderButton:
                calenderButton();
                break;
        }
    }

    private void calenderButton() {
        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(ReportsActivity.this);
        builder.setTitle("Select Date");
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int monthOfYear = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();

                calenderButton.setText("" + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);

                loadCustomReportData(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            }
        });
        builder.show();
    }

    private void loadCustomReportData(final String yyyymmdd) {

        new AsyncTask<Void, Void, String>() {
            String classId = teacherClass.getId();
            ProgressDialog dialog = new ProgressDialog(ReportsActivity.this);

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("date", yyyymmdd);
                hm.put("class_id", classId);

                try {
                    return new WebUtils().post(AppConstants.URL_CUSTOM_REPORT, hm);
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
                        JSONObject object = new JSONObject(result);

                        if (object.has("Error")) {
                            makeToast(object.getString("Error"));
                            return;
                        }

                        customReportAttendanceList.clear();
                        customReportAttendanceList.addAll(DataUtils.getAttendanceListFromJsonString(result));
                        customReportListAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        makeToast("Error in getting data");
                    }
                } else {
                    makeToast("Please check internet connection");
                }
            }
        }.execute();

    }

    private void hideButtonLayout() {
        buttonsLayout.setVisibility(View.GONE);
        otherLayoutShown = true;
    }

    private boolean otherLayoutShown = false;

    private void hideOtherLayout() {
        otherLayoutShown = false;
        absenteeLayout.setVisibility(View.GONE);
        customReportLayout.setVisibility(View.GONE);
        exportReportLayout.setVisibility(View.GONE);
    }

    private void txtExportReport() {
//        hideButtonLayout();
//        exportReportLayout.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, String>() {
            String classId = teacherClass.getId();
            ProgressDialog dialog = new ProgressDialog(ReportsActivity.this);

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
                            makeToast(object.getString("Error"));
                            return;
                        }

                        // {"Message":"Csv successfully saved","link":"http:\/\/abdevs.com\/attendance\/file.csv"}
                        final String fileName = object.getString("link");

                        new AlertDialog.Builder(ReportsActivity.this)
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
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        makeToast("Error in getting data");
                    }
                } else {
                    makeToast("Please check internet connection");
                }
            }
        }.execute();
    }

    private void txtRunCustomReport() {
        hideButtonLayout();
        hideOtherLayout();
        customReportLayout.setVisibility(View.VISIBLE);
//        startActivity(new Intent(this, CustomReportActivity.class));
    }

    ProgressDialog pDialog;

    public void showCustomDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Downloading file. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    /**
     * Background Async Task to download file
     */
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
                    Toast.makeText(ReportsActivity.this, "Unable to create directory to new file", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ReportsActivity.this, "Unable to create new file", Toast.LENGTH_LONG).show();
                }
            }
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

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            pDialog.dismiss();
            if (fileToSave.exists()) {
                Toast.makeText(ReportsActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ReportsActivity.this, "Error in saving file!", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void txtAbsenteeReport() {

        new AsyncTask<Void, Void, String>() {
            String classId = teacherClass.getId();
            ProgressDialog dialog = new ProgressDialog(ReportsActivity.this);

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("user_id", teacherClass.getTeacherId());
                hm.put("class_id", classId);

                try {
                    return new WebUtils().post(AppConstants.URL_SHOW_ATTENDANCE_CURRENT_LOCATION, hm);
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
                        JSONObject object = new JSONObject(result);

                        if (object.has("Error")) {
                            makeToast(object.getString("Error"));
                            return;
                        }

                        expendableListAdapter.update(DataUtils.getReportsMap(result));
                        hideButtonLayout();
                        absenteeLayout.setVisibility(View.VISIBLE);

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        makeToast("Error in getting data");
                    }
                } else {
                    makeToast("Please check internet connection");
                }
            }
        }.execute();


    }


    @Override
    public void onBackPressed() {

        if (otherLayoutShown) {
            hideOtherLayout();
            buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }


    private void makeToast(String title) {
        Toast.makeText(ReportsActivity.this, title, Toast.LENGTH_LONG).show();
    }


    public class AbsentListAdapter extends ArrayAdapter<Attendance> implements View.OnClickListener {

        private LayoutInflater inflater;
        private List<Attendance> attendanceList = new ArrayList<>();


        // show = name(to show student name)
        public AbsentListAdapter(Context context, List<Attendance> attendanceList) {
            super(context, 0, attendanceList);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.attendanceList = attendanceList;
        }

        public class ViewHolder {
            final TextView titleTextView;
            final TextView subtitleDateTextView;
            final TextView status;
            final ImageButton deleteImageButton;
            final ImageButton editImageButton;

            ViewHolder(View view) {
                status = (TextView) view.findViewById(R.id.status);
                titleTextView = (TextView) view.findViewById(R.id.dateTextView);
                subtitleDateTextView = (TextView) view.findViewById(R.id.dateTextView1);
                deleteImageButton = (ImageButton) view.findViewById(R.id.deleteImageButton);
                editImageButton = (ImageButton) view.findViewById(R.id.editImageButton);
            }
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_absent, null);
                view.setTag(new ViewHolder(view));
            }

            Attendance attendance = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.deleteImageButton.setOnClickListener(this);
            holder.editImageButton.setOnClickListener(this);

            holder.titleTextView.setText(attendance.getStudentEmail());
            holder.status.setText(attendance.isPresent() ? "P" : "A");

            String dateString = attendance.getCreateDate();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date;

            try {
                date = format.parse(dateString);
                format.applyPattern("MMMM dd, yyyy");
                holder.subtitleDateTextView.setText("" + format.format(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (showNameDate) {
                holder.subtitleDateTextView.setVisibility(View.VISIBLE);
            }

            if (hideDeleteButton) {
                holder.deleteImageButton.setVisibility(View.GONE);
            }

            if (showEditButton) {
                holder.editImageButton.setVisibility(View.VISIBLE);
            }

            return view;
        }


        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.deleteImageButton) {
                Toast.makeText(getContext(), "deleteImageButton", Toast.LENGTH_LONG).show();
            } else if (v.getId() == R.id.editImageButton) {
                Toast.makeText(getContext(), "editImageButton", Toast.LENGTH_LONG).show();

            }
        }
    }


}
