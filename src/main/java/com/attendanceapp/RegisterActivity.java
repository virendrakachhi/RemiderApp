package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.TwitterApp.TwDialogListener;
import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.Register;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.ValidationUtils;
import com.attendanceapp.utils.WebUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_EDIT = "EXTRA_EDIT";

    protected EditText nameEditText, emailEditText, passwordEditText,cpasswordEditText, schoolEditText, answerEditText;
    protected Button registerButton;
    protected ImageView facebookRegister, googlePlusRegister, twitterRegister;
    protected Spinner registerUserType, securityQuestions;
    protected RelativeLayout registerUserTypeLayout;
    protected TextView titleTextView;

    //For image editing
    ImageView edit_image;
    SharedPreferences shared;
    String base64Image;

    String android_id;
    private CallbackManager callbackManager;
    private SharedPreferences sharedPreferences;

    /* for twitter login */
    private TwitterApp mTwitter;
    private static final String twitter_consumer_key = "LDeRaFeCZC8DtAbxZ1ZMuNpj9";
    private static final String twitter_secret_key = "aWwhKIctIUvqRw3WQJWibt4rWzZh6XFMcSYzvStTRMQoifNVVV";
    private static final String CALLBACK_URL = "twitterapp://connect";


    /* for google login */
    private static final int RC_SIGN_IN = 778;
    private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked;
    private boolean mIntentInProgress;

    /* for gcm registrations */
    String regId;
    protected Gson gson = new Gson();

    Register register;
    User userToEdit;
    boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_register);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        shared=getSharedPreferences("myapp",Context.MODE_PRIVATE);
        register = new Register();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        mTwitter = new TwitterApp(this, twitter_consumer_key, twitter_secret_key, register);
        mTwitter.setListener(mTwLoginDialogListener);
        if (mTwitter.hasAccessToken()) {
            String username = mTwitter.getUsername();
            username = (username.equals("")) ? "Unknown" : username;
        }

        /* facebook login manager */
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        AccessToken accessToken = loginResult.getAccessToken();
//                        String userId = accessToken.getUserId();

                        //new GetFacebookDataAsync().execute(userId);
                        getFacebookData(accessToken);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        makeToast("Some error occurred while register! Please try again.");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        makeToast("Some error occurred while register! Please try again.");
                    }
                });

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        cpasswordEditText = (EditText) findViewById(R.id.cpasswordEditText);

        schoolEditText = (EditText) findViewById(R.id.schoolEditText);
        answerEditText = (EditText) findViewById(R.id.answerEditText);
        registerButton = (Button) findViewById(R.id.registerButton);
        facebookRegister = (ImageView) findViewById(R.id.facebookLogin);
        googlePlusRegister = (ImageView) findViewById(R.id.googlePlusLogin);
        twitterRegister = (ImageView) findViewById(R.id.twitterLogin);
        registerUserType = (Spinner) findViewById(R.id.registerUserType);
        securityQuestions = (Spinner) findViewById(R.id.securityQuestions);
        registerUserTypeLayout = (RelativeLayout) findViewById(R.id.registerUserTypeLayout);
        titleTextView = (TextView) findViewById(R.id.textView1);
        edit_image = (ImageView) findViewById(R.id.edit_image);

        registerButton.setOnClickListener(this);
        facebookRegister.setOnClickListener(this);
        googlePlusRegister.setOnClickListener(this);
        twitterRegister.setOnClickListener(this);

        edit_image.setOnClickListener(this);

        /*if(!shared.getString("imageUri","").equals("")) {
            System.out.println(shared.getString("ImageUrl","b"));
            Picasso.with(RegisterActivity.this).load(shared.getString("ImageUrl","b")).transform(new CircleTransform()).placeholder(R.drawable.ico_user).error(R.drawable.ico_user).into(edit_image);
        }*/

        Intent intent = getIntent();
        userToEdit = (User) intent.getSerializableExtra(EXTRA_USER);
        isEdit = intent.getBooleanExtra(EXTRA_EDIT, false);
        if (isEdit) {
            getImage();
        }
        ArrayAdapter<CharSequence> registerUserTypeAdapter = ArrayAdapter.createFromResource(this, R.array.register_user_type, R.layout.selector_item);
        registerUserTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registerUserType.setPrompt("Select View");
        registerUserType.setAdapter(new NothingSelectedSpinnerAdapter(registerUserTypeAdapter, R.layout.register_spinner_user_view_nothing_selected, this));

        ArrayAdapter<CharSequence> securityQuestionsAdapter = ArrayAdapter.createFromResource(this, R.array.register_security_questions, R.layout.selector_item);
        securityQuestionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestions.setAdapter(new NothingSelectedSpinnerAdapter(securityQuestionsAdapter, R.layout.register_spinner_question_nothing_selected, this));

        if (isEdit) {
            int spinnerPosition = securityQuestionsAdapter.getPosition(userToEdit.getSecurityQuestion());
            securityQuestions.setSelection(spinnerPosition + 1);
        } else {
            securityQuestions.setPrompt("Select security question");
        }

        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        register.setDeviceToken(android_id);
        register.setStatus(1);

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        regId = GCMRegistrar.getRegistrationId(this);

        if (isEdit) {
            register.setEmail(userToEdit.getEmail());
            register.setSchool(userToEdit.getSchool());
            register.setUserView(userToEdit.getUserView());

            titleTextView.setText("Edit Account");
            nameEditText.setText(userToEdit.getUsername());
            emailEditText.setText(userToEdit.getEmail());
            schoolEditText.setText(userToEdit.getSchool());

            registerUserTypeLayout.setVisibility(View.GONE);
            AndroidUtils.disableEditText(emailEditText);

            findViewById(R.id.textView4).setVisibility(View.GONE);
            findViewById(R.id.otherTypeRegisterLayout).setVisibility(View.GONE);

            answerEditText.setText(userToEdit.getSecurityQuestionAnswer());

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButton:
                registerButton();
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
            case R.id.edit_image:
                PickAndUpload();
                break;
        }
    }
    AlertDialog photoDialog;
    private static int GET_PICTURE = 1,CAMERA_REQUEST=101;

    public void PickAndUpload() {
        LayoutInflater factory = LayoutInflater.from(RegisterActivity.this);
        final View imageDialogView = factory.inflate(
                R.layout.dialog_layout, null);
        photoDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        photoDialog.setView(imageDialogView);
        Button button1=(Button) imageDialogView.findViewById(R.id.bt1);
        Button button2=(Button) imageDialogView.findViewById(R.id.bt2);
        RelativeLayout rel1=(RelativeLayout) imageDialogView.findViewById(R.id.rl1);
        RelativeLayout rel2=(RelativeLayout) imageDialogView.findViewById(R.id.rl3);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoDialog.dismiss();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //your business logic
                photoDialog.dismiss();
            }
        });
        rel1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //your business logic
                photoDialog.dismiss();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        rel2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //your business logic
                photoDialog.dismiss();
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GET_PICTURE);
            }
        });
        photoDialog.show();
    }

    static String selectedImagePath = "";
    Bitmap bmp;

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null,
                null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(MediaStore.Images.ImageColumns.DATA);

            result = cursor.getString(idx);

        }
        cursor.close();
        return result;
    }
    //function for getting profile pic
