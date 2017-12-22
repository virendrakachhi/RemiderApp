package com.attendanceapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.adapters.HCModuleDayWeekMonthYearListAdapter;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagdeep.singh on 1/22/2016.
 */
public class HcModuleShowAllClientTotalHoursActivity extends AppCompatActivity implements View.OnClickListener {
    ListView totalhoursList;
    LinearLayout timeSelectorHeader;
    Context context;
    Button downloadButton;

    List<HCModuleDayWeekMonthYearViewBean> scheduleArrayList = new ArrayList<>();

    HCModuleDayWeekMonthYearListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_show_client_total_hours_activity);

        // list_item_hc_module_day_week_month_year
        // totalhoursList

        context = getApplicationContext();

        timeSelectorHeader=(LinearLayout) findViewById(R.id.timeSelectorHeader);
        timeSelectorHeader.setVisibility(View.GONE);


        totalhoursList = (ListView) findViewById(R.id.totalhoursList);

        downloadButton=(Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this);

        // Setting Inittial Colors
        scheduleArrayList.clear();
        HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();
        for (int i = 0; i < 7; i++) {
            obj.heading = "Client " + i;
            obj.date = "Day " + i;
            obj.scheduleDetails = 3 + i * 2 + "";
            scheduleArrayList.add(obj);
        }

        setAdapter();


    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {

            case R.id.downloadButton:
                Toast.makeText(HcModuleShowAllClientTotalHoursActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();

                break;


        }
    }

    private void setAdapter() {

        listAdapter = new HCModuleDayWeekMonthYearListAdapter(HcModuleShowAllClientTotalHoursActivity.this, scheduleArrayList);
        totalhoursList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }
}