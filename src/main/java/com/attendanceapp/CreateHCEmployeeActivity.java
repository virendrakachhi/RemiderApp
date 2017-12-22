package com.attendanceapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.Family;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.attendanceapp.utils.NavigationPage;
import android.widget.FrameLayout;
/**
 * Created by VICKY KUMAR on 16-01-2016.
 */
public class CreateHCEmployeeActivity extends FragmentActivity implements View.OnClickListener {
    TextView headerTextView;
    EditText nameET,emailET,phoneET;
    Button  registerBtn;
    boolean checkFamily = false;
    String locationID;
    ManagerHCClass managerLocData;
    boolean updateAccount=false;
    ImageView goToBackBtn,deleteBtn,editImageBtn;
    String id;
    List<HCFamily> familyList;
    List<HCEmployee> employeeList;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;


    //pic params
    AlertDialog photoDialog;
    private static int GET_PICTURE = 1,CAMERA_REQUEST=101;
    static String selectedImagePath = "";
    Bitmap bmp;
    String base64Image;




    String userType;
    User user;
    UserUtils userUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hc_employee);
        headerTextView = (TextView)findViewById(R.id.header_title);
        nameET = (EditText)findViewById(R.id.nameEditText);
        emailET = (EditText)findViewById(R.id.emailEditText);
        phoneET = (EditText)findViewById(R.id.phoneEditText);
        goToBackBtn = (ImageView)findViewById(R.id.gotoBack);
        deleteBtn = (ImageView)findViewById(R.id.imgDelete);
        editImageBtn = (ImageView)findViewById(R.id.edit_image);

        registerBtn = (Button)findViewById(R.id.registerButton);
//        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();

        userUtils = new UserUtils(CreateHCEmployeeActivity.this);
        int index = getIntent().getExtras().getInt("Index");
        Toast.makeText(getApplicationContext(),"Index== "+String.valueOf(index),Toast.LENGTH_LONG).show();
        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
        locationID = managerLocData.getId();

//        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);
        navigationButton.setVisibility(View.GONE);
         userType = getIntent().getExtras().getString("UserType");
        if (userType.matches("Family"))
        {
            familyList = managerLocData.getFamilyList();
            if(familyList.size()>0)
            {
                nameET.setText(familyList.get(0).getName());
                emailET.setText(familyList.get(0).getEmail());
                emailET.setEnabled(false);
                phoneET.setText(familyList.get(0).getPhone());
                id=familyList.get(0).getId();
                String picName=AppConstants.IMAGE_BASE_URL+familyList.get(0).getEmail()+".png";
                getImage(picName);
                updateAccount = true;
            }
            else
            {
                updateAccount = false;
            }
            checkFamily = true;
            headerTextView.setText("Family");
        }
        else
        {
            int i = getIntent().getExtras().getInt("EmpIndex");
            if(i==-1)
            {
                checkFamily = false;
                updateAccount = false;
            }
            else {
                employeeList = managerLocData.getEmployeeList();
                nameET.setText(employeeList.get(i).getName());
                emailET.setText(employeeList.get(i).getEmail());
                emailET.setEnabled(false);
                phoneET.setText(employeeList.get(i).getPhone());
                checkFamily = false;
                id=employeeList.get(0).getId();
                String picName=AppConstants.IMAGE_BASE_URL+employeeList.get(i).getEmail()+".png";
                getImage(picName);
                updateAccount = true;
            }
        }
    registerBtn.setOnClickListener(this);
        goToBackBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        editImageBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.registerButton:
            {
                String name,email,phone;
                name = nameET.getText().toString().trim();
                email = emailET.getText().toString().trim();
                phone = phoneET.getText().toString().trim();
                if(name == null || name.length() <1)
                {
                    makeToast("Please Enter Full Name");
                    break;
                }
                if(email == null || email.length() <1)
                {
                    makeToast("Please Enter Email");
                    break;
                }
                if(phone == null || phone.length() <1)
                {
                    makeToast("Please Enter Phone Number");
                    break;
                }
                if(!checkFamily)
                {
                Map<String, String> keysAndValues = new HashMap<>();
                   if(updateAccount)
                    {
                        if(id!=null) {
                            keysAndValues.put("employee_id", id);
                            keysAndValues.put("employee_name",name);
                            keysAndValues.put("employee_phone", phone);

                            UploadDataAsync(AppConstants.URL_UPDATE_HC_EMPLOYEE, keysAndValues);
                        }
                    }
                    else {
                       keysAndValues.put("manager_id",user.getUserId());

                       keysAndValues.put("full_name",name);
                       keysAndValues.put("health_employee_email",email);
                       keysAndValues.put("phone", phone);
                       keysAndValues.put("location_id", locationID);
                       keysAndValues.put("role", String.valueOf(11));

                       UploadDataAsync(AppConstants.URL_ADD_HC_EMPLOYEE, keysAndValues);
                    }
                    }
                else
                {
                    Map<String, String> keysAndValues = new HashMap<>();
                    if(updateAccount)
                    {
                        if(id!=null) {
                            keysAndValues.put("family_id", id);
                            keysAndValues.put("family_name",name);
                            keysAndValues.put("family_phone", phone);
                            UploadDataAsync(AppConstants.URL_UPDATE_HC_FAMILY, keysAndValues);
                        }
                    }
                    else {
                        keysAndValues.put("manager_id",user.getUserId());

                        keysAndValues.put("full_name",name);
                        keysAndValues.put("family_email",email);
                        keysAndValues.put("phone", phone);
                        keysAndValues.put("location_id", locationID);
                        keysAndValues.put("role", String.valueOf(12));

                        UploadDataAsync(AppConstants.URL_ADD_HC_FAMILY, keysAndValues);
                    }
                }

                break;
            }
            case R.id.gotoBack:
                onBackPressed();
                break;
            case R.id.imgDelete:
                deleteAccount(userType);
                break;
            case R.id.edit_image:
                PickAndUpload();
                break;

        }
    }



    //profile pic upload
    public void PickAndUpload() {
        LayoutInflater factory = LayoutInflater.from(CreateHCEmployeeActivity.this);
        final View imageDialogView = factory.inflate(
                R.layout.dialog_layout, null);
        photoDialog = new AlertDialog.Builder(CreateHCEmployeeActivity.this).create();
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_PICTURE) {


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
                Picasso.with(CreateHCEmployeeActivity.this).load(selectedImageUri.toString()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(editImageBtn);


                String temp = Base64.encodeToString(data1, Base64.DEFAULT);
//                 System.out.println("string is: "+temp+".......");
//                shared.edit().putString("imageUri",temp).commit();
                base64Image = temp;

            } else if (requestCode == CAMERA_REQUEST) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                final byte[] data2 = bytes.toByteArray();
                String temp1 = Base64.encodeToString(data2, Base64.DEFAULT);
                String path = MediaStore.Images.Media.insertImage(CreateHCEmployeeActivity.this.getContentResolver(), photo, "Title", null);
                System.out.println("Data is" + data2.length);
                System.out.println("Image path is" + path);

                Uri imageContent = Uri.parse(path);
                Picasso.with(CreateHCEmployeeActivity.this).load(imageContent).fit().memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(editImageBtn);
//                shared.edit().putString("imageUri",temp1).commit();
                base64Image = temp1;
            }

        }
        }



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




    void deleteAccount(String type)
    {
if(type.matches("Family"))
{
    Map<String, String> keysAndValues = new HashMap<>();
    if(updateAccount) {
        keysAndValues.put("family_id", id);
        DeleteAccountSync(AppConstants.URL_DELETE_FAMILY, keysAndValues);
    }
    else
    {
        makeToast("Can not Delete Empty Account.");
    }
}
else
{
    Map<String, String> keysAndValues = new HashMap<>();
    if(updateAccount) {
        keysAndValues.put("employee_id", id);
        DeleteAccountSync(AppConstants.URL_DELETE_HC_EMPLOYEE, keysAndValues);
    }
    else
    {
        makeToast("Can not Delete Empty Account.");
    }
}
    }


