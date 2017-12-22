package com.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;


public class UpdatePasswordActivity extends Activity {
    public static final String EXTRA_EMAIL ="EXTRA_EMAIL";
    EditText password;
    Button submit;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        password= (EditText) findViewById(R.id.passwordEditText);
        submit= (Button) findViewById(R.id.submitButton);

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email == null) {
            startActivity(new Intent(UpdatePasswordActivity.this, ForgotPasswordActivity.class));
            finish();
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = password.getText().toString();
                if(pass.isEmpty()) {
                    makeToast("Password must not be empty");
                    return;
                }
                updatePassword(email, pass);
            }
        });
    }

    private void makeToast(String title) {
        Toast.makeText(UpdatePasswordActivity.this, title, Toast.LENGTH_LONG).show();
    }

    private void updatePassword(final String email, final String password) {
            new AsyncTask<Void,Void,Void>(){

                ProgressDialog asyncDialog = new ProgressDialog(UpdatePasswordActivity.this);
                String result;

                @Override
                protected void onPreExecute() {
                    asyncDialog.setMessage("Please wait...");
                    asyncDialog.show();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    HashMap<String ,String > map = new HashMap<>();
                    map.put("email",email);
                    map.put("password",password);

                    try {
                        result = new WebUtils().post(AppConstants.URL_UPDATE_PASSWORD, map);
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
                                makeToast(jsonObject.getString("Password updated successfully"));

                                startActivity(new Intent(UpdatePasswordActivity.this, LoginActivity.class));
                                finish();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }.execute();
    }
}
