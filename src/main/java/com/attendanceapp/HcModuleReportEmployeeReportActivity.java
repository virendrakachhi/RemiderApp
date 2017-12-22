package com.attendanceapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.utils.UserUtils;

/**
 * Created by Jagdeep.singh on 1/22/2016.
 */
public class HcModuleReportEmployeeReportActivity extends AppCompatActivity implements View.OnClickListener {

    Button totalServiceToAllButton;
    String punctualityPercent;
    Button shiftMissedBtn;
    TextView totalServiceToClientButton;

    String locationID;
    ManagerHCClass managerLocData;
    UserUtils userUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_show_client_report_activity);
        int index = getIntent().getExtras().getInt("Index");
        userUtils = new UserUtils(HcModuleReportEmployeeReportActivity.this);

//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);

        locationID = managerLocData.getId();

    /*    totalServiceToAllButton = (Button) findViewById(R.id.totalServiceToAllButton);
        totalServiceToAllButton.setOnClickListener(this);*/

        shiftMissedBtn = (Button) findViewById(R.id.shiftmissedButton);
        shiftMissedBtn.setOnClickListener(this);

        totalServiceToClientButton = (TextView) findViewById(R.id.totalServiceToClientButton);
        punctualityPercent="80";

        totalServiceToClientButton.setText("Punctuality "+punctualityPercent+" %");
        totalServiceToClientButton.setOnClickListener(this);

    }

    public void onClick(View v) {

        switch(v.getId()){

            case R.id.totalServiceToClientButton:
                Intent totalhoursofserviceIntent= new Intent(HcModuleReportEmployeeReportActivity.this, HCModuleReportEmployeePunctualityActivity.class);
                totalhoursofserviceIntent.putExtra("LocationID",locationID);
                startActivity(totalhoursofserviceIntent);

                break;
            /*
            case R.id.totalServiceToAllButton:
                Intent totalhoursofserviceAllClientIntent= new Intent(HcModuleReportEmployeeReportActivity.this, HcModuleShowAllClientTotalHoursActivity.class);
                startActivity(totalhoursofserviceAllClientIntent);

                break;

                */


            case R.id.shiftmissedButton:
                Intent checklistIntent = new Intent(HcModuleReportEmployeeReportActivity.this, HCModuleReportClientShowIndvidualClientMissedshiftsActivity.class);
                startActivity(checklistIntent);

                break;


        }
    }

    private void shiftMissedPicker() {
        View view = getLayoutInflater().inflate(R.layout.hc_module_report_client_missed_type_picker_layout, null, false);
        Button selectAllClient = (Button) view.findViewById(R.id.selectAllClient);
        Button selectIndvidualClient = (Button) view.findViewById(R.id.selectIndvidualClient);
//        final LinearLayout customLocationBox = (LinearLayout) view.findViewById(R.id.customLocationBox);

        final AlertDialog alertDialog = new AlertDialog.Builder(HcModuleReportEmployeeReportActivity.this).setView(view).create();
        alertDialog.show();

        selectAllClient.setOnClickListener(new View.OnClickListener() {
            double longitude = 0, latitude = 0;

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent checklistIntent = new Intent(HcModuleReportEmployeeReportActivity.this, HCModuleReportClientAllclientShiftMissedActivity.class);
                startActivity(checklistIntent);
            }
        });

        selectIndvidualClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //customLocationBox.setVisibility(customLocationBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent checklistIntent = new Intent(HcModuleReportEmployeeReportActivity.this, HCModuleReportClientShowIndvidualClientMissedshiftsActivity.class);
                startActivity(checklistIntent);
            }
        });
    }

    public void gotoBack(View view) {
        super.onBackPressed();
    }

}
