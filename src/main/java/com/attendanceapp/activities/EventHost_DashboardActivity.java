package com.attendanceapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.Absent;
import com.attendanceapp.AppConstants;
import com.attendanceapp.OnSwipeTouchListener;
import com.attendanceapp.R;
import com.attendanceapp.TeacherAddClassActivity;
import com.attendanceapp.TeacherSendMessageToOneClass;
import com.attendanceapp.TeacherShowClassStudentsActivity;
import com.attendanceapp.TeacherTakeAttendanceCurrentLocationActivity;
import com.attendanceapp.adapters.BaseViewPagerAdapter;
import com.attendanceapp.models.ClassEventCompany;
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
import java.util.HashMap;
import java.util.List;

public class EventHost_DashboardActivity extends FragmentActivity implements View.OnClickListener, NavigationPage.NavigationFunctions {

    private static final String TAG = EventHost_DashboardActivity.class.getSimpleName();
    private static final int REQUEST_EDIT_ACCOUNT = 100;


    protected ImageView addClassButton;
    private TextView oneWordTextView;
    protected LinearLayout takeAttendanceBtn, takeAttendanceCurrentLocationBtn, studentsBtn, sendClassNotificationBtn, mainPage;

    /* for settings page functionality */
    protected ImageView settingButton;
    protected LinearLayout classInformationLayout, absenceLayout, reportsLayout, onOffNotifications, settingPage;
    protected RelativeLayout classNotificationLayout, oneWordTitleLayout;
    protected ImageView onOffNotificationImageView;


    private ScrollView swipePage;
    private BaseViewPagerAdapter baseViewPagerAdapter;
    private ViewPager mViewPager;
    private Animation textAnimation;
    private FrameLayout navigationLayout;

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
        setContentView(R.layout.activity_event_host_dashboard);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        swipePage = (ScrollView) findViewById(R.id.swipePage);
        addClassButton = (ImageView) findViewById(R.id.addClassButton);
        oneWordTextView = (TextView) findViewById(R.id.oneWordTitle);

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        takeAttendanceBtn = (LinearLayout) findViewById(R.id.takeAttendanceBtn);
        takeAttendanceCurrentLocationBtn = (LinearLayout) findViewById(R.id.takeAttendanceCurrentLocationBtn);
        studentsBtn = (LinearLayout) findViewById(R.id.studentsBtn);
        sendClassNotificationBtn = (LinearLayout) findViewById(R.id.sendClassNotificationBtn);
        mainPage = (LinearLayout) findViewById(R.id.mainPage);
        oneWordTitleLayout = (RelativeLayout) findViewById(R.id.oneWordTitleLayout);

        /* for setting tab */
        settingPage = (LinearLayout) findViewById(R.id.settingPage);
        settingButton = (ImageView) findViewById(R.id.settingButton);
        classInformationLayout = (LinearLayout) findViewById(R.id.classInformationLayout);
        absenceLayout = (LinearLayout) findViewById(R.id.absenceLayout);
        reportsLayout = (LinearLayout) findViewById(R.id.reportsLayout);
        onOffNotifications = (LinearLayout) findViewById(R.id.onOffNotifications);
        classNotificationLayout = (RelativeLayout) findViewById(R.id.classNotificationLayout);
        onOffNotificationImageView = (ImageView) findViewById(R.id.onOffNotificationImageView);

        addClassButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        takeAttendanceBtn.setOnClickListener(this);
        takeAttendanceCurrentLocationBtn.setOnClickListener(this);
        studentsBtn.setOnClickListener(this);
        sendClassNotificationBtn.setOnClickListener(this);

        classInformationLayout.setOnClickListener(this);
        absenceLayout.setOnClickListener(this);
        reportsLayout.setOnClickListener(this);
        onOffNotifications.setOnClickListener(this);
        classNotificationLayout.setOnClickListener(this);

        oneWordTitleLayout.setOnTouchListener(swipeTouchListener);
        classInformationLayout.setOnTouchListener(swipeTouchListener);
        absenceLayout.setOnTouchListener(swipeTouchListener);
        reportsLayout.setOnTouchListener(swipeTouchListener);
        onOffNotifications.setOnTouchListener(swipeTouchListener);
        classNotificationLayout.setOnTouchListener(swipeTouchListener);

