package com.attendanceapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.SelectLocationActivity;
import com.attendanceapp.TeacherSelectBeaconActivity;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.RepeatType;
import com.attendanceapp.models.SelectedDays;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@SuppressLint("InflateParams")
@SuppressWarnings("unused")
public class CreateClassEventCompanyActivity extends Activity implements View.OnClickListener {

    private static final String TAG = CreateClassEventCompanyActivity.class.getSimpleName();
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String EXTRA_EDITING_CLASS_INDEX = "EXTRA_EDITING_CLASS_INDEX";

    private static final int REQUEST_SELECT_LOCATION = 230;
    private static final int REQUEST_SELECT_BEACONS = 231;

    EditText classNameEditText, districtEditText, codeEditText;
    TextView timeButton, dateButton, dayButton, locationButton, saveButton;
    LayoutInflater layoutInflater;
    protected SharedPreferences sharedPreferences;
    GPSTracker gpsTracker;

    ClassEventCompany classSetup = new ClassEventCompany();
    private User user;
    protected boolean isFirstTime;
    protected boolean isAddedNewClass, isClassDeleted;
    UserUtils userUtils;

    final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private boolean isEditClass;
    private UserRole userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class_event_company);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(CreateClassEventCompanyActivity.this);
        user = userUtils.getUserFromSharedPrefs();

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gpsTracker = new GPSTracker(CreateClassEventCompanyActivity.this);


        isFirstTime = getIntent().getBooleanExtra(EXTRA_IS_FIRST_TIME, false);

        classNameEditText = (EditText) findViewById(R.id.className);
        districtEditText = (EditText) findViewById(R.id.districtField);
        codeEditText = (EditText) findViewById(R.id.codeField);
        timeButton = (TextView) findViewById(R.id.timeField);
        dateButton = (TextView) findViewById(R.id.dateField);
        dayButton = (TextView) findViewById(R.id.dayField);
        locationButton = (Button) findViewById(R.id.locationField);
        saveButton = (Button) findViewById(R.id.saveButton);

        timeButton.setOnClickListener(this);
        dateButton.setOnClickListener(this);
        dayButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();

        userRole = UserRole.valueOf(bundle.getInt(AppConstants.EXTRA_USER_ROLE, -1));
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


            classNameEditText.setText(classSetup.getName());
            timeButton.setText(teacherClass.getStartTime() + " - " + teacherClass.getEndTime());

            Calendar cal;
            cal = classSetup.getStartDate();
            dateButton.setText("" + MONTHS[cal.get(Calendar.MONTH) + 1] + " "
                    + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR));
            cal = classSetup.getEndDate();
            dateButton.setText(dateButton.getText() + " - " + MONTHS[cal.get(Calendar.MONTH) + 1]
                    + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR));

            ((TextView) findViewById(R.id.txtTitle)).setText(teacherClass.getUniqueCode());
            dayButton.setText(classSetup.getRepeatType().toString());
            districtEditText.setText(classSetup.getDistrict());
            codeEditText.setText(classSetup.getCode());

            double latitude = classSetup.getLatitude(), longitude = classSetup.getLongitude();
            if (latitude != 0 && longitude != 0) {
                Address address = userUtils.getAddress(latitude, longitude);
                locationButton.setText(userUtils.getAddressString(address));
            }
        }

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


