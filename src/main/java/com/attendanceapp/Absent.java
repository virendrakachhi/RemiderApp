package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.PageHeader;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Absent extends Activity implements View.OnClickListener {
    public static final String EXTRA_ATTENDANCE_DATA = "EXTRA_ATTENDANCE_DATA";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_LIST_OPTION = "EXTRA_LIST_OPTION";
    public static final String SHOW_NAME_DATE = "SHOW_NAME_DATE";
    public static final String SHOW_EDIT_BUTTON = "SHOW_EDIT_BUTTON";
    private static final String TAG = Absent.class.getSimpleName();
    public static final String EXTRA_CLASS_ID = "EXTRA_CLASS_ID";

    ListView listView;
    List<Attendance> attendanceList = new ArrayList<>();
    AbsentListAdapter listAdapter;
    PageHeader pageHeader;
    UserUtils userUtils;
    Teacher teacher;
    String classId;


    protected boolean showEditButton, showName, showNameDate, hideDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_absent);

        listView = (ListView) this.findViewById(R.id.listView1);

        userUtils = new UserUtils(this);
        teacher = new Teacher(userUtils.getUserFromSharedPrefs());

        setAdapter();

        Intent intent = getIntent();
        showEditButton = intent.getBooleanExtra(SHOW_EDIT_BUTTON, false);
        String attendanceData = intent.getStringExtra(EXTRA_ATTENDANCE_DATA);
        classId = intent.getStringExtra(EXTRA_CLASS_ID);
        String listOption = intent.getStringExtra(EXTRA_LIST_OPTION);
        if (SHOW_NAME_DATE.equalsIgnoreCase(listOption)) {
            showNameDate = true;
        }

        pageHeader = new PageHeader(this, true, false, PageHeader.LeftButtonOption.BACK);
        pageHeader.getTitle().setText(StringUtils.changeFirstLetterToUppercase(intent.getStringExtra(EXTRA_TITLE)));
        pageHeader.getLeftButton().setOnClickListener(this);

        parseDataAsync(attendanceData);
    }

    private void setAdapter() {
        listAdapter = new AbsentListAdapter(this, attendanceList);
        listView.setAdapter(listAdapter);
    }

    private void parseDataAsync(final String attendanceData) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                attendanceList.clear();
                attendanceList.addAll(DataUtils.getAttendanceListFromJsonString(attendanceData));
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                listAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButton:
                onBackPressed();
                break;
        }
    }

    public class AbsentListAdapter extends ArrayAdapter<Attendance> implements View.OnClickListener {

        private LayoutInflater inflater;
        private List<Attendance> list = new ArrayList<>();


        // show = name(to show student name)
        public AbsentListAdapter(Context context, List<Attendance> attendanceList) {
            super(context, 0, attendanceList);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.list = attendanceList;
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

        @SuppressLint("InflateParams")
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

            holder.deleteImageButton.setTag(position);
            holder.editImageButton.setTag(position);

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

            if (showName) {
                holder.subtitleDateTextView.setVisibility(View.GONE);

            } else if (showNameDate) {
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
                deleteImageButton((Integer) v.getTag());

            } else if (v.getId() == R.id.editImageButton) {
                editImageButton((Integer) v.getTag());
            }
        }

        private void editImageButton(final Integer index) {
            View view = getLayoutInflater().inflate(R.layout.view_edit_absent, null, false);

            TextView date = (TextView) view.findViewById(R.id.date);
            TextView studentName = (TextView) view.findViewById(R.id.studentName);
            final RadioButton absentRadioButton = (RadioButton) view.findViewById(R.id.absent);
            final RadioButton presentRadioButton = (RadioButton) view.findViewById(R.id.present);

            Attendance attendance = attendanceList.get(index);

            date.setText(attendance.getCreateDate());
            studentName.setText(attendance.getStudentEmail());

            if (attendance.isPresent()) {
                presentRadioButton.setChecked(true);
            } else {
                absentRadioButton.setChecked(true);
            }

            new AlertDialog.Builder(Absent.this)
                    .setView(view)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            final String url = AppConstants.URL_EDIT_STUDENT_ATTENDANCE_BY_TEACHER;
                            final String attend = presentRadioButton.isChecked() ? "p" : "a";

                            if (!absentRadioButton.isChecked() && !presentRadioButton.isChecked()) {
                                makeToast("Please mark absent or present");
                                return;
                            }

                            new AsyncTask<Void, Void, String>() {
                                ProgressDialog progressDialog = new ProgressDialog(Absent.this);

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                }

                                @Override
                                protected String doInBackground(Void... params) {
                                    String result = null;

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("studentAttendance_id", list.get(index).getId());
                                    map.put("attend", attend);


                                    try {
                                        result = new WebUtils().post(url, map);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, e.getMessage());
                                    }
                                    return result;
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    progressDialog.dismiss();
                                    JSONObject jObject;
                                    if (s == null) {
                                        makeToast("Error in updating!");

                                    } else {
                                        try {
                                            jObject = new JSONObject(s);

                                            // check if result contains Error
                                            if (jObject.has("Error")) {
                                                makeToast(jObject.getString("Error"));

                                            } else if (jObject.has("Message")) {
                                                makeToast("Updated successfully!");
                                                updateDataAsync();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e(TAG, "Error in parsing data: " + s);
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }

                                }
                            }.execute();


                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();


        }

        private void deleteImageButton(final Integer index) {

            new AlertDialog.Builder(Absent.this)
                    .setMessage("Delete attendance!")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            final String url = AppConstants.URL_DELETE_STUDENT_ATTENDANCE_BY_TEACHER;

                            new AsyncTask<Void, Void, String>() {
                                ProgressDialog progressDialog = new ProgressDialog(Absent.this);

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                }

                                @Override
                                protected String doInBackground(Void... params) {
                                    String result = null;

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("studentAttendance_id", list.get(index).getId());

                                    try {
                                        result = new WebUtils().post(url, map);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return result;
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    progressDialog.dismiss();
                                    progressDialog.cancel();

                                    JSONObject jObject;
                                    if (s == null) {
                                        makeToast("Error in deleting!");

                                    } else {
                                        try {
                                            jObject = new JSONObject(s);

                                            // check if result contains Error
                                            if (jObject.has("Error")) {
                                                makeToast(jObject.getString("Error"));

                                            } else if (jObject.has("Message")) {
                                                makeToast("Deleted successfully!");
                                                updateDataAsync();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e(TAG, "Error in parsing data: " + s);
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }

                                }
                            }.execute();


                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();


        }
    }

    private void makeToast(String title) {
        Toast.makeText(Absent.this, title, Toast.LENGTH_LONG).show();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("user_id", teacher.getUserId());
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
                parseDataAsync(result);
            }
        }.execute();

    }

}
