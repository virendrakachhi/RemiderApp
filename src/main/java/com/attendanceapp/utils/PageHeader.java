package com.attendanceapp.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.attendanceapp.R;

public class PageHeader {

    private Activity activity;
    private ImageView leftButton, rightButton;
    private TextView title;

    public enum LeftButtonOption {BACK, ADD}

    public PageHeader(Activity activity, boolean showLeftButton, boolean showRightButton, LeftButtonOption leftButtonOption) {
        this.activity = activity;

        leftButton = (ImageView) activity.findViewById(R.id.leftButton);
        rightButton = (ImageView) activity.findViewById(R.id.rightButton);
        title = (TextView) activity.findViewById(R.id.title);

        if (!showLeftButton) {
            leftButton.setVisibility(View.GONE);
            title.setPadding(40, 0, 0, 0);
        }
        if (!showRightButton) {
            rightButton.setVisibility(View.GONE);
            title.setPadding(0, 0, 40, 0);
        }
        if (leftButtonOption != null && leftButtonOption == LeftButtonOption.BACK) {
            leftButton.setImageResource(R.drawable.backtolist);
        }
    }


    public ImageView getLeftButton() {
        return leftButton;
    }

    public ImageView getRightButton() {
        return rightButton;
    }

    public TextView getTitle() {
        return title;
    }

}
