package com.attendanceapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.adapters.HCEmployeePagerAdapter;
import com.attendanceapp.models.Employee;
import com.attendanceapp.models.HCBeaconEmployee;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.services.HCEmployeeService;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.GPSTracker;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.attendanceapp.webserviceCommunicator.WebServiceHandler;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class EmployeeHCDashboardActivity extends FragmentActivity implements View.OnClickListener, NavigationPage.NavigationFunctions, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = EventHost_DashboardActivity.class.getSimpleName();
    private static final int REQUEST_EDIT_ACCOUNT = 100;
    protected ImageView addLoacationButton;
    public static String picUrl;
    private TextView oneWordTextView;
    protected LinearLayout checklistBtn, schedulingBtn, messageBtn, clientInfoBtn, mainPage;
    public static List<HCFamily> familyList;
    float meters;
    /* for settings page functionality */
    protected ImageView settingButton;
    protected LinearLayout createAccountLayout, reportsHCLayout, reportsLayout, onOffNotifications, locationsLayout, settingPage;
    protected RelativeLayout oneWordTitleLayout;
    protected ImageView onOffNotificationImageView;
    public static List<HCModuleDayWeekMonthYearViewBean> scheduleList = new LinkedList<>();
    Employee employee;

    public static String employeeChecklist_Id = null;


    AlertDialog alertDialog = null;

    private ScrollView swipePage;
    private HCEmployeePagerAdapter baseViewPagerAdapter;
    private ViewPager mViewPager;
    private Animation textAnimation;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    GPSTracker gpsTracker;
    Double longi, lati;
    private UserUtils userUtils;
    protected User user;
    String locationID, managerID;
    Bundle savedInstanceState;

    public static BeaconManager beaconManager;

    Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    List<Beacon> beaconListToSend = new ArrayList<Beacon>();
    List<Beacon> beaconListToCheckFromExit = new ArrayList<Beacon>();

//    private boolean isAttendenceEntered = false;

    int schedulePosition = 0;

    private static boolean breakLoop = false;

    private BluetoothAdapter bluetoothAdapter;

    private static String oldScheduleID = "";
    private static CountDownTimer countDownTimer = null;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String mLocationUpdate;

    private static int serviceState = 0;
    Intent serviceIntent = null;

    private static int counterGetSchedule = 0;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                updateDataAsync();
//                getScheduling();
//                matchDates();
            }
        }
    }
//vvv
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
//        updateDataAsync();

        getScheduling();

        Log.v("After UPdate", "bla bla");
        setContentView(R.layout.activity_employee_hc_module_dashboard);

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).build();
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null)
            AppGlobals.mCurrentLocation = location;


        familyList = new LinkedList<>();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        swipePage = (ScrollView) findViewById(R.id.swipePage);
//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        userUtils = new UserUtils(EmployeeHCDashboardActivity.this);

//        User user = userUtils.getUserFromSharedPrefs();


        employee = userUtils.getUserWithDataFromSharedPrefs(Employee.class);
//        employee = new Employee(user);
        oneWordTextView = (TextView) findViewById(R.id.oneWordTitle);
        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        checklistBtn = (LinearLayout) findViewById(R.id.checklistBtn);
        schedulingBtn = (LinearLayout) findViewById(R.id.schedulingBtn);
        messageBtn = (LinearLayout) findViewById(R.id.messageBtn);
        clientInfoBtn = (LinearLayout) findViewById(R.id.clientInfoBtn);
        mainPage = (LinearLayout) findViewById(R.id.mainPage);
        oneWordTitleLayout = (RelativeLayout) findViewById(R.id.oneWordTitleLayout);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        /* for setting tab */

        createAccountLayout = (LinearLayout) findViewById(R.id.createAccountLayout);
        reportsHCLayout = (LinearLayout) findViewById(R.id.reportsHCLayout);

        onOffNotifications = (LinearLayout) findViewById(R.id.onOffNotifications);
        checklistBtn.setOnClickListener(this);
        schedulingBtn.setOnClickListener(this);
        messageBtn.setOnClickListener(this);
        clientInfoBtn.setOnClickListener(this);
        navigationButton.setOnClickListener(this);
        navigationButton.setOnTouchListener(swipeTouchListener);
        checklistBtn.setOnTouchListener(swipeTouchListener);
        schedulingBtn.setOnTouchListener(swipeTouchListener);
        messageBtn.setOnTouchListener(swipeTouchListener);
        clientInfoBtn.setOnTouchListener(swipeTouchListener);


        //         manager = new Manager(user);
