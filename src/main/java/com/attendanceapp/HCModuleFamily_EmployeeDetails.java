package com.attendanceapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.attendanceapp.utils.NavigationPage;
import android.widget.FrameLayout;

public class HCModuleFamily_EmployeeDetails extends Activity implements View.OnClickListener, NavigationPage.NavigationFunctions {


     TextView  nameEditText, emailEditText, passwordEditText, companyEditText,locationTextView, answerEditText,
        titleTextView;

    //For image editing
    ImageView edit_image;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_family_employeedetails);

        nameEditText = (TextView ) findViewById(R.id.nameTextView);
        emailEditText = (TextView ) findViewById(R.id.emailTextView);
        companyEditText = (TextView ) findViewById(R.id.companyTextView);
        locationTextView = (TextView ) findViewById(R.id.locationTextView);

        titleTextView = (TextView) findViewById(R.id.textView1);
        edit_image = (ImageView) findViewById(R.id.client_image);
        int last = getIntent().getIntExtra("Index",0);

        nameEditText.setText(FamilyHCDashboardActivity.empList.get(last).getName());
        emailEditText.setText(FamilyHCDashboardActivity.empList.get(last).getEmail());
        companyEditText.setText(FamilyHCDashboardActivity.empList.get(last).getLocationId());
        companyEditText.setVisibility(View.GONE);
        locationTextView.setText(FamilyHCDashboardActivity.empList.get(last).getPhone());

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        sharedPreferences.edit().putBoolean("Image Status", false).commit();

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);


        String picName=AppConstants.IMAGE_BASE_URL+FamilyHCDashboardActivity.empList.get(last).getEmail()+".png";
        getImage(picName);

    }


    // getting profile pic
    private void getImage(String pic){
        Picasso.with(HCModuleFamily_EmployeeDetails.this).load(pic).transform(new CircleTransform()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).placeholder(R.drawable.ico_user).error(R.drawable.ico_user).into(edit_image);
    }



    public void gotoBack(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
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
        }
        else
        {
            super.onBackPressed();
        }
    }

}
