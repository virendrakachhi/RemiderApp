package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.adapters.HCModuleDayWeekMonthYearListAdapter;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.RepeatType;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;
import com.squareup.timessquare.CalendarPickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.attendanceapp.utils.NavigationPage;
import android.widget.FrameLayout;
@SuppressLint("InflateParams")
@SuppressWarnings("unused")
public class HCModuleFamilyShowAppointmentActivity extends Activity implements View.OnClickListener {

    private static final String TAG = HCModuleFamilyShowAppointmentActivity.class.getSimpleName();
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String EXTRA_EDITING_CLASS_INDEX = "EXTRA_EDITING_CLASS_INDEX";

    private static final int REQUEST_SELECT_LOCATION = 230;
    private static final int REQUEST_SELECT_BEACONS = 231;

    private FrameLayout navigationLayout;

    private Animation textAnimation;

    private String strScheduleDate = "";


    List<HCModuleDayWeekMonthYearViewBean> scheduleList = new LinkedList<>();
    TextView timeButton, dateButton, locationButton, saveButton, txtTitle, monthlyButton, weeklyButton, monthlyDateSchedule;
    LayoutInflater layoutInflater;
    ImageView backButton, navigationButton;
    LinearLayout weeklyView,monthlyView,schedule_view_layout;
    protected SharedPreferences sharedPreferences;
    GPSTracker gpsTracker;

    ClassEventCompany classSetup = new ClassEventCompany();
    private User user;
    protected boolean isFirstTime;
    protected boolean isAddedNewClass, isClassDeleted;
    UserUtils userUtils;

    final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
     Calendar cal ;

    private boolean isEditClass;
    private UserRole userRole;

    CalendarPickerView calendar;
    ArrayList<Date> allDates = new ArrayList<>();
    String locationID,userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_employee_hc_schedule_new);
        setContentView(R.layout.activity_hc_module_family_appointments);
        cal = Calendar.getInstance();
        locationID = getIntent().getExtras().getString("LocationID");
        userType = getIntent().getExtras().getString("UserType");
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        sharedPreferences.edit().putBoolean("Image Status", false).commit();

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
//        navigationButton.setOnClickListener(this);


        updateDataAsync();
        gpsTracker = new GPSTracker(HCModuleFamilyShowAppointmentActivity.this);


        isFirstTime = getIntent().getBooleanExtra(EXTRA_IS_FIRST_TIME, false);

        weeklyButton = (TextView) findViewById(R.id.weeklyButton);
        monthlyButton = (TextView) findViewById(R.id.monthlyButton);

        weeklyButton.setVisibility(View.GONE);
        monthlyButton.setVisibility(View.GONE);
        backButton = (ImageView) findViewById(R.id.backButton);
//        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);


        //   cal.set(2016, 0, 1);

        Date today = new Date();

        calendar.init(today, nextYear.getTime())
                .inMode(CalendarPickerView.SelectionMode.SINGLE);

        weeklyView = (LinearLayout) findViewById(R.id.weeklyView);
        schedule_view_layout = (LinearLayout) findViewById(R.id.schedule_view_layout);
        schedule_view_layout.setVisibility(View.GONE);
        monthlyDateSchedule = (TextView) findViewById(R.id.monthlyDateSchedule);
        monthlyView = (LinearLayout) findViewById(R.id.monthlyView);