//        Toast.makeText(getApplicationContext(),Integer.toString(employee.getManagerLocationList().size()),Toast.LENGTH_LONG).show();
        if (employee != null) {
            if (employee.getManagerLocationList() != null && employee.getManagerLocationList().size() > 0) {
                familyList = employee.getManagerLocationList().get(0).getFamilyList();
                baseViewPagerAdapter = new HCEmployeePagerAdapter(getSupportFragmentManager(), employee.getManagerLocationList());
                mViewPager.setAdapter(baseViewPagerAdapter);
                locationID = employee.getManagerLocationList().get(0).getLocationId();
                managerID = employee.getManagerLocationList().get(0).getManagerID();
// Raj
                if ((employee.getManagerLocationList().get(0).getLongitude().toString() != null && !(employee.getManagerLocationList().get(0).getLongitude().toString().equals(""))) && (employee.getManagerLocationList().get(0).getLatitude().toString() != null && !(employee.getManagerLocationList().get(0).getLatitude().toString().equals("")))) {
                    longi = Double.parseDouble(employee.getManagerLocationList().get(0).getLongitude().toString());
                    lati = Double.parseDouble(employee.getManagerLocationList().get(0).getLatitude().toString());
                    serviceIntent = new Intent(EmployeeHCDashboardActivity.this, HCEmployeeService.class);
                    Bundle bun = new Bundle();
                    bun.putString("LocationId", locationID);
                    bun.putString("ManagerId", managerID);
                    bun.putDouble("Longitude", longi);
                    bun.putDouble("Latitude", lati);
                    if (employee.getManagerLocationList().get(0).getLocationSetup() != null) {
                        bun.putString("LocationSetup", employee.getManagerLocationList().get(0).getLocationSetup());
                    }
                    if (employee.getManagerLocationList().get(0).getLocationType() != null) {
                        bun.putString("LocationType", employee.getManagerLocationList().get(0).getLocationType());
                    }
                    serviceIntent.putExtras(bun);
                    if (AppGlobals.mCurrentLocation != null) {
                        serviceState = 1;
                        if (!employee.getManagerLocationList().get(0).getLocationType().equalsIgnoreCase("beacon_location"))
                            startService(serviceIntent);
                    }


                    List<HCBeaconEmployee> arrayListBeacons = employee.getManagerLocationList().get(0).getEmployeeBeaconList();
                    if (arrayListBeacons != null && arrayListBeacons.size() > 0) {
//                        Intent serviceIntentBeacon = new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("BeaconList", (Serializable) arrayListBeacons);
//                        serviceIntentBeacon.putExtras(bundle);
//                        startService(serviceIntentBeacon);
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter.isEnabled()) {
                            detectBeacon(arrayListBeacons);
                        } else {
                            showBluetoothEnableDialog(arrayListBeacons);
                        }
//                        startService(new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class));
                    }

//                    startService(new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class));

                } else if (employee.getManagerLocationList().get(0).getEmployeeBeaconList().size() > 0) {
                    List<HCBeaconEmployee> arrayListBeacons = employee.getManagerLocationList().get(0).getEmployeeBeaconList();
                    if (arrayListBeacons != null && arrayListBeacons.size() > 0) {
//                        Intent serviceIntentBeacon = new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("BeaconList", (Serializable) arrayListBeacons);
//                        serviceIntentBeacon.putExtras(bundle);
//                        startService(serviceIntentBeacon);
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter.isEnabled()) {
                            detectBeacon(arrayListBeacons);
                        } else {
                            showBluetoothEnableDialog(arrayListBeacons);
                        }
//                        startService(new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class));
                    }
                }


// raj
                //  Toast.makeText(getApplicationContext(),Double.toString(longi)+"\n"+Double.toString(lati)+"\n"+Double.toString(location.getLongitude())+"\n"+Double.toString(location.getLatitude()),Toast.LENGTH_LONG).show();

//                    Toast.makeText(getApplicationContext(), "You are   " + Float.toString(meters) + "  meters away from your scheduled location. ", Toast.LENGTH_LONG).show();

//                       getScheduling();

            }
        }




