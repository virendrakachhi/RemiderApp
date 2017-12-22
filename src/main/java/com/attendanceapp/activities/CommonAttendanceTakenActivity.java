package com.attendanceapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CommonAttendanceTakenActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_SELECTED_CLASS_INDEX = "EXTRA_EDITING_CLASS_INDEX";
    public static final String EXTRA_ATTENDANCE_DATA = "EXTRA_ATTENDANCE_DATA";

    List<User> allStudentList = new ArrayList<>();
    List<User> absentStudentsList = new ArrayList<>();
    List<User> presentStudentsList = new ArrayList<>();

    List<Attendance> allAttendanceList = new ArrayList<>();
    List<Attendance> absentAttendanceList = new ArrayList<>();
    List<Attendance> presentAttendanceList = new ArrayList<>();

    protected TextView txtAttendanceTaken, absenceLayout, presentLayout;
    protected ListView listView;
    protected Button saveButton;

    private ListAdapter listAdapter;
    protected User user;
    protected UserRole userRole;
    protected ClassEventCompany classEventCompany;
    UserUtils userUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_take_attendance_current_location);

        txtAttendanceTaken = (TextView) findViewById(R.id.txtAttendanceTaken);
        absenceLayout = (TextView) findViewById(R.id.absenceLayout);
        presentLayout = (TextView) findViewById(R.id.presentLayout);
        listView = (ListView) findViewById(R.id.listView);
        saveButton = (Button) findViewById(R.id.saveButton);

        absenceLayout.setOnClickListener(this);
        presentLayout.setOnClickListener(this);

        userUtils = new UserUtils(CommonAttendanceTakenActivity.this);
        user = userUtils.getUserFromSharedPrefs();

        Intent intent = getIntent();

        userRole = UserRole.valueOf(intent.getIntExtra(AppConstants.EXTRA_USER_ROLE, -1));

        int index = intent.getIntExtra(EXTRA_SELECTED_CLASS_INDEX, 0);
        classEventCompany = user.getClassEventCompanyArrayList().get(index);

        String json = intent.getStringExtra(EXTRA_ATTENDANCE_DATA);
        allAttendanceList = DataUtils.getAttendanceListFromJsonString(json);

        for (Attendance attendance : allAttendanceList) {
            if (attendance.isPresent()) {
                presentAttendanceList.add(attendance);
            } else {
                absentAttendanceList.add(attendance);
            }
        }

        allStudentList = classEventCompany.getUsers();

        for (User student : allStudentList) {
            if (isInPresentAttendanceList(student.getUserId())) {
                presentStudentsList.add(student);
            } else {
                absentStudentsList.add(student);
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = new Date();
        format.applyPattern("MMM dd, yyyy");
        txtAttendanceTaken.setText("Attendance Taken " + format.format(date));

        showAbsentList();
        saveButton.setVisibility(View.VISIBLE);
    }

    private void showAbsentList() {
        listAdapter = new ListAdapter(CommonAttendanceTakenActivity.this, absentStudentsList);
        listView.setAdapter(listAdapter);

        absenceLayout.setTextColor(getResources().getColor(R.color.dark_blue));
        absenceLayout.setBackgroundColor(getResources().getColor(R.color.white));
        presentLayout.setTextColor(getResources().getColor(R.color.white));
        presentLayout.setBackgroundColor(getResources().getColor(R.color.dark_blue));
    }

    private boolean isInPresentAttendanceList(String studentId) {
        for (Attendance attendance : presentAttendanceList) {
            if (attendance.getStudentId().equalsIgnoreCase(studentId)) {
                return true;
            }
        }
        return false;
    }

    private void showPresentList() {
        listAdapter = new ListAdapter(CommonAttendanceTakenActivity.this, presentStudentsList);
        listView.setAdapter(listAdapter);

        absenceLayout.setTextColor(getResources().getColor(R.color.white));
        absenceLayout.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        presentLayout.setTextColor(getResources().getColor(R.color.dark_blue));
        presentLayout.setBackgroundColor(getResources().getColor(R.color.white));
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.absenceLayout:
                showAbsentList();
                break;
            case R.id.presentLayout:
                showPresentList();
                break;
        }
    }


    private class ListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Context context;
        private List<User> attendanceList;

        public ListAdapter(Context context, List<User> attendanceList) {
            this.attendanceList = attendanceList;
            this.context = context;
            inflater = (LayoutInflater) this.context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return attendanceList.size();
        }

        @Override
        public User getItem(int position) {
            return attendanceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView studentName, marker;
            ImageView studentImage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_absent_present, null, false);
                holder = new ViewHolder();

                holder.studentName = (TextView) view.findViewById(R.id.studentName);
                holder.marker = (TextView) view.findViewById(R.id.marker);
                holder.studentImage = (ImageView) view.findViewById(R.id.studentImage);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            User attendance = attendanceList.get(position);

//            holder.studentName.setText(attendance.getUsername());
            holder.studentName.setText(attendance.getEmail());
            holder.marker.setTag(position);

            holder.marker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = (Integer) v.getTag();
                    System.out.println("removing position = " + position);
                    presentStudentsList.add(absentStudentsList.get(position));
                    absentStudentsList.remove(position);
                    listAdapter.notifyDataSetChanged();
                }
            });

            return view;
        }
    }


    public void saveButton(View view) {

        new AsyncTask<Void, Void, Void>() {
            private String result;

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();

                hm.put("user_id", user.getUserId());
                hm.put("type", "Manual");

                String url = "";
                if (userRole == UserRole.Teacher) {
                    hm.put("class_code", classEventCompany.getUniqueCode());
                    hm.put("class_id", classEventCompany.getId());
                    hm.put("student_id", StringUtils.getAllIdsFromStudentList(presentStudentsList, ','));
                    url = AppConstants.URL_TAKE_ATTENDANCE_CURRENT_LOCATION;

                } else if (userRole == UserRole.EventHost) {
                    hm.put("event_code", classEventCompany.getUniqueCode());
                    hm.put("event_id", classEventCompany.getId());
                    hm.put("eventee_id", StringUtils.getAllIdsFromStudentList(presentStudentsList, ','));
                    url = AppConstants.URL_TAKE_ATTENDANCE_BY_EVENT_HOST;

                } else if (userRole == UserRole.Manager) {
                    hm.put("company_code", classEventCompany.getUniqueCode());
                    hm.put("company_id", classEventCompany.getId());
                    hm.put("employee_id", StringUtils.getAllIdsFromStudentList(presentStudentsList, ','));
                    url = AppConstants.URL_TAKE_ATTENDANCE_BY_MANAGER;

                }

                try {
                    result = new WebUtils().post(url, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                String message;
                boolean finish;
                if (result != null && !result.contains("Error") && !result.contains("error")) {
                    message = "Attendance taken successfully";
                    finish = true;
                } else {
                    message = "Problem in taking attendance";
                    finish = false;
                }
                Toast.makeText(CommonAttendanceTakenActivity.this, message, Toast.LENGTH_LONG).show();
                if (finish) {
                    finish();
                }
            }
        }.execute();
    }


}