private void getImage(){
    Map<String, String> keysValues = new HashMap<>();
    if (isEdit) {
        keysValues.put("user_id", userToEdit.getUserId());
    }
//    getPicAsyncTask("http://www.abdevs.com/attendance/Mobiles/get_user_image", keysValues);
    getPicAsyncTask(AppConstants.URL_GET_USER_IMAGE, keysValues);
}
    private void registerButton() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String cpassword = cpasswordEditText.getText().toString().trim();
        String school = schoolEditText.getText().toString().trim();
        String answer = answerEditText.getText().toString().trim();

        String userView;

        if (isEdit) {
            if (name.isEmpty() || email.isEmpty() || /*password.isEmpty()|| cpassword.isEmpty() ||*/ school.isEmpty() || answer.isEmpty() || securityQuestions.getSelectedItem() == null) {
                makeToast("Please enter required fields");
                return;
            }
            else if (!password.trim().matches(cpassword.trim()))
            {
                makeToast("These passwords don't match. Try again? ");

                return;
            }
            userView = userToEdit.getUserView();
        }

        else {
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || cpassword.isEmpty() || school.isEmpty() || answer.isEmpty() || registerUserType.getSelectedItem() == null || securityQuestions.getSelectedItem() == null) {
                makeToast("Please enter required fields");
                return;
            }
            else if (!password.trim().matches(cpassword.trim()))
            {
                makeToast("These passwords don't match. Try again? ");

                return;
            }
            userView = registerUserType.getSelectedItem().toString();
        }

        String securityQuestion = securityQuestions.getSelectedItem().toString();

        if (!ValidationUtils.isValidEmail(email)) {
            makeToast("Please check email");
            return;
        }

        register.setFullName(name);
        register.setEmail(email);
        register.setPassword(password);
        register.setSchool(school);
        register.setSecurityQuestionAnswer(answer);
        register.setUserView(userView);
        register.setSecurityQuestion(securityQuestion);

        Map<String, String> keysValues = new HashMap<>();

        if (isEdit) {
            keysValues.put("id", userToEdit.getUserId());
        }

        keysValues.put("email", email);
        keysValues.put("password", password);
        keysValues.put("school", school);
        keysValues.put("userview", userView);
        keysValues.put("securityQuestion", securityQuestion);
        keysValues.put("securityQuestionAnswer", answer);
        keysValues.put("status", "1");
        keysValues.put("full_name", name);
        keysValues.put("deviceToken", android_id);