//                 hide extra fields
//                timeButton.setVisibility(View.GONE);
//                dateButton.setVisibility(View.GONE);
                districtEditText.setVisibility(View.GONE);
            }


            ((TextView) findViewById(R.id.txtTitle)).setText(title);
            classNameEditText.setHint(name);
            codeEditText.setHint(code);
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
            case R.id.dayField:
                processDayButton();
                break;
            case R.id.locationField:
                processLocationButton();
                break;
            case R.id.saveButton:
                processSaveButton();
                break;
        }
    }

    private void processTimeButton() {

        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClassEventCompanyActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClassEventCompanyActivity.this);
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

                AlertDialog dialog1 = new AlertDialog.Builder(CreateClassEventCompanyActivity.this).setTitle("Select End Date").setView(view).create();
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

    private void processDayButton() {

        View view = layoutInflater.inflate(R.layout.class_day_picker, null, false);

        final Spinner spinner = (Spinner) view.findViewById(R.id.repeats);
        final Spinner days = (Spinner) view.findViewById(R.id.days);
        final LinearLayout thirtyDaysLayout = (LinearLayout) view.findViewById(R.id.thirtyDaysLayout);
        final LinearLayout sevenDaysLayout = (LinearLayout) view.findViewById(R.id.sevenDaysLayout);
        final TextView repeatTypeTextView = (TextView) view.findViewById(R.id.repeatTypeTextView);

        CheckBox mon = (CheckBox) view.findViewById(R.id.mon);
        CheckBox tue = (CheckBox) view.findViewById(R.id.tue);
        CheckBox wed = (CheckBox) view.findViewById(R.id.wed);
        CheckBox thu = (CheckBox) view.findViewById(R.id.thu);
        CheckBox fri = (CheckBox) view.findViewById(R.id.fri);
        CheckBox sat = (CheckBox) view.findViewById(R.id.sat);
        CheckBox sun = (CheckBox) view.findViewById(R.id.sun);

        final TreeSet<Integer> selectedDaysSet = new TreeSet<>();

        CompoundButton.OnCheckedChangeListener daySelectionListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Integer integer = null;
                switch (buttonView.getId()) {
                    case R.id.mon:
                        integer = 0;
                        break;
                    case R.id.tue:
                        integer = 1;
                        break;
                    case R.id.wed:
                        integer = 2;
                        break;
                    case R.id.thu:
                        integer = 3;
                        break;
                    case R.id.fri:
                        integer = 4;
                        break;
                    case R.id.sat:
                        integer = 5;
                        break;
                    case R.id.sun:
                        integer = 6;
                        break;
                }
                if (isChecked) {
                    selectedDaysSet.add(integer);
                } else {
                    selectedDaysSet.remove(integer);
                }

                for (Integer integer1 : selectedDaysSet) {
                    SelectedDays selected_days = SelectedDays.values()[integer1];
                    System.out.println("selected_days = " + selected_days);
                    classSetup.getRepeatDays().add(SelectedDays.values()[integer1]);
                }
                if (selectedDaysSet.isEmpty()) {
                    classSetup.getRepeatDays().clear();
                }
            }
        };

        mon.setOnCheckedChangeListener(daySelectionListener);
        tue.setOnCheckedChangeListener(daySelectionListener);
        wed.setOnCheckedChangeListener(daySelectionListener);
        thu.setOnCheckedChangeListener(daySelectionListener);
        fri.setOnCheckedChangeListener(daySelectionListener);
        sat.setOnCheckedChangeListener(daySelectionListener);
        sun.setOnCheckedChangeListener(daySelectionListener);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(CreateClassEventCompanyActivity.this, R.array.days_array, android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        /* for days */
        ArrayAdapter<CharSequence> daysAdapter = ArrayAdapter.createFromResource(CreateClassEventCompanyActivity.this, R.array.interval_time_array, android.R.layout.simple_spinner_dropdown_item);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        days.setAdapter(daysAdapter);

        // save first position by default
        classSetup.setRepeatType(RepeatType.DAILY);


        final AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                thirtyDaysLayout.setVisibility(View.GONE);
                sevenDaysLayout.setVisibility(View.GONE);

                classSetup.setRepeatType(RepeatType.values()[position]);

                if (position == 0 || position == 4 || position == 5 || position == 6) {
                    thirtyDaysLayout.setVisibility(View.VISIBLE);
                }
                if (position == 4) {
                    sevenDaysLayout.setVisibility(View.VISIBLE);
                }

                switch (position) {
                    case 0:
                        repeatTypeTextView.setText("Days");
                        break;
                    case 4:
                        repeatTypeTextView.setText("Weeks");
                        break;
                    case 5:
                        repeatTypeTextView.setText("Months");
                        break;
                    case 6:
                        repeatTypeTextView.setText("Years");
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                classSetup.setRepeatType(null);
            }
        };
        spinner.setOnItemSelectedListener(spinnerItemSelectedListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClassEventCompanyActivity.this);
        builder.setTitle("Select Days");
        builder.setView(view);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = spinner.getSelectedItem().toString();
                dayButton.setText(selectedOption);

                String interval = days.getSelectedItem().toString();
                classSetup.setInterval(Integer.valueOf(interval));

                dialog.dismiss();
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void processLocationButton() {
        View view = layoutInflater.inflate(R.layout.location_picker, null, false);
        Button currentLocation = (Button) view.findViewById(R.id.currentLocation);
        Button customLocation = (Button) view.findViewById(R.id.customLocation);
        final Button addBeacons = (Button) view.findViewById(R.id.addBeacons);

        final AlertDialog alertDialog = new AlertDialog.Builder(CreateClassEventCompanyActivity.this).setView(view).create();
        alertDialog.show();

        currentLocation.setOnClickListener(new View.OnClickListener() {
            double longitude = 0, latitude = 0;

            @Override
            public void onClick(View v) {
                if (gpsTracker.canGetLocation()) {
                    Location location = gpsTracker.getLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    classSetup.setLatitude(latitude);
                    classSetup.setLongitude(longitude);

                    Address address = userUtils.getAddress(latitude, longitude);
                    locationButton.setText(userUtils.getAddressString(address));

                } else {
                    Toast.makeText(getApplicationContext(), "Please check location services", Toast.LENGTH_LONG).show();
                }
                alertDialog.dismiss();
                alertDialog.cancel();
            }
        });

        customLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog.cancel();
                startActivityForResult(new Intent(CreateClassEventCompanyActivity.this, SelectLocationActivity.class), REQUEST_SELECT_LOCATION);
            }
        });


        addBeacons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog.cancel();
                Intent intent = new Intent(CreateClassEventCompanyActivity.this, ListBeaconsActivity.class);
                intent.putParcelableArrayListExtra(ListBeaconsActivity.EXTRA_BEACONS, classSetup.getBeaconList());

                startActivityForResult(intent, REQUEST_SELECT_BEACONS);
            }
        });
    }

    private void processSaveButton() {
        // get data from all fields
        classSetup.setName(classNameEditText.getText().toString().trim());
        classSetup.setDistrict(districtEditText.getText().toString().trim());
        classSetup.setCode(codeEditText.getText().toString().trim());

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
            ProgressDialog alertDialog = new ProgressDialog(CreateClassEventCompanyActivity.this);

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
        Toast.makeText(CreateClassEventCompanyActivity.this, title, Toast.LENGTH_LONG).show();
    }

    private void getTimeFromUser(String title) {
        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClassEventCompanyActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClassEventCompanyActivity.this);
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

    @Override
    public void onBackPressed() {
        if (isFirstTime || isAddedNewClass) {
            updateDataAsync();
            return;
        }
        super.onBackPressed();
    }

    protected void openActivity(Class aClass) {
        startActivity(new Intent(CreateClassEventCompanyActivity.this, aClass));
        finish();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progressDialog = new ProgressDialog(CreateClassEventCompanyActivity.this);

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

}
