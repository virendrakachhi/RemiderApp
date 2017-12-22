package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.attendanceapp.activities.ListBeaconsActivity;
import com.attendanceapp.models.ClassSetup;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.RepeatType;
import com.attendanceapp.models.SelectedDays;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.attendanceapp.webserviceCommunicator.WebServiceHandler;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


@SuppressLint("InflateParams")
public class HealthCareAddLocationActivity extends Activity implements View.OnClickListener {

    private static final String TAG = HealthCareAddLocationActivity.class.getSimpleName();
    public static final String EXTRA_IS_FIRST_TIME = AppConstants.EXTRA_IS_FIRST_TIME;
    public static final String EXTRA_TEACHER_CLASS_INDEX = "EXTRA_EDITING_CLASS_INDEX";

    private static final int REQUEST_SELECT_LOCATION = 230;
    private static final int REQUEST_SELECT_BEACONS = 231;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;

    EditText classNameEditText,
            companyEditText,
            companyCodeEditText;
    ArrayList<Beacon> beaconList;
    //    TextView timeButton;
    Button locationButton, saveButton, beaconButton;
    //        dateButton, dayButton, locationButton, saveButton;
    ImageView imgHelp;
    LayoutInflater layoutInflater;
    protected SharedPreferences sharedPreferences;
    GPSTracker gpsTracker;

    ClassSetup classSetup = new ClassSetup();
    private User user;
    ProgressDialog mDialog;
    protected boolean isFirstTime;
    protected boolean isAddedNewClass;
    Gson gson = new Gson();
    UserUtils userUtils;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private Spinner spnRadius;
    private boolean isEditClass, isDeleteClass;
    private String teacherClassId;

    private static boolean beaconSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_location_healthcare);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        sharedPreferences.edit().putBoolean("Image Status", false).commit();
        userUtils = new UserUtils(HealthCareAddLocationActivity.this);
        user = userUtils.getUserFromSharedPrefs();

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        gpsTracker = new GPSTracker(HealthCareAddLocationActivity.this);


//        userUtils = new UserUtils(HealthCareAddLocationActivity.this);
//        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

        /*isFirstTime = getIntent().getBooleanExtra(EXTRA_IS_FIRST_TIME, false);
        if (isFirstTime) {
            getBeaconsList();

        }*/
        ManagerHCClass beaconsObject = userUtils.getUserWithDataFromSharedPrefs(ManagerHCClass.class);
        if (beaconsObject != null) {
            beaconList = beaconsObject.getBeaconList();
        }

        classNameEditText = (EditText) findViewById(R.id.className);

        companyEditText = (EditText) findViewById(R.id.companyName);
        companyEditText.setVisibility(View.GONE);
        companyCodeEditText = (EditText) findViewById(R.id.companyCode);
//        timeButton = (TextView) findViewById(R.id.timeField);
//        dateButton = (TextView) findViewById(R.id.dateField);
//        dayButton = (TextView) findViewById(R.id.dayField);
        locationButton = (Button) findViewById(R.id.locationField);
        beaconButton = (Button) findViewById(R.id.beaconField);
        saveButton = (Button) findViewById(R.id.saveButton);

        imgHelp = (ImageView) findViewById(R.id.imgHelp);
        mDialog = new ProgressDialog(HealthCareAddLocationActivity.this);
        spnRadius = (Spinner) findViewById(R.id.beacon_radius);

        ArrayAdapter<CharSequence> securityQuestionsAdapter = ArrayAdapter.createFromResource(this, R.array.radius_array, R.layout.selecter_item_radius);
        securityQuestionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRadius.setAdapter(new NothingSelectedSpinnerAdapter(securityQuestionsAdapter, R.layout.radius_spinner_row, this));


