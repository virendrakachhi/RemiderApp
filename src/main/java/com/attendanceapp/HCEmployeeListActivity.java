package com.attendanceapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VICKY KUMAR on 04-03-2016.
 */
public class HCEmployeeListActivity extends Activity {
    ListView lv;
    ArrayAdapter adapter;
    private UserUtils userUtils;
    String locationID,empID;
    ImageView backBtn,addEmployeeBtn;
    List<HCEmployee> employeeList;
    List<String> employeeNameList;
    int index;
    ManagerHCClass managerLocData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_module_manager_check_list);
        lv = (ListView)findViewById(R.id.dynamic_checklist);
        backBtn = (ImageView)findViewById(R.id.imgBack);
        addEmployeeBtn = (ImageView)findViewById(R.id.add_account_button);
        backBtn.setVisibility(View.GONE);
        employeeNameList =  new ArrayList<String>();
        ManagerHCDashboardActivity.sharedPreferences.edit().putBoolean("Image Status", false).commit();

        addEmployeeBtn.setVisibility(View.VISIBLE);
        addEmployeeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(HCEmployeeListActivity.this,CreateHCEmployeeActivity.class);
                Bundle bun = new Bundle();
                bun.putInt("EmpIndex", -1);
                bun.putInt("Index", index);
                bun.putString("UserType", "Employee");
                in.putExtras(bun);
                startActivity(in);
                finish();
            }
        });
        TextView title = (TextView)findViewById(R.id.txtTitle);
        title.setText("Choose a Employee");
        index = getIntent().getExtras().getInt("Index");
//        empID = getIntent().getExtras().getString("EmployeeID");
        userUtils = new UserUtils(HCEmployeeListActivity.this);

//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
        employeeList = managerLocData.getEmployeeList();
        if(employeeList.size()>0)
        {
            for(int i=0; i<employeeList.size(); i++)
            {
                employeeNameList.add(employeeList.get(i).getName());
            }

            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,employeeNameList){ @Override
                                                                                                                       public View getView(int position, View convertView,
                                                                                                                                           ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.BLACK);

                return view;
            }});
        }
        else
        {
            Toast.makeText(getApplicationContext(),"No Employee Added yet.",Toast.LENGTH_LONG).show();
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent in = new Intent(HCEmployeeListActivity.this,CreateHCEmployeeActivity.class);
                Bundle bun = new Bundle();
                bun.putInt("EmpIndex", i);
                bun.putInt("Index", index);
                bun.putString("UserType", "Employee");
                in.putExtras(bun);
                startActivity(in);
                finish();
            }
        });

    }
}
