package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;

@SuppressLint("InflateParams")
@SuppressWarnings("unused")
public class HCModuleManageAccountsActivity extends Activity implements View.OnClickListener {

    Button familyInfoButton, employeesInfoButton;
    String locationID, empID;
    int index;
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
        setContentView(R.layout.activity_hc_manage_accounts);

        familyInfoButton = (Button) findViewById(R.id.family_information);
        familyInfoButton.setOnClickListener(this);
        index = getIntent().getExtras().getInt("Index");
//        empID = getIntent().getExtras().getString("EmployeeID");

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();
        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);


        employeesInfoButton = (Button) findViewById(R.id.employee_information);
        employeesInfoButton.setOnClickListener(this);
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {


        switch (view.getId()) {

            case R.id.family_information:
                Intent in = new Intent(HCModuleManageAccountsActivity.this, CreateHCEmployeeActivity.class);
                Bundle bun = new Bundle();
                bun.putInt("Index", index);
                bun.putString("UserType", "Family");
                in.putExtras(bun);
                startActivity(in);
                finish();
                break;


            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;


            case R.id.employee_information:
//                Toast.makeText(getApplicationContext(),"Emp == "+empID,Toast.LENGTH_LONG).show();

                Intent empInfoIntent = new Intent(HCModuleManageAccountsActivity.this, HCEmployeeListActivity.class);
                Bundle bun1 = new Bundle();
                bun1.putInt("Index", index);
//                bun1.putString("EmployeeID",empID);
                empInfoIntent.putExtras(bun1);

                startActivity(empInfoIntent);
                finish();
                break;
        }
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
        } else {
            super.onBackPressed();
        }
    }


}

