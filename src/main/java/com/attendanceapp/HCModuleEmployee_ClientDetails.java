package com.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.ClientInfoBean;
import com.attendanceapp.models.Employee;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class HCModuleEmployee_ClientDetails extends Activity implements View.OnClickListener, NavigationPage.NavigationFunctions {


    TextView  nameEditText, emailEditText, passwordEditText, companyEditText,locationTextView, answerEditText,
        titleTextView,interests,laundry,shopping,housekeeping,eating,dressing,toileting,bathing,assistive_devices,
    ambulation,livingarrangements_comment,livingarrangements,visualimpairment_comment,visualimpairment,hairloss_comment,
             hairloss,cognitiveloss_comment,cognitiveloss,diagnosis,date,adress,preferred_name,l_name,f_name;

    //For image editing
    ImageView edit_image;


    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;
    SharedPreferences sharedPreferences;
    private String locationID,managerID;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_employee_clientdetails);

         /* nameEditText = (TextView ) findViewById(R.id.nameTextView);
        emailEditText = (TextView ) findViewById(R.id.emailTextView);
        companyEditText = (TextView ) findViewById(R.id.companyTextView);
        locationTextView = (TextView ) findViewById(R.id.locationTextView);*/

        init();

        titleTextView = (TextView) findViewById(R.id.textView1);
        edit_image = (ImageView) findViewById(R.id.client_image);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        sharedPreferences.edit().putBoolean("Image Status", false).commit();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

        Employee employee = userUtils.getUserWithDataFromSharedPrefs(Employee.class);
        locationID = employee.getManagerLocationList().get(0).getLocationId();
        managerID = employee.getManagerLocationList().get(0).getManagerID();
        updateDataAsync();


