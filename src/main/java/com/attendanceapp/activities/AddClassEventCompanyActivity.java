package com.attendanceapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.StudentDashboardActivity;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.StudentClass;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddClassEventCompanyActivity extends Activity {

    private static final String TAG = AddClassEventCompanyActivity.class.getSimpleName();
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String EXTRA_SELECTED_CLASS_INDEX = "EXTRA_STUDENT_CLASS_INDEX";

    EditText codeEditText, nameEditText;
    Button done, skip;
    ImageView saveButton, imgHelp;
    TextView addAnotherClass;
    LayoutInflater layoutInflater;
    SharedPreferences sharedPreferences;

    private UserUtils userUtils;
    private User user;
    private UserRole userRole;
    protected boolean isFirstTime;
    private boolean isClassAdded;
    private boolean isEditClass, isClassDeleted;

    private String urlToSendData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_event_company);

        nameEditText = (EditText) findViewById(R.id.className);
        codeEditText = (EditText) findViewById(R.id.classCode);
        imgHelp = (ImageView) findViewById(R.id.imgHelp);
        saveButton = (ImageView) findViewById(R.id.saveButton);
        addAnotherClass = (TextView) findViewById(R.id.addAnotherClass);
        skip = (Button) findViewById(R.id.skip);
        done = (Button) findViewById(R.id.done);

        saveButton.setOnClickListener(saveButtonClickListener);

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        Bundle bundle = getIntent().getExtras();

        int role = bundle.getInt(AppConstants.EXTRA_USER_ROLE);
        if (role == 0) {
            throw new RuntimeException("must pass role!");
        } else {
            userRole = UserRole.valueOf(role);
            setRoleBasedProperties(userRole);
        }

        isFirstTime = bundle.getBoolean(EXTRA_IS_FIRST_TIME);
        if (isFirstTime) {
            skip.setVisibility(View.VISIBLE);
        }

        int index = bundle.getInt(EXTRA_SELECTED_CLASS_INDEX, -1);
        if (index != -1) {
            ClassEventCompany selectedClassEventCompany = user.getClassEventCompanyArrayList().get(index);
            isEditClass = true;

            nameEditText.setText(selectedClassEventCompany.getName());
            codeEditText.setText(selectedClassEventCompany.getUniqueCode());
            codeEditText.setVisibility(View.GONE);
            addAnotherClass.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);

            done.setText("Save");

            /* delete button */
            imgHelp.setImageResource(R.drawable.delete);
            imgHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(AddClassEventCompanyActivity.this)
                            .setMessage("Press Ok to delete!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Map<String, String> keysAndValues = new HashMap<>();
                                    keysAndValues.put("class_code", codeEditText.getText().toString().trim());
                                    keysAndValues.put("class_name", nameEditText.getText().toString().trim());
                                    keysAndValues.put("student_email", user.getEmail());
                                    keysAndValues.put("status", "0");

                                    // finally upload data to server using async task
                                    uploadDataAsync(AppConstants.URL_ADD_CLASS_BY_STUDENT, keysAndValues);

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    dialog.cancel();
                                }
                            }).create().show();
                }
            });
        }

        addAnotherClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeEditText.setText("");
                nameEditText.setText("");
            }
        });
    }

    View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String code = codeEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();

            Map<String, String> keysAndValues = new HashMap<>();
            keysAndValues.put("status", "1");

            if (userRole == UserRole.Student) {
                keysAndValues.put("class_code", code);
                keysAndValues.put("class_name", name);
                keysAndValues.put("student_email", user.getEmail());

                urlToSendData = AppConstants.URL_ADD_CLASS_BY_STUDENT;

            } else if (userRole == UserRole.Attendee) {
                keysAndValues.put("event_code", code);
                keysAndValues.put("eventee_email", user.getEmail());

                urlToSendData = AppConstants.URL_ADD_EVENT_BY_ATTENDEE;

            } else if (userRole == UserRole.Employee) {
                keysAndValues.put("company_code", code);
                keysAndValues.put("employee_email", user.getEmail());

                urlToSendData = AppConstants.URL_ADD_COMPANY_BY_EMPLOYEE;

            }
            uploadDataAsync(urlToSendData, keysAndValues);
        }
    };

    String title, code, add, txtTitle;

    private void setRoleBasedProperties(UserRole userRole) {
        if (userRole != null) {

            if (userRole == UserRole.Student) {
                txtTitle = "Class setup";
                title = "Class name";
                code = "Class code";
                add = "Add another class";
                urlToSendData = AppConstants.URL_ADD_CLASS_BY_STUDENT;

            } else if (userRole == UserRole.Attendee) {
                txtTitle = "Event setup";
                title = "Event name";
                code = "Event code";
                add = "Add another event";
                urlToSendData = AppConstants.URL_ADD_EVENT_BY_ATTENDEE;

            } else if (userRole == UserRole.Employee) {
                txtTitle = "Meeting Place";
                title = "Meeting Place";
                code = "Meeting code";
                add = "Add another meeting";
                urlToSendData = AppConstants.URL_ADD_COMPANY_BY_EMPLOYEE;

            }
            ((TextView) findViewById(R.id.txtTitle)).setText(txtTitle);
            nameEditText.setHint(title);
            codeEditText.setHint(code);
            addAnotherClass.setText(add);

        }
    }


    private void uploadDataAsync(final String url, final Map<String, String> keysAndValues) {
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    return new WebUtils().post(url, keysAndValues);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    makeToast("Error in uploading data");
                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {
                            if (isEditClass) {
                                makeToast("Deleted successfully!");
                                isClassDeleted = true;
                            } else {
                                makeToast("Saved successfully!");
                                isClassAdded = true;
                            }
                            onBackPressed();
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

    private void makeToast(String title) {
        Toast.makeText(AddClassEventCompanyActivity.this, title, Toast.LENGTH_LONG).show();
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (isFirstTime || isClassAdded || isClassDeleted) {
            updateDataAsync();
            return;
        }
        if (!isEditClass) {
            startActivity(new Intent(AddClassEventCompanyActivity.this, Employee_DashboardActivity.class));
        }

        finish();
    }


    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progressDialog = new ProgressDialog(AddClassEventCompanyActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.Student.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();
                if (result != null) {

                    Student student1 = new Student(user);
                    List<StudentClass> teacherClasses = DataUtils.getStudentClassListFromJsonString(result);

                    if (student1.getStudentClassList().size() != teacherClasses.size()) {

                        student1.getStudentClassList().clear();
                        student1.getStudentClassList().addAll(teacherClasses);

                        userUtils.saveUserWithDataToSharedPrefs(user, Student.class);

                    }
                }
                startActivity(new Intent(AddClassEventCompanyActivity.this, Employee_DashboardActivity.class));
                finish();
            }
        }.execute();

    }


}