//        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                SimpleDateFormat newsdf = new SimpleDateFormat("MM-dd-yyyy");
                String calDate = newsdf.format(calendar.getSelectedDate());
                strScheduleDate = "";
                int month1 = calendar.getSelectedDate().getMonth();
                month1 = month1 + 1;
                int day1 = calendar.getSelectedDate().getDate();
                boolean isScheduleAvailable = false;
                if(scheduleList.size()>0) {
                    for (int k = 0; k < scheduleList.size(); k++) {
//                    String str[] = scheduleList.get(k).date.split("/");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date sDate = sdf.parse(scheduleList.get(k).date);


                            int month2 = sDate.getMonth() + 1;
                            int day2 = sDate.getDate();
//                        Toast.makeText(getApplicationContext(), Integer.toString(calendar.getSelectedDate().getDate()) + "\n" + Integer.toString(calendar.getSelectedDate().getMonth()) + "\n" + Integer.toString(sDate.getMonth())+ "\n" + Integer.toString(sDate.getDate()), Toast.LENGTH_LONG).show();
                            if (month1 == month2) {
                                if (day1 == day2) {
                                    if (userType.matches("Family"))
                                        monthlyDateSchedule.setText("" + " From " + scheduleList.get(k).startTime.toString() + " to " + scheduleList.get(k).endTime.toString() + " with " + scheduleList.get(k).empName);
                                    else {
                                        if (strScheduleDate.trim().length() == 0){
                                            strScheduleDate = " From " + scheduleList.get(k).startTime.toString() + " to " + scheduleList.get(k).endTime.toString() + " at " + scheduleList.get(k).locationName;
                                            monthlyDateSchedule.setText("" + " From " + scheduleList.get(k).startTime.toString() + " to " + scheduleList.get(k).endTime.toString() + " at " + scheduleList.get(k).locationName);
                                            isScheduleAvailable = true;
                                        } else {
                                            strScheduleDate = strScheduleDate + "\n From " + scheduleList.get(k).startTime.toString() + " to " + scheduleList.get(k).endTime.toString() + " at " + scheduleList.get(k).locationName;
                                            monthlyDateSchedule.setText(strScheduleDate);
                                            isScheduleAvailable = true;
                                        }
                                    }

                                } else {

//                                    monthlyDateSchedule.setText("" + calendar.getSelectedDate().toString());
                                    if (!isScheduleAvailable){
                                        monthlyDateSchedule.setText("" + calDate);
                                    }

                                }
                            } else {
//                                monthlyDateSchedule.setText("" + calendar.getSelectedDate().toString());
                                monthlyDateSchedule.setText("" + calDate);

                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
//                    monthlyDateSchedule.setText("" + calendar.getSelectedDate().toString());
                    monthlyDateSchedule.setText("" + calDate);

                }

            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });


//        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
//            @Override
//            public void onDateSelected(Date date) {

