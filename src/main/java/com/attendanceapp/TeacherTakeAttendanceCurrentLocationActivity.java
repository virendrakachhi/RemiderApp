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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.TeacherClass;
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


public class TeacherTakeAttendanceCurrentLocationActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_TEACHER_CLASS_INDEX = "EXTRA_EDITING_CLASS_INDEX";
    public static final String EXTRA_ATTENDANCE_DATA = "EXTRA_ATTENDANCE_DATA";

    List<Student> allStudentList = new ArrayList<>();
    List<Student> absentStudentsList = new ArrayList<>();
    List<Student> presentStudentsList = new ArrayList<>();

    List<Attendance> allAttendanceList = new ArrayList<>();
    List<Attendance> absentAttendanceList = new ArrayList<>();
    List<Attendance> presentAttendanceList = new ArrayList<>();

    protected TextView txtAttendanceTaken, absenceLayout, presentLayout;
    protected ListView listView;
    protected Button saveButton;

    private ListAdapter listAdapter;
    protected TeacherClass teacherClass;
    UserUtils userUtils;
    boolean val=false;

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

        userUtils = new UserUtils(TeacherTakeAttendanceCurrentLocationActivity.this);
        int index = getIntent().getIntExtra(EXTRA_TEACHER_CLASS_INDEX, 0);
        teacherClass = userUtils.getUserWithDataFromSharedPrefs(Teacher.class).getTeacherClassList().get(index);

        String json = getIntent().getStringExtra(EXTRA_ATTENDANCE_DATA);
        allAttendanceList = DataUtils.getAttendanceListFromJsonString(json);

        for (Attendance attendance : allAttendanceList) {
            if (attendance.isPresent()) {
                presentAttendanceList.add(attendance);
            } else {
                absentAttendanceList.add(attendance);
            }
        }

        allStudentList = teacherClass.getStudentList();

        for (Student student : allStudentList) {
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

//        Calendar calendar = Calendar.getInstance();
//        txtAttendanceTaken.setText("Attendance Taken " + calendar.get(Calendar.DAY_OF_MONTH) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR));

        showAbsentList();
        saveButton.setVisibility(View.VISIBLE);
    }

    private void showAbsentList() {
        listAdapter = new ListAdapter(TeacherTakeAttendanceCurrentLocationActivity.this, absentStudentsList);
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
        listAdapter = new ListAdapter(TeacherTakeAttendanceCurrentLocationActivity.this, presentStudentsList);
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
                val=false;
                break;
            case R.id.presentLayout:
                showPresentList();
                val=true;
                break;
        }
    }


    private class ListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Context context;
        private List<Student> attendanceList;
        private boolean hideMarkerButton;

        public ListAdapter(Context context, List<Student> attendanceList) {
            this.attendanceList = attendanceList;
            this.context = context;
            inflater = (LayoutInflater) this.context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        public ListAdapter(Context context, List<Student> attendanceList, boolean hideMarkerButton) {
            this(context, attendanceList);
            this.hideMarkerButton = hideMarkerButton;
        }

        @Override
        public int getCount() {
            return attendanceList.size();
        }

        @Override
        public Student getItem(int position) {
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

            Student attendance = attendanceList.get(position);

//            holder.studentName.setText(attendance.getUsername());
            holder.studentName.setText(attendance.getEmail());
            if(val){
                holder.marker.setText("Mark as absent");
            }
            else{
                holder.marker.setText("Mark as here");
            }
            if (hideMarkerButton) {
                holder.marker.setVisibility(View.GONE);
            } else {
                holder.marker.setTag(position);
                holder.marker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(val){
                            final int position = (Integer) v.getTag();
                            System.out.println("removing position = " + position);
                            absentStudentsList.add(presentStudentsList.get(position));
                            presentStudentsList.remove(position);
                            listAdapter.notifyDataSetChanged();
                        }else {
                            final int position = (Integer) v.getTag();
                            System.out.println("removing position = " + position);
                            presentStudentsList.add(absentStudentsList.get(position));
                            absentStudentsList.remove(position);
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            return view;
        }
    }


//    private class ListAdapter extends BaseAdapter {
//        private LayoutInflater inflater;
//        private Context context;
//        private List<Attendance> attendanceList;
//
//        public ListAdapter(Context context, List<Attendance> attendanceList) {
//            this.attendanceList = attendanceList;
//            this.context = context;
//            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
//        }
//
//        @Override
//        public int getCount() {
//            return attendanceList.size();
//        }
//
//        @Override
//        public Attendance getItem(int position) {
//            return attendanceList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        class ViewHolder {
//            TextView studentName, marker;
//            ImageView studentImage;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = convertView;
//            ViewHolder holder;
//
//            if (view == null) {
//                view = inflater.inflate(R.layout.list_item_absent_present, parent, false);
//                holder = new ViewHolder();
//
//                holder.studentName = (TextView) view.findViewById(R.id.studentName);
//                holder.marker = (TextView) view.findViewById(R.id.marker);
//                holder.studentImage = (ImageView) view.findViewById(R.id.studentImage);
//
//                view.setTag(holder);
//            } else {
//                holder = (ViewHolder) view.getTag();
//            }
//
//            Attendance attendance = attendanceList.get(position);
//
//            holder.studentName.setText(attendance.getUsername());
//            //holder.marker.setVisibility(View.GONE);
//
//            return view;
//        }
//    }

    public void saveButton(View view) {
//             public static final String URL_TAKE_ATTENDANCE_CURRENT_LOCATION = "http://www.abdevs.com/attendance/Mobiles/studentAttendens";
//             "user_id, student_id ( more than one comma separated ), class_code, class_id, type(Manual, Automatic)"

        new AsyncTask<Void, Void, Void>() {
            private String result;

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("user_id", teacherClass.getTeacherId());
                hm.put("class_code", teacherClass.getClassCode());
                hm.put("class_id", teacherClass.getId());
                hm.put("type", "Manual");

                hm.put("student_id", StringUtils.getAllIdsFromStudentList(presentStudentsList, ','));

                try {
                    result = new WebUtils().post(AppConstants.URL_TAKE_ATTENDANCE_CURRENT_LOCATION, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                String message;
                boolean finish;
                System.out.println("This is result >>> "+result);
                if (result != null && !result.contains("Error") && !result.contains("error")) {
                    message = "Attendance taken successfully";
                    finish = true;
                } else {
                    message = "Problem in taking attendance";
                    finish = false;
                }
                Toast.makeText(TeacherTakeAttendanceCurrentLocationActivity.this, message, Toast.LENGTH_LONG).show();
                if (finish) {
                    finish();
                }
            }
        }.execute();
    }


}
