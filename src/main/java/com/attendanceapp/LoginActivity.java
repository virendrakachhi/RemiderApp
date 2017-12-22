package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.attendanceapp.activities.Attendee_DashboardActivity;
import com.attendanceapp.activities.Employee_DashboardActivity;
import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.activities.Manager_DashboardActivity;
import com.attendanceapp.models.Employee;
import com.attendanceapp.models.Family;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.Register;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.services.BeaconMonitorService;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class LoginActivity extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public enum LoginType {APP, FACEBOOK, GOOGLE, TWITTER}

    private static final String TAG = LoginActivity.class.getSimpleName();

    protected EditText emailEditText, passwordEditText;
    protected Button loginButton, forgotPasswordButton;
    protected ImageView facebookLogin, googlePlusLogin, twitterLogin;

    private CallbackManager callbackManager;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 778;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    /**
     * True if the sign-in button was clicked.  When true, we know to resolve all
     * issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;
    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;
SharedPreferences sharedPreferences;
    private TwitterApp mTwitter;
    private static final String twitter_consumer_key = "LDeRaFeCZC8DtAbxZ1ZMuNpj9";
    private static final String twitter_secret_key = "aWwhKIctIUvqRw3WQJWibt4rWzZh6XFMcSYzvStTRMQoifNVVV";
//    private static final String CALLBACK_URL = "twitterapp://connect";
    UserUtils userUtils;
    protected User user;
    Register forTwitterLogin = new Register();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
        forgotPasswordButton = (Button) findViewById(R.id.forgotPasswordButton);
        facebookLogin = (ImageView) findViewById(R.id.facebookLogin);
        googlePlusLogin = (ImageView) findViewById(R.id.googlePlusLogin);
        twitterLogin = (ImageView) findViewById(R.id.twitterLogin);

        loginButton.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
        facebookLogin.setOnClickListener(this);
        googlePlusLogin.setOnClickListener(this);
        twitterLogin.setOnClickListener(this);

        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    emailEditText.setHint("");
            }
        });

        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    passwordEditText.setHint("");
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        AccessToken accessToken = loginResult.getAccessToken();
                        String userId = accessToken.getUserId();

                        LoginAsync(LoginType.FACEBOOK, AppConstants.URL_LOGIN, userId, null);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        forTwitterLogin.setShowMessageOnTwitterConnection(false);
        mTwitter = new TwitterApp(this, twitter_consumer_key, twitter_secret_key, forTwitterLogin);
        mTwitter.setListener(mTwLoginDialogListener);

    }


    private final TwitterApp.TwDialogListener mTwLoginDialogListener = new TwitterApp.TwDialogListener() {
        @Override
        public void onComplete(String value) {
            String userId = mTwitter.getUserId();

            LoginAsync(LoginType.TWITTER, AppConstants.URL_LOGIN, userId, null);
        }

        @Override
        public void onError(String value) {

        }
    };

    private void registerForGcm(final User user) {
          /* register user to receive push notifications */

        GCMRegistrar.checkDevice(LoginActivity.this);
        GCMRegistrar.checkManifest(LoginActivity.this);
        final String regId = GCMRegistrar.getRegistrationId(LoginActivity.this);

        if (regId.equals("")) {
            GCMRegistrar.register(LoginActivity.this, AppConstants.SENDER_ID);
        } else {
            if (!GCMRegistrar.isRegisteredOnServer(LoginActivity.this)) {
                // Try to register again, but not in the UI thread.
                final Context context = LoginActivity.this;
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        ServerUtilities.register(context, regId, user.getUserId(), user.getDeviceToken());
                        return null;
                    }
                }.execute();
            }
