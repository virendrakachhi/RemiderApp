package com.attendanceapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Family;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ritesh.local on 1/22/2016.
 */
public class HCModuleManageShowCheckListCareplan extends Activity {
    String locationID,empID;
    UserUtils userUtils;
    LinearLayout fullLayout;
TextView toileting_ans,toileting_time_edt,toileting_comments_edt,bathing_ans,bathing_shampoo_ans,bathing_shave_ans,bathing_pari_care_ans,bathing_skin_assessment_ans,bathing_time_edt,bathing_comments_edt;
    TextView oral_care_ans,oral_care_edt,oral_care_comments_edt,dressing_ans,dressing_time_edt,dressing_comments_edt,eating_ans,eating_comments_edt,meal_preparation_ans,meal_preparation_comments_edt;
    TextView medication_reminder_ans,medication_reminder_comments_edt,housekeeping_ans,housekeeping_time_edt,housekeeping_comments_edt,shopping_ans,shopping_time_edt,shopping_comments_edt;
    TextView laundry_ans,laundry_time_edt,laundry_comments_edt,socializing_ans,socializing_time_edt,socializing_comments_edt,picture_ans,picture_commens_edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_module_manager_show_checklist_careplan);

       int index = getIntent().getExtras().getInt("Index");
        empID = getIntent().getExtras().getString("EmployeeID");
        Toast.makeText(getApplicationContext(),"Emp == "+empID,Toast.LENGTH_LONG).show();

        userUtils = new UserUtils(HCModuleManageShowCheckListCareplan.this);
        ManagerHCClass teacherClass = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
        locationID = teacherClass.getId();
        shopping_time_edt = (TextView) findViewById(R.id.shopping_time_edt);
        shopping_comments_edt = (TextView) findViewById(R.id.shopping_comments_edt);
        meal_preparation_comments_edt = (TextView) findViewById(R.id.meal_comments_edt);
        eating_ans = (TextView) findViewById(R.id.eating_ans);
        eating_comments_edt = (TextView) findViewById(R.id.eating_comments_edt);
        meal_preparation_ans = (TextView) findViewById(R.id.meal_preparation_ans);
        medication_reminder_ans = (TextView) findViewById(R.id.medication_reminder_ans);
        medication_reminder_comments_edt = (TextView) findViewById(R.id.medication_reminder_comments_edt);
        housekeeping_ans = (TextView) findViewById(R.id.housekeeping_ans);
        housekeeping_time_edt = (TextView) findViewById(R.id.housekeeping_time_edt);
        housekeeping_comments_edt = (TextView) findViewById(R.id.housekeeping_comments_edt);
        shopping_ans = (TextView) findViewById(R.id.shopping_ans);
        laundry_ans = (TextView) findViewById(R.id.laundry_ans);
        laundry_time_edt = (TextView) findViewById(R.id.laundry_time_edt);
        laundry_comments_edt = (TextView) findViewById(R.id.laundry_comments_edt);
        socializing_ans = (TextView) findViewById(R.id.socializing_ans);
        socializing_time_edt = (TextView) findViewById(R.id.socializing_time_edt);
        socializing_comments_edt = (TextView) findViewById(R.id.socializing_comments_edt);
        picture_ans = (TextView) findViewById(R.id.picture_ans);
        picture_commens_edt = (TextView) findViewById(R.id.picture_comments_edt);
        toileting_ans = (TextView) findViewById(R.id.toileting_ans);
        toileting_time_edt = (TextView) findViewById(R.id.toileting_time_edt);
        toileting_comments_edt = (TextView) findViewById(R.id.toileting_comments_edt);
        bathing_ans = (TextView) findViewById(R.id.bathing_ans);
        bathing_shampoo_ans = (TextView) findViewById(R.id.bathing_shampoo_ans);
        bathing_shave_ans = (TextView) findViewById(R.id.bathing_shave_ans);
        bathing_pari_care_ans = (TextView) findViewById(R.id.bathing_pari_care_ans);
        bathing_skin_assessment_ans = (TextView) findViewById(R.id.bathing_skin_assessment_ans);
        bathing_time_edt = (TextView) findViewById(R.id.bathing_time_edt);
        bathing_comments_edt = (TextView) findViewById(R.id.bathing_comments_edt);
        oral_care_edt = (TextView) findViewById(R.id.oral_care_edt);
        oral_care_ans = (TextView) findViewById(R.id.oral_care_ans);
        oral_care_comments_edt = (TextView) findViewById(R.id.oral_care_comments_edt);
        dressing_ans = (TextView) findViewById(R.id.dressing_ans);
        dressing_time_edt = (TextView) findViewById(R.id.dressing_time_edt);
        dressing_comments_edt = (TextView) findViewById(R.id.dressing_comments_edt);
        fullLayout = (LinearLayout)findViewById(R.id.full_layout);
        if(empID!=null) {
//            empID = teacherClass.getEmployeeList().get(teacherClass.getEmployeeList().size()-1).getEmployeeId();
            Toast.makeText(getApplicationContext(),"Emp == "+empID,Toast.LENGTH_LONG).show();
            updateDataAsync();
            fullLayout.setVisibility(LinearLayout.VISIBLE);
        }
        else
        {
            fullLayout.setVisibility(LinearLayout.GONE);
        }







    }


    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                if(empID!=null) {
//                    Toast.makeText(getApplicationContext(),"Inside If",Toast.LENGTH_LONG).show();
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("location_id", locationID);

                    hm.put("emp_id", empID);
                    try {
                        return new WebUtils().post(AppConstants.URL_GET_CHECKLIST_DATA, hm);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
//                    Toast.makeText(getApplicationContext(),"Inside Else",Toast.LENGTH_LONG).show();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

                    try {
                        JSONObject jsn = new JSONObject(result);
                       JSONObject jsnObj = jsn.getJSONObject("Data").getJSONObject("Checklists");
                        toileting_time_edt.setText(jsnObj.getString("toil_times"));
                        toileting_comments_edt.setText(jsnObj.getString("toil_comments"));
                        bathing_comments_edt.setText(jsnObj.getString("bath_comments"));
                        bathing_time_edt.setText(jsnObj.getString("bath_time"));
                        oral_care_comments_edt.setText(jsnObj.getString("oral_comments"));
                        oral_care_edt.setText(jsnObj.getString("oral_time"));
                        dressing_comments_edt.setText(jsnObj.getString("dress_comments"));
                        dressing_time_edt.setText(jsnObj.getString("dress_time"));
                        eating_comments_edt.setText(jsnObj.getString("eat_comments"));
                        eating_ans.setText(jsnObj.getString("eat_time"));
                        meal_preparation_comments_edt.setText(jsnObj.getString("meal_comments"));
                        medication_reminder_comments_edt.setText(jsnObj.getString("medication_comments"));
                        housekeeping_comments_edt.setText(jsnObj.getString("house_comments"));
                        housekeeping_time_edt.setText(jsnObj.getString("house_time"));
                        shopping_comments_edt.setText(jsnObj.getString("shop_comments"));
                        shopping_time_edt.setText(jsnObj.getString("shop_time"));
                        laundry_comments_edt.setText(jsnObj.getString("laundry_comments"));
                        laundry_time_edt.setText(jsnObj.getString("laundry_time"));
                        socializing_comments_edt.setText(jsnObj.getString("social_comments"));
                        socializing_time_edt.setText(jsnObj.getString("social_time"));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                    else {
                    }


            }
        }.execute();

    }

}
