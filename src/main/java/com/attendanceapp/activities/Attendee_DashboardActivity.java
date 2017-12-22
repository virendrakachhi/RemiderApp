package com.attendanceapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.Contact_us;
import com.attendanceapp.Faq;
import com.attendanceapp.OnSwipeTouchListener;
import com.attendanceapp.R;
import com.attendanceapp.TeacherSendMessageToOneClass;
import com.attendanceapp.TeacherShowClassStudentsActivity;
import com.attendanceapp.TeacherTakeAttendanceActivity;
import com.attendanceapp.TeacherTakeAttendanceCurrentLocationActivity;
import com.attendanceapp.adapters.EventHostPagerAdapter;
import com.attendanceapp.models.Event;
import com.attendanceapp.models.Eventee;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Attendee_DashboardActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = Attendee_DashboardActivity.class.getSimpleName();
    protected ImageView addClassButton, navigationButton, settingButton;
    protected TextView oneWordTextView, faqButton, contactUsButton, currentViewType, editAccountButton, addViewButton, storeButton, logoutButton, notificationStatus;
    protected LinearLayout takeAttendanceBtn, takeAttendanceCurrentLocationBtn, studentsBtn, notificationsButton, sendClassNotificationBtn, mainPage, settingPage;
    Animation textAnimation;
    protected FrameLayout navigationLayout;
    ScrollView swipePage;
    EventHostPagerAdapter eventHostPagerAdapter;
    ViewPager mViewPager;
    protected SharedPreferences sharedPreferences;
    FrameLayout navigationSwipe;

    private UserUtils userUtils;
    Eventee eventHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        swipePage = (ScrollView) findViewById(R.id.swipePage);
        addClassButton = (ImageView) findViewById(R.id.addClassButton);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        settingButton = (ImageView) findViewById(R.id.settingButton);
        oneWordTextView = (TextView) findViewById(R.id.oneWordTitle);
        notificationStatus = (TextView) findViewById(R.id.notificationStatus);

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        faqButton = (TextView) findViewById(R.id.faqButton);
        contactUsButton = (TextView) findViewById(R.id.contactUsButton);
        currentViewType = (TextView) findViewById(R.id.currentViewType);
        editAccountButton = (TextView) findViewById(R.id.editAccountButton);
        addViewButton = (TextView) findViewById(R.id.addViewButton);
        notificationsButton = (LinearLayout) findViewById(R.id.notificationsButton);
        storeButton = (TextView) findViewById(R.id.storeButton);
        logoutButton = (TextView) findViewById(R.id.logoutButton);
        navigationSwipe = (FrameLayout) findViewById(R.id.navigationSwipe);

        takeAttendanceBtn = (LinearLayout) findViewById(R.id.takeAttendanceBtn);
        takeAttendanceCurrentLocationBtn = (LinearLayout) findViewById(R.id.takeAttendanceCurrentLocationBtn);
        studentsBtn = (LinearLayout) findViewById(R.id.studentsBtn);
        sendClassNotificationBtn = (LinearLayout) findViewById(R.id.sendClassNotificationBtn);
        mainPage = (LinearLayout) findViewById(R.id.mainPage);
        settingPage = (LinearLayout) findViewById(R.id.settingPage);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(Attendee_DashboardActivity.this);
        User user = userUtils.getUserFromSharedPrefs();
        eventHost = new Eventee(user);

        setAdapter();

        addClassButton.setOnClickListener(this);
        navigationButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        faqButton.setOnClickListener(this);
        contactUsButton.setOnClickListener(this);
        currentViewType.setOnClickListener(this);
        editAccountButton.setOnClickListener(this);
        addViewButton.setOnClickListener(this);
        notificationsButton.setOnClickListener(this);
        storeButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        takeAttendanceBtn.setOnClickListener(this);
        takeAttendanceCurrentLocationBtn.setOnClickListener(this);
        studentsBtn.setOnClickListener(this);
        sendClassNotificationBtn.setOnClickListener(this);
        navigationSwipe.setOnClickListener(this);

        swipePage.setOnTouchListener(swipeTouchListener);
        takeAttendanceBtn.setOnTouchListener(swipeTouchListener);
        takeAttendanceCurrentLocationBtn.setOnTouchListener(swipeTouchListener);
        studentsBtn.setOnTouchListener(swipeTouchListener);
        sendClassNotificationBtn.setOnTouchListener(swipeTouchListener);

        navigationSwipe.setOnTouchListener(new OnSwipeTouchListener() {
            @Override
            public boolean onSwipeLeft() {
                if (navigationLayout.getVisibility() == View.VISIBLE) {
                    return false;
                }
                toggleNavigation();
                return true;
            }
        });

        showMessageIfNoClass();
        setOneWordTextView();
        notificationStatus.setText(sharedPreferences.getBoolean(AppConstants.IS_NOTIFICATIONS_ON, true) ? "On" : "Off");

    }


    private void setOneWordTextView() {
        if (eventHost.getEventList().size() > 0) {
            oneWordTextView.setText(String.valueOf(eventHost.getEventList().get(0).getName().charAt(0)).toUpperCase());
        }
    }


    private void setAdapter() {
        eventHostPagerAdapter = new EventHostPagerAdapter(getSupportFragmentManager(), eventHost.getEventList());
        mViewPager.setAdapter(eventHostPagerAdapter);

    }


    private void showMessageIfNoClass() {
        swipePage.setVisibility(eventHost.getEventList().size() == 0 ? View.GONE : View.VISIBLE);
    }


    OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
        public boolean onSwipeRight() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            changeTitle();
            return true;
        }

        public boolean onSwipeLeft() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            changeTitle();
            return true;
        }

        void changeTitle() {
            int current = mViewPager.getCurrentItem();
            oneWordTextView.setText(String.valueOf(eventHost.getEventList().get(current).getName().charAt(0)).toUpperCase());
        }
    };


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.addClassButton:
                Intent intent = new Intent(Attendee_DashboardActivity.this, EventHost_AddEventActivity.class);