/*
        if (employee != null) {
            if (employee.getManagerLocationList() != null && employee.getManagerLocationList().size() > 0) {

                for (int i = 0; i < employee.getManagerLocationList().size(); i++) {
                    {
                        familyList = employee.getManagerLocationList().get(i).getFamilyList();
                        baseViewPagerAdapter = new HCEmployeePagerAdapter(getSupportFragmentManager(), employee.getManagerLocationList());
                        mViewPager.setAdapter(baseViewPagerAdapter);
                        locationID = employee.getManagerLocationList().get(i).getLocationId();
                        managerID = employee.getManagerLocationList().get(i).getManagerID();
// Raj
                        if ((employee.getManagerLocationList().get(i).getLongitude().toString() != null && !(employee.getManagerLocationList().get(i).getLongitude().toString().equals(""))) && (employee.getManagerLocationList().get(i).getLatitude().toString() != null && !(employee.getManagerLocationList().get(i).getLatitude().toString().equals("")))) {
                            longi = Double.parseDouble(employee.getManagerLocationList().get(i).getLongitude().toString());
                            lati = Double.parseDouble(employee.getManagerLocationList().get(i).getLatitude().toString());
                            serviceIntent = new Intent(EmployeeHCDashboardActivity.this, HCEmployeeService.class);
                            Bundle bun = new Bundle();
                            bun.putString("LocationId", locationID);
                            bun.putString("ManagerId", managerID);
                            bun.putDouble("Longitude", longi);
                            bun.putDouble("Latitude", lati);
                            if (employee.getManagerLocationList().get(i).getLocationSetup() != null) {
                                bun.putString("LocationSetup", employee.getManagerLocationList().get(i).getLocationSetup());
                            }
                            if (employee.getManagerLocationList().get(i).getLocationType() != null) {
                                bun.putString("LocationType", employee.getManagerLocationList().get(i).getLocationType());
                            }
                            serviceIntent.putExtras(bun);
                            if (AppGlobals.mCurrentLocation != null){
                                serviceState = 1;
                                if (!employee.getManagerLocationList().get(i).getLocationType().equalsIgnoreCase("beacon_location"))
                                    startService(serviceIntent);
                            }


                            List<HCBeaconEmployee> arrayListBeacons = employee.getManagerLocationList().get(i).getEmployeeBeaconList();
                            if (arrayListBeacons != null && arrayListBeacons.size() > 0) {
//                        Intent serviceIntentBeacon = new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("BeaconList", (Serializable) arrayListBeacons);
//                        serviceIntentBeacon.putExtras(bundle);
//                        startService(serviceIntentBeacon);
                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                if (bluetoothAdapter.isEnabled()){
                                    detectBeacon(arrayListBeacons);
                                } else {
                                    showBluetoothEnableDialog(arrayListBeacons);
                                }
//                        startService(new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class));
                            }

//                    startService(new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class));

                        } else if (employee.getManagerLocationList().get(i).getEmployeeBeaconList().size() > 0){
                            List<HCBeaconEmployee> arrayListBeacons = employee.getManagerLocationList().get(i).getEmployeeBeaconList();
                            if (arrayListBeacons != null && arrayListBeacons.size() > 0) {
//                        Intent serviceIntentBeacon = new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("BeaconList", (Serializable) arrayListBeacons);
//                        serviceIntentBeacon.putExtras(bundle);
//                        startService(serviceIntentBeacon);
                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                if (bluetoothAdapter.isEnabled()){
                                    detectBeacon(arrayListBeacons);
                                } else {
                                    showBluetoothEnableDialog(arrayListBeacons);
                                }
//                        startService(new Intent(EmployeeHCDashboardActivity.this, BeaconMonitorService.class));
                            }
                        }


// raj
                        //  Toast.makeText(getApplicationContext(),Double.toString(longi)+"\n"+Double.toString(lati)+"\n"+Double.toString(location.getLongitude())+"\n"+Double.toString(location.getLatitude()),Toast.LENGTH_LONG).show();

//                    Toast.makeText(getApplicationContext(), "You are   " + Float.toString(meters) + "  meters away from your scheduled location. ", Toast.LENGTH_LONG).show();

//                       getScheduling();

                    }
                }

            }
        }
*/


        setOneWordTextView(0);

        //noinspection deprecation
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setOneWordTextView(position);
            }
        });


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    private void showBluetoothEnableDialog(final List<HCBeaconEmployee> arrayListBeacons) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("You need to turn on your device bluetooth for attendence");

        /*alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });*/

        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                bluetoothAdapter.enable();
                detectBeacon(arrayListBeacons);
                Toast.makeText(EmployeeHCDashboardActivity.this, "Bluetooth turned on", Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void detectBeacon(final List<HCBeaconEmployee> arrayListBeacon) {
        beaconManager = new BeaconManager(EmployeeHCDashboardActivity.this);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
//                sendPresence(beacons);
                beaconListToCheckFromExit = beacons;

//                if (!isAttendenceEntered) {
                    if (matchDates()) {
                        if (arrayListBeacon.size() > 0) {
                            beaconListToSend.clear();
                            for (int x = 0; x < arrayListBeacon.size(); x++) {
                                HCBeaconEmployee beaconEmployee = arrayListBeacon.get(x);
                                for (int i = 0; i < beacons.size(); i++) {
                                    Beacon beacon = beacons.get(i);
                                    if (Integer.parseInt(beaconEmployee.getBeaconMajor()) == beacon.getMajor() &&
                                            Integer.parseInt(beaconEmployee.getBeaconMinor()) == beacon.getMinor()) {
                                        double beaconDistance = Utils.computeAccuracy(beacons.get(i));
                                        double locationMinDistance = Double.parseDouble(employee.getManagerLocationList().get(0).getDistance());
                                        if (locationMinDistance >= beaconDistance) {
                                            beaconListToSend.add(beacon);
                                        }
//                                        Toast.makeText(EmployeeHCDashboardActivity.this, "Entered beacon region \n Major = " + beacon.getMajor() + " Minor = " + beacon.getMinor(), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }

                            }
                            if (beaconListToSend.size() > 0) {
//                                isAttendenceEntered = true;
//                                sendPresence(beaconListToSend);
                                if (!scheduleList.get(schedulePosition).scheduleID.equalsIgnoreCase(oldScheduleID)) {
                                Date date = new Date();
                                String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                                SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm:ss a");
                                String time = sdfTime.format(date);
                                updateAttendance("P", modifiedDate, scheduleList.get(schedulePosition).scheduleID);
                                sendNotificationToManager(time, scheduleList.get(schedulePosition).scheduleID);
//                                new AsyncAddAttendenceByBeacon().execute(scheduleList.get(schedulePosition).locationID,
//                                        scheduleList.get(schedulePosition).managerID, scheduleList.get(schedulePosition).employeeID,
//                                        scheduleList.get(schedulePosition).startDate, "P");
                                    startTimer(arrayListBeacon);
                                }
                            } else {
//                                isAttendenceEntered = false;
                            }
                        }
                    }
//                }

            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

                /*if (arrayListBeacon.size() > 0) {
                    for (int x = 0; x < arrayListBeacon.size(); x++) {
                        HCBeaconEmployee beaconEmployee = arrayListBeacon.get(x);
                        for (int i = 0; i < list.size(); i++) {
                            Beacon beacon = list.get(i);
                            if (Integer.parseInt(beaconEmployee.getBeaconMajor()) == beacon.getMajor() &&
                                    Integer.parseInt(beaconEmployee.getBeaconMinor()) == beacon.getMinor()) {
                                beaconListToSend.add(beacon);
                                Toast.makeText(EmployeeHCDashboardActivity.this, "Entered beacon region \n Major = " + beacon.getMajor() + " Minor = " + beacon.getMinor(), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }

                    }
                    if (beaconListToSend.size() > 0) {
                        sendPresence(beaconListToSend);
                    }
                }*/
            }

            @Override
            public void onExitedRegion(Region region) {
                if (beaconListToSend.size() > 0) {
                    List<Beacon> beaconListToSendExit = new ArrayList<Beacon>();
                    for (int i = 0; i < beaconListToSend.size(); i++) {
                        Beacon beaconEmployee = beaconListToSend.get(i);
                        boolean add = false;
                        if (beaconListToCheckFromExit.size() == 0) {
//                            isAttendenceEntered = false;
//                            Toast.makeText(EmployeeHCDashboardActivity.this, "Exit beacon region \n Major = " + beaconEmployee.getMajor() + " Minor = " + beaconEmployee.getMinor(), Toast.LENGTH_SHORT).show();
//                            sendPresence(beaconListToSend);
                            break;
                        } else {
                            for (int j = 0; j < beaconListToCheckFromExit.size(); j++) {
                                Beacon beacon = beaconListToCheckFromExit.get(j);
                                if (beaconEmployee.getMajor() == beacon.getMajor() || beaconEmployee.getMinor() == beacon.getMinor()) {
                                    add = false;
                                    break;
                                } else {
                                    add = true;
                                }
                            }
                            if (add) {
//                                Toast.makeText(EmployeeHCDashboardActivity.this, "Exit beacon region \n Major = " + beaconEmployee.getMajor() + " Minor = " + beaconEmployee.getMinor(), Toast.LENGTH_SHORT).show();
                                beaconListToSendExit.add(beaconEmployee);
                            }
                        }
                        if (beaconListToCheckFromExit.size() > 0 && beaconListToSendExit.size() > 0) {
//                            isAttendenceEntered = false;
//                            sendPresence(beaconListToSendExit);
                            beaconListToSend.clear();
                        }
                    }

                }
            }
        });
        try {
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    try {
                        beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                        beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS_REGION);
                    } catch (RemoteException e) {
                        Toast.makeText(EmployeeHCDashboardActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Cannot start ranging", e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private boolean timeConvertion() {

        boolean isLogin = false;

        String startTime = employee.getManagerLocationList().get(0).getStartTime();
        String endTime = employee.getManagerLocationList().get(0).getEndTime();

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
        String currentTime = sdf.format(new Date());

        if (currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0) {
            isLogin = true;
        }
        return isLogin;
    }



/*
    Calendar calendar1 = Calendar.getInstance();
    SimpleDateFormat formatter1 = new SimpleDateFormat("dd/M/yyyy h:mm");
    String currentDate = formatter1.format(calendar1.getTime());

    final String dateString = cursor.getString(4);
    final String timeString = cursor.getString(5);
    String datadb =dateString+" "+timeString;

//  Toast.makeText(context,"databse date:-"+datadb+"Current Date :-"+currentDate,Toast.LENGTH_LONG).show();

    if(currentDate.compareTo(datadb)>=0) {
        myCheckBox.setChecked(true);
        myCheckBox.setEnabled(false);
    }

*/


    private void sendPresence(final List<Beacon> beacons) {
        SharedPreferences preferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        String userData = preferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);

//        final User userNew = new Gson().fromJson(userData, User.class);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", employee.getUserId());

                StringBuilder builder = new StringBuilder();
                Iterator<Beacon> iterator = beacons.iterator();
                while (iterator.hasNext()) {
                    builder.append(iterator.next().getMacAddress());
                    if (iterator.hasNext()) {
                        builder.append(',');
                    }
                }
                map.put("macAddress", builder.toString());

                try {

                    new WebUtils().post(AppConstants.URL_SEND_PRESENCE_USING_BEACONS, map);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void setOneWordTextView(int current) {
        if (employee != null) {
            if (employee.getManagerLocationList().size() > current) {
                oneWordTextView.setText(String.valueOf(employee.getManagerLocationList().get(current).getLocationName().charAt(0)).toUpperCase());
            }

//            showMessageIfNoClass();
            onOffNotificationsSetImage();
        } else {
//            swipePage.setVisibility(View.GONE);

        }

    }

    private HCEmployee getTeacherClassOnThisPage() {
        return employee.getManagerLocationList().size() > mViewPager.getCurrentItem() ? employee.getManagerLocationList().get(mViewPager.getCurrentItem()) : null;
    }

    private void onOffNotificationsSetImage() {
        HCEmployee teacherClass = getTeacherClassOnThisPage();
        if (teacherClass == null) {
            return;
        }
        boolean isOn = userUtils.isClassNotificationOn(teacherClass.getId());
//        onOffNotificationImageView.setImageResource(isOn ? R.drawable.on : R.drawable.off);
    }

    private void showMessageIfNoClass() {
        if (employee != null) {
            swipePage.setVisibility(employee.getManagerLocationList().size() == 0 ? View.GONE : View.VISIBLE);
        }
    }
/*

    @Override
    protected void onResume() {
        super.onResume();
        User user1 = userUtils.getUserFromSharedPrefs();

        if (employee != null) {
            if(employee.getManagerLocationList().size()>0) {

                List<HCEmployee> mainList = employee.getManagerLocationList();
                List<HCEmployee> newList = employee.getManagerLocationList();
                getScheduling();
                matchDates();
                if (mainList != null && newList != null && mainList.size() != newList.size()) {
                    employee.getManagerLocationList().clear();
                    employee.getManagerLocationList().addAll(employee.getManagerLocationList());
                    baseViewPagerAdapter.notifyDataSetChanged();
                    setOneWordTextView(0);
                }
            }
        }

        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
        updateDataAsync();
    }
*/

    private synchronized boolean matchDates() {
        if (counterGetSchedule > 60){
            getScheduling();
            counterGetSchedule = 0;
        } else {
            counterGetSchedule++;
        }

        boolean isMatched = false;
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        for (int i = 0; i < scheduleList.size(); i++) {
            String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            int checkDate = modifiedDate.compareTo(scheduleList.get(i).date);
//            int checkDate2 = modifiedDate.compareTo(scheduleList.get(i).endDate);
//            Toast.makeText(getApplicationContext(),scheduleList.get(i).date+"\n"+modifiedDate+"\n"+Integer.toString(checkDate),Toast.LENGTH_LONG).show();

            if (checkDate == 0) {
//    Toast.makeText(getApplicationContext(),"Date Match Found"+Float.toString(meters),Toast.LENGTH_LONG).show();
                SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss aa");

                String time = sdfTime.format(date);
//    long time = date.getTime();
                String startTime = scheduleList.get(i).startTime;
                String endTime = scheduleList.get(i).endTime;
                /*if (meters < 5.0f && meters >= 0.f) {
//                    Toast.makeText(getApplicationContext(),"Attendance Updated",Toast.LENGTH_LONG).show();

                    updateAttendance("P", modifiedDate);
                    sendNotificationToManager(time, scheduleList.get(i).scheduleID);

                }*/
//                Toast.makeText(getApplicationContext(),time+"\n"+startTime,Toast.LENGTH_LONG).show();
               /* int checkTime = time.compareTo(startTime);
                int checkTime2 = time.compareTo(endTime);
                if (checkTime >= 0 && checkTime2 <= 0) {
//        Toast.makeText(getApplicationContext(),"Time Match Found",Toast.LENGTH_LONG).show();
                    isMatched = true;
                    *//*updateAttendance("P", modifiedDate);
                    sendNotificationToManager(time, scheduleList.get(i).scheduleID);*//*
                } else {
                    isMatched = false;
//sendMessage("Late by 30 Minutes");
                }*/

                if (employee.getUserId().matches(scheduleList.get(i).employeeID)) {

//                    if ((m < 25.0f && m >= 0.f)) {

                    try {
                        SimpleDateFormat sdfTimeNew = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
                        String dd = sdfTimeNew.format(new Date());
                        Date ddd = sdfTimeNew.parse(dd);
                        Date date1 = sdfTimeNew.parse(modifiedDate + " " + startTime.toLowerCase());
                        Date date2 = sdfTimeNew.parse(modifiedDate + " " + endTime.toLowerCase());

                        Log.v("The date1 is >>>>", "smallercasnklcwknkw");
                        if (ddd.after(date1) && ddd.before(date2)) {

//                                updateAttendance("P", modifiedDate);
//                                sendNotificationToManager(time, scheduleList.get(i).scheduleID);
                            isMatched = true;
                            schedulePosition = i;
                            Log.v("The date1 is >>>>", "smaller");
                            break;
                        } else {
                            isMatched = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                   /* } else {
                        try {
                            String dd = sdfTime.format(new java.util.Date());
                            Date ddd = sdfTime.parse(dd);
                            Date date1 = sdfTime.parse(startTime);
                            Date date2 = sdfTime.parse(endTime);

                            if (ddd.before(date2) || ddd.equals(date2)) {
                                updateAttendance("A", modifiedDate);
                                Log.v("The date1 is >>>>", "smaller");
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }*/
                }

            }
        }
        return isMatched;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.checklistBtn:

                if (employee != null) {
                    if (employee.getManagerLocationList().size() > 0) {

//                        Intent checklistIntent = new Intent(EmployeeHCDashboardActivity.this, HcModule_Employee_Check_List_Activity.class);
                        Intent checklistIntent = new Intent(EmployeeHCDashboardActivity.this, HCListOfChecklists.class);

                        Bundle bun = new Bundle();
                        bun.putInt("Index", mViewPager.getCurrentItem());
                        bun.putBoolean("Edit", true);
                        checklistIntent.putExtras(bun);

                        startActivity(checklistIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Location Added yet!!", Toast.LENGTH_LONG).show();
                    }
                    break;
                }

            case R.id.addLocationButton:
                if (employee != null) {
                    if (employee.getManagerLocationList().size() > 0) {
                        Intent i = new Intent(EmployeeHCDashboardActivity.this, HealthCareAddLocationActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Location Added yet!!", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            case R.id.notificationBtn:
//
                break;
            case R.id.messageBtn:
                if (employee != null) {
                    if (employee.getManagerLocationList().size() > 0) {
                        sendClassNotificationBtn();
                    } else {
                        Toast.makeText(getApplicationContext(), "No Location Added yet!!", Toast.LENGTH_LONG).show();
                    }
                }

                break;

            case R.id.schedulingBtn:
                if (employee != null) {
                    if (employee.getManagerLocationList().size() > 0) {

//                Intent scedulingIntent = new Intent(EmployeeHCDashboardActivity.this, CreateEmployeeHomeScheduleActivity.class);
//                startActivity(scedulingIntent);
                        Intent innm = new Intent(EmployeeHCDashboardActivity.this, HCModuleFamilyShowAppointmentActivity.class);
                        String locId = employee.getManagerLocationList().get(mViewPager.getCurrentItem()).getLocationId();
                        Bundle bun2 = new Bundle();
                        bun2.putString("LocationID", locId);
                        bun2.putString("UserType", "Employee");
                        innm.putExtras(bun2);
                        startActivity(innm);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Location Added yet!!", Toast.LENGTH_LONG).show();
                    }
                }

                break;
            case R.id.clientInfoBtn:
                if (employee != null) {
                    if (employee.getManagerLocationList().size() > 0) {

                        Intent clientInfo = new Intent(EmployeeHCDashboardActivity.this, HCModuleEmployee_ClientDetails.class);

                        startActivity(clientInfo);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Location Added yet!!", Toast.LENGTH_LONG).show();
                    }
                }

                break;
            case R.id.reportsHCLayout:
                Intent reportsIntent = new Intent(EmployeeHCDashboardActivity.this, HCModuleReportActivity.class);
                startActivity(reportsIntent);
                break;
            case R.id.locationsLayout:
                if (employee.getManagerLocationList().size() > 0) {

                    Intent in = new Intent(EmployeeHCDashboardActivity.this, HealthCareAddLocationActivity.class);
                    in.putExtra(HealthCareAddLocationActivity.EXTRA_TEACHER_CLASS_INDEX, mViewPager.getCurrentItem());

                    startActivity(in);
                }
                break;
            case R.id.navigationButton:
                if (employee.getManagerLocationList().size() > 0) {

                    textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                    navigationLayout.setAnimation(textAnimation);
                    navigationLayout.setVisibility(View.VISIBLE);
                }
                break;


        }
    }

    OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener() {
        public boolean onSwipeRight() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            setOneWordTextView(mViewPager.getCurrentItem());
            return true;
        }

        public boolean onSwipeLeft() {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            setOneWordTextView(mViewPager.getCurrentItem());
            return true;
        }
    };

    private void sendClassNotificationBtn() {
//        if (!haveStudentsInClass()) {
//            makeToast("Please add students!");
//            return;
//        }
        Intent intent = new Intent(EmployeeHCDashboardActivity.this, EmployeeHCSendMessageToOneLocation.class);
//        intent.putExtra(UserSendMessageToOneClass.EXTRA_SELECTED_CLASS_INDEX, mViewPager.getCurrentItem());
//        intent.putExtra(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
        Bundle bun = new Bundle();
        bun.putInt("Index", mViewPager.getCurrentItem());
        bun.putInt("UserType", 11);

        intent.putExtras(bun);
        startActivity(intent);
    }

    private void updateDataAsync() {
        userUtils = new UserUtils(EmployeeHCDashboardActivity.this);

        user = userUtils.getUserFromSharedPrefs();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("id", user.getUserId());
                hm.put("role", String.valueOf(UserRole.EmployeeHC.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_DATA_BY_ID_FROM_HCEMPLOYEE, hm);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Employee fm = new Employee(user);
                    List<HCEmployee> newList = DataUtils.getHCEmployeeLocationListFromJsonString(result);


//                    if(employee!=null) {
                    if (fm.getManagerLocationList().size() != newList.size()) {

                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

//                            userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
//                            userUtils.saveUserToSharedPrefs(user);

//                            baseViewPagerAdapter.notifyDataSetChanged();
                        setOneWordTextView(0);
                        employeeChecklist_Id = DataUtils.getHCEmployeeCheckListFromJsonString(result);
                    }
//                    }
                    else {
                        fm.getManagerLocationList().clear();
                        fm.getManagerLocationList().addAll(newList);

                        setOneWordTextView(0);
                        employeeChecklist_Id = DataUtils.getHCEmployeeCheckListFromJsonString(result);
                    }
                    employee = fm;
                    userUtils.saveUserWithDataToSharedPrefs(fm, Employee.class);
                    userUtils.saveUserToSharedPrefs(user);
                    Log.v("Inside UPdate", "bla bla");
                }
            }
        }.execute();

    }

    boolean isCallFirst = true;

    private void getScheduling() {
        new AsyncTask<Void, Void, String>() {
//            private ProgressDialog progressDialog = new ProgressDialog(EmployeeHCDashboardActivity.this);

            @Override
            protected void onPreExecute() {
//                progressDialog.setMessage("Please wait...");
//                progressDialog.setCancelable(false);
//                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();

                hm.put("location_id", locationID);
//                hm.put("location_id","80");
//                hm.put("role", String.valueOf(userRole.getRole()));
                try {
                    return new WebUtils().post(AppConstants.URL_GET_EMPLOYEE_SCHEDULE, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
//                progressDialog.dismiss();
                if (result != null) {
                    try {
                        scheduleList.clear();
                        JSONObject jsn = new JSONObject(result);
                        if (jsn.getString("Message").matches("Records Found successfully")) {
                            JSONArray jsnObj = jsn.getJSONArray("Data");
                            for (int i = 0; i < jsnObj.length(); i++) {
                                HCModuleDayWeekMonthYearViewBean bean = new HCModuleDayWeekMonthYearViewBean();
//                                bean.startDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("startDate");
//                                bean.endDate = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("endDate");
                                bean.date = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("date");

                                bean.startTime = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("begin_time");
                                bean.endTime = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("end_time");
                                bean.scheduleID = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("id");
                                bean.employeeID = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("employ_id");
                                bean.managerID = jsnObj.getJSONObject(i).getJSONObject("EmployeeSchedule").getString("user_id");

                                bean.locationID = jsnObj.getJSONObject(i).getJSONObject("Location").getString("id");
                                bean.locationName = jsnObj.getJSONObject(i).getJSONObject("Location").getString("locationName");
                                scheduleList.add(bean);

                            }
//                            matchDates();
                            return;
                        } else {
                            scheduleList.add(new HCModuleDayWeekMonthYearViewBean());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (isCallFirst) {
                            getScheduling();
                        }
                        isCallFirst = false;
                    }

                }

            }
        }.execute();

    }


//    private void updateAttendance() {
//        new AsyncTask<Void, Void, String>() {
////            private ProgressDialog progressDialog = new ProgressDialog(EmployeeHCDashboardActivity.this);
//
//            @Override
//            protected void onPreExecute() {
////                progressDialog.setMessage("Please wait...");
////                progressDialog.setCancelable(false);
////                progressDialog.show();
//            }
//
//            @Override
//            protected String doInBackground(Void... params) {
//                HashMap<String, String> hm = new HashMap<>();
//
//                hm.put("location_id",locationID);
////                hm.put("location_id","80");
////                hm.put("role", String.valueOf(userRole.getRole()));
//                try {
//                    return new WebUtils().post(AppConstants.URL_GET_EMPLOYEE_SCHEDULE, hm);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
////                progressDialog.dismiss();
//                if (result != null) {
//                    try {
//
//                        JSONObject jsn = new JSONObject(result);
//                        if(jsn.getString("Message").matches("Records Found successfully"))
//                        {
//                            JSONArray jsnObj = jsn.getJSONArray("Data");
//                            for(int i=0; i<jsnObj.length(); i++)
//                            {
//                                HCModuleDayWeekMonthYearViewBean bean = new HCModuleDayWeekMonthYearViewBean();
//                                bean.startDate = jsnObj.getJSONObject(i).getJSONObject("HcScheduleEmployee").getString("startDate");
//                                bean.endDate = jsnObj.getJSONObject(i).getJSONObject("HcScheduleEmployee").getString("endDate");
//                                bean.startTime = jsnObj.getJSONObject(i).getJSONObject("HcScheduleEmployee").getString("startTime");
//                                bean.endTime = jsnObj.getJSONObject(i).getJSONObject("HcScheduleEmployee").getString("endTime");
//                                bean.startTime = jsnObj.getJSONObject(i).getJSONObject("Location").getString("id");
//                                bean.endTime = jsnObj.getJSONObject(i).getJSONObject("Location").getString("locationName");
//                                scheduleList.add(bean);
//
//                            }}
//                        else
//                        {
//                            scheduleList.add(new HCModuleDayWeekMonthYearViewBean());
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//            }
//        }.execute();
//
//    }


    private void sendNotificationToManager(String time, String scheID) {
        user = userUtils.getUserFromSharedPrefs();
        final String message = time;
        if (message.length() < 1) {
            return;
        }
        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("current_time", message);
//        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("location_id", locationID);
        stringStringHashMap.put("schedule_id", scheID);
        stringStringHashMap.put("teacher_id", user.getUserId());
        stringStringHashMap.put("user_id", managerID);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(AppConstants.URL_SEND_NOTIFICATION_TO_MANAGER, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {

//                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
//                    String currentDateandTime = sdf.format(new Date());
//                    HCModuleNotifications obj = new HCModuleNotifications();
//                    obj.message = message;
//                    obj.messageDate = currentDateandTime;
////                    obj.senderName = user.getUsername();
////                   obj.receiverName = "reciever";
//                    hcModuleNotificationsArrayList.add(obj);
//                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();


    }


    private void updateAttendance(String attend, String scheduleDate, String scheID) {
        user = userUtils.getUserFromSharedPrefs();

        final String message = attend;
        if (message.length() < 1) {
            return;
        }
        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("attend", message);
//        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("location_id", locationID);
        stringStringHashMap.put("schedule_date", scheduleDate);
        stringStringHashMap.put("employee_id", user.getUserId());
        stringStringHashMap.put("employee_schedule_id", scheID);
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(AppConstants.URL_UPDATE_ATTENDANCE_OF_EMP, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.v("Result", "result=== " + result);
//                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
//                    String currentDateandTime = sdf.format(new Date());
//                    HCModuleNotifications obj = new HCModuleNotifications();
//                    obj.message = message;
//                    obj.messageDate = currentDateandTime;
////                    obj.senderName = user.getUsername();
////                   obj.receiverName = "reciever";
//                    hcModuleNotificationsArrayList.add(obj);
//                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();


    }


    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = (dist * 1.609344) / 1000;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        } else if (unit == "M") {
            dist = (dist * 1.609344);
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    This function converts decimal degrees to radians                         :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    This function converts radians to decimal degrees                         :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


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

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdate();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdate();
    }

    protected void startLocationUpdate() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        AppGlobals.mCurrentLocation = location;
        if (serviceState == 0 && serviceIntent != null) {
            serviceState = 1;
            if (!employee.getManagerLocationList().get(0).getLocationType().equalsIgnoreCase("beacon_location"))
                startService(serviceIntent);
        }
        mLocationUpdate = DateFormat.getTimeInstance().format(new Date());
        Log.e("mCurrentLocation", AppGlobals.mCurrentLocation.getLatitude() + "<<?>>" + AppGlobals.mCurrentLocation.getLongitude());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    private class AsyncAddAttendenceByBeacon extends AsyncTask<String, String, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //        location_id, user_id, employee_id, startDate (yyyy-mm-dd) ,attends
        @Override
        protected String doInBackground(String... strings) {
//            2016-04-12 04:04:58
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String currentDateandTime = sdf.format(new Date());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("location_id", strings[0]));
            nameValuePairs.add(new BasicNameValuePair("user_id", strings[1]));
            nameValuePairs.add(new BasicNameValuePair("employee_id", strings[2]));
            nameValuePairs.add(new BasicNameValuePair("startDate", strings[3]));
            nameValuePairs.add(new BasicNameValuePair("attend", strings[4]));
            nameValuePairs.add(new BasicNameValuePair("employee_schedule_id", strings[5]));
            nameValuePairs.add(new BasicNameValuePair("datetime", currentDateandTime));

            response = new WebServiceHandler().webServiceCall(nameValuePairs, AppConstants.URL_ADD_ATTENDENCE_BY_BEACON);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(EmployeeHCDashboardActivity.this, "Attendence updated on server", Toast.LENGTH_LONG).show();
        }
    }

//
//    new CountDownTimer(Long.MAX_VALUE, 1000 * 15)

    private void startTimer(final List<HCBeaconEmployee> arrayListBeacon) {
        oldScheduleID = scheduleList.get(schedulePosition).scheduleID;
        countDownTimer = null;
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000 * 1 * 60) {
            public void onTick(long millisUntilFinished) {
                if (matchDates()) {
                    if (bluetoothAdapter.isEnabled()) {
                        if (arrayListBeacon.size() > 0) {
//                        beaconListToSend.clear();
                            breakLoop = false;
                            for (int x = 0; x < arrayListBeacon.size(); x++) {
                                HCBeaconEmployee beaconEmployee = arrayListBeacon.get(x);
                                for (int i = 0; i < beaconListToCheckFromExit.size(); i++) {
                                    Beacon beacon = beaconListToCheckFromExit.get(i);
                                    if (Integer.parseInt(beaconEmployee.getBeaconMajor()) == beacon.getMajor() &&
                                            Integer.parseInt(beaconEmployee.getBeaconMinor()) == beacon.getMinor()) {
                                        double beaconDistance = Utils.computeAccuracy(beaconListToCheckFromExit.get(i));
                                        double locationMinDistance = Double.parseDouble(employee.getManagerLocationList().get(0).getDistance());
                                        Toast.makeText(EmployeeHCDashboardActivity.this, "You are " + beaconDistance + " meters away from your scheduled location.", Toast.LENGTH_LONG).show();
                                        if (locationMinDistance >= beaconDistance) {
                                            new AsyncAddAttendenceByBeacon().execute(scheduleList.get(schedulePosition).locationID,
                                                    scheduleList.get(schedulePosition).managerID, scheduleList.get(schedulePosition).employeeID,
                                                    scheduleList.get(schedulePosition).startDate, "P", scheduleList.get(schedulePosition).scheduleID);
//                                Toast.makeText(EmployeeHCDashboardActivity.this, "Entered beacon region \n Major = " + beacon.getMajor() + " Minor = " + beacon.getMinor(), Toast.LENGTH_SHORT).show();
                                            breakLoop = true;
                                        }
                                        break;
                                    }
                                }
                                if (breakLoop) {
                                    break;
                                }
                            }
                            if (!breakLoop) {
                                for (int i = 0; i < 5; i++) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (isSendAttendence(arrayListBeacon)) {
                                        breakLoop = true;
                                        break;
                                    }
                                }
                            }


                            if (!breakLoop) {
                                new AsyncAddAttendenceByBeacon().execute(scheduleList.get(schedulePosition).locationID,
                                        scheduleList.get(schedulePosition).managerID, scheduleList.get(schedulePosition).employeeID,
                                        scheduleList.get(schedulePosition).startDate, "A", scheduleList.get(schedulePosition).scheduleID);
                            }
                        }
                    } else {
                        new AsyncAddAttendenceByBeacon().execute(scheduleList.get(schedulePosition).locationID,
                                scheduleList.get(schedulePosition).managerID, scheduleList.get(schedulePosition).employeeID,
                                scheduleList.get(schedulePosition).startDate, "A", scheduleList.get(schedulePosition).scheduleID);
                    }
                }
            }

            public void onFinish() {
            }

        }.start();
    }


    private boolean isSendAttendence(final List<HCBeaconEmployee> arrayListBeacon) {
        boolean isSend = false;
        for (int x = 0; x < arrayListBeacon.size(); x++) {
            HCBeaconEmployee beaconEmployee = arrayListBeacon.get(x);
            for (int i = 0; i < beaconListToCheckFromExit.size(); i++) {
                Beacon beacon = beaconListToCheckFromExit.get(i);
                if (Integer.parseInt(beaconEmployee.getBeaconMajor()) == beacon.getMajor() &&
                        Integer.parseInt(beaconEmployee.getBeaconMinor()) == beacon.getMinor()) {
                    new AsyncAddAttendenceByBeacon().execute(scheduleList.get(schedulePosition).locationID,
                            scheduleList.get(schedulePosition).managerID, scheduleList.get(schedulePosition).employeeID,
                            scheduleList.get(schedulePosition).startDate, "P", scheduleList.get(schedulePosition).scheduleID);
//                                Toast.makeText(EmployeeHCDashboardActivity.this, "Entered beacon region \n Major = " + beacon.getMajor() + " Minor = " + beacon.getMinor(), Toast.LENGTH_SHORT).show();
                    isSend = true;
                    break;
                }
                if (isSend) {
                    break;
                }
            }
        }
        return isSend;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}