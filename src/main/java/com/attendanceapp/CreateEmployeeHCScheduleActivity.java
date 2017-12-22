package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.attendanceapp.webserviceCommunicator.WebServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("InflateParams")
@SuppressWarnings("unused")
public class CreateEmployeeHCScheduleActivity extends Activity implements View.OnClickListener {

    private static final String TAG = CreateEmployeeHCScheduleActivity.class.getSimpleName();
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String EXTRA_EDITING_CLASS_INDEX = "EXTRA_EDITING_CLASS_INDEX";
    private static final int REQUEST_SELECT_LOCATION = 230;
    private static final int REQUEST_SELECT_BEACONS = 231;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;

    TextView timeButton, dateButton, locationButton, saveButton, txtTitle;
    LayoutInflater layoutInflater;
    protected SharedPreferences sharedPreferences;
    GPSTracker gpsTracker;
    String locationID;
    ManagerHCClass managerLocData;
    ClassEventCompany classSetup = new ClassEventCompany();
    private User user;
    protected boolean isFirstTime;
    protected boolean isAddedNewClass, isClassDeleted;
    UserUtils userUtils;
    String employeeID;
    final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private boolean isEditClass;
    private UserRole userRole;
    private Spinner spnCheckList;
    ArrayAdapter<String> spinnerAdapter;
    private List<String> listCheckListName = new ArrayList<String>();
    private List<String> listCheckListID = new ArrayList<String>();
    private static String checklistID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_hc_schedule);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        sharedPreferences.edit().putBoolean("Image Status", false).commit();
        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

        spnCheckList = (Spinner) findViewById(R.id.spinner_select_checklist);

        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.selecter_item_radius, listCheckListName);
//        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.radius_array, R.layout.selecter_item_radius);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCheckList.setAdapter(new NothingSelectedSpinnerAdapter(spinnerAdapter, R.layout.checklist_spinner_row, this));
//        spnCheckList.setAdapter(spinnerAdapter);

//        gpsTracker = new GPSTracker(CreateEmployeeHCScheduleActivity.this);
//
//
//        isFirstTime = getIntent().getBooleanExtra(EXTRA_IS_FIRST_TIME, false);
        int index = getIntent().getExtras().getInt("Index");

//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);

        locationID = managerLocData.getId();

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        timeButton = (TextView) findViewById(R.id.timeField);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        dateButton = (TextView) findViewById(R.id.dateField);

        saveButton = (Button) findViewById(R.id.saveButton);

        timeButton.setOnClickListener(this);
        dateButton.setOnClickListener(this);

        saveButton.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();

        String SelectedUser = bundle.getString("SelectedUser");
        employeeID = bundle.getString("EmployeeID");
        txtTitle.setText(SelectedUser);

        /*userRole = UserRole.valueOf(bundle.getInt(AppConstants.EXTRA_USER_ROLE, -1));
        setRoleBasedProperties(userRole);

        int index = bundle.getInt(EXTRA_EDITING_CLASS_INDEX, -1);
        if (index != -1) {
            ClassEventCompany teacherClass = user.getClassEventCompanyArrayList().get(index);
            isEditClass = true;

            classSetup.setId(teacherClass.getId());
            classSetup.setName(teacherClass.getName());
            classSetup.setDistrict(teacherClass.getDistrict());
            classSetup.setCode(teacherClass.getCode());
            classSetup.setInterval(teacherClass.getInterval());
            classSetup.setBeaconList(teacherClass.getBeaconList());
            classSetup.setLatitude(teacherClass.getLatitude());
            classSetup.setLongitude(teacherClass.getLongitude());
            classSetup.setRepeatType(teacherClass.getRepeatType());
            classSetup.setStartTime(teacherClass.getStartTime());
            classSetup.setEndTime(teacherClass.getEndTime());
            classSetup.setStartDate(teacherClass.getStartDate());
            classSetup.setEndDate(teacherClass.getEndDate());


            timeButton.setText(teacherClass.getStartTime() + " - " + teacherClass.getEndTime());

            Calendar cal;
            cal = classSetup.getStartDate();
            dateButton.setText("" + MONTHS[cal.get(Calendar.MONTH) + 1] + " "
                    + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR));
            cal = classSetup.getEndDate();
            dateButton.setText(dateButton.getText() + " - " + MONTHS[cal.get(Calendar.MONTH) + 1]
                    + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR));


            }
*/

        new AsyncGetCheckList().execute(locationID);
    }


    String title, name, code;

    private void setRoleBasedProperties(UserRole userRole) {

        if (userRole != null) {

            if (userRole == UserRole.Teacher) {
                title = "Class Setup";
                name = "Class name";

            } else if (userRole == UserRole.EventHost) {
                title = "Event Setup";
                name = "Event name";

            } else if (userRole == UserRole.Manager) {
                title = "Meeting Place";
                name = "Meeting Place";
                code = "Company Code";


            }


        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timeField:
                processTimeButton();
                break;
            case R.id.dateField:
                processDateButton();
                break;
            case R.id.saveButton:
                processSaveButton();
                break;


            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;

        }
    }

    private void processTimeButton() {

        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateEmployeeHCScheduleActivity.this);
        builder.setTitle("Start Time");
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
                timeButton.setText("" + hourOfDay + ":" + minuteString + " " + state);
                classSetup.setStartTime(getCalenderFromTimePicker(timePicker));
                getTimeFromUser("End Time");
            }
        });
        builder.show();

    }

    private void processDateButton() {
        final View view = layoutInflater.inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateEmployeeHCScheduleActivity.this);
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

