package com.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.Employee;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by VICKY KUMAR on 11-03-2016.
 */
public class HCListOfChecklists extends Activity {
    ListView lv;
    ArrayAdapter adapter;
    ImageView backBtn, addEmployeeBtn;

    String locationID, empID;
    String empName = "";
    boolean editStatus;
    List<String> checklistNameList;
    int index;
    ManagerHCClass managerLocData;
    HCEmployee employeeLocData;
    List<HCEmployee> checklistsList;
    List<String> checklistIsComplete;

    ArrayList<String> scheduleArry = new ArrayList<String>();

    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;
    private User user;
    SharedPreferences sharedPreferences;

    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_module_manager_check_list);
        lv = (ListView) findViewById(R.id.dynamic_checklist);
        backBtn = (ImageView) findViewById(R.id.imgBack);
        addEmployeeBtn = (ImageView) findViewById(R.id.add_account_button);
        addEmployeeBtn.setVisibility(View.GONE);
        checklistsList = new LinkedList<>();
        checklistIsComplete = new ArrayList<String>();
        scheduleArry.clear();
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        userUtils = new UserUtils(HCListOfChecklists.this);
        user = userUtils.getUserFromSharedPrefs();
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(HCListOfChecklists.this);
        empName = "";
        sharedPreferences.edit().putBoolean("Image Status", false).commit();


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
        checklistNameList = new ArrayList<String>();
        index = getIntent().getExtras().getInt("Index");
        editStatus = getIntent().getExtras().getBoolean("Edit");

        userUtils = new UserUtils(HCListOfChecklists.this);
        user = userUtils.getUserFromSharedPrefs();

        if (!editStatus) {
            managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
//        employeeList = managerLocData.getEmployeeList();
            locationID = managerLocData.getId();
            empID = getIntent().getExtras().getString("EmployeeID");
            empName = getIntent().getExtras().getString("EmployeeName");
            txtTitle.setText(empName);
        } else {
            employeeLocData = userUtils.getUserWithDataFromSharedPrefs(Employee.class).getManagerLocationList().get(index);
            locationID = employeeLocData.getLocationId();
            empID = user.getUserId();

        }

//        Date date =  new Date();
//        SimpleDateFormat sdfTime = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
//        String dateString = sdfTime.format(date);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!editStatus) {
                    Intent in = new Intent(HCListOfChecklists.this, HcModule_Manager_Check_List_Activity.class);
                    Bundle bun = new Bundle();
                    bun.putString("EmployeeID", empID);
                    bun.putString("CheckListID", checklistsList.get(i).getCheckListId());
                    bun.putString("locationID", locationID);
                    bun.putString("schedule_id", scheduleArry.get(i));
                    bun.putString("EmployeeName", empName);
                    bun.putInt("Index", i);
                    in.putExtras(bun);
                    startActivity(in);
                } else {

                    if (!checklistIsComplete.get(i).equalsIgnoreCase("1")){
                        Intent checklistIntent = new Intent(HCListOfChecklists.this, HcModule_Employee_Check_List_Activity.class);
                        Bundle bun = new Bundle();
                        bun.putString("CheckListID", checklistsList.get(i).getCheckListId());
                        bun.putString("locationID", locationID);
                        bun.putString("schedule_id", scheduleArry.get(i));
                        checklistIntent.putExtras(bun);
                        startActivity(checklistIntent);
                    } else {
                        Toast.makeText(HCListOfChecklists.this, "Checklist cannot be edited", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


    }


    void setAdapter() {
        lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, checklistNameList) {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
                textView.setPadding(10, 10, 10, 10);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(18);
                return view;
            }
        });

    }

    private void getAssignedChecklists() {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progressDialog = new ProgressDialog(HCListOfChecklists.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();

                hm.put("location_id", locationID);
                hm.put("employee_id", empID);
//                hm.put("location_id","80");
//                hm.put("role", String.valueOf(userRole.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_LOCATION_CHECKLIST, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();
                if (result != null) {
                    try {


                        /*{
                            "Message":"Records archieved successfully",
                                "Data":[
                            {
                                "schedule_checklists":{
                                "id":"26",
                                        "employee_id":"872",
                                        "location_id":"281",
                                        "checklist_name":"Default Checklist",
                                        "date":"6\/27\/2016",
                                        "checklist_id":"a5b9fc92f24c3b1ea2c0c084827d7b60",
                                        "schedule_id":"606"
                            }
                            }
                            ]

                        }*/
                        checklistIsComplete.clear();
                        checklistsList.clear();

                        checklistNameList.clear();
                        JSONObject jsn = new JSONObject(result);
                        if (jsn.getString("Message").matches("All list")) {
                            JSONArray jsnObj = jsn.getJSONArray("data");
                            for (int i = 0; i < jsnObj.length(); i++) {
                                HCEmployee bean = new HCEmployee();
//                                bean.startDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("startDate");
//                                bean.endDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("endDate");
                                bean.setCheckListId(jsnObj.getJSONObject(i).getJSONObject("ec").getString("checklist_id"));
                                bean.setCheckListName(jsnObj.getJSONObject(i).getJSONObject("ec").getString("checklist_name"));
                                bean.setStartDate(jsnObj.getJSONObject(i).getJSONObject("lcd").getString("modified"));

                                checklistsList.add(bean);
                                checklistNameList.add(bean.getCheckListName() + "\n\n" + bean.getStartDate());

                            }
                        } else if (jsn.getString("Message").matches("Records archieved successfully")) {
                            JSONArray jsnObj = jsn.getJSONArray("Data");
                            for (int i = 0; i < jsnObj.length(); i++) {
                                HCEmployee bean = new HCEmployee();
//                                bean.startDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("startDate");
//                                bean.endDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("endDate");
                                bean.setCheckListId(jsnObj.getJSONObject(i).getJSONObject("schedule_checklists").getString("checklist_id"));
                                bean.setCheckListName(jsnObj.getJSONObject(i).getJSONObject("schedule_checklists").getString("checklist_name"));
                                bean.setStartDate(jsnObj.getJSONObject(i).getJSONObject("schedule_checklists").getString("date"));

                                scheduleArry.add(jsnObj.getJSONObject(i).getJSONObject("schedule_checklists").getString("schedule_id"));

                                String dateTimeSchedule = jsnObj.getJSONObject(i).getJSONObject("schedule_checklists").getString("date") + "  " + jsnObj.getJSONObject(i).getJSONObject("employee_schedules").getString("begin_time") + " - " + jsnObj.getJSONObject(i).getJSONObject("employee_schedules").getString("end_time");
                                bean.setStartDate(dateTimeSchedule);
                                checklistsList.add(bean);
                                checklistNameList.add(bean.getCheckListName() + "\n" + bean.getStartDate());
                                checklistIsComplete.add(jsnObj.getJSONObject(i).getJSONObject("schedule_checklists").getString("is_complete"));

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (checklistNameList.size() > 0) {
                    setAdapter();
                }

            }
        }.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
        getAssignedChecklists();
    }

    public void onBackPressed() {
        if (navigationLayout.getVisibility() == View.VISIBLE) {
            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
            navigationLayout.setAnimation(textAnimation);
            navigationLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

}
