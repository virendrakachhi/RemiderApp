package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.activities.AlertActivity;
import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.activities.RKSelectEmployeeActivity;
import com.attendanceapp.activities.ReportsActivity;
import com.attendanceapp.activities.ShowClassEventCompanyUsersActivity;
import com.attendanceapp.adapters.HCEmpPickerAdapter;
import com.attendanceapp.adapters.HCViewPagerAdapter;
import com.attendanceapp.models.EmployeeStatus;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.ScheduleStatus;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.attendanceapp.webserviceCommunicator.WebServiceHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ManagerHCDashboardActivity extends FragmentActivity implements View.OnClickListener, NavigationPage.NavigationFunctions, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = EventHost_DashboardActivity.class.getSimpleName();
    private static final int REQUEST_EDIT_ACCOUNT = 100;

    Manager manager;
    protected ImageView addLoacationButton;
    private TextView oneWordTextView;
    protected LinearLayout checklistBtn, messageBtn, notificationBtn, mainPage;
    protected RelativeLayout schedulingBtn;
    public static String picUrl;
    /* for settings page functionality */
    protected ImageView settingButton;
    protected LinearLayout createAccountLayout, reportsHCLayout, reportsLayout, onOffNotifications, locationsLayout, settingPage;
    protected RelativeLayout oneWordTitleLayout;
    protected ImageView onOffNotificationImageView;
    boolean notificationStatus;

    private ScrollView swipePage;
    private HCViewPagerAdapter baseViewPagerAdapter;
    private ViewPager mViewPager;
    private Animation textAnimation;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    public static SharedPreferences sharedPreferences;
    private UserUtils userUtils;
    public static User user;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String mLocationUpdate;

    private ImageView imgScheduleDot;

    private int locationPosition = 0;

    MyReceiver myReceiver;
    public static String MY_ACTION = "MY_ACTION";

    public static ArrayList<ScheduleStatus> statusArrayList = new ArrayList<ScheduleStatus>();

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                updateDataAsync();
            }
        }
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        mGoogleApiClient.connect();

        //Register BroadcastReceiver
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        /*Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        sendBroadcast(intent);*/

        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(myReceiver);
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_hc_dashboard);

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).build();
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null)
            AppGlobals.mCurrentLocation = location;


        mViewPager = (ViewPager) findViewById(R.id.pager);
        swipePage = (ScrollView) findViewById(R.id.swipePage);
        addLoacationButton = (ImageView) findViewById(R.id.addLocationButton);
        oneWordTextView = (TextView) findViewById(R.id.oneWordTitle);

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        checklistBtn = (LinearLayout) findViewById(R.id.checklistBtn);
        schedulingBtn = (RelativeLayout) findViewById(R.id.schedulingBtn);
        messageBtn = (LinearLayout) findViewById(R.id.messageBtn);
        notificationBtn = (LinearLayout) findViewById(R.id.notificationBtn);
        mainPage = (LinearLayout) findViewById(R.id.mainPage);
        oneWordTitleLayout = (RelativeLayout) findViewById(R.id.oneWordTitleLayout);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        imgScheduleDot = (ImageView) findViewById(R.id.img_schedule_dot);
        imgScheduleDot.setOnClickListener(this);
