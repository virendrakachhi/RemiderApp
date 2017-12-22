package com.attendanceapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;

public class HCModuleFamily_ShowPics extends Activity implements View.OnClickListener, NavigationPage.NavigationFunctions {


    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;


    TextView nameEditText, emailEditText, companyEditText, locationTextView,
            titleTextView;

    //For image editing
    ImageView edit_image;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_family_show_pics);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        sharedPreferences.edit().putBoolean("Image Status", false).commit();

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);
    }

    public void gotoBack(View view) {
        onBackPressed();
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
