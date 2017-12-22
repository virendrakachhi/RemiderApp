package com.attendanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
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

import com.attendanceapp.activities.ReportsActivity;
import com.attendanceapp.adapters.TeacherPagerAdapter;
import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.TeacherClass;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherDashboardActivity extends FragmentActivity implements View.OnClickListener, NavigationPage.NavigationFunctions {

    private static final String TAG = TeacherDashboardActivity.class.getSimpleName();
    private static final int REQUEST_EDIT_ACCOUNT = 100;

    GPSTracker gpsTracker;
    protected ImageView addClassButton;
    private TextView oneWordTextView;
    protected LinearLayout takeAttendanceBtn, takeAttendanceCurrentLocationBtn, studentsBtn, sendClassNotificationBtn, mainPage;

    /* for settings page functionality */
    protected ImageView settingButton;
    protected LinearLayout classInformationLayout, absenceLayout, reportsLayout, onOffNotifications, settingPage;
    protected RelativeLayout classNotificationLayout;
    protected ImageView onOffNotificationImageView;
    protected TextView totalNotificationsTextView;
    //---------------------------------------------


    private ScrollView swipePage;
    private TeacherPagerAdapter mTeacherPagerAdapter;
    private ViewPager mViewPager;
    private Animation textAnimation;
    private FrameLayout navigationLayout;

    private UserUtils userUtils;
    protected Teacher teacher;
    protected SharedPreferences sharedPreferences;

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
        setContentView(R.layout.activity_teacher_dashboard);

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
        settingPage = (LinearLayout) findViewById(R.id.settingPage);

        /* for setting tab */
        settingButton = (ImageView) findViewById(R.id.settingButton);
        classInformationLayout = (LinearLayout) findViewById(R.id.classInformationLayout);
        absenceLayout = (LinearLayout) findViewById(R.id.absenceLayout);
        reportsLayout = (LinearLayout) findViewById(R.id.reportsLayout);
        onOffNotifications = (LinearLayout) findViewById(R.id.onOffNotifications);
        classNotificationLayout = (RelativeLayout) findViewById(R.id.classNotificationLayout);
        onOffNotificationImageView = (ImageView) findViewById(R.id.onOffNotificationImageView);
        totalNotificationsTextView = (TextView) findViewById(R.id.totalNotificationsTextView);

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

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(this);
        userUtils = new UserUtils(TeacherDashboardActivity.this);
        teacher = new Teacher(userUtils.getUserFromSharedPrefs());

        mTeacherPagerAdapter = new TeacherPagerAdapter(getSupportFragmentManager(), teacher.getTeacherClassList());
        mViewPager.setAdapter(mTeacherPagerAdapter);
        setOneWordTextView(0);
        setNotificationBadge(0);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setOneWordTextView(position);
            }
        });
        gpsTracker = new GPSTracker(getApplicationContext());
    }

    private void setOneWordTextView(int current) {
        oneWordTextView.setText("");

        if (teacher.getTeacherClassList().size() > current) {
            oneWordTextView.setText(String.valueOf(teacher.getTeacherClassList().get(current).getClassName().charAt(0)).toUpperCase());
        }
        showMessageIfNoClass();
        onOffNotificationsSetImage();
    }

    private void showMessageIfNoClass() {
        swipePage.setVisibility(teacher.getTeacherClassList().size() == 0 ? View.GONE : View.VISIBLE);
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
                AndroidUtils.openActivity(this, TeacherAddClassActivity.class, false);
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
            TeacherClass teacherClass = teacher.getTeacherClassList().get(mViewPager.getCurrentItem());
            String classId = teacherClass.getId();
            ProgressDialog dialog = new ProgressDialog(TeacherDashboardActivity.this);

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("user_id", teacher.getUserId());
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
                if(dialog!=null) {
                    dialog.dismiss();
                }
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);

                        if (object.has("Error")) {
                            makeToast(object.getString("Error"));
                            return;
                        }
                        Intent intent = new Intent(TeacherDashboardActivity.this, Absent.class);
                        intent.putExtra(Absent.EXTRA_ATTENDANCE_DATA, result);
                        intent.putExtra(Absent.EXTRA_TITLE, teacherClass.getClassName());
                        intent.putExtra(Absent.EXTRA_CLASS_ID, classId);
                        intent.putExtra(Absent.EXTRA_LIST_OPTION, Absent.SHOW_NAME_DATE);
                        intent.putExtra(Absent.SHOW_EDIT_BUTTON, true);
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

        Intent intent = new Intent(TeacherDashboardActivity.this, ReportsActivity.class);
        intent.putExtra(ReportsActivity.EXTRA_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);

    }

    private void onOffNotifications() {
        TeacherClass teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        userUtils.toggleClassNotifications(teacherClass.getClassCode());
        onOffNotificationsSetImage();
    }

    private TeacherClass getTeacherClassOnThisPage() {
        return teacher.getTeacherClassList().size() > mViewPager.getCurrentItem() ? teacher.getTeacherClassList().get(mViewPager.getCurrentItem()) : null;
    }

    private void onOffNotificationsSetImage() {
        TeacherClass teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        boolean isOn = userUtils.isClassNotificationOn(teacherClass.getClassCode());
        onOffNotificationImageView.setImageResource(isOn ? R.drawable.on : R.drawable.off);
    }

    private void classNotificationLayout() {
        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }

        TeacherClass teacherClass = teacher.getTeacherClassList().get(mViewPager.getCurrentItem());
        sharedPreferences.edit().putInt(teacher.getUserId() + teacherClass.getClassCode(), 0).apply();
        setNotificationBadge(mViewPager.getCurrentItem());


        Intent intent = new Intent(TeacherDashboardActivity.this, TeacherSendMessageToOneClass.class);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_HIDE_MESSAGE_BOX, true);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_SHOW_NOTIFICATIONS, true);
        startActivity(intent);
    }


    private void setNotificationBadge(int classIndex) {
        if (classIndex >= teacher.getTeacherClassList().size()) {
            return;
        }
        final TeacherClass teacherClass = teacher.getTeacherClassList().get(classIndex);
        int total = sharedPreferences.getInt(teacher.getUserId() + teacherClass.getClassCode(), 0);

        if (total > 0) {
            totalNotificationsTextView.setVisibility(View.VISIBLE);
            totalNotificationsTextView.setText(String.valueOf(total));
        } else {
            totalNotificationsTextView.setVisibility(View.GONE);
        }

    }


    private void classInformationLayout() {
        Intent intent = new Intent(TeacherDashboardActivity.this, TeacherAddClassActivity.class);
        intent.putExtra(TeacherAddClassActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private void sendClassNotificationBtn() {
        if (!haveStudentsInClass()) {
            makeToast("Please add students!");
            return;
        }
        Intent intent = new Intent(TeacherDashboardActivity.this, TeacherSendMessageToOneClass.class);
        intent.putExtra(TeacherSendMessageToOneClass.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private boolean haveStudentsInClass() {
        int viewPagerIndex = mViewPager.getCurrentItem();
        List<TeacherClass> list = teacher.getTeacherClassList();
        TeacherClass teacherClass = list.size() > viewPagerIndex ? list.get(viewPagerIndex) : null;

        return teacherClass != null && teacherClass.getStudentList().size() > 0;
    }

    private void studentsBtn() {
        Intent intent = new Intent(TeacherDashboardActivity.this, TeacherShowClassStudentsActivity.class);
        intent.putExtra(TeacherShowClassStudentsActivity.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
    }

    private void makeToast(String title) {
        Toast.makeText(TeacherDashboardActivity.this, title, Toast.LENGTH_LONG).show();
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
        Teacher teacher1 = userUtils.getUserWithDataFromSharedPrefs(Teacher.class);

        if (teacher1 != null) {
            List<TeacherClass> teacherClasses = teacher.getTeacherClassList();
            List<TeacherClass> teacher1Classes = teacher1.getTeacherClassList();

            if (teacherClasses != null && teacher1Classes != null && teacherClasses.size() != teacher1Classes.size()) {
                teacher.getTeacherClassList().clear();
                teacher.getTeacherClassList().addAll(teacher1.getTeacherClassList());
                mTeacherPagerAdapter.notifyDataSetChanged();
                setOneWordTextView(0);
                setNotificationBadge(0);
            } else {
                setNotificationBadge(mViewPager.getCurrentItem());
            }
        }

        User user = userUtils.getUserFromSharedPrefs();
        new NavigationPage(this, user);
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

                    if (teacher.getTeacherClassList().size() != teacherClasses.size()) {

                        teacher.getTeacherClassList().clear();
                        teacher.getTeacherClassList().addAll(teacherClasses);

                        userUtils.saveUserWithDataToSharedPrefs(teacher, Teacher.class);

                        mTeacherPagerAdapter.notifyDataSetChanged();
                        setOneWordTextView(0);
                        setNotificationBadge(0);
                    }
                }
            }
        }.execute();

    }

    public void takeAttendance(final String attendanceUsing) {
        Teacher teacher1 = userUtils.getUserWithDataFromSharedPrefs(Teacher.class);
        final TeacherClass teacherClass = teacher1.getTeacherClassList().get(mViewPager.getCurrentItem());
        final List<Student> studentList = teacherClass.getStudentList();

        if (studentList.size() < 1) {
            makeToast("Please add students to take attendance");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            private ProgressDialog dialog = new ProgressDialog(TeacherDashboardActivity.this);

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
                hm.put("user_id", teacher.getUserId());
                hm.put("class_code", teacherClass.getClassCode());
                hm.put("class_id", teacherClass.getId());
                hm.put("student_id", StringUtils.getAllIdsFromStudentList(studentList, ','));

                if ("beacons".equalsIgnoreCase(attendanceUsing)) {
                    hm.put("type", "ByBeacon");
                } else if ("gps".equalsIgnoreCase(attendanceUsing)) {
                    hm.put("type", "Automatic");
                    if (gpsTracker.canGetLocation()) {
                        Location location = gpsTracker.getLocation();

                        if (location != null) {
                            final double latitude = location.getLatitude();
                            final double longitude = location.getLongitude();

                            hm.put("teach_lat",String.valueOf(latitude));
                            hm.put("teach_long", String.valueOf(longitude));

                        }
                    }
                    else{
                        gpsTracker.showSettingsAlert();
                    }
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
                                hm.put("user_id", teacher.getUserId());
                                hm.put("class_id", teacherClass.getId());

                                try {
                                    result = new WebUtils().post(AppConstants.URL_SHOW_ATTENDANCE_CURRENT_LOCATION, hm);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return result;
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                if(dialog!=null) {
                                    dialog.dismiss();
                                }
                                if (result != null) {
                                    Intent intent = new Intent(TeacherDashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                                    intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
                                    startActivity(intent);
                                } else {
                                    makeToast("Please check internet connection");
                                }
                            }
                        }.execute();

                    } else {
                        if(dialog!=null) {
                            dialog.dismiss();
                        }
                        Intent intent;
//                        if ("beacons".equalsIgnoreCase(attendanceUsing)) {
//                            intent = new Intent(TeacherDashboardActivity.this, TeacherTakeAttendanceActivity.class);
//                            intent.putExtra(TeacherTakeAttendanceActivity.EXTRA_TEACHER_CLASS, user.getTeacherClassList().get(mViewPager.getCurrentItem()));
//                            intent.putExtra(TeacherTakeAttendanceActivity.EXTRA_ATTENDANCE_DATA, result);
//                        } else if ("gps".equalsIgnoreCase(attendanceUsing)) {
                        intent = new Intent(TeacherDashboardActivity.this, TeacherTakeAttendanceCurrentLocationActivity.class);
                        intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());
                        intent.putExtra(TeacherTakeAttendanceCurrentLocationActivity.EXTRA_ATTENDANCE_DATA, result);
//                        }
                        startActivity(intent);
                    }
                } else {
                    if(dialog!=null) {
                        dialog.dismiss();
                    }
                    makeToast("Please check internet connection");
                }
            }
        }.execute();

    }

    OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
        public boolean onSwipeRight() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            setOneWordTextView(mViewPager.getCurrentItem());
            setNotificationBadge(mViewPager.getCurrentItem());
            return true;
        }

        public boolean onSwipeLeft() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            setOneWordTextView(mViewPager.getCurrentItem());
            setNotificationBadge(mViewPager.getCurrentItem());
            return true;
        }
    };

}