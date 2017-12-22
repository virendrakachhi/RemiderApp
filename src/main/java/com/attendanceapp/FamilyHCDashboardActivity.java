package com.attendanceapp;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.Absent;
import com.attendanceapp.AppConstants;
import com.attendanceapp.CreateHCEmployeeActivity;
import com.attendanceapp.EmployeeHCSendMessageToOneLocation;
import com.attendanceapp.OnSwipeTouchListener;
import com.attendanceapp.R;
import com.attendanceapp.TeacherAddClassActivity;
import com.attendanceapp.TeacherSendMessageToOneClass;

import com.attendanceapp.activities.CommonAttendanceTakenActivity;
import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.activities.HealthCareEditLocationActivity;
import com.attendanceapp.activities.ReportsActivity;
import com.attendanceapp.activities.ShowClassEventCompanyUsersActivity;
import com.attendanceapp.adapters.BaseViewPagerAdapter;
import com.attendanceapp.adapters.FamilyPagerAdapter;
import com.attendanceapp.adapters.HCEmpPickerAdapter;
import com.attendanceapp.adapters.HCViewPagerAdapter;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.Family;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FamilyHCDashboardActivity extends FragmentActivity implements View.OnClickListener, NavigationPage.NavigationFunctions {

    private static final String TAG = EventHost_DashboardActivity.class.getSimpleName();
    private static final int REQUEST_EDIT_ACCOUNT = 100;
    public static String picUrl;

    protected ImageView addLoacationButton;
    private TextView oneWordTextView;
    protected LinearLayout checklistBtn, schedulingBtn, messageBtn, notificationBtn, mainPage;

    /* for settings page functionality */
    protected ImageView settingButton;
    private LinearLayout mEnployeeInfo, mMessage, mApointment, mPicture;
    protected RelativeLayout  oneWordTitleLayout;
    protected ImageView onOffNotificationImageView,navigationButton;
    public static List<HCEmployee> empList;

    private ScrollView swipePage;
    private FamilyPagerAdapter baseViewPagerAdapter;
    private ViewPager mViewPager;
    private Animation textAnimation;
    private FrameLayout navigationLayout;
    Family family;

    private UserUtils userUtils;
    protected User user;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                updateDataAsync();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_hc_dashboard);
        empList = new LinkedList<>();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        swipePage = (ScrollView) findViewById(R.id.swipePage);
        addLoacationButton = (ImageView) findViewById(R.id.addLocationButton);
        oneWordTextView = (TextView) findViewById(R.id.oneWordTitle);

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
//        checklistBtn = (LinearLayout) findViewById(R.id.checklistBtn);
        schedulingBtn = (LinearLayout) findViewById(R.id.schedulingBtn);
        notificationBtn = (LinearLayout) findViewById(R.id.notificationBtn);
        mainPage = (LinearLayout) findViewById(R.id.mainPage);
        oneWordTitleLayout = (RelativeLayout) findViewById(R.id.oneWordTitleLayout);

        /* for setting tab */
        navigationButton = (ImageView) findViewById(R.id.navigationButton);

        settingButton = (ImageView) findViewById(R.id.settingButton);
        mEnployeeInfo = (LinearLayout) findViewById(R.id.emp_infoBtn);
        mMessage = (LinearLayout) findViewById(R.id.messageBtn);
        mApointment = (LinearLayout) findViewById(R.id.apointmentBtn);
        mPicture = (LinearLayout) findViewById(R.id.pictureBtn);
        userUtils = new UserUtils(FamilyHCDashboardActivity.this);
        user = userUtils.getUserFromSharedPrefs();
//        updateDataAsync();


        family = userUtils.getUserWithDataFromSharedPrefs(Family.class);

        addLoacationButton.setOnClickListener(this);
//        checklistBtn.setOnClickListener(this);
        navigationButton.setOnClickListener(this);
        navigationButton.setOnTouchListener(swipeTouchListener);

        mEnployeeInfo.setOnClickListener(this);
        mMessage.setOnClickListener(this);
        mApointment.setOnClickListener(this);
        mPicture.setOnClickListener(this);

        mEnployeeInfo.setOnTouchListener(swipeTouchListener);
        mMessage.setOnTouchListener(swipeTouchListener);
        mApointment.setOnTouchListener(swipeTouchListener);
        mPicture.setOnTouchListener(swipeTouchListener);

        if(family!=null) {
            if(family.getManagerLocationList().size()>0) {
                empList = family.getManagerLocationList().get(0).getEmployeeList();
                baseViewPagerAdapter = new FamilyPagerAdapter(getSupportFragmentManager(), family.getManagerLocationList());
                mViewPager.setAdapter(baseViewPagerAdapter);
            }
            }
//        else
        setOneWordTextView(0);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setOneWordTextView(position);
            }
        });


