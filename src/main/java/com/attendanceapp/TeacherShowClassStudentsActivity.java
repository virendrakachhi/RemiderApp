package com.attendanceapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.TeacherClass;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class TeacherShowClassStudentsActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_STUDENT_CLASS = "extra_student_class";
    public static final String EXTRA_STUDENT_CLASS_INDEX = "EXTRA_STUDENT_CLASS_INDEX";
    public static final Integer REQUEST_ADD_STUDENT = 30;
    public static final Integer REQUEST_UPDATE_STUDENT = 31;


    protected ImageView helpButton;
    protected TextView addStudentButton, className;
    protected ExpandableListView studentListView;
    private StudentExpandableListAdapter studentExpandableListAdapter;
    private TeacherClass teacherClass;
    protected SharedPreferences sharedPreferences;
    protected Teacher teacher;
    UserUtils userUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
// Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stdents);

        helpButton = (ImageView) findViewById(R.id.helpButton);
        addStudentButton = (TextView) findViewById(R.id.addStudentButton);
        className = (TextView) findViewById(R.id.className);
        studentListView = (ExpandableListView) findViewById(R.id.studentListView);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(TeacherShowClassStudentsActivity.this);
        teacher = userUtils.getUserWithDataFromSharedPrefs(Teacher.class);
        int teacherClassIndex = getIntent().getIntExtra(EXTRA_STUDENT_CLASS_INDEX, -1);
        teacherClass = teacher.getTeacherClassList().get(teacherClassIndex);

        setAdapter();
        studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        studentListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    studentListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        addStudentButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        className.setText(teacherClass.getClassName());
    }

    private void setAdapter() {
        studentExpandableListAdapter = new StudentExpandableListAdapter(TeacherShowClassStudentsActivity.this, teacherClass);
        studentListView.setAdapter(studentExpandableListAdapter);
    }

    public void gotoBack(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.helpButton:
                makeToast("Show help");
                break;
            case R.id.addStudentButton:
                Intent addStudentIntent = new Intent(TeacherShowClassStudentsActivity.this, TeacherInviteStudent.class);
                addStudentIntent.putExtra(TeacherInviteStudent.EXTRA_CLASS_CODE, teacherClass.getClassCode());
                addStudentIntent.putExtra(TeacherInviteStudent.EXTRA_CLASS_ID, teacherClass.getId());
                addStudentIntent.putExtra(TeacherInviteStudent.EXTRA_IS_FIRST_TIME, false);
                startActivityForResult(addStudentIntent, REQUEST_ADD_STUDENT);
                break;
        }
    }

    private void makeToast(String message) {
        Toast.makeText(TeacherShowClassStudentsActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        studentExpandableListAdapter.notifyDataSetChanged();
        if (requestCode == REQUEST_ADD_STUDENT) {
            if (resultCode == RESULT_OK) {
//                makeToast("Student added");
            } else {
//                makeToast("No student added");
            }
        } else if (requestCode == REQUEST_UPDATE_STUDENT) {
            if (resultCode == RESULT_OK) {
//                makeToast("Student updated");
            } else {
//                makeToast("Error in updating student");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDataAsync();
    }


    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", teacher.getUserId());
                hm.put("role", String.valueOf(UserRole.Teacher.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    List<TeacherClass> teacherClasses = DataUtils.getTeacherClassListFromJsonString(result);

                    teacher.getTeacherClassList().clear();
                    teacher.getTeacherClassList().addAll(teacherClasses);

                    userUtils.saveUserWithDataToSharedPrefs(teacher, Teacher.class);

                    for (TeacherClass aClass : teacherClasses) {
                        if (teacherClass.getId().equalsIgnoreCase(aClass.getId())) {
                            teacherClass = aClass;
                            setAdapter();
                            break;
                        }
                    }
                }
            }
        }.execute();
    }

}
