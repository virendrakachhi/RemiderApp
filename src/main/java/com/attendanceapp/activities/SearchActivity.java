package com.attendanceapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.attendanceapp.R;
import com.attendanceapp.utils.PageHeader;

public class SearchActivity extends Activity implements View.OnClickListener {

    private PageHeader pageHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        pageHeader = new PageHeader(this, true, false, PageHeader.LeftButtonOption.BACK);
        pageHeader.getTitle().setText("Search");
        pageHeader.getLeftButton().setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == pageHeader.getLeftButton().getId()) {
            finish();
        }

    }
}
