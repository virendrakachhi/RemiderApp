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
public class HCModuleReportClientShowIndvidualClientMissedshiftsActivity extends AppCompatActivity implements View.OnClickListener {
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



        downloadButton=(Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this);
        totalhoursList = (ListView) findViewById(R.id.totalhoursList);


        // Setting Inittial Colors
        scheduleArrayList.clear();
        HCModuleDayWeekMonthYearViewBean obj = new HCModuleDayWeekMonthYearViewBean();
        for (int i = 0; i < 7; i++) {

            obj.heading = "Clients " + i;
            obj.date = "Day " + i;
            obj.scheduleDetails = "10:30 am to 6:00 pm ";
            scheduleArrayList.add(obj);
        }

        setAdapter();


    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.downloadButton:
                Toast.makeText(HCModuleReportClientShowIndvidualClientMissedshiftsActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();

                break;


        }

    }

    private void setAdapter() {

        listAdapter = new HCModuleDayWeekMonthYearListAdapter(HCModuleReportClientShowIndvidualClientMissedshiftsActivity.this, scheduleArrayList);
        totalhoursList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }
}







/*
package com.attendanceapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.attendanceapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class HCModuleReportClientShowIndvidualClientMissedshiftsActivity extends Activity {

    private Spinner employeeSpinner;
    private Button showReportData ;
    private ListView list;

    List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_report_show_shifts_missed_indvidualclient_activity);

        employeeSpinner = (Spinner) findViewById(R.id.employeeSpinner);

        showReportData= (Button) findViewById(R.id.showReportData);
        list = (ListView) findViewById(R.id.list);

        employeeSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);


    }

    public void showReport(View v) {
        int selectedItem = employeeSpinner.getSelectedItemPosition();
    }

    final AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            if (position == 0 || position == 4 || position == 5 || position == 6) {
//                thirtyDaysLayout.setVisibility(View.VISIBLE);
//            }
//            if (position == 4) {
//                sevenDaysLayout.setVisibility(View.VISIBLE);
//            }
//
//            switch (position) {
//                case 0:
//                    repeatTypeTextView.setText("Days");
//                    break;
//                case 4:
//                    repeatTypeTextView.setText("Weeks");
//                    break;
//                case 5:
//                    repeatTypeTextView.setText("Months");
//                    break;
//                case 6:
//                    repeatTypeTextView.setText("Years");
//                    break;
//            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

}
*/