//        imgScheduleDot.setVisibility(View.GONE);
        /* for setting tab */
        settingPage = (LinearLayout) findViewById(R.id.settingPage);
        settingButton = (ImageView) findViewById(R.id.settingButton);
        createAccountLayout = (LinearLayout) findViewById(R.id.createAccountLayout);
        reportsHCLayout = (LinearLayout) findViewById(R.id.reportsHCLayout);
        reportsLayout = (LinearLayout) findViewById(R.id.reportsHCLayout);
        onOffNotifications = (LinearLayout) findViewById(R.id.onOffNotifications);
        locationsLayout = (LinearLayout) findViewById(R.id.locationsLayout);
        onOffNotificationImageView = (ImageView) findViewById(R.id.onOffNotificationImageView);
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(ManagerHCDashboardActivity.this);
        addLoacationButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        checklistBtn.setOnClickListener(this);
        schedulingBtn.setOnClickListener(this);
        messageBtn.setOnClickListener(this);
        notificationBtn.setOnClickListener(this);
        onOffNotificationImageView.setOnClickListener(this);
        createAccountLayout.setOnClickListener(this);
        reportsHCLayout.setOnClickListener(this);
        reportsLayout.setOnClickListener(this);
        onOffNotifications.setOnClickListener(this);
        locationsLayout.setOnClickListener(this);

        oneWordTitleLayout.setOnTouchListener(swipeTouchListener);
        createAccountLayout.setOnTouchListener(swipeTouchListener);
        reportsHCLayout.setOnTouchListener(swipeTouchListener);
        reportsLayout.setOnTouchListener(swipeTouchListener);
        onOffNotifications.setOnTouchListener(swipeTouchListener);
        locationsLayout.setOnTouchListener(swipeTouchListener);

        settingButton.setOnTouchListener(swipeTouchListener);
        mainPage.setOnTouchListener(swipeTouchListener);
        settingPage.setOnTouchListener(swipeTouchListener);
        swipePage.setOnTouchListener(swipeTouchListener);
        checklistBtn.setOnTouchListener(swipeTouchListener);
        schedulingBtn.setOnTouchListener(swipeTouchListener);
        messageBtn.setOnTouchListener(swipeTouchListener);
        notificationBtn.setOnTouchListener(swipeTouchListener);
        navigationButton.setOnClickListener(this);
        navigationButton.setOnTouchListener(swipeTouchListener);

        userUtils = new UserUtils(ManagerHCDashboardActivity.this);
        user = userUtils.getUserFromSharedPrefs();
        manager = userUtils.getUserWithDataFromSharedPrefs(Manager.class);
        try {
            if (manager != null) {
                if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > 0) {


//         manager = new Manager(user);
                    //  Toast.makeText(getApplicationContext(),Integer.toString(manager.getManagerLocationList().size()),Toast.LENGTH_LONG).show();
                    baseViewPagerAdapter = new HCViewPagerAdapter(getSupportFragmentManager(), manager.getManagerLocationList());
                    mViewPager.setAdapter(baseViewPagerAdapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setOneWordTextView(0);

        //noinspection deprecation
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                locationPosition = position;
                setOneWordTextView(position);
                setScheduleStatus(position);
            }
        });
    }

    private void setOneWordTextView(int current) {
        if (manager != null) {
            if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > current) {
                oneWordTextView.setText(String.valueOf(manager.getManagerLocationList().get(current).getLocationName().charAt(0)).toUpperCase());
            }
        }
        showMessageIfNoClass();
        onOffNotificationsSetImage();
    }

    private void showMessageIfNoClass() {
        if (manager != null) {
            if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > 0) {

                swipePage.setVisibility(manager.getManagerLocationList().size() == 0 ? View.GONE : View.VISIBLE);
            }
        } else {
            swipePage.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.checklistBtn:
                employeePicker(true);
                break;

            case R.id.addLocationButton:
                Intent i = new Intent(ManagerHCDashboardActivity.this, HealthCareAddLocationActivity.class);
                startActivity(i);
                finish();
                break;

            case R.id.settingButton:
                settingButton();
                break;
            case R.id.notificationBtn:
                classNotificationLayout();
                break;
            case R.id.messageBtn:
                sendClassNotificationBtn();
                break;
            case R.id.createAccountLayout:
                userPicker();
                break;
            case R.id.schedulingBtn:
                employeePicker(false);
                break;
            case R.id.reportsHCLayout:
//                Intent reportsIntent = new Intent(ManagerHCDashboardActivity.this, HCModuleReportActivity.class);
//                startActivity(reportsIntent);
                reportsUserPicker();
                break;
            case R.id.locationsLayout:
                Intent in = new Intent(ManagerHCDashboardActivity.this, HealthCareAddLocationActivity.class);
                in.putExtra(HealthCareAddLocationActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
                startActivity(in);
                break;
            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.onOffNotificationImageView:
                if (notificationStatus) {
                    updateNotificationStatus("0", mViewPager.getCurrentItem());
                    notificationStatus = false;
                    onOffNotificationImageView.setImageResource(notificationStatus ? R.drawable.on : R.drawable.off);
                } else {
                    updateNotificationStatus("1", mViewPager.getCurrentItem());
                    notificationStatus = true;
                    onOffNotificationImageView.setImageResource(notificationStatus ? R.drawable.on : R.drawable.off);
                }
                break;

            case R.id.img_schedule_dot:
                boolean isAvailable = false;
                if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > 0) {
                    String currentLocationId = manager.getManagerLocationList().get(locationPosition).getId();
                    new AsyncGetActiveEmp().execute(currentLocationId);
                        /*for (int j = 0; j < statusArrayList.size(); j++) {
                            if (statusArrayList.get(j).getLocationId().equalsIgnoreCase(currentLocationId)){
                                if (!statusArrayList.get(j).getStatus().equalsIgnoreCase("off")){
                                    isAvailable = true;
                                    new AsyncGetActiveEmp().execute(currentLocationId);
                                    break;
                                }
                            }
                        }

                    if (statusArrayList.size() == 0){
                        Toast.makeText(ManagerHCDashboardActivity.this, "Feature not active", Toast.LENGTH_LONG).show();
                    } else if (!isAvailable){
                        Toast.makeText(ManagerHCDashboardActivity.this, "Feature not active", Toast.LENGTH_LONG).show();
                    }*/


                }

//                employeePickerScheduleDot(false);
                break;
        }
    }

    /*
    private void absenceLayout() {

        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            ClassEventCompany teacherClass = manager.getManagerLocationList().(mViewPager.getCurrentItem());
            String classId = teacherClass.getId();
            ProgressDialog dialog = new ProgressDialog(ManagerHCDashboardActivity.this);

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("user_id", user.getUserId());
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
                dialog.dismiss();
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);

                        if (object.has("Error")) {
                            makeToast(object.getString("Error"));
                            return;
                        }
                        Intent intent = new Intent(ManagerHCDashboardActivity.this, Absent.class);
                        intent.putExtra(Absent.EXTRA_ATTENDANCE_DATA, result);
                        intent.putExtra(Absent.EXTRA_TITLE, teacherClass.getName());
                        intent.putExtra(Absent.EXTRA_LIST_OPTION, Absent.SHOW_NAME_DATE);
                        startActivity(intent);

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        makeToast("Error in getting data");
                    }
                } else {
                    makeToast("Please check internet connection");
                }
            }
        }.execute();

    }
*/
    private void reportsLayout() {

//        if (!haveStudentsInClass()) {
//            makeToast("Please add students!");
//            return;
//        }

        Intent intent = new Intent(ManagerHCDashboardActivity.this, ReportsActivity.class);
        intent.putExtra(ReportsActivity.EXTRA_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);

    }

    private void onOffNotifications() {
        ManagerHCClass teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        userUtils.toggleClassNotifications(teacherClass.getId());
        onOffNotificationsSetImage();
    }


    private ManagerHCClass getTeacherClassOnThisPage() {

        return manager.getManagerLocationList().size() > mViewPager.getCurrentItem() ? manager.getManagerLocationList().get(mViewPager.getCurrentItem()) : null;

    }

    private void onOffNotificationsSetImage() {
        if (manager != null) {
            if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > 0) {

                ManagerHCClass teacherClass = getTeacherClassOnThisPage();
                if (teacherClass == null) {
                    return;
                }
                getNotificationStatus(mViewPager.getCurrentItem());
//                boolean isOn = userUtils.isClassNotificationOn(teacherClass.getId());
//                onOffNotificationImageView.setImageResource(isOn ? R.drawable.on : R.drawable.off);
            }
        }
    }

    private void classNotificationLayout() {
//        if (!haveStudentsInClass()) {
//            makeToast("Please add students!");
//            return;
//        }
        Intent intent = new Intent(ManagerHCDashboardActivity.this, AlertActivity.class);
//        intent.putExtra(HCManagerSendNotificationActivity.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        intent.putExtra("location_id", "");
        Bundle bun = new Bundle();
        bun.putInt("Index", mViewPager.getCurrentItem());
        intent.putExtras(bun);
        startActivity(intent);

    }

    private void createAccountLayout() {
        Intent intent = new Intent(ManagerHCDashboardActivity.this, TeacherAddClassActivity.class);
        intent.putExtra(TeacherAddClassActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private void sendClassNotificationBtn() {
//        if (!haveStudentsInClass()) {
//            makeToast("Please add students!");
//            return;
//        }
//        intent.putExtra(EmployeeHCSendMessageToOneLocation.EXTRA_SELECTED_CLASS_INDEX, mViewPager.getCurrentItem());
        Intent intent = new Intent(ManagerHCDashboardActivity.this, EmployeeHCSendMessageToOneLocation.class);
        Bundle bun = new Bundle();
        bun.putInt("Index", mViewPager.getCurrentItem());
        bun.putInt("UserType", 10);
        bun.putString("ChatType", "Multiple");
        intent.putExtras(bun);
//        intent.putExtra(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
        startActivity(intent);
    }

//    private boolean haveStudentsInClass() {
//        int viewPagerIndex = mViewPager.getCurrentItem();
//        List<ManagerHCClass> list = manager.getManagerLocationList();
//        ManagerHCClass teacherClass = list.size() > viewPagerIndex ? list.get(viewPagerIndex) : null;
//
//        return teacherClass != null && teacherClass.getStudentList().size() > 0;
//    }

    private void studentsBtn() {
        Bundle bundle = new Bundle();
        bundle.putInt(AppConstants.EXTRA_USER_ROLE, UserRole.ManagerHC.getRole());
        bundle.putInt(AppConstants.EXTRA_SELECTED_INDEX, mViewPager.getCurrentItem());

        AndroidUtils.openActivity(this, ShowClassEventCompanyUsersActivity.class, bundle, false);
    }

    private void makeToast(String title) {
        Toast.makeText(ManagerHCDashboardActivity.this, title, Toast.LENGTH_LONG).show();
    }

    private void settingButton() {
        if (settingPage.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
            mainPage.setAnimation(textAnimation);
            mainPage.setVisibility(View.VISIBLE);

            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out);
            settingPage.setAnimation(textAnimation);
            settingPage.setVisibility(View.GONE);

            settingButton.setImageResource(R.drawable.setting);

        } else {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out);
            mainPage.setAnimation(textAnimation);
            mainPage.setVisibility(View.GONE);

            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
            settingPage.setAnimation(textAnimation);
            settingPage.setVisibility(View.VISIBLE);

            settingButton.setImageResource(R.drawable.home_blue);

        }
    }

    @Override
    public void onBackPressed() {
        if (navigationLayout.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
            navigationLayout.setAnimation(textAnimation);
            navigationLayout.setVisibility(View.GONE);
        } else if (settingPage.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
            mainPage.setAnimation(textAnimation);
            mainPage.setVisibility(View.VISIBLE);

            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out);
            settingPage.setAnimation(textAnimation);
            settingPage.setVisibility(View.GONE);

            settingButton.setImageResource(R.drawable.setting);
        } else {
            super.onBackPressed();
        }
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdate();
        }
        Manager m = userUtils.getUserWithDataFromSharedPrefs(Manager.class);
