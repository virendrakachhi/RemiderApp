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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.activities.AlertActivity;
import com.attendanceapp.models.HCModuleNotifications;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.TeacherClass;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class HCManagerSendNotificationActivity extends Activity implements View.OnClickListener {

    public static final String EXTRA_STUDENT_CLASS_INDEX = "EXTRA_STUDENT_CLASS_INDEX";
    public static final String EXTRA_TEACHER_CLASS = "EXTRA_STUDENT_CLASS";
    public static final String EXTRA_HIDE_MESSAGE_BOX = "EXTRA_HIDE_MESSAGE_BOX";
    public static final String EXTRA_SHOW_NOTIFICATIONS = "EXTRA_SHOW_NOTIFICATIONS";
    private static final String TAG = HCManagerSendNotificationActivity.class.getSimpleName();
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;

    ListView messagesListView;
    EditText messageEditText;
    TextView classNameTextView;
    SharedPreferences sharedPreferences;
    Button sendMessageButton;
    String locationID;
    ManagerHCClass managerLocData;
    User user;
    ArrayList<HCModuleNotifications> hcModuleNotificationsArrayList = new ArrayList<>();
    ListAdapter listAdapter;
    NotificationListAdapter notificationListAdapter;
    TeacherClass teacherClass;
    //    String allStudentEmailsInClass;
//    String allStudentIdsInClass;
    UserUtils userUtils;

    boolean hideMessageBox, showNotifications;

    private static String notificationJSON = "";
    private static int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_module_notification);
        messageEditText = (EditText) findViewById(R.id.editMessage);
        messagesListView = (ListView) findViewById(R.id.messagesList);
        classNameTextView = (TextView) findViewById(R.id.className);
        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
        classNameTextView.setText("Notifications");
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(HCManagerSendNotificationActivity.this);
        sharedPreferences.edit().putBoolean("Image Status", false).commit();
//        userUtils = new UserUtils(this);
//        user = userUtils.getUserFromSharedPrefs();

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

        index = getIntent().getExtras().getInt("Index");
        int type = getIntent().getExtras().getInt("UserType");
//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
//        if(type == 10) {
        String location_id = "";
        try {
            location_id = getIntent().getStringExtra("location_id");
        } catch (Exception e){
            e.printStackTrace();
        }
        if (location_id.trim().length() > 0){
            locationID = location_id;
        } else {
            managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
            locationID = managerLocData.getId();
        }

//        }
//        else if(type==11){
//            HCEmployee obj  = userUtils.getUserWithDataFromSharedPrefs(Employee.class).getManagerLocationList().get(index);
//            locationID = obj.getLocationId();
//
//        }
//        else if(type==12)
//        {
//            HCFamily obj  = userUtils.getUserWithDataFromSharedPrefs(Family.class).getManagerLocationList().get(index);
//            locationID = obj.getLocationId();
//
//        }
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(HCManagerSendNotificationActivity.this, EmployeeHCSendMessageToOneLocation.class);
                Bundle bun = new Bundle();
                bun.putInt("Index", index);
                bun.putInt("UserType", 10);
                bun.putString("ChatType", "Single");
                bun.putString("User", hcModuleNotificationsArrayList.get(i).senderName);
                bun.putString("ReceiverID", hcModuleNotificationsArrayList.get(i).senderID);
                intent.putExtras(bun);
//        intent.putExtra(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
                startActivity(intent);

            }
        });

        hideMessageBox = getIntent().getBooleanExtra(EXTRA_HIDE_MESSAGE_BOX, false);
        showNotifications = getIntent().getBooleanExtra(EXTRA_SHOW_NOTIFICATIONS, false);
        updateDataAsync();
//        hcModuleNotificationsArrayList.add(new HCModuleNotifications("Ricky","Me","","Hey folks ","19/01/2016 05:35"));
//        hcModuleNotificationsArrayList.add(new HCModuleNotifications("Me","Raj","","Hi Ricky ","19/01/2016 05:36"));
//        hcModuleNotificationsArrayList.add(new HCModuleNotifications("Vicky","Me","","Hi Guys... ","19/01/2016 05:37"));
//        hcModuleNotificationsArrayList.add(new HCModuleNotifications("Me","Raj","","How are you Guys","19/01/2016 05:38"));
        sendMessageButton.setOnClickListener(this);
