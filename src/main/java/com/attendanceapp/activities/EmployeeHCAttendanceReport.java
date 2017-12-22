package com.attendanceapp.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.attendanceapp.AppConstants;
import com.attendanceapp.ManagerHCDashboardActivity;
import com.attendanceapp.R;
import com.attendanceapp.adapters.ExpandableListAdapter;
import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.EmployeeSubAttendance;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.Toast;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.attendanceapp.utils.NavigationPage;
import android.widget.FrameLayout;

public class EmployeeHCAttendanceReport extends Activity {


    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    String locationID,managerID,empID;
    List<String> listDataHeader;
    HashMap<String, List<Attendance>> listDataChild;
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
        setContentView(R.layout.employee_hc_attendance_report);
        locationID = getIntent().getExtras().getString("LocationID");
        managerID = getIntent().getExtras().getString("UserID");
        empID = getIntent().getExtras().getString("EmpID");
        try {
            ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();
        } catch (Exception e){
            e.printStackTrace();
        }


        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();


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


        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
//                updateData();
                return false;
            }
        });
        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listDataHeader.get(groupPosition) + " Expanded",
//                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listDataHeader.get(groupPosition) + " Collapsed",
//                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
//                Toast.makeText(
//                        getApplicationContext(),
//                        listDataHeader.get(groupPosition)
//                                + " : "
//                                + listDataChild.get(
//                                listDataHeader.get(groupPosition)).get(
//                                childPosition), Toast.LENGTH_SHORT)
//                        .show();
                return false;
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Attendance>>();

        // Adding child data
//        listDataHeader.add("RajK");
//        listDataHeader.add("Emp");
//        listDataHeader.add("Ritesh");
        int counter = 0;
        for(int m=0; m<RKSelectEmployeeActivity.rowItems.size(); m++)
        {
            if(RKSelectEmployeeActivity.rowItems.get(m).getIsChecked())
            {
                listDataHeader.add(RKSelectEmployeeActivity.rowItems.get(m).getmEmail()+"  Punctuality: "+RKSelectEmployeeActivity.rowItems.get(m).getPunctuality());


            updateData(RKSelectEmployeeActivity.rowItems.get(m).getEmpID(), counter);
                counter++;
            }
        }

        // Adding child data
//        List<EmployeeSubAttendance> ritesh = new ArrayList<EmployeeSubAttendance>();
//        ritesh.add(new EmployeeSubAttendance("A","2/3/2016"));
//        ritesh.add(new EmployeeSubAttendance("A","2/2/2016"));
//
//
//        List<EmployeeSubAttendance> vicky = new ArrayList<EmployeeSubAttendance>();
//        vicky.add(new EmployeeSubAttendance("A", "2/1/2016"));
//        vicky.add(new EmployeeSubAttendance("A","2/4/2016"));
//
//
//        List<EmployeeSubAttendance> raj = new ArrayList<EmployeeSubAttendance>();
//        raj.add(new EmployeeSubAttendance("A", "2/2/2016"));
//        raj.add(new EmployeeSubAttendance("A", "31/1/2016"));


//        listDataChild.put(listDataHeader.get(0), ritesh); // Header, Child data
//        listDataChild.put(listDataHeader.get(1), vicky);
//        listDataChild.put(listDataHeader.get(2), raj);
    }
    public void gotoBack(View view) {


        super.onBackPressed();
    }



    private void updateData(final String id, final int count) {
        new AsyncTask<Void, Void, String>() {

            ProgressDialog alertDialog = new ProgressDialog(EmployeeHCAttendanceReport.this);

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> map = new HashMap<String, String>();
                // class_id, email
                map.put("user_id", ManagerHCDashboardActivity.user.getUserId());
                map.put("employee_id", id);
                map.put("location_id", locationID);
                try {
                    return new WebUtils().post(AppConstants.URL_GET_ATTENDANCE_OF_EMP, map);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String s) {
                alertDialog.dismiss();
                if (s != null) {

                    final List<Attendance> stringArrayList = DataUtils.getEmployeeAttendanceListFromJsonString(s);


//                    for (Attendance attendance : stringArrayList) {
//                        if (!attendance.isPresent()) {
//                            absentArrayList.add(attendance);
                            listDataChild.put(listDataHeader.get(count), stringArrayList);

//                        }

//                    }

//                    setAdapter();
                }

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

}