//                getDateFromUser("Select End Date");
//
//                AlertDialog dialog1 = new AlertDialog.Builder(CreateEmployeeHCScheduleActivity.this).setTitle("Select End Date").setView(view).create();
//                dialog1.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        int year = datePicker.getYear();
//                        int monthOfYear = datePicker.getMonth();
//                        int dayOfMonth = datePicker.getDayOfMonth();
//                        dateButton.setText(dateButton.getText() + " - " + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
//                        classSetup.setEndDate(getDateFromDatePicker(datePicker));
//                    }
//                });
            }
        });
        builder.show();
    }

    private void processSaveButton() {
        // get data from all fields
        String errorMessage = null;

        // validate all required fields
//        if (classSetup.getName() == null || classSetup.getName().length() < 1) {
//            errorMessage = "Please enter name";
//        } else if (classSetup.getRepeatType() == null) {
//            errorMessage = "Please select repeat type";
//        } else if ((classSetup.getLatitude() == 0.0d || classSetup.getLongitude() == 0.0d)
//                && "".equals(classSetup.getBeaconsJsonString())) {
//            errorMessage = "Please select location";
//        }

//        if (userRole != UserRole.Manager) {
        if ((classSetup.getStartTime() == null || classSetup.getEndTime() == null)) {
            errorMessage = "Please select start and end time";
//        } else if (classSetup.getStartDate() == null || classSetup.getEndDate() == null) {
//            errorMessage = "Please select start and end date";
//        }
        } else if (classSetup.getStartDate() == null) {
            errorMessage = "Please select date";
        } else {
            if (spnCheckList.getSelectedItemPosition() != 0) {
                checklistID = listCheckListID.get(spnCheckList.getSelectedItemPosition()-1);
            } else {
                errorMessage = "Please select checklist";
            }
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

            keysAndValues.put("manager_id", user.getUserId());
            keysAndValues.put("location_id", locationID);

            keysAndValues.put("emp_id", employeeID);
            keysAndValues.put("checklist_id", checklistID);

//            if (userRole != UserRole.Manager) {
            calendar = classSetup.getStartTime();
            keysAndValues.put("startTime", String.format(formatForTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            calendar = classSetup.getEndTime();
            keysAndValues.put("endTime", String.format(formatForTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            calendar = classSetup.getStartDate();
            keysAndValues.put("date", String.format(formatForDate, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
//            calendar = classSetup.getEndDate();
//            keysAndValues.put("endDate", String.format(formatForDate, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
//            }

//            keysAndValues.put("latitude", String.valueOf(classSetup.getLatitude()));
//            keysAndValues.put("longitude", String.valueOf(classSetup.getLongitude()));
//            keysAndValues.put("repeatType", classSetup.getRepeatType().toString());
//            keysAndValues.put("repeatDays", classSetup.getRepeatDays().toString());
//            keysAndValues.put("interval", String.valueOf(classSetup.getInterval()));
//            keysAndValues.put("beacon_id", classSetup.getBeaconsJsonString());

//            if (classSetup.getRepeatType().equals(RepeatType.WEEKLY)) {
//                String s = classSetup.getRepeatDays().toString();
//                s = s.substring(1, s.length() - 1);
//                keysAndValues.put("repeatDays", s);
//            }
//
//            if (classSetup.getDistrict() != null) {
//                keysAndValues.put("district", classSetup.getDistrict());
//            }
//
//            if (classSetup.getCode() != null) {
//                keysAndValues.put("code", classSetup.getCode());
//            }

            Log.i(TAG, keysAndValues.toString());
            // user_id,className,startTime,endTime,startDate,endDate,repeatType,repeatDays(if values of repeatType is WEEKLY),interval,district,code,latitude,longitude,status
            // finally upload data to server using async task


            UploadDataAsync(AppConstants.URL_SCHEDULE_EMPLOYEE, keysAndValues);

        }
    }

    private void UploadDataAsync(final String url, final Map<String, String> keysAndValues) {
        new AsyncTask<Object, Void, String>() {
            ProgressDialog alertDialog = new ProgressDialog(CreateEmployeeHCScheduleActivity.this);

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
//                            isAddedNewClass = true;
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
        Toast.makeText(CreateEmployeeHCScheduleActivity.this, title, Toast.LENGTH_LONG).show();
    }

    private void getTimeFromUser(String title) {
        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateEmployeeHCScheduleActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateEmployeeHCScheduleActivity.this);
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
        startActivity(new Intent(CreateEmployeeHCScheduleActivity.this, aClass));
        finish();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progressDialog = new ProgressDialog(CreateEmployeeHCScheduleActivity.this);

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(userRole.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();
                if (result != null) {

                    User teacher = user;
                    ArrayList<ClassEventCompany> teacherClasses = DataUtils.getClassEventCompanyArrayListFromJsonString(result);

                    if (teacher.getClassEventCompanyArrayList().size() != teacherClasses.size()) {

                        teacher.getClassEventCompanyArrayList().clear();
                        teacher.getClassEventCompanyArrayList().addAll(teacherClasses);

                        userUtils.saveUserWithDataToSharedPrefs(teacher, User.class);

                    }
                }
                openActivity(EventHost_DashboardActivity.class);
                finish();
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
        } else {
            super.onBackPressed();
        }
    }


    private class AsyncGetCheckList extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog = new ProgressDialog(CreateEmployeeHCScheduleActivity.this);
        String response = "";

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("location_id", strings[0]));

            response = new WebServiceHandler().webServiceCall(nameValuePairs, AppConstants.URL_GET_ASSIGNED_CHECKLISTS);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            /*{
                "Message":"All list",
                    "data":[
                {
                    "ec":{
                    "id":"7157",
                            "first_name":"Default List",
                            "location_id":"281",
                            "key":"Ambulation",
                            "value":"1",
                            "checklist_id":"3ed261530d7f5eba0d9eaa6f1e1ef561",
                            "yes_no":true,
                            "na":true,
                            "comment":true,
                            "time_range":false,
                            "user_id":"568"
                },
                    "lcd":{
                    "modified":"2016-06-15 04:24:22"
                }
                }
                ]

            }*/

            try {
                JSONObject objectResponse = new JSONObject(s);
                JSONArray arrayCheckList = objectResponse.optJSONArray("data");
                for (int i = 0; i < arrayCheckList.length(); i++) {
                    JSONObject objectEC = arrayCheckList.optJSONObject(i);
                    JSONObject objectDetail = objectEC.optJSONObject("location_checklists");
                    String checkListId = objectDetail.optString("checklist_id");
                    String checkListName = objectDetail.optString("first_name");
                    listCheckListName.add(checkListName);
                    listCheckListID.add(checkListId);
                }
                spinnerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}


