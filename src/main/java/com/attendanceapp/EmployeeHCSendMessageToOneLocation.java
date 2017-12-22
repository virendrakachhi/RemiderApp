package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.Employee;
import com.attendanceapp.models.Family;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.HCFamily;
import com.attendanceapp.models.HCModuleMessage;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.ScheduleStatus;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.NavigationPage;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class EmployeeHCSendMessageToOneLocation extends Activity implements View.OnClickListener {

    public static final String EXTRA_SELECTED_CLASS_INDEX = "EXTRA_STUDENT_CLASS_INDEX";
    public static final String EXTRA_HIDE_MESSAGE_BOX = "EXTRA_HIDE_MESSAGE_BOX";
    ListView messagesListView;
    Button sendMessageButton;
    EditText messageEditText;
    int mtype;
    String senderImage, imageSenderID;
    LinearLayout editMessagelayout;
    TextView classNameTextView;
    SharedPreferences sharedPreferences;
    String locationID, ChatType, receiverName, receiverID;
    ManagerHCClass managerLocData;
    TextView noNotiTV;
    ArrayList<HCModuleMessage> hcModuleMessageArrayList = new ArrayList<>();
    ListAdapter listAdapter;
    String allStudentEmailsInClass;
    String allStudentIdsInClass;
    boolean hideMessageBox;

    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    private UserUtils userUtils;


    private ClassEventCompany teacherClass;
    public static User user;
    private UserRole userRole;

    public static boolean isOnChatScreen = false;

    public static String receiverIDNotify = "";

    MyMessageReceiver myReceiver;
    public static String MY_MESSAGE_ACTION = "MY_MESSAGE_ACTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_hc_send_message_to_one_location);

        messageEditText = (EditText) findViewById(R.id.editMessage);
        messagesListView = (ListView) findViewById(R.id.messagesList);
        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
        classNameTextView = (TextView) findViewById(R.id.className);
        noNotiTV = (TextView) findViewById(R.id.noNotifications);
        editMessagelayout = (LinearLayout) findViewById(R.id.linearLayout2);
        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        sharedPreferences.edit().putBoolean("Image Status", false).commit();

        editMessagelayout.setVisibility(View.GONE);
        //        userUtils = new UserUtils(this);
//        user = userUtils.getUserFromSharedPrefs();


        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(this);

        int index = getIntent().getExtras().getInt("Index");
        mtype = getIntent().getExtras().getInt("UserType");
        boolean pushStatus = getIntent().getBooleanExtra("push", false);

//        sharedPreferences = AndroidUtils.getCommonSharedPrefs(EmployeeHCSendMessageToOneLocation.this);
        if (mtype == 10) {
            editMessagelayout.setVisibility(View.VISIBLE);
            ChatType = getIntent().getExtras().getString("ChatType");

            if (ChatType.matches("Multiple")) {
                classNameTextView.setText("Chat");
                managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
                if (pushStatus) {
                    locationID = getIntent().getExtras().getString("LocationID");
                } else
                    locationID = managerLocData.getId();
                getMessages(mtype);
            } else {
                receiverName = getIntent().getExtras().getString("User");
                receiverID = getIntent().getExtras().getString("ReceiverID");
                managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
                if (pushStatus) {
                    locationID = getIntent().getExtras().getString("LocationID");
                } else
                    locationID = managerLocData.getId();
                classNameTextView.setText(receiverName);
                getMessages(mtype);
            }

        } else if (mtype == 11) {
            editMessagelayout.setVisibility(View.VISIBLE);
//           receiverName = getIntent().getExtras().getString("User");


            HCEmployee obj = userUtils.getUserWithDataFromSharedPrefs(Employee.class).getManagerLocationList().get(index);
            if (pushStatus) {
                locationID = getIntent().getExtras().getString("LocationID");
            } else
                locationID = obj.getLocationId();


            receiverID = obj.getManagerID();
            receiverName = obj.getName();
            classNameTextView.setText(obj.getLocationName());

            getMessages(mtype);

        } else if (mtype == 12) {
            HCFamily obj = userUtils.getUserWithDataFromSharedPrefs(Family.class).getManagerLocationList().get(index);
            if (pushStatus) {
                locationID = getIntent().getExtras().getString("LocationID");
            } else
                locationID = obj.getLocationId();
            classNameTextView.setText(obj.getLocationName());

            getMessages(mtype);

        }

//        hcModuleMessageArrayList.add(new HCModuleMessage("Ricky","Me","","Hey folks ","19/01/2016 05:35"));
//        hcModuleMessageArrayList.add(new HCModuleMessage("Me","Raj","","Hi Ricky ","19/01/2016 05:36"));
//        hcModuleMessageArrayList.add(new HCModuleMessage("Vicky","Me","","Hi Guys... ","19/01/2016 05:37"));
//        hcModuleMessageArrayList.add(new HCModuleMessage("Me","Raj","","How are you Guys","19/01/2016 05:38"));
        sendMessageButton.setOnClickListener(this);
//        setAdapter();


//        userRole = UserRole.valueOf(getIntent().getIntExtra(AppConstants.EXTRA_USER_ROLE, -1));
//
//        hideMessageBox = getIntent().getBooleanExtra(EXTRA_HIDE_MESSAGE_BOX, false);
//        int index = getIntent().getIntExtra(EXTRA_SELECTED_CLASS_INDEX, -1);
//
//
//      teacherClass = userUtils.getUserFromSharedPrefs().getClassEventCompanyArrayList().get(index);
//
//
//        String savedMessages = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getUniqueCode(), null);
//        if (savedMessages != null) {
//            hcModuleMessageArrayList = DataUtils.getMessagesArrayList(savedMessages);
//        }
//
//
//
//
//        classNameTextView.setText(teacherClass.getName());
//        allStudentEmailsInClass = StringUtils.getAllEmailsFromStudentList(teacherClass.getUsers(), ',');
//        allStudentIdsInClass = StringUtils.getAllIdsFromStudentList(teacherClass.getUsers(), ',');
//
//        // get all last messages
//        updateDataAsync();
//
//        if (hideMessageBox) {
//            findViewById(R.id.linearLayout2).setVisibility(View.GONE);
//        }
//


    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        //Register BroadcastReceiver
        myReceiver = new MyMessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_MESSAGE_ACTION);
        registerReceiver(myReceiver, intentFilter);

        /*Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        sendBroadcast(intent);*/

        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(myReceiver);
        super.onStop();
    }


    private void setAdapter() {
        listAdapter = new ListAdapter(getApplicationContext(), hcModuleMessageArrayList);
        messagesListView.setAdapter(listAdapter);
        messagesListView.invalidate();

        try {
            messagesListView.setSelection(hcModuleMessageArrayList.size() - 1);
        } catch (Exception e){
            e.printStackTrace();
        }

        if (imageSenderID != null)
            getImage(imageSenderID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnChatScreen = true;
        receiverIDNotify = receiverID;
        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
        getMessages(mtype);
        // updateDataAsync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnChatScreen = false;
        receiverIDNotify = "";
    }

    private void getMessages(final int type) {

        new AsyncTask<Void, Void, Void>() {
            private String result;

            @Override
            protected Void doInBackground(Void... params) {
                // (user_id, class_code)if user login
                // (email, class_code)student_login
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationID);
                hm.put("sender_id", user.getUserId());
                hm.put("receiver_id", receiverID);

//                hm.put("class_code", teacherClass.getUniqueCode());

                try {
                    result = new WebUtils().post(AppConstants.URL_GET_SAVED_HC_CHAT, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsnArr = jsonObject.getJSONArray("Data");
                        hcModuleMessageArrayList.clear();
                        if (type == 11 || type == 12) {
                            for (int i = 0; i < jsnArr.length(); i++) {
                                String senderName = jsnArr.getJSONObject(i).getJSONObject("User").getString("username");
                                String senderEmail = jsnArr.getJSONObject(i).getJSONObject("User").getString("email");
                                String chatMessage = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("message");
                                String senderID = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("sender_id");
                                String sentDate = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("created");
                                String recID = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("receiver_id");

                                HCModuleMessage obj = new HCModuleMessage();
                                obj.senderName = senderName;
                                obj.senderID = senderID;
                                imageSenderID = senderID;
                                obj.message = chatMessage;
                                obj.senderEmail = senderEmail;
                                obj.messageDate = sentDate;
                                obj.receiverID = recID;
                                if (obj.receiverID.matches(user.getUserId()) || obj.receiverID.matches("0")) {
                                    hcModuleMessageArrayList.add(obj);
                                } else if (mtype == 11) {
                                    if (obj.senderID.matches(user.getUserId()))
                                        hcModuleMessageArrayList.add(obj);

                                }

                            }
                        } else {
                            if (receiverName == null) {
                                for (int i = 0; i < jsnArr.length(); i++) {
                                    String senderName = jsnArr.getJSONObject(i).getJSONObject("User").getString("username");
                                    String senderEmail = jsnArr.getJSONObject(i).getJSONObject("User").getString("email");
                                    String chatMessage = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("message");
                                    String senderID = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("sender_id");
                                    String sentDate = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("created");
                                    String recID = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("receiver_id");

                                    HCModuleMessage obj = new HCModuleMessage();
                                    obj.senderName = senderName;
                                    obj.senderID = senderID;
                                    obj.message = chatMessage;
                                    obj.senderEmail = senderEmail;
                                    obj.messageDate = sentDate;
                                    obj.receiverID = recID;
                                    if (recID.matches("0")) {
                                        hcModuleMessageArrayList.add(obj);
                                    }

                                }


//                                else {
//                                    hcModuleMessageArrayList.add(obj);
//                                }
                            } else {
                                for (int i = 0; i < jsnArr.length(); i++) {
                                    String senderName = jsnArr.getJSONObject(i).getJSONObject("User").getString("username");
                                    String senderEmail = jsnArr.getJSONObject(i).getJSONObject("User").getString("email");
                                    String chatMessage = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("message");
                                    String senderID = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("sender_id");
                                    String sentDate = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("created");
                                    String recID = jsnArr.getJSONObject(i).getJSONObject("Chat").getString("receiver_id");

                                    HCModuleMessage obj = new HCModuleMessage();
                                    obj.senderName = senderName;
                                    obj.senderID = senderID;
                                    obj.message = chatMessage;
                                    obj.senderEmail = senderEmail;
                                    obj.messageDate = sentDate;
                                    obj.receiverID = recID;
                                    if (recID.matches(receiverID)) {
                                        hcModuleMessageArrayList.add(obj);
                                    } else if (recID.matches(user.getUserId())) {
                                        hcModuleMessageArrayList.add(obj);
                                    }
                                }

                            }
                        }
//                    sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getUniqueCode(), result).apply();


                        if (hcModuleMessageArrayList.size() == 0) {
                            findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                setAdapter();
            }
        }.execute();
    }

    public void gotoBack(View view) {
        finish();
    }

    private void makeToast(String title) {
        Toast.makeText(EmployeeHCSendMessageToOneLocation.this, title, Toast.LENGTH_LONG).show();
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
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        final String currentDateandTime = sdf.format(new Date());

        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("message", message);
        stringStringHashMap.put("sender_id", user.getUserId());
        stringStringHashMap.put("created", currentDateandTime);

        if (receiverName != null) {
            stringStringHashMap.put("receiver_id", receiverID);

        } else {
            if (mtype == 10)
                stringStringHashMap.put("receiver_id", "0");
            else if (mtype == 11)
                stringStringHashMap.put("receiver_id", receiverID);
        }

        stringStringHashMap.put("location_id", locationID);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                messageEditText.setText("");
                result = null;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    result = new WebUtils().post(AppConstants.URL_SAVE_HC_CHAT, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {
                    HCModuleMessage obj = new HCModuleMessage();
                    obj.message = message;
                    obj.messageDate = currentDateandTime;
                    obj.senderName = user.getUsername();

                    hcModuleMessageArrayList.add(obj);

//                    hcModuleMessageArrayList.add(new HCModuleMessage(message, new Date().toString()));
                    listAdapter.notifyDataSetChanged();
                    messagesListView.invalidate();
                    try {
                        messagesListView.setSelection(hcModuleMessageArrayList.size() - 1);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
//                   getMessages();
                }
            }
        }.execute();


    }


    private void getImage(String uid) {
        Map<String, String> keysValues = new HashMap<>();
        keysValues.put("user_id", uid);

//        getPicAsyncTask("http://www.abdevs.com/attendance/Mobiles/get_user_image", keysValues);
        getPicAsyncTask(AppConstants.URL_GET_USER_IMAGE, keysValues);
    }

    private void getPicAsyncTask(final String url, final Map<String, String> map) {

        new AsyncTask<Void, Void, String>() {

            //  private ProgressDialog progressDialog = new ProgressDialog(TeacherDashboardActivity.this);

            @Override
            protected void onPreExecute() {
//                progressDialog.setMessage("Getting profile details...");
//                progressDialog.setCancelable(false);
//                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return new WebUtils().post(url, map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
//                progressDialog.dismiss();
//
                if (result == null) {
                    makeToast("Error in connection");
                } else {
                    try {


                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.has("Error")) {
                            makeToast(jsonObject.getString("Error"));
                        } else {


//                            String pic=jsonObject.getJSONObject("data").getString("full_image_url")+jsonObject.getJSONObject("data").getJSONObject("userdata").getString("profile_pic");
//                            String pic="http://182.71.22.43//attendance/app/webroot/bss_files/"+jsonObject.getJSONObject("data").getJSONObject("userdata").getString("profile_pic");
                            senderImage = AppConstants.IMAGE_BASE_URL + jsonObject.getJSONObject("data").getJSONObject("userdata").getString("profile_pic");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                    listAdapter.notifyDataSetChanged();
                    messagesListView.invalidate();

                }
            }

        }.execute();

    }


    private class ListAdapter extends BaseAdapter {
        private ArrayList<HCModuleMessage> messagesStringArrayList;
        LayoutInflater inflater;
        Context context;

        public ListAdapter(Context context, ArrayList<HCModuleMessage> messagesStringArrayList) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.messagesStringArrayList = messagesStringArrayList;
        }


        @Override
        public int getCount() {
            return messagesStringArrayList.size();
        }

        @Override
        public HCModuleMessage getItem(int position) {
            return messagesStringArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView message, time, senderName;
            ImageView photoImg;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.list_item_class_message, null, false);
                holder = new ViewHolder();

                holder.message = (TextView) view.findViewById(R.id.message);
                holder.time = (TextView) view.findViewById(R.id.time);
                holder.senderName = (TextView) view.findViewById(R.id.sender_name);
                holder.photoImg = (ImageView) view.findViewById(R.id.imgPhoto);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            HCModuleMessage classMessage = messagesStringArrayList.get(position);
            holder.message.setText(classMessage.message);
            Date modifiedDate = null;
//            String mdDate = null;
//            try {
//                modifiedDate = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").parse(classMessage.messageDate);
//
////                mdDate = mdDate + Integer.toString(modifiedDate.getMonth()) +"-"+ Integer.toString(modifiedDate.getDate()) +"-"+Integer.toString(modifiedDate.getYear()) + " " +Integer.toString(modifiedDate.getHours()) + ":"+Integer.toString(modifiedDate.getMinutes()) +":"+Integer.toString(modifiedDate.getSeconds());
////                mdDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(modifiedDate);
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

//            holder.time.setText(mdDate);
            holder.senderName.setVisibility(View.VISIBLE);
            holder.senderName.setText(classMessage.senderName);
            holder.time.setText(classMessage.messageDate);
//            classMessage.
//            if(user.getUserRoles().get(0).getRole()==10)
//                Picasso.with(context).load(ManagerHCDashboardActivity.picUrl).transform(new CircleTransform()).placeholder(R.drawable.per).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.per).into(holder.photoImg);
//            else
//            {
//                if(senderImage!=null)
//                Picasso.with(context).load(senderImage).transform(new CircleTransform()).placeholder(R.drawable.per).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.per).into(holder.photoImg);
//
//            }


//            Picasso.with(context).load(AppConstants.IMAGE_BASE_URL+classMessage.senderEmail+".png").transform(new CircleTransform()).placeholder(R.drawable.per).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.per).into(holder.photoImg);
            Picasso.with(context).load(AppConstants.IMAGE_BASE_URL + classMessage.senderEmail + ".png").transform(new CircleTransform()).placeholder(R.drawable.per).error(R.drawable.per).into(holder.photoImg);


//            else if(user.getUserRoles().get(0).getRole()==11)
//                Picasso.with(context).load(EmployeeHCDashboardActivity.picUrl).transform(new CircleTransform()).placeholder(R.drawable.per).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.per).into(holder.photoImg);
//            else if(user.getUserRoles().get(0).getRole()==12)
//                Picasso.with(context).load(FamilyHCDashboardActivity.picUrl).transform(new CircleTransform()).placeholder(R.drawable.per).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.per).into(holder.photoImg);


            return view;
        }
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


    private class MyMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            try {

                boolean isLocIdAvailable = false;
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                final String currentDateandTime = sdf.format(new Date());
                String message = arg1.getStringExtra("message");

                HCModuleMessage obj = new HCModuleMessage();
                obj.message = message;
                obj.messageDate = currentDateandTime;
                obj.senderName = user.getUsername();

                hcModuleMessageArrayList.add(obj);

//                    hcModuleMessageArrayList.add(new HCModuleMessage(message, new Date().toString()));
                listAdapter.notifyDataSetChanged();
                messagesListView.invalidate();

                messagesListView.setSelection(hcModuleMessageArrayList.size() - 1);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
