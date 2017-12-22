package com.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.ValidationUtils;
import com.attendanceapp.utils.WebUtils;

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

public class TeacherInviteStudent extends Activity implements View.OnClickListener {

    private static final String TAG = "TeacherInviteStudent";
    public static final String EXTRA_CLASS_CODE = "ClassCode";
    public static final String EXTRA_CLASS_ID = "ClassId";
    public static final String EXTRA_STUDENT_ID = "EXTRA_STUDENT_ID";
    public static final String EXTRA_STUDENT_EMAIL = "EXTRA_STUDENT_EMAIL";
    public static final String EXTRA_PARENT_EMAIL = "EXTRA_PARENT_EMAIL";
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String RESULT_ADDED_STUDENT = "RESULT_ADDED_STUDENT";
    private static final int REQUEST_STUDENT_IMAGE = 234;

    protected TextView classCode, addAnotherStudentBtn, SkipForNowBtn, addStudentButton;
    protected ImageView addStudentBtn, selectedImage, imgHelp;
    private EditText studentEmail, parentEmail;
    Button addImageButton;
    RelativeLayout skipButtonLayout;

    protected String requestClassCode, requestClassId, requestStudentId, requestStudentEmail, selectedImageName;
    ArrayList<String> requestParentEmail;
    private boolean isFirstTime = true, isEditStudent;
    Bitmap selectedImageBitmap;

    /* parent emails */
    LinearLayout parentEmailLayout;
    private boolean showAddPageAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        classCode = (TextView) findViewById(R.id.txtClassCode);
        addAnotherStudentBtn = (TextView) findViewById(R.id.txtAddAnotherStudent);
        SkipForNowBtn = (TextView) findViewById(R.id.txtSkipForNow);
        imgHelp = (ImageView) findViewById(R.id.imgHelp);
        classCode = (TextView) findViewById(R.id.txtClassCode);
        addStudentButton = (TextView) findViewById(R.id.addStudentButton);
        addStudentBtn = (ImageView) findViewById(R.id.imgAdd);
        selectedImage = (ImageView) findViewById(R.id.selectedImage);
        studentEmail = (EditText) findViewById(R.id.editStudentEmail);
        parentEmail = (EditText) findViewById(R.id.editParentEmail);
        addImageButton = (Button) findViewById(R.id.img);
        skipButtonLayout = (RelativeLayout) findViewById(R.id.skipButtonLayout);

        /* parent layout */
        parentEmailLayout = (LinearLayout) findViewById(R.id.parentEmailLayout);
        parentEditTextList.add(parentEmail);

        Intent intent = getIntent();
        requestClassCode = intent.getStringExtra(EXTRA_CLASS_CODE);
        requestClassId = intent.getStringExtra(EXTRA_CLASS_ID);
        requestStudentId = intent.getStringExtra(EXTRA_STUDENT_ID);
        requestStudentEmail = intent.getStringExtra(EXTRA_STUDENT_EMAIL);
        requestParentEmail = intent.getStringArrayListExtra(EXTRA_PARENT_EMAIL);
        isFirstTime = intent.getBooleanExtra(EXTRA_IS_FIRST_TIME, true);

        classCode.setText(classCode.getText() + requestClassCode);
        if (requestStudentEmail != null) {
            studentEmail.setText(requestStudentEmail);
        }
        if (requestParentEmail != null) {
            for (String s : requestParentEmail) {
                createEditTextForParent(s);
            }
        }

        if (!isFirstTime) {
            skipButtonLayout.setVisibility(View.GONE);
        }
        if (requestStudentId != null && !"".equals(requestStudentId)) {
            isEditStudent = true;
        }

        addStudentButton.setOnClickListener(this);
        addStudentBtn.setOnClickListener(this);
        addImageButton.setOnClickListener(this);
        selectedImage.setOnClickListener(this);
        addAnotherStudentBtn.setOnClickListener(this);