//        setAdapter();


    }

    private void setAdapter(boolean isNotify) {
//        if (showNotifications) {
        if (isNotify){
            notificationListAdapter.notifyDataSetChanged();
        } else {
            notificationListAdapter = new NotificationListAdapter(HCManagerSendNotificationActivity.this, hcModuleNotificationsArrayList);
            messagesListView.setAdapter(notificationListAdapter);
        }

//        } else {
//            listAdapter = new ListAdapter(HCManagerSendNotificationActivity.this, hcModuleNotificationsArrayList);
//            messagesListView.setAdapter(listAdapter);
//        }

    }


    private void updateDataAsync() {

        new AsyncTask<Void, Void, Void>() {
            private String result;
            ProgressDialog progressDialog = new ProgressDialog(HCManagerSendNotificationActivity.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {

                HashMap<String, String> hm = new HashMap<>();
//                hm.put("user_id", teacherClass.getTeacherId());
                hm.put("location_id", locationID);

                try {

//                    if (showNotifications) {
////                        hm.put("class_id", teacherClass.getId());
//                        hm.put("class_id", "caks");
//                        result = new WebUtils().post(AppConstants.URL_CLASS_NOTIFICATION_LIST, hm);
//
//                    } else {
//                        hm.put("class_code", teacherClass.getClassCode());
//                        hm.put("class_code","cABK");
                    result = new WebUtils().post(AppConstants.URL_GET_SAVED_HC_NOTIFICATION, hm);
                    System.out.println(""+result);
//                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
                if (result != null) {
                    try {
                        notificationJSON = result;
                  /*  if (showNotifications) {
                        hcModuleNotificationsArrayList = DataUtils.getNotificationsArrayList(result);
                    } else {
                        hcModuleNotificationsArrayList = DataUtils.getMessagesArrayList(result);
                    }*/

//                    sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getClassCode(), result).apply();
//                    sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + "JLKCSAH", result).apply();

                        boolean isNotify = false;
                        JSONObject jsonObject = new JSONObject(result);
                       // result= {"Error":"No Record Found"}

                        JSONArray jsnArr = jsonObject.getJSONArray("Data");
                        if (hcModuleNotificationsArrayList.size() > 0){
                            isNotify = true;
                        }
                        hcModuleNotificationsArrayList.clear();
                        for (int i = 0; i < jsnArr.length(); i++) {
                            String chatMessage = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("message");
                            String chatMessageLeft = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("message_left");
                            String sentDate = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("created");
                            String notificationID = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("id");
                            String senderName = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("employee_name");
                            String senderID = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("hc_employee_id");
                            String locationID = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("location_id");
                            String archived = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("archived");

                            HCModuleNotifications obj = new HCModuleNotifications();
                            obj.message = chatMessage;
                            obj.messageLeft = chatMessageLeft;
                            obj.senderName = senderName;
                            obj.messageDate = sentDate;
                            obj.notificationID = notificationID;
                            obj.senderID = senderID;
                            obj.locationID = locationID;
                            obj.archived = archived;
                            hcModuleNotificationsArrayList.add(obj);
                            /*if (archived.equalsIgnoreCase("0")){
                                hcModuleNotificationsArrayList.add(obj);
                            }*/
                        }

                        setAdapter(isNotify);
                        if (hcModuleNotificationsArrayList.size() == 0) {
                            findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        hcModuleNotificationsArrayList.clear();
                        setAdapter(false);
                    }
                }
            }
        }.execute();
    }

    public void gotoBack(View view) {
        finish();
    }

    private void makeToast(String title) {
        Toast.makeText(HCManagerSendNotificationActivity.this, title, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                sendMessage();
                break;

            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;


        }
    }

    String result = null;

    private void sendMessage() {
        final String message = messageEditText.getText().toString().trim();
        if (message.length() < 1) {
            makeToast("Please enter text before send");
            return;
        }
        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("notification_msg", message);
//        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("location_id", locationID);
//        stringStringHashMap.put("student_id", allStudentIdsInClass);
        stringStringHashMap.put("manager_id", user.getUserId());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                messageEditText.setText("");
                result = null;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    result = new WebUtils().post(AppConstants.URL_SAVE_HC_NOTIFICATION, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
                    String currentDateandTime = sdf.format(new Date());
                    HCModuleNotifications obj = new HCModuleNotifications();
                    obj.message = message;
                    obj.messageDate = currentDateandTime;
//                    obj.senderName = user.getUsername();
//                   obj.receiverName = "reciever";
                    hcModuleNotificationsArrayList.add(obj);
                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();


    }

    private class ListAdapter extends ArrayAdapter<HCModuleNotifications> {
        LayoutInflater inflater;

        public ListAdapter(Context context, ArrayList<HCModuleNotifications> messagesStringArrayList) {
            super(context, 0, messagesStringArrayList);
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }


        class ViewHolder {
            final TextView time;
            final TextView message;

            public ViewHolder(View view) {
                message = (TextView) view.findViewById(R.id.message);
                time = (TextView) view.findViewById(R.id.time);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_class_message, null, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            holder = (ViewHolder) view.getTag();
            HCModuleNotifications classMessage = getItem(position);

            holder.message.setText(classMessage.message);
            holder.time.setText(classMessage.messageTime);

            return view;
        }
    }


    private class NotificationListAdapter extends ArrayAdapter<HCModuleNotifications> {
        LayoutInflater inflater;

        public NotificationListAdapter(Context context, ArrayList<HCModuleNotifications> messagesStringArrayList) {
            super(context, 0, messagesStringArrayList);
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        class ViewHolder {
            final TextView time;
            final TextView message;
            final TextView messageLeft;
            final TextView empName;
            final ImageView deleteButton;
            final ImageView archiveButton;
            final ImageView alertButton;

            public ViewHolder(View view) {
                message = (TextView) view.findViewById(R.id.message);
                time = (TextView) view.findViewById(R.id.time);
                empName = (TextView) view.findViewById(R.id.emp_name);

                messageLeft = (TextView) view.findViewById(R.id.messageLeft);

                deleteButton = (ImageView) view.findViewById(R.id.deleteButton);
                archiveButton = (ImageView) view.findViewById(R.id.archiveButton);
                alertButton = (ImageView) view.findViewById(R.id.alertButton);
            }

        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_class_notification, null, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            holder = (ViewHolder) view.getTag();
            HCModuleNotifications hcModuleNotifications = getItem(position);

            holder.message.setText(hcModuleNotifications.message);
            holder.messageLeft.setText(hcModuleNotifications.messageLeft);

            holder.time.setText(changeDateFormat(hcModuleNotifications.messageDate));
            holder.empName.setText(hcModuleNotifications.senderName);


            holder.deleteButton.setOnClickListener(onClickListener);

            holder.alertButton.setOnClickListener(onClickListener);
            holder.archiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(HCManagerSendNotificationActivity.this, AlertActivity.class);
                    intent.putExtra(AppConstants.INTENT_NOTIFICATION_JSON, notificationJSON);
                    intent.putExtra("Index", index);
                    startActivity(intent);
                }
            });

            holder.deleteButton.setTag(position);
            holder.archiveButton.setTag(position);
            holder.alertButton.setTag(position);

            return view;
        }

        /*String DATE_FORMAT_NOW = "hh:mm a | EEEE, dd MMMM, yyyy";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
        Date date = originalFormat.parse(strDatePosted);
        strDatePosted = targetFormat.format(date);*/

        private String changeDateFormat(String dateString){
            String formatedDate = "";
            try {
                SimpleDateFormat sdfOldDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                SimpleDateFormat sdfNewDate = new SimpleDateFormat("M/d/yyyy hh:mm:ss");
                Date date = sdfOldDate.parse(dateString);
                formatedDate = sdfNewDate.format(date);
            } catch (Exception e){
                e.printStackTrace();
            }

            return formatedDate;
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer index = (Integer) view.getTag();

                String notificationId = getItem(index).notificationID;

                if (view.getId() == R.id.deleteButton) {
                    deleteButton(notificationId);
                }

//                else if (view.getId() == R.id.archiveButton) {
//                    archiveButton(notificationId);
//
//                } else if (view.getId() == R.id.alertButton) {
//                    alertButton(notificationId);
//                }
            }
        };

        private void deleteButton(final String notificationId) {
            new AlertDialog.Builder(HCManagerSendNotificationActivity.this)
                    .setMessage("Delete notification!")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            final String url = AppConstants.URL_DELETE_NOTIFICATION;

                            new AsyncTask<Void, Void, String>() {
                                ProgressDialog progressDialog = new ProgressDialog(HCManagerSendNotificationActivity.this);

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
                                    map.put("id", notificationId);

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
                                                updateDataAsync();
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

        private void archiveButton(String index) {

        }

        private void alertButton(String index) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
        updateDataAsync();
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


}
