package com.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.StudentClass;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StudentCheckAttendanceActivity extends Activity {

    public static final String EXTRA_STUDENT_CLASS = "EXTRA_STUDENT_CLASS";
    public static final String EXTRA_STUDENT = "EXTRA_STUDENTS";

    ListView listView;

    Student student;
    StudentClass studentClass;
    List<Attendance> stringArrayList = new ArrayList<>();
    List<Attendance> absentArrayList = new ArrayList<>();
    ListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_check_attendance);

        listView = (ListView) findViewById(R.id.absentListView);

        studentClass = (StudentClass) getIntent().getSerializableExtra(EXTRA_STUDENT_CLASS);
        student = (Student) getIntent().getSerializableExtra(EXTRA_STUDENT);

        updateData();
    }

    private void setAdapter() {

        listAdapter = new ListAdapter(getApplicationContext(), absentArrayList);
        listView.setAdapter(listAdapter);
    }


    private void updateData() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<String, String>();
                // class_id, email
                map.put("class_id", studentClass.getStudentClassId());
                map.put("email", student.getEmail());
                try {
                    return new WebUtils().post(AppConstants.URL_STUDENT_CHECK_ATTENDANCE_SINGLE_CLASS, map);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    stringArrayList = DataUtils.getAttendanceListFromJsonString(s);

                    absentArrayList.clear();
                    for (Attendance attendance : stringArrayList) {
                        if (!attendance.isPresent()) {
                            absentArrayList.add(attendance);
                        }
                    }

                    setAdapter();
                }

            }
        }.execute();
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
            TextView dateTextView;
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
                holder.deleteImageButton = (ImageButton) view.findViewById(R.id.deleteImageButton);
                holder.deleteImageButton.setVisibility(View.GONE);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.dateTextView.setText(messagesStringArrayList.get(position).getCreateDate());

            return view;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

}
