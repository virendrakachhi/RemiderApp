package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ParentAddChildActivity extends Activity {

    public static final String EXTRA_PARENT = "extra_parent";
    private static final String TAG = "ParentAddChildActivity";

    public static final String EXTRA_IS_EDIT_STUDENT = "EXTRA_IS_EDIT_STUDENT";
    public static final String EXTRA_STUDENT_NAME = "EXTRA_STUDENT_NAME";
    public static final String EXTRA_STUDENT_ID = "EXTRA_STUDENT_ID";


    EditText classCodeEditText, classNameEditText;
    Button done, skip, saveIt;
    ImageView saveButton;
    LinearLayout addAnotherClass;
    ImageButton deleteButton;


    LayoutInflater layoutInflater;
    SharedPreferences sharedPreferences;

    //    private Student student;
    protected boolean isFirstTime;
    private boolean isClassAdded;
    //    private Parent parent;
    private User parent;

    protected UserUtils userUtils;

    private String studentName, studentId;
    private boolean finishActivity, isEditStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_add_child);

        classNameEditText = (EditText) findViewById(R.id.className);
        classCodeEditText = (EditText) findViewById(R.id.classCode);
        saveButton = (ImageView) findViewById(R.id.saveButton);
        addAnotherClass = (LinearLayout) findViewById(R.id.addAnotherClass);
        skip = (Button) findViewById(R.id.skip);
        saveIt=(Button) findViewById(R.id.saveIt);
        done = (Button) findViewById(R.id.done);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(this);
        parent = userUtils.getUserFromSharedPrefs();

        Intent intent = getIntent();

        isFirstTime = intent.getBooleanExtra(AppConstants.EXTRA_IS_FIRST_TIME, false);
        isEditStudent = intent.getBooleanExtra(EXTRA_IS_EDIT_STUDENT, false);
        studentName = intent.getStringExtra(EXTRA_STUDENT_NAME);
        studentId = intent.getStringExtra(EXTRA_STUDENT_ID);
        classNameEditText.setText(studentName);


        saveIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finishActivity = true;

                String childName = classNameEditText.getText().toString().trim();
                String childCode = classCodeEditText.getText().toString().trim();

                if (childName.length() < 1) {
                    makeToast("Please enter child name");
                    return;
                }

                Map<String, String> keysAndValues = new HashMap<>();

                keysAndValues.put("id", "");

                if (isEditStudent) {
                    keysAndValues.put("id", studentId);
                    keysAndValues.put("child_name", childName);

                    addChildAsync(AppConstants.URL_PARENT_EDIT_CHILD, keysAndValues);

                } else {

                    if (childCode.length() < 6) {
                        makeToast("Please enter correct code");
                        return;
                    }

                    keysAndValues.put("code", childCode);
                    keysAndValues.put("user_id", parent.getUserId());
                    keysAndValues.put("childName", childName);

                    addChildAsync(AppConstants.URL_PARENT_ADD_CHILD, keysAndValues);
                }
            }
        });
        addAnotherClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classCodeEditText.setText("");
                classNameEditText.setText("");
            }
        });
        /*addAnotherClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity = false;

                String childName = classNameEditText.getText().toString().trim();
                String childCode = classCodeEditText.getText().toString().trim();

                if (childName.length() < 1) {
                    makeToast("Please enter child name");
                    return;
                }

                Map<String, String> keysAndValues = new HashMap<>();

                keysAndValues.put("id", "");

                if (isEditStudent) {
                    keysAndValues.put("id", studentId);
                    keysAndValues.put("child_name", childName);

                    addChildAsync(AppConstants.URL_PARENT_EDIT_CHILD, keysAndValues);

                } else {

                    if (childCode.length() < 6) {
                        makeToast("Please enter correct code");
                        return;
                    }

                    keysAndValues.put("code", childCode);
                    keysAndValues.put("user_id", parent.getUserId());
                    keysAndValues.put("childName", childName);

                    addChildAsync(AppConstants.URL_PARENT_ADD_CHILD, keysAndValues);
                }


                classCodeEditText.setText("");
                classNameEditText.setText("");
            }
        });*/

        if (isFirstTime) {
            skip.setVisibility(View.VISIBLE);
        }

        if (isEditStudent) {
            classCodeEditText.setVisibility(View.GONE);
            addAnotherClass.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(ParentAddChildActivity.this)
                        .setMessage("Delete child!")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                new AsyncTask<Void, Void, String>() {
                                    private ProgressDialog progressDialog = new ProgressDialog(ParentAddChildActivity.this);

                                    @Override
                                    protected void onPreExecute() {
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                    }

                                    @Override
                                    protected String doInBackground(Void... params) {
                                        HashMap<String, String> hm = new HashMap<>();
                                        hm.put("id", studentId);
                                        try {
                                            return new WebUtils().post(AppConstants.URL_PARENT_DELETE_CHILD, hm);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(String result) {
                                        progressDialog.dismiss();
                                        progressDialog.cancel();

                                        if (result == null) {
                                            makeToast("Error in deleting child!");
                                            setResult(RESULT_CANCELED);
                                        } else {
                                            makeToast("Deleted successfully!");
                                            setResult(RESULT_OK);
                                            onBackPressed();
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
        });
    }

    private void addChildAsync(final String url, final Map<String, String> params) {

        new AsyncTask<Void, Void, String>() {

            private ProgressDialog progressDialog = new ProgressDialog(ParentAddChildActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }


            @Override
            protected String doInBackground(Void... voids) {
                String result = null;
                try {

                    result = new WebUtils().post(url, params);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                progressDialog.dismiss();
                progressDialog.cancel();

                if (s == null) {
                    makeToast("Error in uploading data");
                    if (isEditStudent) {
                        setResult(RESULT_CANCELED);
                    }
                } else {
                    makeToast("Child is saved!");

                    if (isEditStudent) {
                        setResult(RESULT_OK);
                    } else {
                        isClassAdded = true;
                    }

                    onBackPressed();
                }
            }
        }.execute();

    }

    private void makeToast(String title) {
        Toast.makeText(ParentAddChildActivity.this, title, Toast.LENGTH_LONG).show();
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (isFirstTime || isClassAdded) {
            startActivity(new Intent(ParentAddChildActivity.this, ParentDashboardActivity.class));
        }
        if (finishActivity) {
            finish();
            return;
        }
        super.onBackPressed();
    }

}
