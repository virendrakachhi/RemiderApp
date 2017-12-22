package com.attendanceapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.ValidationUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteUser extends Activity implements View.OnClickListener {

    private static final String TAG = "TeacherInviteStudent";
    public static final String EXTRA_CLASS_CODE = "ClassCode";
    public static final String EXTRA_CLASS_ID = "ClassId";
    public static final String EXTRA_STUDENT_ID = "EXTRA_STUDENT_ID";
    public static final String EXTRA_STUDENT_EMAIL = "EXTRA_STUDENT_EMAIL";
    public static final String EXTRA_PARENT_EMAIL = "EXTRA_PARENT_EMAIL";
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String RESULT_ADDED_STUDENT = "RESULT_ADDED_STUDENT";
    private static final int REQUEST_STUDENT_IMAGE = 234;

    protected TextView classCode, addAnotherStudentBtn, SkipForNowBtn;
    protected ImageView addStudentBtn, selectedImage;
    private EditText studentEmail, parentEmail;
    Button addImageButton;
    RelativeLayout skipButtonLayout;

    protected String requestClassCode, requestClassId, requestStudentId, requestStudentEmail, requestParentEmail, selectedImageName;
    private boolean isFirstTime = true;
    Bitmap selectedImageBitmap;
    private UserRole userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_user);

        classCode = (TextView) findViewById(R.id.txtClassCode);
        addAnotherStudentBtn = (TextView) findViewById(R.id.txtAddAnotherStudent);
        SkipForNowBtn = (TextView) findViewById(R.id.txtSkipForNow);
        classCode = (TextView) findViewById(R.id.txtClassCode);
        addStudentBtn = (ImageView) findViewById(R.id.imgAdd);
        selectedImage = (ImageView) findViewById(R.id.selectedImage);
        studentEmail = (EditText) findViewById(R.id.editStudentEmail);
        parentEmail = (EditText) findViewById(R.id.editParentEmail);
        addImageButton = (Button) findViewById(R.id.img);
        skipButtonLayout = (RelativeLayout) findViewById(R.id.skipButtonLayout);

        Bundle bundle = getIntent().getExtras();
        userRole = UserRole.valueOf(bundle.getInt(AppConstants.EXTRA_USER_ROLE));
        requestClassCode = bundle.getString(EXTRA_CLASS_CODE);
        requestClassId = bundle.getString(EXTRA_CLASS_ID);
        requestStudentId = bundle.getString(EXTRA_STUDENT_ID);
        requestStudentEmail = bundle.getString(EXTRA_STUDENT_EMAIL);
        requestParentEmail = bundle.getString(EXTRA_PARENT_EMAIL);
        isFirstTime = bundle.getBoolean(EXTRA_IS_FIRST_TIME, true);

        setRoleBasedProperties(userRole);

        classCode.setText(classCode.getText() + requestClassCode);
        if (requestStudentEmail != null) {
            studentEmail.setText(requestStudentEmail);
        }
        if (requestParentEmail != null) {
            parentEmail.setText(requestParentEmail);
        }

        if (!isFirstTime) {
            skipButtonLayout.setVisibility(View.GONE);
        }

        addStudentBtn.setOnClickListener(this);
        addImageButton.setOnClickListener(this);
        selectedImage.setOnClickListener(this);
    }

    private void setRoleBasedProperties(UserRole userRole) {
        String title, code;

        if (userRole == UserRole.Teacher) {
            title = "Student";
            code = "Class";
            code = "";

        } else if (userRole == UserRole.EventHost) {
            title = "Attendee";
            code = "Event";
            parentEmail.setVisibility(View.GONE);

        } else if (userRole == UserRole.Manager) {
            title = "Employee";
            code = "Company";
            parentEmail.setVisibility(View.GONE);

        } else {
            title = "";
            code = "";
        }

        ((TextView)findViewById(R.id.txtTitle)).setText("Add " + title);
        classCode.setText(code + " code is: ");
        studentEmail.setHint(title + " Email");
        addAnotherStudentBtn.setText("Add another " + title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.img:
            case R.id.selectedImage:
                AndroidUtils.selectImage(InviteUser.this, REQUEST_STUDENT_IMAGE);
                break;

            case R.id.imgAdd:
                addStudentBtn();
                break;
        }
    }

    private void addStudentBtn() {
        String parentEmailText = parentEmail.getText().toString().trim();
        String studentEmailText = studentEmail.getText().toString().trim();

        if (studentEmailText.isEmpty()) {
            makeToast("Email is required");
            return;
        }
        if (!ValidationUtils.isValidEmail(studentEmailText)) {
            makeToast("Email is not valid");
            return;
        }
        if (!parentEmailText.isEmpty() && !ValidationUtils.isValidEmail(parentEmailText)) {
            makeToast("Parent email is not valid");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("parentEmail", parentEmailText);

        if (selectedImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            params.put("image", encodedImage);
            params.put("image_name", selectedImageName);
        } else {
            params.put("image", "");
            params.put("image_name", "");
        }

        params.put("status", "1");
        if (requestStudentId != null) {
            params.put("id", requestStudentId);
        }


        // finally send data to server

        if (userRole == UserRole.Teacher) {
            // "classId,classCode,parentEmail,studentEmail,status, image
            params.put("classId", requestClassId);
            params.put("classCode", requestClassCode);
            params.put("studentEmail", studentEmailText);
            new UploadDataAsync().execute(AppConstants.URL_ADD_STUDENT, params);


        } else if (userRole == UserRole.EventHost) {
            // event_id,eventCode,parentEmail,eventeeEmail,status, image
            params.put("event_id", requestClassId);
            params.put("eventCode", requestClassCode);
            params.put("eventeeEmail", studentEmailText);
            new UploadDataAsync().execute(AppConstants.URL_ADD_ATTENDEE_TO_EVENT, params);

        } else if (userRole == UserRole.Manager) {
            //"company_id,companyCode,parentEmail,employeeEmail,status, image
            params.put("company_id", requestClassId);
            params.put("companyCode", requestClassCode);
            params.put("employeeEmail", studentEmailText);
            new UploadDataAsync().execute(AppConstants.URL_ADD_EMPLOYEE_TO_COMPANY, params);

        }
    }


    private class UploadDataAsync extends AsyncTask<Object, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(InviteUser.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String urlString = (String) params[0];
            Map<String, String> keysAndValues = (Map) params[1];
            String result = null;

            DefaultHttpClient dhc = new DefaultHttpClient();
            ResponseHandler<String> res = new BasicResponseHandler();
            HttpPost postMethod = new HttpPost(urlString);
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            for (String key : keysAndValues.keySet()) {
                String value = keysAndValues.get(key);
                nameValuePairs.add(new BasicNameValuePair(key, value));
            }

            try {
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
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
            progressDialog.dismiss();
            JSONObject jObject = null;
            if (s == null) {
                makeToast("Error, Not added!");

            } else {
                try {
                    jObject = new JSONObject(s);

                    // check if result contains Error
                    if (jObject.has("Error")) {
                        makeToast(jObject.getString("Error"));

                    } else if (jObject.has("Message")) {
                        makeToast("Added successfully!");

                        // clear email fields
                        parentEmail.setText("");
                        studentEmail.setText("");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error in parsing data: " + s);
                    Log.e(TAG, e.getMessage());
                }
            }
            if (!isFirstTime) {
                Intent intent = getIntent();
                if (s == null || jObject == null || jObject.has("Error")) {
                    setResult(RESULT_CANCELED, intent);
                } else {
                    intent.putExtra(RESULT_ADDED_STUDENT, s);
                    setResult(RESULT_OK, intent);
                }
                finish();

            }
        }
    }

    public void gotoBack(View view) {
        finish();
    }

    private void makeToast(String title) {
        Toast.makeText(InviteUser.this, title, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_STUDENT_IMAGE) {
            Uri imageUri = data.getData();
            String selectedImagePath = imageUri.getPath();
//            String selectedImagePath = DataUtils.getRealPathFromURI(imageUri, this);
            selectedImageName = DataUtils.getImageNameFromPathPlusName(selectedImagePath);

            try {

                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                selectedImage.setImageBitmap(selectedImageBitmap);
                selectedImage.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