/*
        userUtils = new UserUtils(ManagerHCDashboardActivity.this);
        user = userUtils.getUserFromSharedPrefs();

        baseViewPagerAdapter = new BaseViewPagerAdapter(getSupportFragmentManager(), user.getClassEventCompanyArrayList());
        mViewPager.setAdapter(baseViewPagerAdapter);

        setOneWordTextView(0);

        //noinspection deprecation
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setOneWordTextView(position);
            }
        });*/
    }

    private void setOneWordTextView(int current) {
        if(family!=null) {

            if (family.getManagerLocationList().size() > current) {
                oneWordTextView.setText(String.valueOf(family.getManagerLocationList().get(current).getLocationName().charAt(0)).toUpperCase());
            }
//            showMessageIfNoClass();
            onOffNotificationsSetImage();
        }
        else
        {
//            swipePage.setVisibility(View.GONE);
        }
    }

    private void showMessageIfNoClass() {
        swipePage.setVisibility(family.getManagerLocationList().size() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.notificationBtn:
                Intent intent = new Intent(FamilyHCDashboardActivity.this, HCManagerSendNotificationActivity.class);
                intent.putExtra(HCManagerSendNotificationActivity.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
                intent.putExtra(HCManagerSendNotificationActivity.EXTRA_HIDE_MESSAGE_BOX, true);
                startActivity(intent);
//                userPicker();
                break;
            case R.id.schedulingBtn:
                if(family!=null) {
                    if(family.getManagerLocationList().size()>0) {

                        Intent in = new Intent(FamilyHCDashboardActivity.this, HCModuleFamilyShowAppointmentActivity.class);
                        startActivity(in);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No Location Added yet!!",Toast.LENGTH_LONG).show();
                    }
                    break;
                }


            case R.id.emp_infoBtn:
                if(family!=null) {
                    if(family.getManagerLocationList().size()>0) {
                        employeePicker();

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No Location Added yet!!",Toast.LENGTH_LONG).show();
                    }
                    break;
                }

            case R.id.messageBtn:
//                Toast messageToast = Toast.makeText(FamilyHCDashboardActivity.this, "Under Construction", Toast.LENGTH_LONG);
//                messageToast.show();
                if(family!=null) {
                    if(family.getManagerLocationList().size()>0) {

                        Intent intent2 = new Intent(FamilyHCDashboardActivity.this, EmployeeHCSendMessageToOneLocation.class);
                    Bundle bun = new Bundle();
                    bun.putInt("Index", mViewPager.getCurrentItem());
                    bun.putInt("UserType", 12);
                    intent2.putExtras(bun);
                    startActivity(intent2);}
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No Location Added yet!!",Toast.LENGTH_LONG).show();

                    }
                }

                break;

            case R.id.apointmentBtn:
//                Toast apointmentToast = Toast.makeText(FamilyHCDashboardActivity.this, "Under Construction", Toast.LENGTH_LONG);
//                apointmentToast.show();
                if(family!=null) {
                    if(family.getManagerLocationList().size()>0) {

                        Intent innm = new Intent(FamilyHCDashboardActivity.this, HCModuleFamilyShowAppointmentActivity.class);
                        String locId = family.getManagerLocationList().get(mViewPager.getCurrentItem()).getLocationId();
                        Bundle bun5 = new Bundle();
                        bun5.putString("UserType","Family");
                        bun5.putString("LocationID", locId);
                        innm.putExtras(bun5);
                        startActivity(innm);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No Location Added yet!!",Toast.LENGTH_LONG).show();
                    }
                }

                break;

            case R.id.pictureBtn:
                if(family!=null) {
                    if(family.getManagerLocationList().size()>0) {

                        Intent picsIntent = new Intent(FamilyHCDashboardActivity.this, HCModuleFamily_ShowPics.class);
                        startActivity(picsIntent);

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No Location Added yet!!",Toast.LENGTH_LONG).show();
                    }
                    break;

                }
            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void absenceLayout() {

        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            ClassEventCompany teacherClass = user.getClassEventCompanyArrayList().get(mViewPager.getCurrentItem());
            String classId = teacherClass.getId();
            ProgressDialog dialog = new ProgressDialog(FamilyHCDashboardActivity.this);

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
                        Intent intent = new Intent(FamilyHCDashboardActivity.this, Absent.class);
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

    private void reportsLayout() {

        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }

        Intent intent = new Intent(FamilyHCDashboardActivity.this, ReportsActivity.class);
        intent.putExtra(ReportsActivity.EXTRA_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);

    }

    private void onOffNotifications() {
        HCFamily teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        userUtils.toggleClassNotifications(teacherClass.getId());
        onOffNotificationsSetImage();
    }

    private HCFamily getTeacherClassOnThisPage() {
        return family.getManagerLocationList().size() > mViewPager.getCurrentItem() ? family.getManagerLocationList().get(mViewPager.getCurrentItem()) : null;
    }

    private void onOffNotificationsSetImage() {
        HCFamily teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        boolean isOn = userUtils.isClassNotificationOn(teacherClass.getId());
//        onOffNotificationImageView.setImageResource(isOn ? R.drawable.on : R.drawable.off);
    }

    private void classNotificationLayout() {
//        if (!haveStudentsInClass()) {
//            makeToast("Please add students!");
//            return;
//        }
        Intent intent = new Intent(FamilyHCDashboardActivity.this, TeacherSendMessageToOneClass.class);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_HIDE_MESSAGE_BOX, true);
        startActivity(intent);
    }

    private void createAccountLayout() {
        Intent intent = new Intent(FamilyHCDashboardActivity.this, TeacherAddClassActivity.class);
        intent.putExtra(TeacherAddClassActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private void sendClassNotificationBtn() {
//        if (!haveStudentsInClass()) {
//            makeToast("Please add students!");
//            return;
//        }
        Intent intent = new Intent(FamilyHCDashboardActivity.this, EmployeeHCSendMessageToOneLocation.class);
//        intent.putExtra(UserSendMessageToOneClass.EXTRA_SELECTED_CLASS_INDEX, mViewPager.getCurrentItem());
//        intent.putExtra(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
        startActivity(intent);
    }

    private boolean haveStudentsInClass() {
        int viewPagerIndex = mViewPager.getCurrentItem();
        List<ClassEventCompany> list = user.getClassEventCompanyArrayList();
        ClassEventCompany teacherClass = list.size() > viewPagerIndex ? list.get(viewPagerIndex) : null;

        return teacherClass != null && teacherClass.getUsers().size() > 0;
    }

    private void studentsBtn() {
        Bundle bundle = new Bundle();
        bundle.putInt(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
        bundle.putInt(AppConstants.EXTRA_SELECTED_INDEX, mViewPager.getCurrentItem());

        AndroidUtils.openActivity(this, ShowClassEventCompanyUsersActivity.class, bundle, false);
    }

    private void makeToast(String title) {
        Toast.makeText(FamilyHCDashboardActivity.this, title, Toast.LENGTH_LONG).show();
    }



    private void updateDataAsync() {
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
                    if(family!=null) {
                        if (fm.getManagerLocationList().size() != newList.size()) {

                            fm.getManagerLocationList().clear();
                            fm.getManagerLocationList().addAll(newList);
                            userUtils.saveUserWithDataToSharedPrefs(fm, Family.class);
                            userUtils.saveUserToSharedPrefs(user);

                            baseViewPagerAdapter.notifyDataSetChanged();
                            setOneWordTextView(0);
                        }
                    }
                   else {
                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

                        userUtils.saveUserWithDataToSharedPrefs(fm, Family.class);
                    }

                }
            }
        }.execute();

    }

    public void takeAttendance(final String attendanceUsing) {
        final User user = userUtils.getUserFromSharedPrefs();
        final ClassEventCompany classEventCompany = user.getClassEventCompanyArrayList().get(mViewPager.getCurrentItem());
        final List<User> studentList = classEventCompany.getUsers();

        if (studentList.size() < 1) {
            makeToast("Please add students to take attendance");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            private ProgressDialog dialog = new ProgressDialog(FamilyHCDashboardActivity.this);

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
                        intent = new Intent(FamilyHCDashboardActivity.this, CommonAttendanceTakenActivity.class);
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




    private void employeePicker() {
        //
        final List<HCEmployee> nameList= empList;
        // String names[] = {"Vicky", "Raj", "Rahul", "Ritesh","Doda","Employee 3", "Employee 4"};
//        makeToast(String.valueOf(nameList.size()));
        if(nameList.size()==0)
        {
            HCEmployee ob = new HCEmployee();
            ob.setName("No Employee added yet.");
            nameList.add(ob);
        }
        final AlertDialog dialogBox;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FamilyHCDashboardActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.employee_picker_layout, null);

        alertDialog.setView(convertView);
        alertDialog.setTitle("Select employee for Information");

        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        final HCEmpPickerAdapter adapter = new HCEmpPickerAdapter(this, nameList);
        lv.setAdapter(adapter);

        alertDialog.setCancelable(true);
        dialogBox =alertDialog.create();
        dialogBox.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBox.dismiss();
//                alertDialog.show().dismiss();
                // alertDialog.dismiss();
                if (!nameList.get(i).getName().matches("No Employee added yet.")) {
                    Intent emp_infoBtn = new Intent(FamilyHCDashboardActivity.this, HCModuleFamily_EmployeeDetails.class);
                   emp_infoBtn.putExtra("Index",i);
                    startActivity(emp_infoBtn);


                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (navigationLayout.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
            navigationLayout.setAnimation(textAnimation);
            navigationLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new NavigationPage(this, userUtils.getUserFromSharedPrefs());

    }

}