//                Log.v("Highlighted ",calendar.getSelectedDate().toString());
//                int day = calendar.getSelectedDate().getDay();
//                int month = calendar.getSelectedDate().getMonth();
//                int  year = calendar.getSelectedDate().getYear();
//                year = year + 1900;
////                Calendar cal1 = Calendar.getInstance();
//                cal.set(year, month, day, 00, 00, 00);
////                Toast.makeText(getApplicationContext(),cal.getTime().toString()+"   "+allDates.get(0).toString()+"   " +String.valueOf(calendar.getSelectedDate().getYear()), Toast.LENGTH_SHORT).show();
//                try {
//                for ( int k=0; k<allDates.size(); k++) {
////                    System.out.println(calendar.getSelectedDate().toString());
////                    System.out.println(dt.toString());
////                    Log.v("Calender", dt.toString());
////                    Log.v("Highlighted ", calendar.getSelectedDate().toString());
//
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//
//                        Date mdate = sdf.parse(allDates.get(k).toString());
//                  int check =  mdate.compareTo(cal.getTime());
//                    if(check==1)
//                    {
//
//                        monthlyDateSchedule.setText("Your Schedule on " + calendar.getSelectedDate().toString() + " Is very hectic");
////                        cal.clear();
//
//                        break;
//
//                    }
//                    else
//                    {
//
//                        monthlyDateSchedule.setText("Your do not have schedule on this date.. ");
//
////                        Toast.makeText(getApplicationContext(),calendar.getSelectedDate().toString(),Toast.LENGTH_LONG).show();
//                    }
//                }
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onDateUnselected(Date date) {
//
//
//            }
//        });



        weeklyButton.setOnClickListener(this);
        monthlyButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        navigationButton.setOnClickListener(this);
    }

    void setScheduleCalender()
    {
        if(scheduleList.size()>0) {

            try {
                int m=0;
                strScheduleDate = "";
//            Toast.makeText(getApplicationContext(),"Size== "+String.valueOf(scheduleList.size()),Toast.LENGTH_LONG).show();
                for (int i = 0; i < scheduleList.size(); i++) {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date crntDate = new Date();
            String crntDateString = sdf.format(crntDate);

                    Date date = sdf.parse(scheduleList.get(i).date.toString());

//                    int crntMonth = crntDate.getMonth();

//                    int scndMonth = date.getMonth();
                    int chk = scheduleList.get(i).date.toString().compareTo(crntDateString);
                    if (date.after(crntDate)) {
                        m=m+1;
                        /*if(m==1) {
//                            if (userType.matches("Family"))
//                                monthlyDateSchedule.setText("Your Meeting date is " + scheduleList.get(i).date.toString() + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " with " + scheduleList.get(i).empName);
//                            else
//                                monthlyDateSchedule.setText("Your Schedule date is " + scheduleList.get(i).date.toString() + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName);

                            if (userType.matches("Family"))
                                monthlyDateSchedule.setText("" + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " with " + scheduleList.get(i).empName);
                            else {
                                if (strScheduleDate.trim().length() == 0){
                                    strScheduleDate = " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName;
                                    monthlyDateSchedule.setText("" + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName);
                                } else {
                                    strScheduleDate = strScheduleDate + "\n From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName;
                                    monthlyDateSchedule.setText(strScheduleDate);
                                }
                            }
//                            else
//                                monthlyDateSchedule.setText("" + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName);


                        }*/
                            allDates.add(date);


                    } else if (chk == 0) {
                        m=m+1;
//                        int m = i;
//                        if (m < scheduleList.size())
//                            m = m + 1;
//                        if(m==1) {
//                            if (userType.matches("Family"))
//                                monthlyDateSchedule.setText("Your Meeting date is " + scheduleList.get(i).date.toString() + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " with " + scheduleList.get(i).empName);
//                            else
//                                monthlyDateSchedule.setText("Your Schedule date is " + scheduleList.get(i).date.toString() + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName);
                            if (userType.matches("Family"))
                                monthlyDateSchedule.setText("" +  " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " with " + scheduleList.get(i).empName);
                            else {
                                if (strScheduleDate.trim().length() == 0){
                                    strScheduleDate = " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName;
                                    monthlyDateSchedule.setText("" + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName);
                                } else {
                                    strScheduleDate = strScheduleDate + "\n From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName;
                                    monthlyDateSchedule.setText(strScheduleDate);
                                }
                            }
                            /*else
                                monthlyDateSchedule.setText("" + " From " + scheduleList.get(i).startTime.toString() + " to " + scheduleList.get(i).endTime.toString() + " at " + scheduleList.get(i).locationName);*/


//                        }
                            allDates.add(date);
                    }



                }


//            int d,m,y;
//            d=new Date().getDay();
//            m=new Date(scheduleList.get(i).startDate).getMonth();
//            y=new Date(scheduleList.get(i).startDate).getYear();


//            cal.set(y, d, m, 00, 00, 00);


//        cal.set(2016, 1, 2, 00, 00, 00);
//        allDates.add(cal.getTime());
//        cal.set(2016, 1, 3, 00, 00, 00);
//        allDates.add(cal.getTime());
                calendar.highlightDates(allDates);
//                calendar.setBackgroundColor(Integer.parseInt("#000000"));
//                List<CalendarCellDecorator> calendarCellDecorators = new ArrayList<CalendarCellDecorator>();
//                calendarCellDecorators.add(new MonthDecorator());
//                calendar.setDecorators(calendarCellDecorators);

         } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
            finally {
                calendar.invalidate();

            }
        } else {
//            monthlyDateSchedule.setText("Your do not have schedule on this date.. ");
    }

    }

    /*public class MonthDecorator implements CalendarCellDecorator {

        @Override
        public void decorate(CalendarCellView calendarCellView, Date date) {
            for (int x = 0; x < allDates.size(); x++)
                if (allDates.get(x).equals(date)){
                    calendarCellView.setBackgroundResource(R.drawable.bluuee);
                    break;
                }
        }
    }*/

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.monthlyButton:
                monthlyView.setVisibility(View.VISIBLE);
                weeklyView.setVisibility(View.GONE);


                monthlyButton.setTextColor(Color.WHITE);
                monthlyButton.setBackgroundColor(Color.parseColor("#2e95d7"));

                weeklyButton.setTextColor(Color.parseColor("#2e95d7"));
                weeklyButton.setBackgroundColor(Color.WHITE);

                break;

            case R.id.weeklyButton:
                weeklyView.setVisibility(View.VISIBLE);
                monthlyView.setVisibility(View.GONE);

                weeklyButton.setTextColor(Color.WHITE);
                weeklyButton.setBackgroundColor(Color.parseColor("#2e95d7"));

                monthlyButton.setTextColor(Color.parseColor("#2e95d7"));
                monthlyButton.setBackgroundColor(Color.WHITE);

                break;


            case R.id.backButton:
                onBackPressed();
                break;


        }

    }


    private void processDateButton() {
        final View view = layoutInflater.inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HCModuleFamilyShowAppointmentActivity.this);
        builder.setTitle("Select Start Date");
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int monthOfYear = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();

                dateButton.setText("" + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
                classSetup.setStartDate(getDateFromDatePicker(datePicker));

                getDateFromUser("Select End Date");

                AlertDialog dialog1 = new AlertDialog.Builder(HCModuleFamilyShowAppointmentActivity.this).setTitle("Select End Date").setView(view).create();
                dialog1.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = datePicker.getYear();
                        int monthOfYear = datePicker.getMonth();
                        int dayOfMonth = datePicker.getDayOfMonth();
                        dateButton.setText(dateButton.getText() + " - " + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
                        classSetup.setEndDate(getDateFromDatePicker(datePicker));
                    }
                });
            }
        });
        builder.show();
    }

    private void processSaveButton() {
        // get data from all fields
        String errorMessage = null;

        // validate all required fields
        if (classSetup.getName() == null || classSetup.getName().length() < 1) {
            errorMessage = "Please enter name";
        } else if (classSetup.getRepeatType() == null) {
            errorMessage = "Please select repeat type";
        } else if ((classSetup.getLatitude() == 0.0d || classSetup.getLongitude() == 0.0d)
                && "".equals(classSetup.getBeaconsJsonString())) {
            errorMessage = "Please select location";
        }

//        if (userRole != UserRole.Manager) {
        if ((classSetup.getStartTime() == null || classSetup.getEndTime() == null)) {
            errorMessage = "Please select start and end time";
        } else if (classSetup.getStartDate() == null || classSetup.getEndDate() == null) {
            errorMessage = "Please select start and end date";
        }
//        }

//
//        if (errorMessage != null) {
//            makeToast(errorMessage);
//            return;
//        }

        // validate for correct values
//                if (classSetup.getStartTime().after(classSetup.getEndTime()) || classSetup.getStartTime().equals(classSetup.getEndTime())) {
//                    errorMessage = "Start time should be less than end time";
//                } else if (classSetup.getStartDate().get(Calendar.MONTH) < Calendar.getInstance().get(Calendar.MONTH)
//                        || classSetup.getStartDate().get(Calendar.YEAR) < Calendar.getInstance().get(Calendar.YEAR)
//                        || classSetup.getStartDate().get(Calendar.DATE) < Calendar.getInstance().get(Calendar.DATE)) {
//                    errorMessage = "Start date should not be before than current date";
//                } else if (classSetup.getStartDate().equals(classSetup.getEndDate()) || classSetup.getStartDate().after(classSetup.getEndDate())) {
//                    errorMessage = "Start date should be less than end date";
//                } else
//        if (classSetup.getRepeatType().equals(RepeatType.REPEAT_DAY) && classSetup.getRepeatDays().isEmpty()) {
//            errorMessage = "Please select repeat days";
//        } else if (classSetup.getRepeatType().equals(RepeatType.REPEAT_DATE) && classSetup.getRepeatDates().isEmpty()) {
//            errorMessage = "Please select repeat dates";
//        }

        // submit data if valid
        if (errorMessage != null) {
            makeToast(errorMessage);
        } else {
            //Log.i(TAG, classSetup.toString());

            String formatForTime = "%d:%d";
            String formatForDate = "%d/%d/%d";
            Calendar calendar;

            Map<String, String> keysAndValues = new HashMap<>();

            keysAndValues.put("user_id", user.getUserId());
            keysAndValues.put("status", "1");

            if (isEditClass) {
                keysAndValues.put("id", classSetup.getId());
            }

            if (userRole == UserRole.Manager) {
                keysAndValues.put("companyName", classSetup.getName());
            } else if (userRole == UserRole.EventHost) {
                keysAndValues.put("eventName", classSetup.getName());
            } else {
                keysAndValues.put("className", classSetup.getName());
            }

//            if (userRole != UserRole.Manager) {
            calendar = classSetup.getStartTime();
            keysAndValues.put("startTime", String.format(formatForTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            calendar = classSetup.getEndTime();
            keysAndValues.put("endTime", String.format(formatForTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            calendar = classSetup.getStartDate();
            keysAndValues.put("startDate", String.format(formatForDate, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
            calendar = classSetup.getEndDate();
            keysAndValues.put("endDate", String.format(formatForDate, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
//            }

            keysAndValues.put("latitude", String.valueOf(classSetup.getLatitude()));
            keysAndValues.put("longitude", String.valueOf(classSetup.getLongitude()));
            keysAndValues.put("repeatType", classSetup.getRepeatType().toString());
            keysAndValues.put("repeatDays", classSetup.getRepeatDays().toString());
            keysAndValues.put("interval", String.valueOf(classSetup.getInterval()));
            keysAndValues.put("beacon_id", classSetup.getBeaconsJsonString());

            if (classSetup.getRepeatType().equals(RepeatType.WEEKLY)) {
                String s = classSetup.getRepeatDays().toString();
                s = s.substring(1, s.length() - 1);
                keysAndValues.put("repeatDays", s);
            }

            if (classSetup.getDistrict() != null) {
                keysAndValues.put("district", classSetup.getDistrict());
            }

            if (classSetup.getCode() != null) {
                keysAndValues.put("code", classSetup.getCode());
            }

            Log.i(TAG, keysAndValues.toString());
            // user_id,className,startTime,endTime,startDate,endDate,repeatType,repeatDays(if values of repeatType is WEEKLY),interval,district,code,latitude,longitude,status
            // finally upload data to server using async task


            if (userRole == UserRole.Manager) {
                UploadDataAsync(AppConstants.URL_CREATE_COMPANY, keysAndValues);
            } else if (userRole == UserRole.EventHost) {
                UploadDataAsync(AppConstants.URL_CREATE_EVENT, keysAndValues);
            } else {
                UploadDataAsync(AppConstants.URL_CREATE_CLASS, keysAndValues);
            }

        }
    }

    private void UploadDataAsync(final String url, final Map<String, String> keysAndValues) {
        new AsyncTask<Object, Void, String>() {
            ProgressDialog alertDialog = new ProgressDialog(HCModuleFamilyShowAppointmentActivity.this);

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Object... params) {
                try {

                    return new WebUtils().post(url, keysAndValues);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                alertDialog.dismiss();
                alertDialog.cancel();

                if (s == null) {
                    makeToast("Error in uploading data");
                } else {
                    try {
                        JSONObject jObject = new JSONObject(s);

                        // check if result contains Error
                        if (jObject.has("Error")) {
                            makeToast(jObject.getString("Error"));

                        } else {
                            makeToast("Saved successfully!");
                            isAddedNewClass = true;
                            onBackPressed();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error in parsing data: " + s);
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }.execute();
    }

    private void makeToast(String title) {
        Toast.makeText(HCModuleFamilyShowAppointmentActivity.this, title, Toast.LENGTH_LONG).show();
    }

    private void getTimeFromUser(String title) {
        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HCModuleFamilyShowAppointmentActivity.this);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int hourOfDay = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String state = "am";
                if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    state = "pm";
                } else if (hourOfDay == 0) {
                    hourOfDay = 12;
                } else if (hourOfDay == 12) {
                    state = "pm";
                }
                String minuteString = String.valueOf(minute).length() < 2 ? "0" + minute : "" + minute;
                timeButton.setText(timeButton.getText() + "  -  " + hourOfDay + ":" + minuteString + " " + state);
                classSetup.setEndTime(getCalenderFromTimePicker(timePicker));

            }
        });
        builder.show();
    }

    private void getDateFromUser(String title) {
        final View view = layoutInflater.inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HCModuleFamilyShowAppointmentActivity.this);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int monthOfYear = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();
                dateButton.setText(dateButton.getText() + " - " + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
                classSetup.setEndDate(getDateFromDatePicker(datePicker));
            }
        });
        builder.show();
    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    public static Calendar getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar;
    }

    public static Calendar getCalenderFromTimePicker(TimePicker timePicker) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());

        return calendar;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_LOCATION) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra(SelectLocationActivity.EXTRA_LATITUDE, 0.0);
                double lng = data.getDoubleExtra(SelectLocationActivity.EXTRA_LONGITUDE, 0.0);

                classSetup.setLatitude(lat);
                classSetup.setLongitude(lng);

                locationButton.setText(userUtils.getAddressString(userUtils.getAddress(lat, lng)));
            }
        } else if (requestCode == REQUEST_SELECT_BEACONS) {
            if (resultCode == RESULT_OK) {
                classSetup.setBeaconsJsonString(data.getStringExtra(TeacherSelectBeaconActivity.RESPONSE_SELECTED_BEACONS));
            } else {
                classSetup.setBeaconsJsonString("");
            }
        }
    }


    protected void openActivity(Class aClass) {
        startActivity(new Intent(HCModuleFamilyShowAppointmentActivity.this, aClass));
        finish();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progressDialog = new ProgressDialog(HCModuleFamilyShowAppointmentActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id",locationID);
                if(!userType.matches("Family"))
                {
                    hm.put("employee_id",user.getUserId());
                }
//                hm.put("location_id","80");
//                hm.put("role", String.valueOf(userRole.getRole()));
                try {
                    if(userType.matches("Family")) {
                        return new WebUtils().post(AppConstants.URL_GET_EMPLOYEE_SCHEDULE, hm);
                    }
                    else
                    {
                        return new WebUtils().post(AppConstants.URL_GET_EMPLOYEE_SINGLE_SCHEDULE, hm);

                    }
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

                        JSONObject jsn = new JSONObject(result);
                        if(jsn.getString("Message").matches("Records Found successfully"))
                        {
                        JSONArray jsnObj = jsn.getJSONArray("Data");
                        for(int i=0; i<jsnObj.length(); i++)
                        {
                            HCModuleDayWeekMonthYearViewBean bean = new HCModuleDayWeekMonthYearViewBean();
                            bean.date= jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("date");

//                            bean.startDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("startDate");
//                            bean.endDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("endDate");
                            bean.startTime = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("begin_time");
                            bean.endTime = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("end_time");
                            bean.locationID = jsnObj.getJSONObject(i).getJSONObject("Location").getString("id");
                            bean.locationName = jsnObj.getJSONObject(i).getJSONObject("Location").getString("locationName");
                            bean.empName = jsnObj.getJSONObject(i).getJSONObject("User").getString("username");

                            scheduleList.add(bean);

                        }}
                        else
                        {
                            scheduleList.add(new HCModuleDayWeekMonthYearViewBean());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                setScheduleCalender();
//                openActivity(EventHost_DashboardActivity.class);
//                finish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

