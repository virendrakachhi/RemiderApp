package com.attendanceapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.AppGlobals;
import com.attendanceapp.NothingSelectedSpinnerAdapter;
import com.attendanceapp.R;
import com.attendanceapp.SplashActivity;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddUserViewActivity extends Activity implements View.OnClickListener {

    private static final String TAG = AddUserViewActivity.class.getSimpleName();
    Button teacherButton, parentButton, studentButton, attendeeButton, eventHostButton, managerButton, employeeButton;
    LinearLayout schoolLayout, eventLayout, companyLayout;
    protected Spinner registerUserType;

    User user;
    private SharedPreferences sharedPreferences;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_view);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        final String userString = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);
        user = gson.fromJson(userString, User.class);

        if (user == null) {
            startActivity(new Intent(AddUserViewActivity.this, SplashActivity.class));
            finish();
        }

        registerUserType = (Spinner) findViewById(R.id.registerUserType);

        schoolLayout = (LinearLayout) findViewById(R.id.schoolLayout);
        eventLayout = (LinearLayout) findViewById(R.id.eventLayout);
        companyLayout = (LinearLayout) findViewById(R.id.companyLayout);

        teacherButton = (Button) findViewById(R.id.btn_teacher);
        parentButton = (Button) findViewById(R.id.btn_parent);
        studentButton = (Button) findViewById(R.id.btn_student);

        attendeeButton = (Button) findViewById(R.id.btn_attendee);
        eventHostButton = (Button) findViewById(R.id.btn_event_host);

        managerButton = (Button) findViewById(R.id.btn_manager);
        employeeButton = (Button) findViewById(R.id.btn_employee);

        teacherButton.setOnClickListener(this);
        parentButton.setOnClickListener(this);
        studentButton.setOnClickListener(this);

        attendeeButton.setOnClickListener(this);
        eventHostButton.setOnClickListener(this);

        managerButton.setOnClickListener(this);
        employeeButton.setOnClickListener(this);

        if ("School".equalsIgnoreCase(user.getUserView())) {
            schoolLayout.setVisibility(View.VISIBLE);
        } else if ("Event".equalsIgnoreCase(user.getUserView())) {
            eventLayout.setVisibility(View.VISIBLE);
        } else if ("Corporation".equalsIgnoreCase(user.getUserView())) {
            companyLayout.setVisibility(View.VISIBLE);
        }


        ArrayAdapter<CharSequence> registerUserTypeAdapter = ArrayAdapter.createFromResource(this, R.array.register_user_type, R.layout.selector_item);
        registerUserTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registerUserType.setPrompt("Select View");
        registerUserType.setAdapter(new NothingSelectedSpinnerAdapter(registerUserTypeAdapter, R.layout.register_spinner_user_view_nothing_selected, this));
        registerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 1) {
                    schoolLayout.setVisibility(View.VISIBLE);
                    eventLayout.setVisibility(View.GONE);
                    companyLayout.setVisibility(View.GONE);
                } else if (position == 2) {
                    schoolLayout.setVisibility(View.GONE);
                    eventLayout.setVisibility(View.VISIBLE);
                    companyLayout.setVisibility(View.GONE);
                } else if (position == 3) {
                    schoolLayout.setVisibility(View.GONE);
                    eventLayout.setVisibility(View.GONE);
                    companyLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                makeToast("Nothing Selected");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_teacher:
                updateRoll(1);
                break;
            case R.id.btn_parent:
                updateRoll(3);
                break;
            case R.id.btn_student:
                updateRoll(2);
                break;
            case R.id.btn_manager:
                updateRoll(4);
                break;
            case R.id.btn_employee:
                updateRoll(5);
                break;
            case R.id.btn_event_host:
                updateRoll(6);
                break;
            case R.id.btn_attendee:
                updateRoll(7);
                break;
        }
    }


    private void updateRoll(final int role) {

        List<UserRole> list = user.getUserRoles();
        for (UserRole userRole : list) {
            if (role == userRole.getRole()) {
                makeToast("This view is already activated");
                return;
            }
        }

        class UpdateRollAsync extends AsyncTask<Void, Void, String> {
            @SuppressWarnings("deprecation")
            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                DefaultHttpClient dhc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                HttpPost postMethod = new HttpPost(AppConstants.URL_ADD_ROLL);

                List<NameValuePair> namePairs = new ArrayList<>();
                namePairs.add(new BasicNameValuePair("user_id", user.getUserId()));
                namePairs.add(new BasicNameValuePair("role", String.valueOf(role)));

                try {
                    postMethod.setEntity(new UrlEncodedFormEntity(namePairs));
                    result = dhc.execute(postMethod, res);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                int result = RESULT_CANCELED;
                if (s == null) {
                    makeToast("Error in updating role");
                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {

                            user = DataUtils.getUserFromJsonString(s);
                            ((AppGlobals) getApplication()).setUser(user);

                            sharedPreferences.edit()
                                    .putString(AppConstants.KEY_LOGGED_IN_USER, gson.toJson(user, User.class)).apply();

                            result = RESULT_OK;

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error in parsing data: " + s);
                        Log.e(TAG, e.getMessage());
                    }
                }

                setResult(result);
                finish();

            }
        }

        new UpdateRollAsync().execute();

    }

    private void makeToast(String title) {
        Toast.makeText(AddUserViewActivity.this, title, Toast.LENGTH_SHORT).show();
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

}
