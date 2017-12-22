package com.attendanceapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.RepeatType;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class CustomReportActivity extends Activity {

    private Spinner studentsSpinner;
    private Button calenderButton;
    private ListView list;

    List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_report);

        studentsSpinner = (Spinner) findViewById(R.id.studentsSpinner);
        calenderButton = (Button) findViewById(R.id.calenderButton);
        list = (ListView) findViewById(R.id.list);

        studentsSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);


    }

    public void calenderButtonClicked(final View v) {
        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CustomReportActivity.this);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int monthOfYear = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();

                ((TextView) v).setText("" + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);

            }
        });
        builder.show();
    }


    public void showReport(View v) {
        int selectedItem = studentsSpinner.getSelectedItemPosition();
    }

    final AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            if (position == 0 || position == 4 || position == 5 || position == 6) {
//                thirtyDaysLayout.setVisibility(View.VISIBLE);
//            }
//            if (position == 4) {
//                sevenDaysLayout.setVisibility(View.VISIBLE);
//            }
//
//            switch (position) {
//                case 0:
//                    repeatTypeTextView.setText("Days");
//                    break;
//                case 4:
//                    repeatTypeTextView.setText("Weeks");
//                    break;
//                case 5:
//                    repeatTypeTextView.setText("Months");
//                    break;
//                case 6:
//                    repeatTypeTextView.setText("Years");
//                    break;
//            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

}
