package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

@SuppressLint("InflateParams")
@SuppressWarnings("unused")
public class HCModuleEmployeeCheckListActivity extends Activity implements View.OnClickListener {

    Button carePlanCheckListButton,webCheckListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_hc_module_check_list);

        carePlanCheckListButton= (Button) findViewById(R.id.carePlanCheckListButton);
        webCheckListButton= (Button) findViewById(R.id.webCheckListButton);

        carePlanCheckListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent carePlanIntent = new Intent(HCModuleEmployeeCheckListActivity.this, HcModule_Employee_Check_List_Activity.class );
                Bundle bun = new Bundle();
                bun.putInt("Index", getIntent().getExtras().getInt("Index"));
                carePlanIntent.putExtras(bun);

                startActivity(carePlanIntent);
            }
        });

    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