//            else
//            {
//                unregisterReceiver(mHandleMessageReceiver);
//            }
        }

        registerReceiver(mHandleMessageReceiver, new IntentFilter(AppConstants.DISPLAY_MESSAGE_ACTION));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                processLogin();
                break;
            case R.id.forgotPasswordButton:
                processForgotPassword();
                break;
            case R.id.facebookLogin:
                processLoginUsingFacebook();
                break;
            case R.id.googlePlusLogin:
                processLoginUsingGooglePlus();
                break;
            case R.id.twitterLogin:
                processLoginUsingTwitter();
                break;

        }
    }

    private void processLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        LoginAsync(LoginType.APP, AppConstants.URL_LOGIN, email, password);
    }

    private void processForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void processLoginUsingFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("public_profile"));
    }

    private void processLoginUsingGooglePlus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    private void processLoginUsingTwitter() {
        onTwitterClick();
    }

    private void onTwitterClick() {
        if (mTwitter.hasAccessToken()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Delete current Twitter connection?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mTwitter.resetAccessToken();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();

            alert.show();
        } else {
            mTwitter.authorize();
        }
    }

    boolean isBackgroundWorkDone = false;

    public void LoginAsync(final LoginType loginType, final String url, final String emailOrOtherAppId, final String password) {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

        class MyAsyncTask extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                isBackgroundWorkDone = false;
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {

                String result = null;

                DefaultHttpClient dhc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                HttpPost postMethod = new HttpPost(url);

                List<NameValuePair> namePairs = new ArrayList<>();

                if (loginType == LoginType.APP) {
                    namePairs.add(new BasicNameValuePair("email", emailOrOtherAppId));
                    namePairs.add(new BasicNameValuePair("password", password));
                } else if (loginType == LoginType.FACEBOOK) {
                    namePairs.add(new BasicNameValuePair("facebook_id", emailOrOtherAppId));
                } else if (loginType == LoginType.GOOGLE) {
                    namePairs.add(new BasicNameValuePair("google_id", emailOrOtherAppId));
                } else if (loginType == LoginType.TWITTER) {
                    namePairs.add(new BasicNameValuePair("twitter_id", emailOrOtherAppId));
                }

                try {
                    postMethod.setEntity(new UrlEncodedFormEntity(namePairs));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    result = dhc.execute(postMethod, res);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                isBackgroundWorkDone = true;
                progressDialog.dismiss();
                if (s == null) {
                    makeToast("Error in login");

                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {

                            User user = DataUtils.getUserFromJsonString(s);
                            ((AppGlobals) getApplication()).setUser(user);

                            // save data to shared prefs
                            UserUtils userUtils = new UserUtils(LoginActivity.this);
                            userUtils.clearSharedPrefs();
                            userUtils.saveUserToSharedPrefs(user);

//                            startService(new Intent(LoginActivity.this, SendLocationService.class));
//                            startService(new Intent(LoginActivity.this, BeaconMonitorService.class));

                            registerForGcm(user);

                            Intent activityToStart;
                            String userRole=null;
                            if(user!=null)
                            userRole  = user.getUserRoles().size() == 0 ? null : String.valueOf(user.getUserRoles().get(0).getRole());

                            if (userRole == null) {
                                activityToStart = new Intent(LoginActivity.this, RoleSelectActivity.class);
                            } else if ("1".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                            } else if ("2".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                            } else if ("3".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, ParentDashboardActivity.class);
                            } else if ("4".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, Manager_DashboardActivity.class);
                            } else if ("5".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, Employee_DashboardActivity.class);
                            } else if ("6".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, EventHost_DashboardActivity.class);
                            } else if ("7".equalsIgnoreCase(userRole)) {
                                activityToStart = new Intent(LoginActivity.this, Attendee_DashboardActivity.class);
                            }
//                            else if ("10".equalsIgnoreCase(userRole)) {
//                                activityToStart = new Intent(LoginActivity.this, HealthCareAddLocationActivity.class);
//                            }
                            else if ("10".equalsIgnoreCase(userRole)) {
                                updateManagerDataAsync();
                                return;
//                                activityToStart = new Intent(LoginActivity.this, ManagerHCDashboardActivity.class);
                            }
                            else if ("11".equalsIgnoreCase(userRole)) {
                                updateDataAsync();
                                return;
//                                activityToStart = new Intent(LoginActivity.this, EmployeeHCDashboardActivity.class);
                            }
                            else if ("12".equalsIgnoreCase(userRole)) {
                                updateFamilyDataAsync();
                                return;
//                                activityToStart = new Intent(LoginActivity.this, FamilyHCDashboardActivity.class);
                            }
                            else {
                                activityToStart = new Intent(LoginActivity.this, RoleSelectActivity.class);
                            }
                            startActivity(activityToStart);
                            finish();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        makeToast("Error in login");
                        Log.e(TAG, "Error in parsing data: " + s);
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }

        final MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!asyncTask.isCancelled()) {
                    if (!isBackgroundWorkDone) {
                        asyncTask.cancel(true);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            progressDialog.cancel();
                        }
                        makeToast("Please check your internet connection!");
                    }
                }
            }
        }, 10000);
    }

    public void gotoBack(View view) {
        finish();
    }

    private void makeToast(String title) {
        Toast.makeText(LoginActivity.this, title, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mSignInClicked = false;
//        makeToast("User is connected!");

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String personGooglePlusProfile = currentPerson.getUrl();
            String id = currentPerson.getId();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            System.out.println("id = " + id + ", personName: " + personName + ", personGooglePlusProfile: " + personGooglePlusProfile + ", email: " + email);

            LoginAsync(LoginType.GOOGLE, AppConstants.URL_LOGIN, id, null);
        }
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    /**
     * Receiving push messages
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(AppConstants.EXTRA_BROADCAST_MESSAGE);
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());

            /**
             * Take appropriate action on this message
             * depending upon your app requirement
             * */

            // Showing received message
            //lblMessage.append(newMessage + "\n");


            Toast.makeText(getApplicationContext(), "New Message: " + newMessage, Toast.LENGTH_LONG).show();

            // Releasing wake lock
            WakeLocker.release();
        }
    };


    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }

    private void updateDataAsync() {
        userUtils = new UserUtils(LoginActivity.this);

        user = userUtils.getUserFromSharedPrefs();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.EmployeeHC.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_HCEMPLOYEE, hm);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Employee fm = new Employee(user);
                    List<HCEmployee> newList = DataUtils.getHCEmployeeLocationListFromJsonString(result);



