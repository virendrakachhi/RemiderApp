package com.attendanceapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.attendanceapp.activities.AddClassEventCompanyActivity;
import com.attendanceapp.activities.Attendee_AddEventActivity;
import com.attendanceapp.activities.CreateClassEventCompanyActivity;
import com.attendanceapp.models.Employee;
import com.attendanceapp.models.EventHost;
import com.attendanceapp.models.Eventee;
import com.attendanceapp.models.Family;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.Parent;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.services.BeaconMonitorService;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.google.android.gcm.GCMRegistrar;

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
import java.util.HashMap;
import java.util.List;


public class RoleSelectActivity extends Activity implements View.OnClickListener {
    private static final String TAG = RoleSelectActivity.class.getSimpleName();
    protected Button teacherButton, parentButton, studentButton, attendeeButton, eventHostButton, managerButton, employeeButton, familyButton, managerHCButton, employeeHCButton;
    protected LinearLayout schoolLayout, eventLayout, companyLayout,healthCareLayout;
    private User user;
    private UserUtils userUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_the_one);

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        schoolLayout = (LinearLayout)findViewById(R.id.schoolLayout);
        eventLayout = (LinearLayout) findViewById(R.id.eventLayout);
        companyLayout = (LinearLayout) findViewById(R.id.companyLayout);
        healthCareLayout = (LinearLayout) findViewById(R.id.health_care_Layout);

        teacherButton = (Button) findViewById(R.id.btn_teacher);
        parentButton = (Button) findViewById(R.id.btn_parent);
        studentButton = (Button) findViewById(R.id.btn_student);

        attendeeButton = (Button) findViewById(R.id.btn_attendee);
        eventHostButton = (Button) findViewById(R.id.btn_event_host);

        managerButton = (Button) findViewById(R.id.btn_manager);
        employeeButton = (Button) findViewById(R.id.btn_employee);

        familyButton = (Button) findViewById(R.id.btn_family);
        managerHCButton = (Button) findViewById(R.id.btn_manager_hc);
        employeeHCButton = (Button) findViewById(R.id.btn_employee_hc);

        teacherButton.setOnClickListener(this);
        parentButton.setOnClickListener(this);
        studentButton.setOnClickListener(this);

        attendeeButton.setOnClickListener(this);
        eventHostButton.setOnClickListener(this);

        managerButton.setOnClickListener(this);
        employeeButton.setOnClickListener(this);


        familyButton.setOnClickListener(this);
        managerHCButton.setOnClickListener(this);
        employeeHCButton.setOnClickListener(this);

        if ("School".equalsIgnoreCase(user.getUserView())) {
            schoolLayout.setVisibility(View.VISIBLE);
        } else if ("Event".equalsIgnoreCase(user.getUserView())) {
            eventLayout.setVisibility(View.VISIBLE);
        } else if ("Company".equalsIgnoreCase(user.getUserView())) {
            companyLayout.setVisibility(View.VISIBLE);
        }
        else if ("Healthcare".equalsIgnoreCase(user.getUserView())) {
            healthCareLayout.setVisibility(View.VISIBLE);
        }
    }

    public void gotoBack(View view) {
        finish();
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
            case R.id.btn_manager_hc:
                updateRoll(10);
                break;
           case R.id.btn_employee_hc:
                updateRoll(11);
                break;
            case R.id.btn_family:
                updateRoll(12);
                break;


        }
    }

    private void updateRoll(final int role) {

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
                if (s == null) {
                    makeToast("Error in updating student");

                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {

                            AppGlobals globals = (AppGlobals) getApplication();
                            globals.setUser(user);

                            switch (role) {
                                case 1:
                                    user.getUserRoles().add(UserRole.Teacher);
                                    globals.setTeacher(new Teacher(user));
                                    break;
                                case 2:
                                    user.getUserRoles().add(UserRole.Student);
                                    globals.setStudent(new Student(user));
                                    break;
                                case 3:
                                    user.getUserRoles().add(UserRole.Parent);
                                    globals.setParent(new Parent(user));
                                    break;
                                case 4:
                                    user.getUserRoles().add(UserRole.Manager);
                                    globals.setManager(new Manager(user));
                                    break;
                                case 5:
                                    user.getUserRoles().add(UserRole.Employee);
                                    globals.setEmployee(new Employee(user));
                                    break;
                                case 6:
                                    user.getUserRoles().add(UserRole.EventHost);
                                    globals.setEventHost(new EventHost(user));
                                    break;
                                case 7:
                                    user.getUserRoles().add(UserRole.Attendee);
                                    globals.setEventee(new Eventee(user));
                                    break;
                                case 10:
                                    user.getUserRoles().add(UserRole.ManagerHC);
                                    globals.setManager(new Manager(user));
                                    break;
                                case 11:
                                    user.getUserRoles().add(UserRole.EmployeeHC);
                                    globals.setEmployee(new Employee(user));
                                    break;
                                case 12:
                                    user.getUserRoles().add(UserRole.Family);
                                    globals.setFamily(new Family(user));
                                    break;

                            }

                            userUtils.saveUserToSharedPrefs(user);


                            startService(new Intent(RoleSelectActivity.this, SendLocationService.class));
                            startService(new Intent(RoleSelectActivity.this, BeaconMonitorService.class));
                            registerForGcm(user);


                            Intent activityToStart = new Intent();

                            String userRole = String.valueOf(role);


                            if ("1".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, TeacherAddClassActivity.class);
                            } else if ("2".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, StudentAddClassActivity.class);
                            } else if ("3".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, ParentAddChildActivity.class);
                            } else if ("4".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, CreateClassEventCompanyActivity.class);
                            } else if ("5".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, AddClassEventCompanyActivity.class);
                            } else if ("6".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, CreateClassEventCompanyActivity.class);
                            } else if ("7".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, Attendee_AddEventActivity.class);
                            }
                            else if ("10".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(RoleSelectActivity.this, HealthCareAddLocationActivity.class);

                            }
                            else if ("11".equalsIgnoreCase(userRole)) {
                                updateDataAsync();
                                return;
//                                activityToStart = new Intent(RoleSelectActivity.this, EmployeeHCDashboardActivity.class);
                            }
                            else if ("12".equalsIgnoreCase(userRole)) {
                                updateFamilyDataAsync();
                                return;
//                                activityToStart = new Intent(RoleSelectActivity.this, FamilyHCDashboardActivity.class);
                            }

                            activityToStart.putExtra(AppConstants.EXTRA_IS_FIRST_TIME, true);

                            Bundle bundle = new Bundle();
                            bundle.putInt(AppConstants.EXTRA_USER_ROLE, role);
                            bundle.putBoolean(AppConstants.EXTRA_IS_FIRST_TIME, true);
                            activityToStart.putExtras(bundle);

                            startActivity(activityToStart);
                            finish();


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error in parsing data: " + s);
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }

        new UpdateRollAsync().execute();

    }

    private void makeToast(String title) {
        Toast.makeText(RoleSelectActivity.this, title, Toast.LENGTH_LONG).show();
    }


    private void registerForGcm(final User user) {
        Activity activity = RoleSelectActivity.this;
        GCMRegistrar.checkDevice(activity);
        GCMRegistrar.checkManifest(activity);
        final String regId = GCMRegistrar.getRegistrationId(activity);

        if (regId.equals("")) {
            GCMRegistrar.register(activity, AppConstants.SENDER_ID);
        } else {
            if (GCMRegistrar.isRegisteredOnServer(activity)) {
                Log.i(TAG, "already registered to server");
                // Skips registration.
            } else {
                // Try to register again, but not in the UI thread.
                final Context context = activity;
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        ServerUtilities.register(context, regId, user.getUserId(), user.getDeviceToken());
                        return null;
                    }
                }.execute();
            }
        }

        registerReceiver(mHandleMessageReceiver, new IntentFilter(AppConstants.DISPLAY_MESSAGE_ACTION));
    }

    /**
     * Receiving push messages
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(AppConstants.EXTRA_BROADCAST_MESSAGE);
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());

            /**
             * Take appropriate action on this message
             * depending upon your app requirement
             * */

            // Showing received message
            //lblMessage.append(newMessage + "\n");


            Toast.makeText(getApplicationContext(), "New Message: " + newMessage, Toast.LENGTH_LONG).show();

            // Releasing wake lock
            WakeLocker.release();
        }
    };


    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }



    private void updateDataAsync() {
//        new AsyncTask<Void, Void, String>() {
//
//            @Override
//            protected String doInBackground(Void... params) {
//                HashMap<String, String> hm = new HashMap<>();
//                hm.put("id", user.getUserId());
//                hm.put("role", String.valueOf(UserRole.EmployeeHC.getRole()));
//                try {
//                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_HCEMPLOYEE, hm);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                if (result != null) {
//                    Employee fm = new Employee(user);
//                    List<HCEmployee> newList = DataUtils.getHCEmployeeLocationListFromJsonString(result);
//                       if (fm.getManagerLocationList().size() != newList.size()) {
//
//                            fm.getManagerLocationList().clear();
//                            fm.getManagerLocationList().addAll(newList);
//                            userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
//                            userUtils.saveUserToSharedPrefs(user);
//                        }
//                    }
//
//
//            }
//        }.execute();
//        userUtils = new UserUtils(RoleSelectActivity.this);

//        user = userUtils.getUserFromSharedPrefs();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.EmployeeHC.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_HCEMPLOYEE, hm);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Employee fm = new Employee(user);
                    List<HCEmployee> newList = DataUtils.getHCEmployeeLocationListFromJsonString(result);



//                    if(employee!=null) {
                    if (fm.getManagerLocationList().size() != newList.size()) {

                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

//                            userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
//                            userUtils.saveUserToSharedPrefs(user);

//                            baseViewPagerAdapter.notifyDataSetChanged();
//                        setOneWordTextView(0);
//                        employeeChecklist_Id = DataUtils.getHCEmployeeCheckListFromJsonString(result);
                    }
//                    }
                    else {
                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

//                        setOneWordTextView(0);
//                        employeeChecklist_Id = DataUtils.getHCEmployeeCheckListFromJsonString(result);
                    }
//                    employee = fm;
                    userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
                    userUtils.saveUserToSharedPrefs(user);
                    Log.v("Inside UPdate","bla bla");
                    startActivity( new Intent(RoleSelectActivity.this, EmployeeHCDashboardActivity.class));
                    finish();
                }
            }
        }.execute();

    }

    private void updateFamilyDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.Family.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_FAMILY, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Family fm = new Family(user);
                    List<HCFamily> newList = DataUtils.getFamilyLocationListFromJsonString(result);
//                    if(family!=null) {
                    if (fm.getManagerLocationList().size() != newList.size()) {

                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);
                        userUtils.saveUserWithDataToSharedPrefs(fm, Family.class);
                        userUtils.saveUserToSharedPrefs(user);

//                            baseViewPagerAdapter.notifyDataSetChanged();
//                            setOneWordTextView(0);
//                        }
                    }
                    else {
                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

                        userUtils.saveUserWithDataToSharedPrefs(fm, Family.class);
                    }
                    startActivity( new Intent(RoleSelectActivity.this, FamilyHCDashboardActivity.class));
                    finish();
                }
            }
        }.execute();

    }




}