        settingButton.setOnTouchListener(swipeTouchListener);
        mainPage.setOnTouchListener(swipeTouchListener);
        settingPage.setOnTouchListener(swipeTouchListener);
        swipePage.setOnTouchListener(swipeTouchListener);
        takeAttendanceBtn.setOnTouchListener(swipeTouchListener);
        takeAttendanceCurrentLocationBtn.setOnTouchListener(swipeTouchListener);
        studentsBtn.setOnTouchListener(swipeTouchListener);
        sendClassNotificationBtn.setOnTouchListener(swipeTouchListener);


        userUtils = new UserUtils(EventHost_DashboardActivity.this);
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
        });
    }

    private void setOneWordTextView(int current) {
        if (user.getClassEventCompanyArrayList().size() > current) {
            oneWordTextView.setText(String.valueOf(user.getClassEventCompanyArrayList().get(current).getName().charAt(0)).toUpperCase());
        }
        showMessageIfNoClass();
        onOffNotificationsSetImage();
    }


    private void showMessageIfNoClass() {
        swipePage.setVisibility(user.getClassEventCompanyArrayList().size() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.takeAttendanceBtn:
                takeAttendance("beacons");
                break;

            case R.id.takeAttendanceCurrentLocationBtn:
                takeAttendance("gps");
                break;

            case R.id.addClassButton:
                Bundle bundle = new Bundle();
                bundle.putInt(AppConstants.EXTRA_USER_ROLE, UserRole.EventHost.getRole());
                AndroidUtils.openActivity(this, CreateClassEventCompanyActivity.class, bundle, false);
                break;

            case R.id.settingButton:
                settingButton();
                break;

            case R.id.studentsBtn:
                studentsBtn();
                break;

            case R.id.sendClassNotificationBtn:
                sendClassNotificationBtn();
                break;

            case R.id.classInformationLayout:
                classInformationLayout();
                break;

            case R.id.absenceLayout:
                absenceLayout();
                break;

            case R.id.reportsLayout:
                reportsLayout();
                break;

            case R.id.onOffNotifications:
                onOffNotifications();
                break;

            case R.id.classNotificationLayout:
                classNotificationLayout();
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
            ProgressDialog dialog = new ProgressDialog(EventHost_DashboardActivity.this);

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
                        Intent intent = new Intent(EventHost_DashboardActivity.this, Absent.class);
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

        Intent intent = new Intent(EventHost_DashboardActivity.this, ReportsActivity.class);
        intent.putExtra(ReportsActivity.EXTRA_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);

    }

    private void onOffNotifications() {
        ClassEventCompany teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        userUtils.toggleClassNotifications(teacherClass.getUniqueCode());
        onOffNotificationsSetImage();
    }

    private ClassEventCompany getTeacherClassOnThisPage() {
        return user.getClassEventCompanyArrayList().size() > mViewPager.getCurrentItem() ? user.getClassEventCompanyArrayList().get(mViewPager.getCurrentItem()) : null;
    }

    private void onOffNotificationsSetImage() {
        ClassEventCompany teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        boolean isOn = userUtils.isClassNotificationOn(teacherClass.getUniqueCode());
        onOffNotificationImageView.setImageResource(isOn ? R.drawable.on : R.drawable.off);
    }

    private void classNotificationLayout() {
        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }
        Intent intent = new Intent(EventHost_DashboardActivity.this, TeacherSendMessageToOneClass.class);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_HIDE_MESSAGE_BOX, true);
        startActivity(intent);
    }

    private void classInformationLayout() {
        Intent intent = new Intent(EventHost_DashboardActivity.this, TeacherAddClassActivity.class);
        intent.putExtra(TeacherAddClassActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private void sendClassNotificationBtn() {
        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }
        Intent intent = new Intent(EventHost_DashboardActivity.this, TeacherSendMessageToOneClass.class);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private boolean haveStudentsInClass() {
        int viewPagerIndex = mViewPager.getCurrentItem();
        List<ClassEventCompany> list = user.getClassEventCompanyArrayList();
        ClassEventCompany teacherClass = list.size() > viewPagerIndex ? list.get(viewPagerIndex) : null;

        return teacherClass != null && teacherClass.getUsers().size() > 0;
    }

    private void studentsBtn() {
        Intent intent = new Intent(EventHost_DashboardActivity.this, TeacherShowClassStudentsActivity.class);
        intent.putExtra(TeacherShowClassStudentsActivity.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private void makeToast(String title) {
        Toast.makeText(EventHost_DashboardActivity.this, title, Toast.LENGTH_LONG).show();
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
        super.onResume();
        User user1 = userUtils.getUserWithDataFromSharedPrefs(User.class);

        if (user1 != null) {
            List<ClassEventCompany> mainList = user.getClassEventCompanyArrayList();
            List<ClassEventCompany> newList = user1.getClassEventCompanyArrayList();

            if (mainList != null && newList != null && mainList.size() != newList.size()) {
                user.getClassEventCompanyArrayList().clear();
                user.getClassEventCompanyArrayList().addAll(user1.getClassEventCompanyArrayList());
                baseViewPagerAdapter.notifyDataSetChanged();
                setOneWordTextView(0);
            }
        }

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
        updateDataAsync();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.EventHost.getRole()));
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

                    List<ClassEventCompany> newList = DataUtils.getClassEventCompanyArrayListFromJsonString(result);

                    if (user.getClassEventCompanyArrayList().size() != newList.size()) {

                        user.getClassEventCompanyArrayList().clear();
                        user.getClassEventCompanyArrayList().addAll(newList);

                        userUtils.saveUserWithDataToSharedPrefs(user, User.class);

                        baseViewPagerAdapter.notifyDataSetChanged();
                        setOneWordTextView(0);
                    }
                }
            }
        }.execute();

    }

    public void takeAttendance(final String attendanceUsing) {
        User user = userUtils.getUserWithDataFromSharedPrefs(User.class);
        final ClassEventCompany classEventCompany = user.getClassEventCompanyArrayList().get(mViewPager.getCurrentItem());
        final List<User> studentList = classEventCompany.getUsers();

        if (studentList.size() < 1) {
            makeToast("Please add students to take attendance");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            private ProgressDialog dialog = new ProgressDialog(EventHost_DashboardActivity.this);

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                HashMap<String, String> hm = new HashMap<>();
                // user_id, student_id ( more than one comma separated ), class_code
                hm.put("user_id", EventHost_DashboardActivity.this.user.getUserId());
                hm.put("class_code", classEventCompany.getUniqueCode());
                hm.put("class_id", classEventCompany.getId());
                hm.put("student_id", StringUtils.getAllIdsFromStudentList(studentList, ','));

                if ("beacons".equalsIgnoreCase(attendanceUsing)) {
                    hm.put("type", "ByBeacon");
                } else if ("gps".equalsIgnoreCase(attendanceUsing)) {
                    hm.put("type", "Automatic");
                }

                try {
                    result = new WebUtils().post(AppConstants.URL_TAKE_ATTENDANCE_CURRENT_LOCATION, hm);
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    if (result.contains("Error")) {
                        new AsyncTask<Void, Void, String>() {

                            @Override
                            protected String doInBackground(Void... params) {
                                String result = null;
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("user_id", EventHost_DashboardActivity.this.user.getUserId());
                                hm.put("class_id", classEventCompany.getId());

                                try {
                                    result = new WebUtils().post(AppConstants.URL_SHOW_ATTENDANCE_CURRENT_LOCATION, hm);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return result;
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                dialog.dismiss();
                                dialog.cancel();
                                if (result != null) {
                                    Intent intent = new Intent(EventHost_DashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                                    intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
                                    startActivity(intent);
                                } else {
                                    makeToast("Please check internet connection");
                                }
                            }
                        }.execute();

                    } else {
                        dialog.dismiss();
                        dialog.cancel();
                        Intent intent;
                        intent = new Intent(EventHost_DashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                        intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
                        intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
//                        }
                        startActivity(intent);
                    }
                } else {
                    dialog.dismiss();
                    dialog.cancel();
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


//    @Override
//    public void viewNavigationNotifications() {
//
//    }
}