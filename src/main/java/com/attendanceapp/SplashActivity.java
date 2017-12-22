package com.attendanceapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.attendanceapp.activities.AddClassEventCompanyActivity;
import com.attendanceapp.activities.Attendee_DashboardActivity;
import com.attendanceapp.activities.Employee_DashboardActivity;
import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.activities.Manager_DashboardActivity;
import com.attendanceapp.models.EventHost;
import com.attendanceapp.models.User;
import com.attendanceapp.services.AppMonitorService;
import com.attendanceapp.trance.ExceptionHandler;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Date;


public class SplashActivity extends Activity implements View.OnClickListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    protected Button previewButton, loginButton, getStartedButton;
    protected SharedPreferences sharedPreferences;
    protected Gson gson = new Gson();
    private UserUtils userUtils;


    private static final long INTERVAL = 1000*10;
    private static final long FASTEST_INTERVAL = 1000*5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String mLocationUpdate;

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        ExceptionHandler.register(this, "http://crashreporting.canopussystems.com/server/collect/server.php");

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).build();
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null)
            AppGlobals.mCurrentLocation = location;


        previewButton = (Button) findViewById(R.id.previewButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        getStartedButton = (Button) findViewById(R.id.getStartedButton);

        previewButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        getStartedButton.setOnClickListener(this);

        // check if user previously logged in, skip this
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(SplashActivity.this);
        String userString = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);

        // start service to get activity on front
        startService(new Intent(this, AppMonitorService.class));

        if (userString != null && !"null".equalsIgnoreCase(userString) ) {
            User user = gson.fromJson(userString, User.class);
            String role = null;
            if(user!=null)
             role = user.getUserRoles().size() == 0 ? null : String.valueOf(user.getUserRoles().get(0).getRole());

            if (role == null) {
                openActivity(RoleSelectActivity.class, true);

            } else if ("1".equalsIgnoreCase(role)) {
                openActivity(TeacherDashboardActivity.class, true);

            } else if ("2".equalsIgnoreCase(role)) {
                openActivity(StudentDashboardActivity.class, true);

            } else if ("3".equalsIgnoreCase(role)) {
                openActivity(ParentDashboardActivity.class, true);

            } else if ("4".equalsIgnoreCase(role)) {
                openActivity(Manager_DashboardActivity.class, true);

            } else if ("5".equalsIgnoreCase(role)) {
                openActivity(Employee_DashboardActivity.class, true);

            } else if ("6".equalsIgnoreCase(role)) {
                openActivity(EventHost_DashboardActivity.class, true);

            } else if ("7".equalsIgnoreCase(role)) {
                openActivity(Attendee_DashboardActivity.class, true);

            }
            else if ("10".equalsIgnoreCase(role)) {
                openActivity(ManagerHCDashboardActivity.class, true);

            }

            else if ("11".equalsIgnoreCase(role)) {
                openActivity(EmployeeHCDashboardActivity.class, true);

            }
            else if ("12".equalsIgnoreCase(role)) {
                openActivity(FamilyHCDashboardActivity.class, true);

            }
            else {
                openActivity(RoleSelectActivity.class, true);
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previewButton:
                makeToast("Preview Button Clicked!");
                break;
            case R.id.loginButton:
                openActivity(LoginActivity.class, false);
                break;
            case R.id.getStartedButton:
                openActivity(RegisterActivity.class, false);
                break;
        }
    }

    private void openActivity(Class aClass, boolean finishThis) {
        Intent intent = new Intent(SplashActivity.this, aClass);
        startActivity(intent);

        if (finishThis) {
            finish();
        }
    }

    private void makeToast(String title) {
        Toast.makeText(SplashActivity.this, title, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER, null) != null) {
            onBackPressed();
        }
        if(mGoogleApiClient.isConnected()){
            startLocationUpdate();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdate();
    }

    protected void startLocationUpdate(){
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        AppGlobals.mCurrentLocation  = location;
        mLocationUpdate = DateFormat.getTimeInstance().format(new Date());
        Log.e("mCurrentLocation", AppGlobals.mCurrentLocation.getLatitude() + "<<?>>" + AppGlobals.mCurrentLocation.getLongitude());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


}