//                    if(employee!=null) {
                    if (fm.getManagerLocationList().size() != newList.size()) {

                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

//                            userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
//                            userUtils.saveUserToSharedPrefs(user);

//                            baseViewPagerAdapter.notifyDataSetChanged();
//                        setOneWordTextView(0);
//                        employeeChecklist_Id = DataUtils.getHCEmployeeCheckListFromJsonString(result);
                    }
//                    }
                    else {
                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

//                        setOneWordTextView(0);
//                        employeeChecklist_Id = DataUtils.getHCEmployeeCheckListFromJsonString(result);
                    }
//                    employee = fm;
                    userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
                    userUtils.saveUserToSharedPrefs(user);
                    Log.v("Inside UPdate","bla bla");
                    startActivity( new Intent(LoginActivity.this, EmployeeHCDashboardActivity.class));
                    finish();
                }
            }
        }.execute();

    }

    private void updateFamilyDataAsync() {
        userUtils = new UserUtils(LoginActivity.this);

        user = userUtils.getUserFromSharedPrefs();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.Family.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_FAMILY, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Family fm = new Family(user);
                    List<HCFamily> newList = DataUtils.getFamilyLocationListFromJsonString(result);
//                    if(family!=null) {
                        if (fm.getManagerLocationList().size() != newList.size()) {

                            fm.getManagerLocationList().clear();
                            fm.getManagerLocationList().addAll(newList);
                            userUtils.saveUserWithDataToSharedPrefs(fm, Family.class);
                            userUtils.saveUserToSharedPrefs(user);

//                            baseViewPagerAdapter.notifyDataSetChanged();
//                            setOneWordTextView(0);
//                        }
                    }
                    else {
                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

                        userUtils.saveUserWithDataToSharedPrefs(fm, Family.class);
                    }
                    startActivity( new Intent(LoginActivity.this, FamilyHCDashboardActivity.class));
                    finish();
                }
            }
        }.execute();

    }


    private void updateManagerDataAsync() {
        userUtils = new UserUtils(LoginActivity.this);

        user = userUtils.getUserFromSharedPrefs();

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
             }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.ManagerHC.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_MANAGER, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    Manager manager = new Manager(user);
                    List<ManagerHCClass> teacherClasses = DataUtils.getManagerLocationListFromJsonString(result);
                        if (manager.getManagerLocationList().size() != teacherClasses.size()) {
                            System.out.println("isEditClass==true Inside IF");
                            manager.getManagerLocationList().clear();
                            manager.getManagerLocationList().addAll(teacherClasses);
                        }
                    userUtils.saveUserWithDataToSharedPrefs(manager, Manager.class);

                }

                startActivity( new Intent(LoginActivity.this, ManagerHCDashboardActivity.class));

                finish();
            }
        }.execute();

    }

}
