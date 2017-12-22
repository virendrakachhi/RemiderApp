package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import android.widget.FrameLayout;

import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.utils.UserUtils;

/**
 * Created by Jagdeep.singh on 1/22/2016.
 */
public class HcModuleShowClientReportActivity extends Activity implements View.OnClickListener {
    private static final String TAG = HcModuleShowClientReportActivity.class.getSimpleName();

    Button totalServiceToAllButton;
    Button shiftMissedBtn;
    TextView totalServiceToClientButton;

    String locationID;
    ManagerHCClass managerLocData;

    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_show_client_report_activity);
        int index = getIntent().getExtras().getInt("Index");

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();

        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);

        locationID = managerLocData.getId();

        totalServiceToAllButton = (Button) findViewById(R.id.totalServiceToAllButton);
        totalServiceToAllButton.setOnClickListener(this);

        shiftMissedBtn = (Button) findViewById(R.id.shiftmissedButton);
        shiftMissedBtn.setOnClickListener(this);

        totalServiceToClientButton = (TextView) findViewById(R.id.totalServiceToClientButton);
        totalServiceToClientButton.setOnClickListener(this);

    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.totalServiceToClientButton:
                Intent totalhoursofserviceIntent = new Intent(HcModuleShowClientReportActivity.this, HcModuleShowClientTotalHoursActivity.class);
                totalhoursofserviceIntent.putExtra("LocationID", locationID);
                startActivity(totalhoursofserviceIntent);

                break;

            case R.id.totalServiceToAllButton:
                Intent totalhoursofserviceAllClientIntent = new Intent(HcModuleShowClientReportActivity.this, HcModuleShowAllClientTotalHoursActivity.class);
                startActivity(totalhoursofserviceAllClientIntent);

                break;

            case R.id.shiftmissedButton:
//                shiftMissedPicker();
                if (managerLocData.getEmployeeList().size() > 0) {
                    Intent checklistIntent = new Intent(HcModuleShowClientReportActivity.this, HCModuleReportClientAllclientShiftMissedActivity.class);
                    checklistIntent.putExtra("LocationID", locationID);
                    checklistIntent.putExtra("EmpID", managerLocData.getManagerId());
                    checklistIntent.putExtra("UserID", managerLocData.getEmployeeList().get(managerLocData.getEmployeeList().size() - 1).getEmployeeId());

                    startActivity(checklistIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "No Shifts Missed", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;

        }
    }

    private void shiftMissedPicker() {
        View view = getLayoutInflater().inflate(R.layout.hc_module_report_client_missed_type_picker_layout, null, false);
        Button selectAllClient = (Button) view.findViewById(R.id.selectAllClient);
        Button selectIndvidualClient = (Button) view.findViewById(R.id.selectIndvidualClient);
//        final LinearLayout customLocationBox = (LinearLayout) view.findViewById(R.id.customLocationBox);

        final AlertDialog alertDialog = new AlertDialog.Builder(HcModuleShowClientReportActivity.this).setView(view).create();
        alertDialog.show();

        selectAllClient.setOnClickListener(new View.OnClickListener() {
            double longitude = 0, latitude = 0;

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent checklistIntent = new Intent(HcModuleShowClientReportActivity.this, HCModuleReportClientAllclientShiftMissedActivity.class);
                startActivity(checklistIntent);
            }
        });

        selectIndvidualClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //customLocationBox.setVisibility(customLocationBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent checklistIntent = new Intent(HcModuleShowClientReportActivity.this, HCModuleReportClientShowIndvidualClientMissedshiftsActivity.class);
                startActivity(checklistIntent);
            }
        });
    }


    public void gotoBack(View view) {
        super.onBackPressed();
    }

    // in OnResume
    @Override
    protected void onResume() {
        super.onResume();

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());

    }

// On Back Pressed


    @Override
    public void onBackPressed() {
        if (navigationLayout.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
            navigationLayout.setAnimation(textAnimation);
            navigationLayout.setVisibility(View.GONE);
        }
        else
        {
            super.onBackPressed();
        }
    }



}
