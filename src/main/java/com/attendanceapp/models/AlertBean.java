package com.attendanceapp.models;

import android.widget.TextView;

/**
 * Created by user on 12/5/16.
 */
public class AlertBean {

    private TextView UserName;

    public TextView getUserName() {
        return UserName;
    }

    public void setUserName(TextView userName) {
        UserName = userName;
    }

    public TextView getTime() {
        return time;
    }

    public void setTime(TextView time) {
        this.time = time;
    }

    private TextView time;

   }