//        timeButton.setOnClickListener(this);
//        dateButton.setOnClickListener(this);
//        dayButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        beaconButton.setOnClickListener(this);


        int index = getIntent().getIntExtra(EXTRA_TEACHER_CLASS_INDEX, -1);

        if (index != -1) {
            Toast.makeText(getApplicationContext(), "Inside IF", Toast.LENGTH_LONG).show();
            ManagerHCClass teacherClass = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
            isEditClass = true;

            teacherClassId = teacherClass.getId();
            classSetup.setId(teacherClassId);
            classSetup.setClassName(teacherClass.getLocationName());
//                classSetup.setDistrict(teacherClass.getDistrict());
            classSetup.setCode(teacherClass.getCompanyCode());
//                classSetup.setInterval(String.valueOf(teacherClass.getInterval()));
//                classSetup.setBeaconList(teacherClass.getBeaconList());
            classSetup.setLatitude(teacherClass.getLatitude());
            classSetup.setLongitude(teacherClass.getLongitude());
            classSetup.setCompanyName(teacherClass.getCompanyName());
            classSetup.setCompanyCode(teacherClass.getCompanyCode());
            classSetup.setDistance(teacherClass.getDistance());

//                classSetup.setRepeatType(teacherClass.getRepeatType());
            classSetup.setCompanyName(teacherClass.getCompanyName());
            Calendar calendar = Calendar.getInstance();

            try {
                if (classSetup.getDistance().equalsIgnoreCase("5")) {
                    spnRadius.setSelection(1);
                } else if (classSetup.getDistance().equalsIgnoreCase("10")) {
                    spnRadius.setSelection(2);
                } else if (classSetup.getDistance().equalsIgnoreCase("15")) {
                    spnRadius.setSelection(3);
                } else if (classSetup.getDistance().equalsIgnoreCase("20")) {
                    spnRadius.setSelection(4);
                } else if (classSetup.getDistance().equalsIgnoreCase("30")) {
                    spnRadius.setSelection(5);
                } else if (classSetup.getDistance().equalsIgnoreCase("40")) {
                    spnRadius.setSelection(6);
                } else if (classSetup.getDistance().equalsIgnoreCase("50")) {
                    spnRadius.setSelection(7);
                } else if (classSetup.getDistance().equalsIgnoreCase("60")) {
                    spnRadius.setSelection(8);
                } else if (classSetup.getDistance().equalsIgnoreCase("70")) {
                    spnRadius.setSelection(9);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                simpleDateFormat.applyPattern("HH:mm:ss");

                calendar.setTime(simpleDateFormat.parse(teacherClass.getStartTime()));
                classSetup.setStartTime(calendar);

                calendar.setTime(simpleDateFormat.parse(teacherClass.getEndTime()));
                classSetup.setEndTime(calendar);

                simpleDateFormat.applyPattern("yyyy-mm-dd");

//                    calendar.setTime(simpleDateFormat.parse(teacherClass.getStartDate()));
//                    classSetup.setStartDate(calendar);
//
//                    calendar.setTime(simpleDateFormat.parse(teacherClass.getEndDate()));
//                    classSetup.setEndDate(calendar);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            classNameEditText.setText(classSetup.getClassName());
//            timeButton.setText(teacherClass.getStartTime() + " - " + teacherClass.getEndTime());
            companyEditText.setText(classSetup.getCompanyName());
            companyCodeEditText.setText(classSetup.getCompanyCode());

            calendar = classSetup.getStartDate();
//            dateButton.setText("" + MONTHS[calendar.get(Calendar.MONTH) + 1] + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR));
            calendar = classSetup.getEndDate();
//            dateButton.setText(dateButton.getText() + " - " + MONTHS[calendar.get(Calendar.MONTH) + 1] + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR));

            ((TextView) findViewById(R.id.txtTitle)).setText(teacherClass.getLocationName());
//            dayButton.setText(classSetup.getRepeatType().toString());
//            districtEditText.setText(classSetup.getDistrict());
//            codeEditText.setText(classSetup.getCode());

            // Raj
            double latitude, longitude;


            if ((classSetup.getLatitude() != null) && !classSetup.getLatitude().equals("") && !classSetup.getLatitude().equalsIgnoreCase("null")) {
                latitude = Double.parseDouble(classSetup.getLatitude());
            } else {
                latitude = 0.0;
            }

            if (classSetup.getLongitude() != null && !classSetup.getLongitude().equals("") && !classSetup.getLongitude().equalsIgnoreCase("null")) {
                longitude = Double.parseDouble(classSetup.getLongitude());
            } else {
                longitude = 0.0;
            }

// raj
            if (latitude != 0.0 && longitude != 0.0) {
                Address address = userUtils.getAddress(latitude, longitude);
                locationButton.setText(userUtils.getAddressString(address));
            }
            // delete button functionality
            imgHelp.setImageResource(R.drawable.delete);
            imgHelp.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.timeField:
//                processTimeButton();
//                break;
            case R.id.dateField:
                processDateButton();
                break;

            case R.id.dayField:
                processDayButton();
                break;

            case R.id.locationField:
                gpsTracker = new GPSTracker(HealthCareAddLocationActivity.this);
                processLocationButton();
                break;

            case R.id.beaconField:
                beaconSelected = true;
                Intent intent = new Intent(HealthCareAddLocationActivity.this, ListBeaconsActivity.class);
                intent.putParcelableArrayListExtra(ListBeaconsActivity.EXTRA_BEACONS, beaconList);
                startActivityForResult(intent, REQUEST_SELECT_BEACONS);
                break;

            case R.id.saveButton:
                processSaveButton();
                break;

            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;


            case R.id.imgHelp:
                deleteClass();
        }
    }

    private void deleteClass() {

        new AlertDialog.Builder(HealthCareAddLocationActivity.this)
                .setMessage("Delete Location!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        final String url = AppConstants.URL_DELETE_LOCATION_BY_MANAGER;

                        new AsyncTask<Void, Void, String>() {
                            ProgressDialog progressDialog = new ProgressDialog(HealthCareAddLocationActivity.this);

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                progressDialog.setMessage("Please wait...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                            }

                            @Override
                            protected String doInBackground(Void... params) {
                                String result = null;

                                HashMap<String, String> map = new HashMap<>();
                                map.put("location_id", teacherClassId);

                                try {
                                    result = new WebUtils().post(url, map);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return result;
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                progressDialog.dismiss();
                                JSONObject jObject = null;
                                if (s == null) {
                                    makeToast("Error in deleting!");

                                } else {
                                    try {
                                        jObject = new JSONObject(s);

                                        // check if result contains Error
                                        if (jObject.has("Error")) {
                                            makeToast(jObject.getString("Error"));

                                        } else if (jObject.has("Message")) {
                                            makeToast("Deleted successfully!");

                                            isDeleteClass = true;
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
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();

    }


    void getBeaconsList() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                if (mDialog != null) {
                    mDialog.setMessage("Please wait...");
                    mDialog.setCancelable(false);
                    mDialog.show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                try {
                    return new WebUtils().post(AppConstants.URL_GET_BEACONS_LIST, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    if ((mDialog != null) && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    if (result != null) {

                        beaconList = (ArrayList) DataUtils.getHCBeaconListFromJson(result);
                        ManagerHCClass m = new ManagerHCClass();
                        m.setBeaconList(beaconList);
                        userUtils.saveUserWithDataToSharedPrefs(m, ManagerHCClass.class);
                    }
                } catch (Exception ex) {
                }
            }
        }.execute();

    }


    /*private void processTimeButton() {

        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HealthCareAddLocationActivity.this);
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

    }*/

    private void processDateButton() {
        final View view = layoutInflater.inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HealthCareAddLocationActivity.this);
        builder.setTitle("Select Start Date");
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int monthOfYear = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();

//                dateButton.setText("" + MONTHS[monthOfYear] + ":" + dayOfMonth + ":" + year);
//                         Jun 1, 2015
//                dateButton.setText("" + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
                classSetup.setStartDate(getDateFromDatePicker(datePicker));

                getDateFromUser("Select End Date");

                AlertDialog dialog1 = new AlertDialog.Builder(HealthCareAddLocationActivity.this).setTitle("Select End Date").setView(view).create();
                dialog1.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = datePicker.getYear();
                        int monthOfYear = datePicker.getMonth();
                        int dayOfMonth = datePicker.getDayOfMonth();
//                        dateButton.setText(dateButton.getText() + " - " + MONTHS[monthOfYear] + ":" + dayOfMonth + ":" + year);
//                         Jun 1, 2015
//                        dateButton.setText(dateButton.getText() + " - " + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
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

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(HealthCareAddLocationActivity.this, R.array.days_array, android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        /* for days */
        ArrayAdapter<CharSequence> daysAdapter = ArrayAdapter.createFromResource(HealthCareAddLocationActivity.this, R.array.interval_time_array, android.R.layout.simple_spinner_dropdown_item);
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

//        class GridAdapter extends BaseAdapter {
//            Context context;
//            List<Integer> integerList;
//
//            public GridAdapter(Context context, List<Integer> integerList) {
//                this.context = context;
//                this.integerList = integerList;
//            }
//
//            @Override
//            public int getCount() {
//                return integerList.size();
//            }
//
//            @Override
//            public Object getItem(int position) {
//                return integerList.get(position);
//            }
//
//            @Override
//            public long getItemId(int position) {
//                return position;
//            }
//
//            class ViewHolder {
//                public TextView textView;
//            }
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                final ViewHolder viewHolder;
//                View view = convertView;
//
//                if (view == null) {
//                    viewHolder = new ViewHolder();
//                    view = layoutInflater.inflate(R.layout.day_picker, null, false);
//                    viewHolder.textView = (TextView) view.findViewById(R.id.textView);
//
//                } else {
//                    viewHolder = (ViewHolder) view.getTag();
//                }
//                viewHolder.textView.setText("" + integerList.get(position));
//                view.setTag(viewHolder);
//
//                return view;
//            }
//        }
//        List<Integer> integers = new ArrayList<>();
//        for (int i = 1; i <= 30; i++) {
//            integers.add(i);
//        }

        //dateGrid.setAdapter(new GridAdapter(HealthCareAddLocationActivity.this, integers));

//        final TreeSet<Integer> selectedDates = new TreeSet<>();

//        dateGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Integer integer = position + 1;
//                if (selectedDates.contains(integer)) {
//                    selectedDates.remove(integer);
//                } else {
//                    selectedDates.add(integer);
//                }
//                selectedDatesText.setText(selectedDates.toString());
//
//                classSetup.setRepeatDates(selectedDates);
//            }
//        });

        AlertDialog.Builder builder = new AlertDialog.Builder(HealthCareAddLocationActivity.this);
        builder.setTitle("Select Days");
        builder.setView(view);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = spinner.getSelectedItem().toString();
//                dayButton.setText(selectedOption);

                String interval = days.getSelectedItem().toString();
                classSetup.setInterval(interval);

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
        Button wifiLocation = (Button) view.findViewById(R.id.addWifi);
        wifiLocation.setVisibility(View.GONE);
        final Button addBeacons = (Button) view.findViewById(R.id.addBeacons);
        addBeacons.setVisibility(View.GONE);
//        final LinearLayout customLocationBox = (LinearLayout) view.findViewById(R.id.customLocationBox);

        final AlertDialog alertDialog = new AlertDialog.Builder(HealthCareAddLocationActivity.this).setView(view).create();
        alertDialog.show();

        currentLocation.setOnClickListener(new View.OnClickListener() {
            double longitude = 0, latitude = 0;

            @Override
            public void onClick(View v) {

                if (AppGlobals.mCurrentLocation != null) {
                    Location location = AppGlobals.mCurrentLocation;
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    classSetup.setLatitude(Double.toString(latitude));
                    classSetup.setLongitude(Double.toString(longitude));
                    classSetup.setLocationType("gps_location");

                    Address address = userUtils.getAddress(latitude, longitude);
                    locationButton.setText(userUtils.getAddressString(address));
                } else if (gpsTracker.canGetLocation()) {
                    Location location = gpsTracker.getLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    classSetup.setLatitude(Double.toString(latitude));
                    classSetup.setLongitude(Double.toString(longitude));
                    classSetup.setLocationType("gps_location");

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
                //customLocationBox.setVisibility(customLocationBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                alertDialog.dismiss();
                alertDialog.cancel();
                startActivityForResult(new Intent(HealthCareAddLocationActivity.this, SelectLocationActivity.class), REQUEST_SELECT_LOCATION);
            }
        });


       /* addBeacons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Add Beacons from website", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                alertDialog.cancel();
                beaconSelected = true;
//                startActivityForResult(new Intent(HealthCareAddLocationActivity.this, TeacherSelectBeaconActivity.class), REQUEST_SELECT_BEACONS);
                Intent intent = new Intent(HealthCareAddLocationActivity.this, ListBeaconsActivity.class);
//                intent.putParcelableArrayListExtra(ListBeaconsActivity.EXTRA_BEACONS, classSetup.getBeaconList());
                intent.putParcelableArrayListExtra(ListBeaconsActivity.EXTRA_BEACONS, beaconList);

                startActivityForResult(intent, REQUEST_SELECT_BEACONS);
            }
        });*/

        wifiLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Add Beacons from website", Toast.LENGTH_SHORT).show();


                alertDialog.dismiss();
                alertDialog.cancel();


                LocationManager locationManager = (LocationManager)
                        getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String bestProvider = locationManager.getBestProvider(criteria, false);
                Location location = locationManager.getLastKnownLocation(bestProvider);
                Double lat, lon;
                try {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                } catch (NullPointerException e) {
                    lat = -1.0;
                    lon = -1.0;
                }
                Toast.makeText(getApplicationContext(), "lat== " + Double.toString(lat) + "\n" + "lon== " + Double.toString(lon), Toast.LENGTH_SHORT).show();


//                startActivityForResult(new Intent(HealthCareAddLocationActivity.this, TeacherSelectBeaconActivity.class), REQUEST_SELECT_BEACONS);
//                Intent intent = new Intent(HealthCareAddLocationActivity.this, WifiActivity.class);
//
//
//                intent.putParcelableArrayListExtra(ListBeaconsActivity.EXTRA_BEACONS, classSetup.getBeaconList());
//
//                startActivityForResult(intent, 1);
            }
        });

    }


    private void processSaveButton() {
        // get data from all fields
        classSetup.setClassName(classNameEditText.getText().toString().trim());
        classSetup.setCompanyName(companyEditText.getText().toString().trim());
        classSetup.setCompanyCode(companyCodeEditText.getText().toString().trim());

        String strRadiusDis = "";
        if (spnRadius.getSelectedItemPosition() != 0) {
            classSetup.setDistance(spnRadius.getSelectedItem().toString());
        }

        String errorMessage = null;

        // validate all required fields
        if (classSetup.getClassName() == null || classSetup.getClassName().length() < 1) {
            errorMessage = "Please enter correct Location name";
        } /*else if (classSetup.getStartTime() == null || classSetup.getEndTime() == null) {
            errorMessage = "Please select start and end time";
//        } else if (classSetup.getStartDate() == null || classSetup.getEndDate() == null) {
//            errorMessage = "Please select start and end date";
//        } else if (classSetup.getRepeatType() == null) {
//            errorMessage = "Please select class repeat type";
        }*/ /*else if (!beaconSelected) {
            if (classSetup.getLatitude() == null || (Double.parseDouble(classSetup.getLatitude()) == 0.0d || Double.parseDouble(classSetup.getLongitude()) == 0.0d) && "".equals(classSetup.getBeacons())) {
                errorMessage = "Please select location";
            }
        }*/ else if (classSetup.getLatitude() == null || (Double.parseDouble(classSetup.getLatitude()) == 0.0d || Double.parseDouble(classSetup.getLongitude()) == 0.0d) && "".equals(classSetup.getBeacons())) {
            errorMessage = "Please select location";
        } else if (classSetup.getBeacons() != null && classSetup.getBeacons().trim().length() != 0 && classSetup.getDistance() == null){
            errorMessage = "Please select beacon radius";
        }

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

            if (isEditClass) {
                keysAndValues.put("id", classSetup.getId());
            }
            keysAndValues.put("user_id", user.getUserId());
            keysAndValues.put("status", "1");
            keysAndValues.put("locationName", classSetup.getClassName());
            keysAndValues.put("companyName", classSetup.getCompanyName());
            keysAndValues.put("companyCode", classSetup.getCompanyCode());
            /*calendar = classSetup.getStartTime();
            keysAndValues.put("startTime", String.format(formatForTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            calendar = classSetup.getEndTime();
            keysAndValues.put("endTime", String.format(formatForTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));*/
//            calendar = classSetup.getStartDate();
//            keysAndValues.put("startDate", String.format(formatForDate, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
//            calendar = classSetup.getEndDate();
//            keysAndValues.put("endDate", String.format(formatForDate, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
            keysAndValues.put("latitude", String.valueOf(classSetup.getLatitude()));
            keysAndValues.put("longitude", String.valueOf(classSetup.getLongitude()));
            keysAndValues.put("beacon_udid", String.valueOf("1231231"));

//            keysAndValues.put("repeatType", classSetup.getRepeatType().toString());
//            keysAndValues.put("repeatDays", classSetup.getRepeatDays().toString());
//            keysAndValues.put("interval", classSetup.getInterval());
            keysAndValues.put("beacon_id", classSetup.getBeacons());
            keysAndValues.put("location_type", classSetup.getLocationType());
            keysAndValues.put("location_Setup", classSetup.getLocationSetup());

            keysAndValues.put("distance", classSetup.getDistance());

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
            UploadDataAsync(AppConstants.URL_ADD_LOCATION, keysAndValues);
        }
    }

    private void UploadDataAsync(final String url, final Map<String, String> keysAndValues) {
        new AsyncTask<Object, Void, String>() {
            ProgressDialog alertDialog = new ProgressDialog(HealthCareAddLocationActivity.this);
            String response = "";

            @Override
            protected void onPreExecute() {
                alertDialog.setMessage("Please wait...");
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            protected String doInBackground(Object... params) {
                try {

//                    return new WebUtils().post(url, keysAndValues);

//                    beacon_id,beacon_udid,companyCode,companyName,distance,endTime,id,latitude,location_Setup,location_type,
// locationName,longitude,startTime,status,user_id

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("id", keysAndValues.get("id")));
                    nameValuePairs.add(new BasicNameValuePair("user_id", keysAndValues.get("user_id")));
                    nameValuePairs.add(new BasicNameValuePair("status", keysAndValues.get("status")));
                    nameValuePairs.add(new BasicNameValuePair("locationName", keysAndValues.get("locationName")));
                    nameValuePairs.add(new BasicNameValuePair("companyName", keysAndValues.get("companyName")));
                    nameValuePairs.add(new BasicNameValuePair("companyCode", keysAndValues.get("companyCode")));
                    nameValuePairs.add(new BasicNameValuePair("startTime", keysAndValues.get("startTime")));
                    nameValuePairs.add(new BasicNameValuePair("endTime", keysAndValues.get("endTime")));
                    nameValuePairs.add(new BasicNameValuePair("startDate", keysAndValues.get("startDate")));
                    nameValuePairs.add(new BasicNameValuePair("latitude", keysAndValues.get("latitude")));
                    nameValuePairs.add(new BasicNameValuePair("longitude", keysAndValues.get("longitude")));
                    nameValuePairs.add(new BasicNameValuePair("beacon_udid", keysAndValues.get("beacon_udid")));
                    nameValuePairs.add(new BasicNameValuePair("beacon_id", keysAndValues.get("beacon_id")));
                    if (keysAndValues.get("beacon_id") != null && keysAndValues.get("beacon_id").trim().length() > 0) {
                        nameValuePairs.add(new BasicNameValuePair("location_type", "beacon_location"));
                    } else {
                        nameValuePairs.add(new BasicNameValuePair("location_type", keysAndValues.get("location_type")));
                    }
                    nameValuePairs.add(new BasicNameValuePair("location_Setup", keysAndValues.get("location_Setup")));
                    nameValuePairs.add(new BasicNameValuePair("distance", keysAndValues.get("distance")));

                    response = new WebServiceHandler().webServiceCall(nameValuePairs, url);
                    return response;

                } catch (Exception e) {
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
                            makeToast("Location is saved!");
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
        Toast.makeText(HealthCareAddLocationActivity.this, title, Toast.LENGTH_LONG).show();
    }

    /*private void getTimeFromUser(String title) {
        View view = layoutInflater.inflate(R.layout.time_picker, null, false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HealthCareAddLocationActivity.this);
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
    }*/

    private void getDateFromUser(String title) {
        final View view = layoutInflater.inflate(R.layout.date_picker, null, false);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);

        AlertDialog.Builder builder = new AlertDialog.Builder(HealthCareAddLocationActivity.this);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = datePicker.getYear();
                int monthOfYear = datePicker.getMonth();
                int dayOfMonth = datePicker.getDayOfMonth();
//                dateButton.setText(dateButton.getText() + " - " + MONTHS[monthOfYear] + " " + dayOfMonth + ", " + year);
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

                classSetup.setLatitude(Double.toString(lat));
                classSetup.setLongitude(Double.toString(lng));
                classSetup.setLocationType("map_location");
                beaconSelected = false;
                locationButton.setText(userUtils.getAddressString(userUtils.getAddress(lat, lng)));
            }
        } else if (requestCode == REQUEST_SELECT_BEACONS) {
            if (resultCode == RESULT_OK) {
                classSetup.setLocationType("beacon_location");
                beaconSelected = true;
                classSetup.setBeacons(data.getStringExtra(TeacherSelectBeaconActivity.RESPONSE_SELECTED_BEACONS));
//                locationButton.setText("Beacon Location");
            } else {
                beaconSelected = false;
                classSetup.setBeacons("");
            }


        } else if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                locationButton.setText(result);
                classSetup.setLocationType("wifi_location");
                classSetup.setLatitude(Double.toString(1.1));
                classSetup.setLongitude(Double.toString(1.1));
                classSetup.setLocationSetup(result);

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    protected void openActivity(Class aClass) {
        startActivity(new Intent(HealthCareAddLocationActivity.this, aClass));
        finish();
    }

    private void updateDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.ManagerHC.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_MANAGER, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if ((mDialog != null) && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                if (result != null) {

                    Manager manager = new Manager(user);
                    List<ManagerHCClass> teacherClasses = DataUtils.getManagerLocationListFromJsonString(result);
                    System.out.println("list size== " + teacherClasses.size());
                    if (isEditClass || isDeleteClass) {
                        System.out.println("isEditClass==true");
                        manager.getManagerLocationList().clear();
                        manager.getManagerLocationList().addAll(teacherClasses);
                        userUtils.saveUserWithDataToSharedPrefs(manager, Manager.class);

                    } else {
                        System.out.println("isEditClass==false");
                        if (manager.getManagerLocationList().size() != teacherClasses.size()) {
                            System.out.println("isEditClass==true Inside IF");
                            manager.getManagerLocationList().clear();
                            manager.getManagerLocationList().addAll(teacherClasses);

                            userUtils.saveUserWithDataToSharedPrefs(manager, Manager.class);

                        }
                    }
                }
                if (!isEditClass) {
                    openActivity(ManagerHCDashboardActivity.class);
                }
                finish();
            }
        }.execute();

    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(wifiReciever);

        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
//        mDialog = null;
    }


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
            updateDataAsync();
        }
    }


}