// getting profile pic
private void getImage(String pic){
    Picasso.with(CreateHCEmployeeActivity.this).load(pic).transform(new CircleTransform()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).placeholder(R.drawable.ico_user).error(R.drawable.ico_user).into(editImageBtn);
}



    private void UploadDataAsync(final String url, final Map<String, String> keysAndValues) {

        if(base64Image!=null) {
            keysAndValues.put("profile_pic", base64Image);
            System.out.println("profile_pic=== " + base64Image);
        }
        else
        {
            keysAndValues.put("profile_pic", "");
            System.out.println("profile_pic=== "+base64Image);
        }



        new AsyncTask<Object, Void, String>() {
            ProgressDialog alertDialog = new ProgressDialog(CreateHCEmployeeActivity.this);

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Object... params) {
                try {

                        return new WebUtils().post(url, keysAndValues);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                alertDialog.dismiss();
                alertDialog.cancel();

                if (s == null) {
                    makeToast("Error in uploading data");
                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {


                           String messageObj = jObject.getString("Message");
                            if(messageObj.matches("Saved successfully"))
                            {
                                makeToast(messageObj);
//                                HCEmployee empObj = new HCEmployee();
//                                JSONObject obj = jObject.getJSONObject("Data");
//                                empObj.setName(obj.getString("full_name"));
//                                empObj.setEmail(obj.getString("health_employee_email"));
//                                empObj.setPhone(obj.getString("full_name"));

                            }
                            else
                            {
                                makeToast(messageObj);
                            }



//                            isAddedNewClass = true;
//                            onBackPressed();
                            updateDataAsync();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Log.e(TAG, "Error in parsing data: " + s);
//                        Log.e(TAG, e.getMessage());
                        System.out.println(e.getMessage().toString());
                    }
                }
            }
        }.execute();
    }















    private void DeleteAccountSync(final String url, final Map<String, String> keysAndValues) {
        new AsyncTask<Object, Void, String>() {
            ProgressDialog alertDialog = new ProgressDialog(CreateHCEmployeeActivity.this);

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Object... params) {
                try {

                    return new WebUtils().post(url, keysAndValues);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                alertDialog.dismiss();
                alertDialog.cancel();

                if (s == null) {
                    makeToast("Error in uploading data");
                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {


                            String messageObj = jObject.getString("Message");
                            makeToast(messageObj);

//                            isAddedNewClass = true;
//                            onBackPressed();
                            updateDataAsync();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Log.e(TAG, "Error in parsing data: " + s);
//                        Log.e(TAG, e.getMessage());
                        System.out.println(e.getMessage().toString());
                    }
                }
            }
        }.execute();
    }

   private void updateDataAsync() {
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
                    System.out.println("list size== "+teacherClasses.size());
                        if (manager.getManagerLocationList().size() != teacherClasses.size()) {
                            System.out.println("isEditClass==true Inside IF");
                            manager.getManagerLocationList().clear();
                            manager.getManagerLocationList().addAll(teacherClasses);



                    }
                    userUtils.saveUserWithDataToSharedPrefs(manager, Manager.class);

                }

                    startActivity(new Intent(CreateHCEmployeeActivity.this,ManagerHCDashboardActivity.class));
                    finish();
            }
        }.execute();

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



    private void makeToast(String title) {
        Toast.makeText(CreateHCEmployeeActivity.this, title, Toast.LENGTH_LONG).show();
    }
}
