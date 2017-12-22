package com.attendanceapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.attendanceapp.AppConstants;
import com.attendanceapp.HcModuleShowClientReportActivity;
import com.attendanceapp.ManagerHCDashboardActivity;
import com.attendanceapp.R;
import com.attendanceapp.adapters.RKSelectEmployeeAdapter;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.RKSelectEmployeeBean;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.attendanceapp.utils.NavigationPage;
import android.widget.FrameLayout;

/**
 * Created by ritesh.local on 2/5/2016.
 */
public class RKSelectEmployeeActivity extends Activity {

   public static List<RKSelectEmployeeBean> rowItems;
    ListView emplistview;
    RKSelectEmployeeAdapter listAdapter;
    Button next;
    String locationID;
    ManagerHCClass managerLocData;

    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rk_select_employees);
        int index = getIntent().getExtras().getInt("Index");

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();
        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
            }
        });


//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);

        locationID = managerLocData.getId();
        next = (Button) findViewById(R.id.select_emp_next_btn);

        rowItems = new ArrayList<RKSelectEmployeeBean>();

//        rowItems.add(0,new RKSelectEmployeeBean("Ritesh","ritesh@mobilyte.com",false));
//        rowItems.add(0,new RKSelectEmployeeBean("Raj","raj@mobilyte.com",false));
//        rowItems.add(0,new RKSelectEmployeeBean("Rahul","rahul@mobilyte.com",false));
//        rowItems.add(0,new RKSelectEmployeeBean("Jagdeep","jagdeep@mobilyte.com",false));

        emplistview = (ListView) findViewById(R.id.select_emp_list);

        listAdapter = new RKSelectEmployeeAdapter(RKSelectEmployeeActivity.this, R.layout.rk_select_employees,rowItems);
        emplistview.setAdapter(listAdapter);

        updateDataAsync();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RKSelectEmployeeActivity.this,EmployeeHCAttendanceReport.class);
                i.putExtra("LocationID",locationID);
                i.putExtra("EmpID",managerLocData.getManagerId());
                i.putExtra("UserID",managerLocData.getEmployeeList().get(managerLocData.getEmployeeList().size()-1).getEmployeeId());

                startActivity(i);
            }
        });
    }
    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            ProgressDialog alertDialog = new ProgressDialog(RKSelectEmployeeActivity.this);

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                //                    Toast.makeText(getApplicationContext(),"Inside If",Toast.LENGTH_LONG).show();
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationID);

                try {
                    return new WebUtils().post(AppConstants.URL_GET_PUNCTUALITY_OF_EMPLOYEES, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                alertDialog.dismiss();
                if (result != null) {

                    try {
                        JSONObject jsn = new JSONObject(result);
                        JSONArray jsnArry = jsn.getJSONArray("Data");
                        if(jsn.getString("Message").matches("Success")) {
                            for (int i = 0; i < jsnArry.length(); i++) {
                                RKSelectEmployeeBean obj = new RKSelectEmployeeBean();

                                obj.setmName(jsnArry.getJSONObject(i).getJSONObject("employee_data").getString("employee_name"));
                                obj.setmEmail(jsnArry.getJSONObject(i).getJSONObject("employee_data").getString("employee_email"));
                                obj.setEmpID(jsnArry.getJSONObject(i).getJSONObject("employee_data").getString("hc_employee_id"));

                                obj.setPunctuality(jsnArry.getJSONObject(i).getString("punctuality"));

                                rowItems.add(obj);

                            }
                           listAdapter.notifyDataSetChanged();
                            emplistview.invalidate();
                            if(rowItems.size()==0)
                            {findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);}
                        //  setAdapter();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                else {
                }


            }
        }.execute();

    }
    public void gotoBack(View view) {


        super.onBackPressed();
    }

   /* private void setAdapter() {
        if (listAdapter == null) {
            listAdapter = new RKSelectEmployeeActivity(RKSelectEmployeeActivity.this, rowItems);
            emplistview.setAdapter(listAdapter);
        } else {
            listAdapter.notifyDataSetChanged();
        }
        }
*/



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




}