//        keysValues.put("profile_pic", shared.getString("imageUri",""));
//        System.out.println("profile_pic=== "+shared.getString("imageUri",""));
        if(base64Image!=null) {
            keysValues.put("profile_pic", base64Image);
            System.out.println("profile_pic=== " + base64Image);
        }
        else
        {
            keysValues.put("profile_pic", "");
            System.out.println("profile_pic=== "+base64Image);
        }
        if (register.getFacebookId() != null && !register.getFacebookId().isEmpty()) {
            keysValues.put("facebook_id", register.getFacebookId());
        }
        if (register.getGooglePlusId() != null && !register.getGooglePlusId().isEmpty()) {
            keysValues.put("google_id", register.getGooglePlusId());
        }
        if (register.getTwitterId() != null && !register.getTwitterId().isEmpty()) {
            keysValues.put("twitter_id", register.getTwitterId());
        }

//        new RegisterAsync().execute(AppConstants.URL_REGISTER, keysValues);
        registerAsyncTask(AppConstants.URL_REGISTER, keysValues);
    }
    private void getPicAsyncTask(final String url, final Map<String, String> map) {

        new AsyncTask<Void, Void, String>() {

            private ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Getting profile details...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(url, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();

                if (result == null) {
                    makeToast("Error in connection");
                } else {
                    try {

                        Log.e(TAG, result);

                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.has("Error")) {
                            makeToast(jsonObject.getString("Error"));
                        } else {

//                        String pic=jsonObject.getJSONObject("data").getString("full_image_url")+jsonObject.getJSONObject("data").getJSONObject("userdata").getString("profile_pic");
                            String pic=AppConstants.IMAGE_BASE_URL+jsonObject.getJSONObject("data").getJSONObject("userdata").getString("profile_pic");


                            Picasso.with(RegisterActivity.this).load(pic).transform(new CircleTransform()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).placeholder(R.drawable.ico_user).error(R.drawable.ico_user).into(edit_image);
                            System.out.println("User image is following"+pic);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
            }

        }.execute();

    }

    private void registerAsyncTask(final String url, final Map<String, String> map) {

        new AsyncTask<Void, Void, String>() {

            private ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(url, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();

                if (result == null) {
                    makeToast("Error in connection");
                } else {
                    try {

                        Log.e(TAG, result);

                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.has("Error")) {
                            makeToast(jsonObject.getString("Error"));
                        } else {

                            User user = DataUtils.getUserFromJsonString(result);
                            shared.edit().putString("ImageUrl",jsonObject.getJSONObject("Data").getString("full_image_url")+jsonObject.getJSONObject("Data").getJSONObject("User").getString("profile_pic")).apply();
                            System.out.println(jsonObject.getJSONObject("Data").getString("full_image_url")+jsonObject.getJSONObject("Data").getJSONObject("User").getString("profile_pic"));
                            if (isEdit) {

                                sharedPreferences.edit()
                                        .putString(AppConstants.KEY_LOGGED_IN_USER, gson.toJson(user, User.class))
                                        .apply();

                                setResult(RESULT_OK);

                            } else {

                                sharedPreferences.edit().clear()
                                        .putString(AppConstants.KEY_LOGGED_IN_USER, gson.toJson(user, User.class))
                                        .apply();

                                // upload gcm registration key to server
                                updateServerForGcm(user);

                                Intent activityToStart = new Intent(RegisterActivity.this, RoleSelectActivity.class);
                                startActivity(activityToStart);

                            }

                            finish();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
            }

        }.execute();

    }

    private void updateServerForGcm(final User user) {
        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(RegisterActivity.this);

        // Check if regid already presents
        if (regId.equals("")) {
            // Registration is not present, register now with GCM
            GCMRegistrar.register(RegisterActivity.this, AppConstants.SENDER_ID);
        } else {
            // Device is already registered on GCM
            if (GCMRegistrar.isRegisteredOnServer(RegisterActivity.this)) {
                // Skips registration.
                // Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = RegisterActivity.this;
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        // Register on our server
                        // On server creates a new user
                        //	ServerUtilities.register(context, name, email, regId);
//                      reg_id,user_id,devtok,
                        ServerUtilities.register(context, regId, user.getUserId(), user.getDeviceToken());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);


                    }
                }.execute();
            }
        }
    }

    private void processLoginUsingFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
    }

    private void processLoginUsingGooglePlus() {
//        makeToast("process login using google plus");
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    private void processLoginUsingTwitter() {
//        makeToast("process login using twitter");

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

    public void gotoBack(View view) {
        finish();
    }

    private void makeToast(String title) {
        Toast.makeText(RegisterActivity.this, title, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        for google plus login
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }

//        for facebook login
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_PICTURE) {

                if (bmp != null) {
                    bmp.recycle();

                }

                Uri selectedImageUri = data.getData();
                selectedImagePath = getRealPathFromURI(selectedImageUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                options.inSampleSize = calculateInSampleSize(options, 300, 300);
                options.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeFile(selectedImagePath, options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                final byte[] data1 = stream.toByteArray();
                System.out.println("Data is" + data1.length);
                System.out.println("Image path is" + selectedImagePath);
                Picasso.with(RegisterActivity.this).load(selectedImageUri.toString()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(edit_image);


                String temp= Base64.encodeToString(data1, Base64.DEFAULT);
//                 System.out.println("string is: "+temp+".......");
//                shared.edit().putString("imageUri",temp).commit();
                base64Image = temp;

            }
            else if(requestCode==CAMERA_REQUEST){

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                final byte[] data2 = bytes.toByteArray();
                String temp1= Base64.encodeToString(data2, Base64.DEFAULT);
                String path = MediaStore.Images.Media.insertImage(RegisterActivity.this.getContentResolver(), photo, "Title", null);
                System.out.println("Data is" + data2.length);
                System.out.println("Image path is" + path);

                Uri imageContent = Uri.parse(path);
                Picasso.with(RegisterActivity.this).load(imageContent).fit().memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(edit_image);
//                shared.edit().putString("imageUri",temp1).commit();
                base64Image = temp1;
            }
        }
    }

    private void getFacebookData(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        System.out.println("object = " + object);
                        setDataFromFacebookJsonObject(object);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setDataFromFacebookJsonObject(final JSONObject facebookJsonObject) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
//               {"id":"477185732450844","link":"https:\/\/www.facebook.com\/app_scoped_user_id\/477185732450844\/","name":"Hemant Sharma"}
                String data = "";
                try {
                    if (facebookJsonObject.has("id")) {
                        register.setFacebookId(facebookJsonObject.getString("id"));
                        data += "id";
                    }
                    if (facebookJsonObject.has("email")) {
                        register.setEmail(facebookJsonObject.getString("email"));
                        emailEditText.setText(facebookJsonObject.getString("email"));
                        data += ", email";

                    }
                    if (facebookJsonObject.has("name")) {
                        register.setFullName(facebookJsonObject.getString("name"));
                        data += ", name";
                    }

                    makeToast("Successfully get required data from facebook: " + data);
                    makeToast("Please enter remaining data");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//         mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
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

            register.setGooglePlusId(id);
            register.setEmail(email);
            register.setFullName(personName);

            emailEditText.setText(email);

            makeToast("Successfully get required data from Google+: id, name, email");
            makeToast("Please enter remaining data");
        }
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
        @Override
        public void onComplete(String value) {
            String username = mTwitter.getUsername();
            username = (username.equals("")) ? "No Name" : username;

//            Toast.makeText(RegisterActivity.this, "Connected to Twitter as " + username, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(String value) {
//            Toast.makeText(RegisterActivity.this, "Twitter connection failed", Toast.LENGTH_LONG).show();

        }
    };
}
