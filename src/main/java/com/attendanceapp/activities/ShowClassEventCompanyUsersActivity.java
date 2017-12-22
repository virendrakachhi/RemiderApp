package com.attendanceapp.activities;

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

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.TeacherInviteStudent;
import com.attendanceapp.adapters.UserExpandableListAdapter;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ShowClassEventCompanyUsersActivity extends Activity implements View.OnClickListener {

    public static final Integer REQUEST_ADD_STUDENT = 30;
    public static final Integer REQUEST_UPDATE_STUDENT = 31;


    protected ImageView helpButton;
    protected TextView addStudentButton, className;
    protected ExpandableListView studentListView;
    private UserExpandableListAdapter userExpandableListAdapter;
    private ClassEventCompany classEventCompany;
    protected SharedPreferences sharedPreferences;
    protected User user;
    private UserRole userRole;

    UserUtils userUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stdents);

        helpButton = (ImageView) findViewById(R.id.helpButton);
        addStudentButton = (TextView) findViewById(R.id.addStudentButton);
        className = (TextView) findViewById(R.id.className);
        studentListView = (ExpandableListView) findViewById(R.id.studentListView);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(ShowClassEventCompanyUsersActivity.this);
        user = userUtils.getUserFromSharedPrefs();

        Bundle bundle = getIntent().getExtras();

        int role = bundle.getInt(AppConstants.EXTRA_USER_ROLE);
        if (role == 0) {
            throw new RuntimeException("must pass role!");
        } else {
            userRole = UserRole.valueOf(role);
            setRoleBasedProperties(userRole);
        }

        int index = bundle.getInt(AppConstants.EXTRA_SELECTED_INDEX, -1);
        if (index == -1) {
            throw new RuntimeException("must pass EXTRA_SELECTED_INDEX!");
        } else {
            classEventCompany = user.getClassEventCompanyArrayList().get(index);
        }

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

        className.setText(classEventCompany.getName());
    }

    String addStudentButtonText;

    private void setRoleBasedProperties(UserRole userRole) {
        if (userRole == UserRole.Teacher) {
            addStudentButtonText = "Student";

        } else if (userRole == UserRole.EventHost) {
            addStudentButtonText = "Attendee";

        } else if (userRole == UserRole.Manager) {
            addStudentButtonText = "Employee";

        }

        addStudentButton.setText("Add " + addStudentButtonText);
    }

    private void setAdapter() {
        userExpandableListAdapter = new UserExpandableListAdapter(ShowClassEventCompanyUsersActivity.this, classEventCompany);
        studentListView.setAdapter(userExpandableListAdapter);
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
                Bundle bundle = new Bundle();
                bundle.putInt(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
                bundle.putString(InviteUser.EXTRA_CLASS_CODE, classEventCompany.getUniqueCode());
                bundle.putString(InviteUser.EXTRA_CLASS_ID, classEventCompany.getId());
                bundle.putBoolean(InviteUser.EXTRA_IS_FIRST_TIME, false);

                Intent addStudentIntent = new Intent(ShowClassEventCompanyUsersActivity.this, InviteUser.class);
                addStudentIntent.putExtras(bundle);
                startActivityForResult(addStudentIntent, REQUEST_ADD_STUDENT);
                break;
        }
    }

    private void makeToast(String message) {
        Toast.makeText(ShowClassEventCompanyUsersActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        userExpandableListAdapter.notifyDataSetChanged();
        if (requestCode == REQUEST_ADD_STUDENT) {
            if (resultCode == RESULT_OK) {
                makeToast("Added successfully!");
            } else {
                makeToast("Error, not added!");
            }
        } else if (requestCode == REQUEST_UPDATE_STUDENT) {
            if (resultCode == RESULT_OK) {
                makeToast("Updated successfully!");
            } else {
                makeToast("Error, not updated!");
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
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(userRole.getRole()));
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

                    List<ClassEventCompany> teacherClasses = DataUtils.getClassEventCompanyArrayListFromJsonString(result);

                    user.getClassEventCompanyArrayList().clear();
                    user.getClassEventCompanyArrayList().addAll(teacherClasses);

                    userUtils.saveUserToSharedPrefs(user);

                    for (ClassEventCompany aClass : teacherClasses) {
                        if (classEventCompany.getId().equalsIgnoreCase(aClass.getId())) {
                            classEventCompany = aClass;
                            setAdapter();
                            break;
                        }
                    }
                }
            }
        }.execute();
    }

}