/*
        if(EmployeeHCDashboardActivity.familyList.size()>0) {
            int last = EmployeeHCDashboardActivity.familyList.size() - 1;
            companyEditText.setVisibility(View.GONE);
            nameEditText.setText(EmployeeHCDashboardActivity.familyList.get(last).getName());
            emailEditText.setText(EmployeeHCDashboardActivity.familyList.get(last).getEmail());
   //        companyEditText.setText(EmployeeHCDashboardActivity.familyList.get(last).getLocationId());
            locationTextView.setText(EmployeeHCDashboardActivity.familyList.get(last).getPhone());
            String picName=AppConstants.IMAGE_BASE_URL+EmployeeHCDashboardActivity.familyList.get(last).getEmail()+".png";
            getImage(picName);

        }*/
    }
    private void getImage(String pic){
        Picasso.with(HCModuleEmployee_ClientDetails.this).load(pic).transform(new CircleTransform()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).placeholder(R.drawable.ico_user).error(R.drawable.ico_user).into(edit_image);
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
    public void gotoBack(View view) {
        onBackPressed();
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
    public void init(){
        interests = (TextView ) findViewById(R.id.interests);
        laundry = (TextView ) findViewById(R.id.laundry);
        shopping = (TextView ) findViewById(R.id.shopping);
        housekeeping = (TextView ) findViewById(R.id.housekeeping);
        eating = (TextView ) findViewById(R.id.eating);
        dressing = (TextView ) findViewById(R.id.dressing);
        toileting = (TextView ) findViewById(R.id.toileting);
        bathing = (TextView ) findViewById(R.id.bathing);
        assistive_devices = (TextView ) findViewById(R.id.assistive_devices);

        ambulation = (TextView ) findViewById(R.id.ambulation);
        livingarrangements_comment = (TextView ) findViewById(R.id.livingarrangements_comment);
        livingarrangements = (TextView ) findViewById(R.id.livingarrangements);
        visualimpairment_comment = (TextView ) findViewById(R.id.visualimpairment_comment);
        visualimpairment = (TextView ) findViewById(R.id.visualimpairment);
        hairloss_comment = (TextView ) findViewById(R.id.hairloss_comment);

        hairloss = (TextView ) findViewById(R.id.hairloss);
        cognitiveloss_comment = (TextView ) findViewById(R.id.cognitiveloss_comment);
        cognitiveloss = (TextView ) findViewById(R.id.cognitiveloss);
        diagnosis = (TextView ) findViewById(R.id.diagnosis);
        date = (TextView ) findViewById(R.id.date);
        adress = (TextView ) findViewById(R.id.adress);
        preferred_name = (TextView ) findViewById(R.id.preferred_name);
        l_name = (TextView ) findViewById(R.id.l_name);
        f_name = (TextView ) findViewById(R.id.f_name);

    }
    private void updateDataAsync(){
        userUtils = new UserUtils(HCModuleEmployee_ClientDetails.this);
        user = userUtils.getUserFromSharedPrefs();
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progressDialog = new ProgressDialog(HCModuleEmployee_ClientDetails.this);
            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {

                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("user_id",managerID);//905
                    hm.put("location_id",locationID);//291
                    try {
                        return new WebUtils().post(AppConstants.URL_GET_CLIENT_INFO_FORM_EMPLOYEE, hm);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();

                if (result == null) {
                } else {
                    try{
                        scrollView.setVisibility(View.VISIBLE);
                        Log.d("Client Info : ",result);
                        JSONObject jsonResponse = new JSONObject(result);
                        JSONArray jsonArray=jsonResponse.getJSONArray("Data");
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        JSONObject client_infos=jsonObject.getJSONObject("client_infos");
                        ClientInfoBean clientInfoBean=new Gson().fromJson(client_infos.toString(),ClientInfoBean.class);

                        setData(clientInfoBean);
                       // getIds();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

        }.execute();
    }


    public void setData(ClientInfoBean data) {

        if(data.getInterests()!=null){
            interests.setText(data.getInterests());
        }else{
            interests.setVisibility(View.GONE);
        }

        if(data.getInterests()!=null){
            laundry.setText("Laundry                 :  "+data.getDressing());
        }else{
            laundry.setVisibility(View.GONE);
        }
        if(data.getShopping()!=null){
            shopping.setText("Shopping              :  "+data.getShopping());
        }else{
            shopping.setVisibility(View.GONE);
        }
        if(data.getHousekeeping()!=null){
            housekeeping.setText("Housekeeping      :  "+data.getHousekeeping());
        }else{
            housekeeping.setVisibility(View.GONE);
        }
        if(data.getEating()!=null){
            eating.setText("Eating                    :  "+data.getEating());
        }else{
            eating.setVisibility(View.GONE);
        }
        if(data.getDressing()!=null){
            dressing.setText("Dressing                :  "+data.getDressing());
        }else{
            dressing.setVisibility(View.GONE);
        }
        if(data.getToileting()!=null){
            toileting.setText("Toileting                :  "+data.getToileting());
        }else{
            toileting.setVisibility(View.GONE);
        }
        if(data.getBathing()!=null){
            bathing.setText("Bathing                  :  "+data.getBathing());
        }else{
            bathing.setVisibility(View.GONE);
        }
        if(data.getAssistive_devices()!=null){
            assistive_devices.setText("Assistive Devices :  "+data.getAssistive_devices());
        }else{
            assistive_devices.setVisibility(View.GONE);
        }
        if(data.getAmbulation()!=null){
            ambulation.setText("Ambulation           :  "+data.getAmbulation());
        }else{
            ambulation.setVisibility(View.GONE);
        }

        if(data.getLivingcomment()!=null){
            livingarrangements_comment.setText("Comment :  "+data.getLivingcomment());
        }else{
            livingarrangements_comment.setVisibility(View.GONE);
        }

        if(data.getLivingarrangements()!=null){
            livingarrangements.setText(data.getLivingarrangements());
        }else{
            livingarrangements.setVisibility(View.GONE);
        }
        if(data.getVisualcomment()!=null){
            visualimpairment_comment.setText("Comment :  "+data.getVisualcomment());
        }else{
            visualimpairment_comment.setVisibility(View.GONE);
        }
        if(data.getVisualimpairment()!=null){
            visualimpairment.setText(data.getVisualimpairment());
        }else{
            visualimpairment.setVisibility(View.GONE);
        }
        if(data.getHairlosscomment()!=null){
            hairloss_comment.setText("Comment : "+data.getAmbulation());
        }else{
            hairloss_comment.setVisibility(View.GONE);
        }
        if(data.getHairloss()!=null){
            hairloss.setText(data.getHairloss());
        }else{
            hairloss.setVisibility(View.GONE);
        }
        if(data.getConetivecomment()!=null){
            cognitiveloss_comment.setText("Comment : "+data.getConetivecomment());
        }else{
            cognitiveloss_comment.setVisibility(View.GONE);
        }
        if(data.getCognitiveloss()!=null){
            cognitiveloss.setText(data.getCognitiveloss());
        }else{
            cognitiveloss.setVisibility(View.GONE);
        }

        if(data.getDiagnosis()!=null){
            diagnosis.setText("Diagnosis            : "+data.getDiagnosis());
        }else{
            diagnosis.setVisibility(View.GONE);
        }

        if(data.getDate()!=null){
            date.setText("Date                     :  "+data.getDate());
        }else{
            date.setVisibility(View.GONE);
        }
        if(data.getAddress()!=null){
            adress.setText("Address               :  "+data.getAddress());
        }else{
            adress.setVisibility(View.GONE);
        }
        if(data.getPreferredname()!=null){
            preferred_name.setText("Preffered Name :  "+data.getPreferredname());
        }else{
            preferred_name.setVisibility(View.GONE);
        }
        if(data.getLastname()!=null){
            l_name.setText("Last Name          :  "+data.getLastname());
        }else{
            l_name.setVisibility(View.GONE);
        }
        if(data.getFirstname()!=null){
            f_name.setText("First Name          :  "+data.getFirstname());
        }else{
            f_name.setVisibility(View.GONE);
        }

    }
}
