package com.attendanceapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.attendanceapp.models.TeacherClass;

import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("deprecation")
public class AttendanceTakenActivity extends TabActivity {
    TabHost tabHost;
    //TextView TextcurrentDate;
    TextView tv;

    public static final String EXTRA_TEACHER_CLASS = "EXTRA_TEACHER_CLASS";

    TeacherClass teacherClass;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        teacherClass = (TeacherClass) getIntent().getSerializableExtra(EXTRA_TEACHER_CLASS);

        //TextcurrentDate = (TextView) findViewById(R.id.textView1);
        Date currentDate = (Date) Calendar.getInstance().getTime();

        // Gets the standard date formatter for the current locale of

        java.text.DateFormat dateFormat;
        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        // Formats the current date according to the locale
        String formattedCurrentDate = dateFormat.format(currentDate);
        //TextcurrentDate.setText("Attendence Taken " + formattedCurrentDate);

        // Tab Host
        tabHost = getTabHost();
        // Absent Tab
        TabSpec absentspec = tabHost.newTabSpec("Absent");
        absentspec.setIndicator("Absent");
        Intent absentsIntent = new Intent(this, Absent.class);
        absentspec.setContent(absentsIntent);
        // present tab
        TabSpec presentspec = tabHost.newTabSpec("Present");
        presentspec.setIndicator("Present");
        Intent presentIntent = new Intent(this, Present.class);
        presentspec.setContent(presentIntent);
        // Adding all TabSpec to TabHost
        tabHost.addTab(absentspec);
        tabHost.addTab(presentspec);
        tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.WHITE);
        tv = (TextView) tabHost.getTabWidget().getChildAt(0)
                .findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#187ebe"));
        // tv.setAllCaps(false);

        tabHost.getTabWidget().getChildAt(1)
                .setBackgroundColor(Color.parseColor("#187ebe"));
        tv = (TextView) tabHost.getTabWidget().getChildAt(1)
                .findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        // tv.setAllCaps(false);

        tabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String arg0) {
                setTabColor(tabHost);

            }
        });

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("NewApi")
    public static void setTabColor(TabHost tabhost) {

        for (int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
            tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#187ebe")); // unselected
            TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            // tv.setAllCaps(false);

        }
        tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.parseColor("#FFFFFF"));
        // selected
        TextView tv = (TextView) tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#187ebe"));
        // tv.setAllCaps(false);

    }
    public void gotoBack(View view) {
        onBackPressed();
    }

}
