package com.attendanceapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class ForgotPasswordActivity extends Activity implements View.OnClickListener {
    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();

    protected EditText emailEditText, securityQuestionAnswer, codeEditText;
    protected Button havePasswordCode, submitButton;
    private boolean isCodeLayoutShown, isEmailLayoutShown, isAskQuestionLayoutShown;
    LinearLayout codeLayout, askQuestionLayout, emailLayout;
    TextView securityQuestionTextView;
    String securityQuestion, email;

    //A Code has been sent to your email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        securityQuestionAnswer = (EditText) findViewById(R.id.securityQuestionAnswer);
        codeEditText = (EditText) findViewById(R.id.codeEditText);
        submitButton = (Button) findViewById(R.id.submitButton);
        havePasswordCode = (Button) findViewById(R.id.havePasswordCode);
        codeLayout = (LinearLayout) findViewById(R.id.codeLayout);
        askQuestionLayout = (LinearLayout) findViewById(R.id.askQuestionLayout);
        emailLayout= (LinearLayout) findViewById(R.id.emailLayout);
        securityQuestionTextView= (TextView) findViewById(R.id.securityQuestion);

        submitButton.setOnClickListener(this);
        havePasswordCode.setOnClickListener(this);

    }

    private void showCodeLayout() {
        havePasswordCode.setText("Don't have password code?");
        codeLayout.setVisibility(View.VISIBLE);
        askQuestionLayout.setVisibility(View.GONE);
        emailLayout.setVisibility(View.GONE);
        isCodeLayoutShown = true;
        isAskQuestionLayoutShown = false;
        isEmailLayoutShown = false;
    }

    private void showEmailLayout() {
        emailLayout.setVisibility(View.VISIBLE);
        codeLayout.setVisibility(View.GONE);
        askQuestionLayout.setVisibility(View.GONE);
        isEmailLayoutShown = true;
        isCodeLayoutShown = false;
        isAskQuestionLayoutShown = false;
        havePasswordCode.setText("Have a password code?");
    }

    private void showAskQuestionLayout() {
        askQuestionLayout.setVisibility(View.VISIBLE);
        codeLayout.setVisibility(View.GONE);
        emailLayout.setVisibility(View.GONE);
        isEmailLayoutShown = false;
        isCodeLayoutShown = false;
        isAskQuestionLayoutShown = true;
        havePasswordCode.setText("Have a password code?");
    }

    @Override
    public void onClick(View v) {
        if (R.id.submitButton == v.getId()) {

            if (isEmailLayoutShown) {
                email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    makeToast("Please enter email");
                    return;
                }
                verifyEmail(email);


            } else if (isCodeLayoutShown) {
                if (codeEditText.getText().toString().isEmpty()) {
                    makeToast("Please enter code");
                    return;
                }
                matchCode(codeEditText.getText().toString());


            } else if(isAskQuestionLayoutShown){
                if (securityQuestionAnswer.getText().toString().isEmpty()) {
                    makeToast("Please enter answer");
                    return;
                }
                processSecurityQuestion(email, securityQuestion, securityQuestionAnswer.getText().toString());
            }
        }

        if (R.id.havePasswordCode == v.getId()) {
            if (isCodeLayoutShown) {
                showEmailLayout();
            } else {
                showCodeLayout();
            }
        }

    }

    private void verifyEmail(final String email) {
        new AsyncTask<Void, Void, Void>() {

            ProgressDialog asyncDialog = new ProgressDialog(ForgotPasswordActivity.this);
            String result = null;

            @Override
            protected void onPreExecute() {
                asyncDialog.setMessage("Please wait...");
                asyncDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", email);

                try {
                    result = new WebUtils().post(AppConstants.URL_FORGOT_PASSWORD_VERIFY_EMAIL, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();

                if (result == null) {
                    makeToast("Error in connection");
                } else {
                    try {

                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.has("Error")) {
                            makeToast(jsonObject.getString("Error"));
                        } else {

//                            {
//                                "Message": "Successfully find your question",
//                                    "Data": {
//                                "User": {
//                                    "question": "What is your pet name?"
//                                }
//                            }
//                            }

                            JSONObject dataObject = jsonObject.getJSONObject("Data");
                            JSONObject userObject = dataObject.getJSONObject("User");
                            securityQuestion = userObject.getString("question");
                            securityQuestionTextView.setText(securityQuestion);
                            showAskQuestionLayout();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.execute();
    }
    private void matchCode(final String securityCode) {
        new AsyncTask<Void, Void, Void>() {

            ProgressDialog asyncDialog = new ProgressDialog(ForgotPasswordActivity.this);
            String result = null;

            @Override
            protected void onPreExecute() {
                asyncDialog.setMessage("Please wait...");
                asyncDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<>();
                map.put("code", securityCode);

                try {
                    result = new WebUtils().post(AppConstants.URL_FORGOT_PASSWORD_VERIFY_CODE, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();

                if (result == null) {
                    makeToast("Error in connection");
                } else {
                    try {

                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.has("Error")) {
                            makeToast(jsonObject.getString("Error"));
                        } else {

                            Intent intent = new Intent(ForgotPasswordActivity.this, UpdatePasswordActivity.class);
                            intent.putExtra(UpdatePasswordActivity.EXTRA_EMAIL, result);
                            startActivity(intent);
                            finish();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.execute();
    }

    private void processSecurityQuestion(final String email, final String securityQuestion, final String securityQuestionAnswer) {

        new AsyncTask<Void, Void, Void>() {

            ProgressDialog asyncDialog = new ProgressDialog(ForgotPasswordActivity.this);
            String result = null;

            @Override
            protected void onPreExecute() {
                asyncDialog.setMessage("Please wait...");
                asyncDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", email);
                map.put("security_question", securityQuestion);
                map.put("security_question_answer", securityQuestionAnswer);

                try {
                    result = new WebUtils().post(AppConstants.URL_FORGOT_PASSWORD_VERIFY_QUESTION, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();

                if (result == null) {
                    makeToast("Error in connection");
                } else {
                    try {

                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.has("Error")) {
                            makeToast(jsonObject.getString("Error"));
                        } else {

                            new AlertDialog.Builder(ForgotPasswordActivity.this)
                                    .setMessage("A Code has been sent to your email")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            showCodeLayout();
                                        }
                                    }).create().show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.execute();

    }

//    private class ProcessForgotPasswordAsync extends AsyncTask<Void, Void, String> {
//        String url, email, securityQuestion, securityQuestionAnswer;
//
//        public ProcessForgotPasswordAsync(String url, String email, String securityQuestion, String securityQuestionAnswer) {
//            this.url = url;
//            this.email = email;
//            this.securityQuestion = securityQuestion;
//            this.securityQuestionAnswer = securityQuestionAnswer;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            String result = null;
//
//            DefaultHttpClient dhc = new DefaultHttpClient();
//            ResponseHandler<String> res = new BasicResponseHandler();
//            HttpPost postMethod = new HttpPost(url);
//
//            List<NameValuePair> namepairs = new ArrayList<NameValuePair>();
//
//            namepairs.add(new BasicNameValuePair("email", email));
//            namepairs.add(new BasicNameValuePair("security_question", securityQuestion));
//            namepairs.add(new BasicNameValuePair("security_question_answer", securityQuestionAnswer));
//
//            try {
//                postMethod.setEntity(new UrlEncodedFormEntity(namepairs));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            try {
//                result = dhc.execute(postMethod, res);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if (s == null) {
//                makeToast("Error in connection!");
//
//            } else {
//                try {
//                    JSONObject jObject = new JSONObject(s);
//
//                    // check if result contains Error
//                    if (jObject.has("Error")) {
//                        makeToast("Error! Please enter correct values");
//
//                    } else {
//
//                        makeToast("Error! Please enter correct values");
//
//                        new AlertDialog.Builder(ForgotPasswordActivity.this)
//                                .setMessage("Code has been sent to your email")
//                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                        finish();
//                                    }
//                                }).create().show();
//
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "Error in parsing data: " + s);
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        }
//    }


    private void makeToast(String title) {
        Toast.makeText(ForgotPasswordActivity.this, title, Toast.LENGTH_LONG).show();
    }

    public void gotoBack(View view) {
        finish();
    }
}