//                intent.putExtra(EventHost_AddEventActivity.TEACHER_DATA, user);
                startActivity(intent);
                // finish();
                break;
            case R.id.settingButton:
                settingButton();
                break;
            case R.id.navigationButton:
                toggleNavigation();
                break;
            case R.id.faqButton:
                openActivity(Faq.class, false);
                break;
            case R.id.contactUsButton:
                openActivity(Contact_us.class, false);
                break;
            case R.id.editAccountButton:
                break;
            case R.id.addViewButton:
                break;
            case R.id.notificationsButton:
                toggleNotification();
                break;
            case R.id.storeButton:
                break;
            case R.id.logoutButton:
                userUtils.userLogout();
                break;
            case R.id.takeAttendanceBtn:
                takeAttendanceBtn();
                break;
            case R.id.takeAttendanceCurrentLocationBtn:
                takeAttendanceCurrentLocationBtn();
                break;
            case R.id.studentsBtn:
                studentsBtn();
                break;
            case R.id.sendClassNotificationBtn:
                sendClassNotificationBtn();
                break;
        }
    }


    private void toggleNotification() {
        notificationStatus.setText(userUtils.toggleNotification() ? "On" : "Off");
    }


    private void takeAttendanceBtn() {
        final List<Eventee> studentList = eventHost.getEventList().get(mViewPager.getCurrentItem()).getEventeeList();
        if (studentList.size() < 1) {
            makeToast("Please add Eventee to take attendance");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            private ProgressDialog dialog = new ProgressDialog(Attendee_DashboardActivity.this);
            Event event;

            @Override
            protected void onPreExecute() {
                event = eventHost.getEventList().get(mViewPager.getCurrentItem());

                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                HashMap<String, String> hm = new HashMap<>();
                // user_id, student_id ( more than one comma separated ), class_code
                hm.put("user_id", eventHost.getUserId());
                hm.put("class_code", event.getUniqueCode());
                hm.put("class_id", event.getId());
                hm.put("type", "ByBeacon");
                hm.put("student_id", StringUtils.getAllIdsFromStudentList(event.getEventeeList(), ','));

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
                                hm.put("user_id", eventHost.getUserId());
                                hm.put("class_id", event.getId());

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
                                if (result != null) {
                                    Intent intent = new Intent(Attendee_DashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                                    intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
                                    startActivity(intent);
                                } else {
                                    makeToast("Please check internet connection");
                                }
                            }
                        }.execute();

                    } else {
                        dialog.dismiss();
                        Intent intent = new Intent(Attendee_DashboardActivity.this, TeacherTakeAttendanceActivity.class);
                        intent.putExtra(TeacherTakeAttendanceActivity.EXTRA_TEACHER_CLASS, eventHost.getEventList().get(mViewPager.getCurrentItem()));
                        intent.putExtra(TeacherTakeAttendanceActivity.EXTRA_ATTENDANCE_DATA, result);
                        startActivity(intent);
                    }
                } else {
                    dialog.dismiss();
                    makeToast("Please check internet connection");
                }
            }
        }.execute();
    }


    private void takeAttendanceCurrentLocationBtn() {
        final List<Eventee> studentList = eventHost.getEventList().get(mViewPager.getCurrentItem()).getEventeeList();
        if (studentList.size() < 1) {
            makeToast("Please add Eventee to take attendance");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            private ProgressDialog dialog = new ProgressDialog(Attendee_DashboardActivity.this);
            Event event;

            @Override
            protected void onPreExecute() {
                event = eventHost.getEventList().get(mViewPager.getCurrentItem());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                HashMap<String, String> hm = new HashMap<>();
                // user_id, student_id ( more than one comma separated ), class_code
                hm.put("user_id", eventHost.getUserId());
                hm.put("class_code", event.getUniqueCode());
                hm.put("class_id", event.getId());
                hm.put("type", "Automatic");
                hm.put("student_id", StringUtils.getAllIdsFromStudentList(studentList, ','));

                try {
                    result = new WebUtils().post(AppConstants.URL_TAKE_ATTENDANCE_CURRENT_LOCATION, hm);
                } catch (IOException e) {
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
                                hm.put("user_id", eventHost.getUserId());
                                hm.put("class_id", event.getId());

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
                                if (result != null) {
                                    Intent intent = new Intent(Attendee_DashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                                    intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
                                    startActivity(intent);
                                } else {
                                    makeToast("Please check internet connection");
                                }
                            }
                        }.execute();

                    } else {
                        dialog.dismiss();
                        Intent intent = new Intent(Attendee_DashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                        intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
                        intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
                        startActivity(intent);
                    }
                } else {
                    dialog.dismiss();
                    makeToast("Please check internet connection");
                }
            }
        }.execute();

    }


    private void sendClassNotificationBtn() {
        Intent intent = new Intent(Attendee_DashboardActivity.this, TeacherSendMessageToOneClass.class);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_TEACHER_CLASS, eventHost.getEventList().get(mViewPager.getCurrentItem()));
        startActivity(intent);
    }


    private void studentsBtn() {
        Intent intent = new Intent(Attendee_DashboardActivity.this, TeacherShowClassStudentsActivity.class);
        intent.putExtra(TeacherShowClassStudentsActivity.EXTRA_STUDENT_CLASS, eventHost.getEventList().get(mViewPager.getCurrentItem()));
        startActivity(intent);
    }


    private void openActivity(Class aClass, boolean finishThis) {
        Intent intent = new Intent(Attendee_DashboardActivity.this, aClass);
        startActivity(intent);
        if (finishThis) {
            finish();
        }
    }


    private void makeToast(String title) {
        Toast.makeText(Attendee_DashboardActivity.this, title, Toast.LENGTH_LONG).show();
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


    private void toggleNavigation() {
        textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
        navigationLayout.setAnimation(textAnimation);
        navigationLayout.setVisibility(navigationLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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
    }

}