//
        if (m != null) {
            List<ManagerHCClass> mainList = manager.getManagerLocationList();
//
            List<ManagerHCClass> newList = m.getManagerLocationList();
//
            if (mainList != null && newList != null && mainList.size() != newList.size()) {
                manager.getManagerLocationList().clear();
                manager.getManagerLocationList().addAll(newList);
                baseViewPagerAdapter.notifyDataSetChanged();
                setOneWordTextView(0);
            }
            super.onResume();
        } else {
            super.onResume();
        }

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());

        setScheduleStatus(locationPosition);

//        updateDataAsync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putBoolean("Image Status", false).commit();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.ManagerHC.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_MANAGER, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {

                        List<ManagerHCClass> newList = DataUtils.getManagerLocationListFromJsonString(result);
                        if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() != newList.size()) {

                            manager.getManagerLocationList().clear();
                            manager.getManagerLocationList().addAll(newList);

                            userUtils.saveUserToSharedPrefs(user);
                            userUtils.saveUserWithDataToSharedPrefs(manager, Manager.class);

                            baseViewPagerAdapter.notifyDataSetChanged();
                            setOneWordTextView(0);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }

    /*
        public void takeAttendance(final String attendanceUsing) {
            final User user = userUtils.getUserFromSharedPrefs();
            final ManagerHCClass classEventCompany = manager.getManagerLocationList().get(mViewPager.getCurrentItem());
            final List<User> studentList = classEventCompany.getUsers();

            if (studentList.size() < 1) {
                makeToast("Please add students to take attendance");
                return;
            }

            new AsyncTask<Void, Void, String>() {
                private ProgressDialog dialog = new ProgressDialog(ManagerHCDashboardActivity.this);

                @Override
                protected void onPreExecute() {
                    dialog.setMessage("Please wait...");
                    dialog.setCancelable(false);
                    dialog.show();
                }

                @Override
                protected String doInBackground(Void... params) {
                    String result = null;
                    // user_id, employee_id ( more than one comma separated ), company_code, company_id, type(Manual, Automatic, ByBeacon)"
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("user_id", user.getUserId());
                    hm.put("company_code", classEventCompany.getUniqueCode());
                    hm.put("company_id", classEventCompany.getId());
                    hm.put("employee_id", StringUtils.getAllIdsFromStudentList(studentList, ','));

                    if ("beacons".equalsIgnoreCase(attendanceUsing)) {
                        hm.put("type", "ByBeacon");
                    } else if ("gps".equalsIgnoreCase(attendanceUsing)) {
                        hm.put("type", "Automatic");
                    }

                    try {
                        result = new WebUtils().post(AppConstants.URL_TAKE_ATTENDANCE_BY_MANAGER, hm);
                    } catch (IOException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    }

                    return result;
                }

                @Override
                protected void onPostExecute(String result) {
                    if (result != null) {

                        if (result.contains("Error") || result.contains("error")) {
                            makeToast("Error in getting attendance!");

                        } else {
                            dialog.dismiss();
                            Intent intent;
                            intent = new Intent(ManagerHCDashboardActivity.this, CommonAttendanceTakenActivity.class);
                            intent.putExtra(CommonAttendanceTakenActivity.EXTRA_SELECTED_CLASS_INDEX, mViewPager.getCurrentItem());
                            intent.putExtra(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
                            intent.putExtra(CommonAttendanceTakenActivity.EXTRA_ATTENDANCE_DATA, result);

                            startActivity(intent);
                        }
                    } else {
                        dialog.dismiss();
                        makeToast("Please check internet connection");
                    }
                }
            }.execute();

        }
    */
    OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
        public boolean onSwipeRight() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            setOneWordTextView(mViewPager.getCurrentItem());
            return true;
        }

        public boolean onSwipeLeft() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            setOneWordTextView(mViewPager.getCurrentItem());
            return true;
        }
    };


    private void userPicker() {


        Intent in = new Intent(ManagerHCDashboardActivity.this, HCModuleManageAccountsActivity.class);
        Bundle bun = new Bundle();
        bun.putInt("Index", mViewPager.getCurrentItem());
        in.putExtras(bun);
        startActivity(in);


//        View view = getLayoutInflater().inflate(R.layout.user_picker_layout, null, false);
//        Button addEmployee = (Button) view.findViewById(R.id.addEmployee);
//        Button addFamily = (Button) view.findViewById(R.id.addFamily);
////        final LinearLayout customLocationBox = (LinearLayout) view.findViewById(R.id.customLocationBox);
//
//        final AlertDialog alertDialog = new AlertDialog.Builder(ManagerHCDashboardActivity.this).setView(view).create();
//        alertDialog.show();
//
//        addEmployee.setOnClickListener(new View.OnClickListener() {
//            double longitude = 0, latitude = 0;
//
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//                alertDialog.cancel();
//                Intent in = new Intent(ManagerHCDashboardActivity.this,CreateHCEmployeeActivity.class);
//                Bundle bun = new Bundle();
//                bun.putString("UserType", "Employee");
//                bun.putInt("Index", mViewPager.getCurrentItem());
////                in.putExtra(HealthCareAddLocationActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
//
//                in.putExtras(bun);
//                startActivity(in);
//                finish();
//             //   Toast.makeText(getApplicationContext(),"Employee",Toast.LENGTH_LONG).show();
//            }
//        });
//
//        addFamily.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //customLocationBox.setVisibility(customLocationBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//                alertDialog.dismiss();
//                alertDialog.cancel();
//                Intent in = new Intent(ManagerHCDashboardActivity.this,CreateHCEmployeeActivity.class);
//                Bundle bun = new Bundle();
//                bun.putInt("Index", mViewPager.getCurrentItem());
//                bun.putString("UserType","Family");
//                in.putExtras(bun);
//                startActivity(in);
//             //   Toast.makeText(getApplicationContext(),"Family",Toast.LENGTH_LONG).show();
//            }
//        });
    }

    private void employeePicker(final boolean checklist) {
        //
        if (manager.getManagerLocationList() != null) {
            final List<HCEmployee> nameList = manager.getManagerLocationList().get(mViewPager.getCurrentItem()).getEmployeeList();
            // String names[] = {"Vicky", "Raj", "Rahul", "Ritesh","Doda","Employee 3", "Employee 4"};
//        makeToast(String.valueOf(nameList.size()));
            if (nameList.size() == 0) {
                HCEmployee ob = new HCEmployee();
                ob.setName("No Employee added yet.");
                nameList.add(ob);
            }
            final AlertDialog dialogBox;
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManagerHCDashboardActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.employee_picker_layout, null);

            alertDialog.setView(convertView);
            if (checklist)
                alertDialog.setTitle("Select employee for CheckListing");
            else
                alertDialog.setTitle("Select employee for scheduling");

            ListView lv = (ListView) convertView.findViewById(R.id.lv);
            final HCEmpPickerAdapter adapter = new HCEmpPickerAdapter(this, nameList);
            lv.setAdapter(adapter);

            alertDialog.setCancelable(true);
            dialogBox = alertDialog.create();
            dialogBox.show();

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    dialogBox.dismiss();
//                alertDialog.show().dismiss();
                    // alertDialog.dismiss();
                    if (!nameList.get(i).getName().matches("No Employee added yet.")) {
                        if (checklist) {
                            Toast.makeText(getApplicationContext(), "Emp Main == " + nameList.get(i).getEmployeeId(), Toast.LENGTH_LONG).show();

                            Intent in = new Intent(ManagerHCDashboardActivity.this, HCListOfChecklists.class);
                            Bundle bun = new Bundle();
                            bun.putString("EmployeeID", nameList.get(i).getEmployeeId());
                            bun.putString("EmployeeName", nameList.get(i).getName());
//                        bun.putString("CheckListID", nameList.get(i).getCheckListId());
                            bun.putBoolean("Edit", false);
                            bun.putInt("Index", mViewPager.getCurrentItem());
                            in.putExtras(bun);
                            startActivity(in);

                        } else {
                            Intent in = new Intent(ManagerHCDashboardActivity.this, CreateEmployeeHCScheduleActivity.class);

                            Bundle bun = new Bundle();
                            bun.putString("SelectedUser", nameList.get(i).getName());
                            bun.putString("EmployeeID", nameList.get(i).getEmployeeId());
                            bun.putInt("Index", mViewPager.getCurrentItem());
                            in.putExtras(bun);

                            startActivity(in);

                            Toast.makeText(getApplicationContext(), "Selected Employee " + nameList.get(i).getName(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

    }

    private void reportsUserPicker() {
        View view = getLayoutInflater().inflate(R.layout.hc_module_checklist_user_picker_layout, null, false);
        Button selectClient = (Button) view.findViewById(R.id.selectClient);
        Button selectEmployee = (Button) view.findViewById(R.id.selectEmployee);
//        final LinearLayout customLocationBox = (LinearLayout) view.findViewById(R.id.customLocationBox);

        final AlertDialog alertDialog = new AlertDialog.Builder(ManagerHCDashboardActivity.this).setView(view).create();
        alertDialog.show();

        selectClient.setOnClickListener(new View.OnClickListener() {
            double longitude = 0, latitude = 0;

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent checklistIntent = new Intent(ManagerHCDashboardActivity.this, HcModuleShowClientReportActivity.class);

                Bundle bun = new Bundle();
                bun.putString("ReportType", "Client Reports");
//                bun.putString("EmployeeID",);
                bun.putInt("Index", mViewPager.getCurrentItem());

                checklistIntent.putExtras(bun);
                // Toast.makeText(getApplicationContext(), "Client Reports", Toast.LENGTH_LONG).show();
                startActivity(checklistIntent);
            }
        });

        selectEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //customLocationBox.setVisibility(customLocationBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent checklistIntent = new Intent(ManagerHCDashboardActivity.this, RKSelectEmployeeActivity.class);

                Bundle bun = new Bundle();
                bun.putString("ReportType", "Employee Reports");
                bun.putInt("Index", mViewPager.getCurrentItem());

                checklistIntent.putExtras(bun);
                //  Toast.makeText(getApplicationContext(), "Employee Reports", Toast.LENGTH_LONG).show();
                startActivity(checklistIntent);
            }
        });
    }


    //get Notification Status
    private void getNotificationStatus(final int i) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (manager.getManagerLocationList() != null) {
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("location_id", manager.getManagerLocationList().get(i).getId());
                        try {
                            return new WebUtils().post(AppConstants.URL_GET_NOTIFICATION_STATUS, hm);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    try {
                        JSONObject jsnObj = new JSONObject(result);
                        JSONArray jsnArry = jsnObj.getJSONArray("data");
                        if (jsnArry.length() > 0) {
                            JSONObject obj = jsnArry.getJSONObject(0).getJSONObject("locations");
                            Boolean status = obj.getBoolean("notifications");
                            notificationStatus = status;
                            onOffNotificationImageView.setImageResource(status ? R.drawable.on : R.drawable.off);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }

    // update notification status
    private void updateNotificationStatus(final String st, final int i) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", manager.getManagerLocationList().get(i).getId());
                hm.put("status", st);
                try {
                    return new WebUtils().post(AppConstants.URL_UPDATE_NOTIFICATION_STATUS, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    try {
                        JSONObject jsnObj = new JSONObject(result);
                        if (jsnObj != null) {
                            String msg = jsnObj.getString("Message");
                            makeToast(msg);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }


    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdate();
    }

    protected void startLocationUpdate() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        AppGlobals.mCurrentLocation = location;
        mLocationUpdate = DateFormat.getTimeInstance().format(new Date());
        Log.e("mCurrentLocation", AppGlobals.mCurrentLocation.getLatitude() + "<<?>>" + AppGlobals.mCurrentLocation.getLongitude());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


//    @Override
//    public void viewNavigationNotifications() {
//
//    }


    private void employeePickerScheduleDot(final ArrayList<EmployeeStatus> empStatusArr) {

        if (manager.getManagerLocationList() != null) {
//            final List<HCEmployee> nameList = manager.getManagerLocationList().get(mViewPager.getCurrentItem()).getEmployeeList();
            // String names[] = {"Vicky", "Raj", "Rahul", "Ritesh","Doda","Employee 3", "Employee 4"};
//        makeToast(String.valueOf(nameList.size()));
            if (empStatusArr.size() == 0) {
                EmployeeStatus ob = new EmployeeStatus();
                ob.setEmpName("Employee not available.");
                empStatusArr.add(ob);
            } else if (empStatusArr.size() == 1) {
                if (empStatusArr.get(0).getEmpName().trim().equalsIgnoreCase("")) {
                    EmployeeStatus ob = new EmployeeStatus();
                    ob.setEmpName("Employee not available.");
                    empStatusArr.add(ob);
                }
            }
            final AlertDialog dialogBox;
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManagerHCDashboardActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.employee_picker_layout, null);

            alertDialog.setView(convertView);
            /*if (checklist)
                alertDialog.setTitle("Select employee for CheckListing");
            else
                alertDialog.setTitle("Select employee for scheduling");*/

            ListView lv = (ListView) convertView.findViewById(R.id.lv);
            final HCEmpPickerAdapterSchedule adapter = new HCEmpPickerAdapterSchedule(this, empStatusArr);
            lv.setAdapter(adapter);

            alertDialog.setCancelable(true);
            dialogBox = alertDialog.create();
            dialogBox.show();


//            http://abdevs.com/abdevs_beacon/attendance/index.php/Mobiles/get_employee_by_location

            /*lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    dialogBox.dismiss();
//                alertDialog.show().dismiss();
                    // alertDialog.dismiss();
                    if (!nameList.get(i).getName().matches("No Employee added yet.")) {
                        if (checklist) {
                            Toast.makeText(getApplicationContext(), "Emp Main == " + nameList.get(i).getEmployeeId(), Toast.LENGTH_LONG).show();

                            Intent in = new Intent(ManagerHCDashboardActivity.this, HCListOfChecklists.class);
                            Bundle bun = new Bundle();
                            bun.putString("EmployeeID", nameList.get(i).getEmployeeId());
//                        bun.putString("CheckListID", nameList.get(i).getCheckListId());
                            bun.putBoolean("Edit", false);
                            bun.putInt("Index", mViewPager.getCurrentItem());
                            in.putExtras(bun);
                            startActivity(in);

                        } else {
                            Intent in = new Intent(ManagerHCDashboardActivity.this, CreateEmployeeHCScheduleActivity.class);

                            Bundle bun = new Bundle();
                            bun.putString("SelectedUser", nameList.get(i).getName());
                            bun.putString("EmployeeID", nameList.get(i).getEmployeeId());
                            bun.putInt("Index", mViewPager.getCurrentItem());
                            in.putExtras(bun);

                            startActivity(in);

                            Toast.makeText(getApplicationContext(), "Selected Employee " + nameList.get(i).getName(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });*/
        }

    }


    private class AsyncGetActiveEmp extends AsyncTask<String, String, String> {

        String response = "";
        final ProgressDialog progressDialog = new ProgressDialog(ManagerHCDashboardActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("location_id", strings[0]));
            response = new WebServiceHandler().webServiceCall(nameValuePairs, AppConstants.URL_GET_EMP_WITH_STATUS);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            /*{
                "Message":"Success",
                    "employee_data":[
                {
                    "username":"emp22",
                        "active":"A"
                },
                {
                    "username":"emp22",
                        "active":"A"
                }
                ]
            }*/

            ArrayList<EmployeeStatus> empList = new ArrayList<EmployeeStatus>();
//            {"Message":"Success","employee_data":[{"attend":"P","username":"ka"}]}
            try {
                JSONObject objectResponse = new JSONObject(s);
                String message = objectResponse.optString("Message");
                if (message.equalsIgnoreCase("Success")) {
                    JSONArray jsonArrEmp = objectResponse.optJSONArray("employee_data");
                    for (int i = 0; i < jsonArrEmp.length(); i++) {
                        JSONObject objectEmp = jsonArrEmp.optJSONObject(i);
                        String empName = objectEmp.optString("username");
                        String empStatus = objectEmp.optString("attend");

                        if (empList.size() == 0) {
                            EmployeeStatus employeeStatus = new EmployeeStatus();
                            employeeStatus.setEmpName(empName);
                            employeeStatus.setEmpStatus(empStatus);
                            empList.add(employeeStatus);
                        } else {
                            for (int j = 0; j < empList.size(); j++) {
                                if (!empName.equalsIgnoreCase(empList.get(j).getEmpName())) {
                                    EmployeeStatus employeeStatus = new EmployeeStatus();
                                    employeeStatus.setEmpName(empName);
                                    employeeStatus.setEmpStatus(empStatus);
                                    empList.add(employeeStatus);
                                    break;
                                }
                            }
                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            employeePickerScheduleDot(empList);
        }
    }


    public class HCEmpPickerAdapterSchedule extends ArrayAdapter<EmployeeStatus> {
        Activity ctx;
        ArrayList<EmployeeStatus> empList;
        private LayoutInflater inflater;

        public HCEmpPickerAdapterSchedule(Activity context, ArrayList<EmployeeStatus> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            empList = objects;
            ctx = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.hc_emp_picker_item_schedule, null);
            TextView empName = (TextView) convertView.findViewById(R.id.emp_name_txt_schedule);
            ImageView imgEmpStatus = (ImageView) convertView.findViewById(R.id.img_schedule_dot_schedule);
            empName.setText(empList.get(position).getEmpName());

            if (empList.get(position).getEmpStatus().equalsIgnoreCase("p")) {
                imgEmpStatus.setImageResource(R.drawable.schedule_green_dot);
            } else if (empList.get(position).getEmpStatus().equalsIgnoreCase("y")) {

                imgEmpStatus.setImageResource(R.drawable.schedule_red_dot);

                if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > 0) {
                    String currentLocationId = manager.getManagerLocationList().get(locationPosition).getId();
                    for (int i = 0; i < statusArrayList.size(); i++) {
                        if (statusArrayList.get(i).getLocationId().equalsIgnoreCase(currentLocationId)) {
                            if (statusArrayList.get(i).getStatus().equalsIgnoreCase("active")) {
//                                imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                                break;
                            } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("off")) {
//                                imgScheduleDot.setImageResource(R.drawable.schedule_grey_dot);
                                imgEmpStatus.setImageResource(R.drawable.schedule_grey_dot);
                                break;
                            } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("schedule started")) {
//                                imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                                break;
                            } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("employee has arrived")) {
//                                imgScheduleDot.setImageResource(R.drawable.schedule_green_dot);
                                break;
                            } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("employee has left")) {
//                                imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                                break;
                            } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("employee not available")) {
//                                imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                                break;
                            } else {
                                imgEmpStatus.setImageResource(R.drawable.schedule_grey_dot);
                            }
                        } else {
                            imgEmpStatus.setImageResource(R.drawable.schedule_grey_dot);
                        }
                    }

                    if (statusArrayList.size() == 0){
                        imgEmpStatus.setImageResource(R.drawable.schedule_grey_dot);
                    }

                }

            }

            if (empList.get(position).getEmpName().equalsIgnoreCase("Employee not available.")) {
                imgEmpStatus.setVisibility(View.GONE);
            } else if (empList.get(position).getEmpName().trim().equalsIgnoreCase("")) {
                imgEmpStatus.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            try {

                boolean isLocIdAvailable = false;

                String status = arg1.getStringExtra("state");
                String locationId = arg1.getStringExtra("location_id");

                for (int i = 0; i < statusArrayList.size(); i++) {
                    if (statusArrayList.get(i).getLocationId().equalsIgnoreCase(locationId)) {
                        statusArrayList.remove(i);
                        ScheduleStatus scheduleStatus = new ScheduleStatus();
                        scheduleStatus.setStatus(status);
                        scheduleStatus.setLocationId(locationId);
                        statusArrayList.add(scheduleStatus);
                        isLocIdAvailable = true;
                    }
                }

                if (!isLocIdAvailable) {
                    ScheduleStatus scheduleStatus = new ScheduleStatus();
                    scheduleStatus.setStatus(status);
                    scheduleStatus.setLocationId(locationId);
                    statusArrayList.add(scheduleStatus);
                }
                setScheduleStatus(locationPosition);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private void setScheduleStatus(int position) {

        if (manager.getManagerLocationList() != null && manager.getManagerLocationList().size() > 0) {
            String currentLocationId = manager.getManagerLocationList().get(position).getId();
            for (int i = 0; i < statusArrayList.size(); i++) {
                if (statusArrayList.get(i).getLocationId().equalsIgnoreCase(currentLocationId)) {
                    if (statusArrayList.get(i).getStatus().equalsIgnoreCase("active")) {
                        imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                        break;
                    } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("off")) {
                        imgScheduleDot.setImageResource(R.drawable.schedule_grey_dot);
                        break;
                    } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("schedule started")) {
                        imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                        break;
                    } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("employee has arrived")) {
                        imgScheduleDot.setImageResource(R.drawable.schedule_green_dot);
                        break;
                    } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("employee has left")) {
                        imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                        break;
                    } else if (statusArrayList.get(i).getStatus().equalsIgnoreCase("employee not available")) {
                        imgScheduleDot.setImageResource(R.drawable.schedule_red_dot);
                        break;
                    } else {
                        imgScheduleDot.setImageResource(R.drawable.schedule_grey_dot);
                        break;
                    }
                } else {
                    imgScheduleDot.setImageResource(R.drawable.schedule_grey_dot);
                }
            }
        }

//

    }

}