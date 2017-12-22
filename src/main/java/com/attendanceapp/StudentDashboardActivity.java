package com.attendanceapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Student;
import com.attendanceapp.models.StudentClass;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDashboardActivity extends FragmentActivity implements View.OnClickListener, NavigationPage.NavigationFunctions {

    private static final String TAG = StudentDashboardActivity.class.getSimpleName();

    protected ImageView addClassButton;
    protected TextView oneWordTextView, totalNotificationsTextView, addParent, addClass;
    FrameLayout addButtonLayout;
    RelativeLayout classNotificationLayout;
    ScrollView swipePage;
    LinearLayout attendanceLayout, mainPage;
    ViewPager mViewPager;
    private FrameLayout navigationLayout;

    /* for settings layout */
    ImageView settingButton;
    protected LinearLayout classInformationLayout, settingPage;

    StudentPagerAdapter mStudentPagerAdapter;
    protected SharedPreferences sharedPreferences;
    Animation textAnimation;

    Student student;

    protected UserUtils userUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        addClassButton = (ImageView) findViewById(R.id.addClassButton);
        oneWordTextView = (TextView) findViewById(R.id.oneWordTitle);
        attendanceLayout = (LinearLayout) findViewById(R.id.attendanceLayout);
        classNotificationLayout = (RelativeLayout) findViewById(R.id.classNotificationLayout);
        addButtonLayout = (FrameLayout) findViewById(R.id.addLayout);
        totalNotificationsTextView = (TextView) findViewById(R.id.totalNotificationsTextView);
        addParent = (TextView) findViewById(R.id.addParent);
        addClass = (TextView) findViewById(R.id.addClass);
        swipePage = (ScrollView) findViewById(R.id.swipePage);
        mainPage = (LinearLayout) findViewById(R.id.mainPage);

        /* for setting tab */
        settingButton = (ImageView) findViewById(R.id.settingButton);
        classInformationLayout = (LinearLayout) findViewById(R.id.classInformationLayout);
        settingButton.setOnClickListener(this);
        settingPage = (LinearLayout) findViewById(R.id.settingPage);
        settingPage.setOnTouchListener(swipeTouchListener);
        classInformationLayout.setOnClickListener(this);
        classInformationLayout.setOnTouchListener(swipeTouchListener);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(StudentDashboardActivity.this);
        User user = userUtils.getUserFromSharedPrefs();
        student = new Student(user);

        addClassButton.setOnClickListener(this);
        addParent.setOnClickListener(this);
        addClass.setOnClickListener(this);
        classNotificationLayout.setOnClickListener(this);
        attendanceLayout.setOnClickListener(this);

        mainPage.setOnTouchListener(swipeTouchListener);
        settingButton.setOnTouchListener(swipeTouchListener);
        swipePage.setOnTouchListener(swipeTouchListener);
        attendanceLayout.setOnTouchListener(swipeTouchListener);
        classNotificationLayout.setOnTouchListener(swipeTouchListener);

        setNotificationBadge(0);
        setAdapter();
        setOneWordTextView(0);


        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setOneWordTextView(position);
            }
        });

    }

    private void setAdapter() {
        mStudentPagerAdapter = new StudentPagerAdapter(getSupportFragmentManager(), student.getStudentClassList());
        mViewPager.setAdapter(mStudentPagerAdapter);
    }


    private void setOneWordTextView(int current) {
        oneWordTextView.setText("");

        if (student.getStudentClassList().size() > current) {
            oneWordTextView.setText(String.valueOf(student.getStudentClassList().get(current).getClassName().charAt(0)).toUpperCase());
        }

        showMessageIfNoClass();
    }

    private void showMessageIfNoClass() {
        swipePage.setVisibility(student.getStudentClassList().size() < 1 ? View.GONE : View.VISIBLE);
    }

    private void setNotificationBadge(int classIndex) {
        if (classIndex >= student.getStudentClassList().size()) {
            return;
        }
        final StudentClass studentClass = student.getStudentClassList().get(classIndex);

        int total = sharedPreferences.getInt(student.getUserId() + studentClass.getClassUniqueCode(), 0);
        if (total > 0) {
            totalNotificationsTextView.setVisibility(View.VISIBLE);
            totalNotificationsTextView.setText(String.valueOf(total));
        } else {
            totalNotificationsTextView.setVisibility(View.GONE);
        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.addClassButton:
                addButtonLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.attendanceLayout:
                attendanceLayout();
                break;
            case R.id.classNotificationLayout:
                classNotificationLayout();
                break;
            case R.id.addParent:
                HashMap<String, String> map = new HashMap<>(1);
                map.put("user_id", student.getUserId());
                getParentCode(AppConstants.URL_ADD_PARENT_BY_STUDENT, map);
                break;
            case R.id.addClass:
                addClassButton();
                break;
            case R.id.settingButton:
                settingButton();
                break;
            case R.id.classInformationLayout:
                classInformationLayout();
                break;

        }
    }

    private void classInformationLayout() {
        Intent intent = new Intent(StudentDashboardActivity.this, StudentAddClassActivity.class);
        intent.putExtra(StudentAddClassActivity.EXTRA_STUDENT_CLASS_INDEX, mViewPager.getCurrentItem());
        startActivity(intent);
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


    private void getParentCode(final String url, final Map<String, String> params) {

        new AsyncTask<Void, Void, String>() {
            ProgressDialog alertDialog = new ProgressDialog(StudentDashboardActivity.this);


            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Void... param) {
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
                alertDialog.dismiss();
                alertDialog.cancel();

                if (s == null) {
                    makeToast("Please check your internet connection");
                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {
                            String code = jObject.getString("Code");

                            new AlertDialog.Builder(StudentDashboardActivity.this)
                                    .setTitle("Share code with parent")
                                    .setMessage(code)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onBackPressed();
                                        }
                                    })
                                    .create().show();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }


    private void makeToast(String title) {
        Toast.makeText(StudentDashboardActivity.this, title, Toast.LENGTH_LONG).show();
    }


    private void addClassButton() {
        startActivity(new Intent(StudentDashboardActivity.this, StudentAddClassActivity.class));
        finish();
    }

    private void classNotificationLayout() {
        StudentClass studentClass = student.getStudentClassList().get(mViewPager.getCurrentItem());
        sharedPreferences.edit().putInt(student.getUserId() + studentClass.getClassUniqueCode(), 0).apply();
        setNotificationBadge(mViewPager.getCurrentItem());

        Intent intent = new Intent(StudentDashboardActivity.this, StudentNotificationActivity.class);
        intent.putExtra(StudentNotificationActivity.EXTRA_STUDENT_CLASS, studentClass);
        startActivity(intent);
    }

    private void attendanceLayout() {
        Intent intent = new Intent(StudentDashboardActivity.this, StudentCheckAttendanceActivity.class);
        intent.putExtra(StudentCheckAttendanceActivity.EXTRA_STUDENT_CLASS, student.getStudentClassList().get(mViewPager.getCurrentItem()));
        intent.putExtra(StudentCheckAttendanceActivity.EXTRA_STUDENT, student);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        if (addButtonLayout.getVisibility() == View.VISIBLE) {
            addButtonLayout.setVisibility(View.GONE);

        } else if (navigationLayout.getVisibility() == View.VISIBLE) {
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
        Student student1 = new Gson().fromJson(userUtils.getUserDataFromSharedPrefs(), Student.class);

        if (student1 != null) {
            List<StudentClass> teacherClasses = student.getStudentClassList();
            List<StudentClass> teacher1Classes = student1.getStudentClassList();

            if (teacherClasses != null && teacher1Classes != null && teacherClasses.size() != teacher1Classes.size()) {
                student.getStudentClassList().clear();
                student.getStudentClassList().addAll(student1.getStudentClassList());
                mStudentPagerAdapter.notifyDataSetChanged();
                setOneWordTextView(0);
                setNotificationBadge(0);
            } else {
                setNotificationBadge(mViewPager.getCurrentItem());
            }
        }

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());

        updateDataAsync();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, Void>() {
            private String result;

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", student.getUserId());
                hm.put("role", String.valueOf(UserRole.Student.getRole()));
                try {
                    result = new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {

                    List<StudentClass> teacherClasses = DataUtils.getStudentFromJsonString(result).getStudentClassList();

                    if (student.getStudentClassList().size() != teacherClasses.size()) {

                        student.getStudentClassList().clear();
                        student.getStudentClassList().addAll(teacherClasses);

                        sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_DATA, new Gson().toJson(student, Student.class)).apply();

                        mStudentPagerAdapter.notifyDataSetChanged();
                        setOneWordTextView(0);
                        setNotificationBadge(0);
                    }

                }
            }
        }.execute();
    }

    OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
        public boolean onSwipeRight() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            setNotificationBadge(mViewPager.getCurrentItem());
            setOneWordTextView(mViewPager.getCurrentItem());
            return true;
        }

        public boolean onSwipeLeft() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            setNotificationBadge(mViewPager.getCurrentItem());
            setOneWordTextView(mViewPager.getCurrentItem());
            return true;
        }

    };

}