        if (isEditStudent) {
            addAnotherStudentBtn.setVisibility(View.GONE);
            addStudentButton.setVisibility(View.GONE);
            skipButtonLayout.setVisibility(View.VISIBLE);
            SkipForNowBtn.setText("Save");
            SkipForNowBtn.setOnClickListener(this);
            imgHelp.setImageResource(R.drawable.delete);
            imgHelp.setOnClickListener(this);
            findViewById(R.id.txtInvite).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.txtInvite)).setText("Edit Student");
        }
    }

    @Override
    public void onClick(View v) {
        showAddPageAgain = false;
        switch (v.getId()) {

            case R.id.img:
            case R.id.selectedImage:
                AndroidUtils.selectImage(TeacherInviteStudent.this, REQUEST_STUDENT_IMAGE);
                break;

            case R.id.imgAdd:
                createEditTextForParent();
                break;

            case R.id.txtAddAnotherStudent:
                showAddPageAgain = true;
                addStudentBtn();
                break;

            case R.id.txtSkipForNow:
            case R.id.addStudentButton:
                addStudentBtn();
                break;

            case R.id.imgHelp:
                deleteStudent();
                break;
        }
    }

    private void deleteStudent() {
        final String url = AppConstants.URL_DELETE_STUDENT_FROM_CLASS;
        final String studentId = requestStudentId;

        new AsyncTask<Void, Void, String>() {
            ProgressDialog progressDialog = new ProgressDialog(TeacherInviteStudent.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = null;

                HashMap<String, String> map = new HashMap<>();
                map.put("student_id", studentId);

                try {
                    result = new WebUtils().post(url, map);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                progressDialog.dismiss();
                JSONObject jObject = null;
                if (s == null) {
                    makeToast("Error in deleting!");

                } else {
                    try {
                        jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else if (jObject.has("Message")) {
                            makeToast("Deleted successfully!");

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error in parsing data: " + s);
                        Log.e(TAG, e.getMessage());
                    }
                }

                Intent intent = getIntent();
                if (s == null || jObject == null || jObject.has("Error")) {
                    setResult(RESULT_CANCELED, intent);
                } else {
                    intent.putExtra(RESULT_ADDED_STUDENT, s);
                    setResult(RESULT_OK, intent);
                }
                finish();

            }
        }.execute();
    }


    private List<EditText> parentEditTextList = new ArrayList<>(1);

    private void createEditTextForParent() {
        createEditTextForParent(parentEmail.getText().toString());
        parentEmail.setText("");
    }

    private void createEditTextForParent(String setText) {

        EditText editText = new EditText(this);
        editText.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        editText.setEms(30);
        editText.setText(setText);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, AndroidUtils.convertPixelsToDp(200, this));
        layoutParams.setMargins(0, AndroidUtils.convertPixelsToDp(40, this), 0, 0);
        editText.setLayoutParams(layoutParams);
        editText.setHint("Parent email");
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setPadding(10, 0, 0, 0);
        editText.setTextColor(getResources().getColor(R.color.white));
        editText.setHintTextColor(getResources().getColor(R.color.white));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        editText.setId(parentEditTextList.size());

        parentEditTextList.add(editText);
        parentEmailLayout.setVisibility(View.VISIBLE);
        parentEmailLayout.addView(editText);

    }

    private void addStudentBtn() {
        String studentEmailText = studentEmail.getText().toString().trim();
        String parentEmailText = "";

        if (studentEmailText.isEmpty()) {
            makeToast("Student email is required");
            return;
        }
        if (!ValidationUtils.isValidEmail(studentEmailText)) {
            makeToast("Student email is not valid");
            return;
        }

        if (parentEditTextList.size() == 1) {
            String text = parentEmail.getText().toString();
            if (!text.isEmpty() && !ValidationUtils.isValidEmail(text)) {
                makeToast("Parent email is not valid");
                return;
            }
        } else {
            for (int i = 0; i < parentEditTextList.size(); i++) {
                String text = parentEditTextList.get(i).getText().toString();

                if (text.isEmpty() || !ValidationUtils.isValidEmail(text)) {
                    makeToast("Parent email is not valid");
                    return;
                } else {
                    parentEmailText += text;
                    parentEmailText += ",";
                }

            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("classId", requestClassId);
        params.put("classCode", requestClassCode);
        params.put("parentEmail", parentEmailText);
        params.put("studentEmail", studentEmailText);

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
        params.put("id", requestStudentId != null ? requestStudentId : "");

        new UploadDataAsync().execute(AppConstants.URL_ADD_STUDENT, params);
    }


    private class UploadDataAsync extends AsyncTask<Object, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(TeacherInviteStudent.this);

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
                makeToast("No student added");

            } else {
                try {
                    jObject = new JSONObject(s);

                    // check if result contains Error
                    if (jObject.has("Error")) {
                        makeToast(jObject.getString("Error"));

                    } else if (jObject.has("Message")) {
                        makeToast("Student added");

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

            if (showAddPageAgain) {
                parentEmailLayout.removeAllViews();
                studentEmail.setText("");
                parentEmail.setText("");

            } else if (!isFirstTime) {
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
        Toast.makeText(TeacherInviteStudent.this, title, Toast.LENGTH_LONG).show();
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
