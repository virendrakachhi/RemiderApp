package com.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.ClassMessage;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.TeacherClass;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class TeacherSendMessageToOneClass extends Activity implements View.OnClickListener {

    public static final String EXTRA_STUDENT_CLASS_INDEX = "EXTRA_STUDENT_CLASS_INDEX";
    public static final String EXTRA_TEACHER_CLASS = "EXTRA_STUDENT_CLASS";
    public static final String EXTRA_HIDE_MESSAGE_BOX = "EXTRA_HIDE_MESSAGE_BOX";
    public static final String EXTRA_SHOW_NOTIFICATIONS = "EXTRA_SHOW_NOTIFICATIONS";
    private static final String TAG = TeacherSendMessageToOneClass.class.getSimpleName();

    ListView messagesListView;
    Button sendMessageButton;
    EditText messageEditText;
    TextView classNameTextView;
    SharedPreferences sharedPreferences;

    ArrayList<ClassMessage> classMessageArrayList = new ArrayList<>();
    ListAdapter listAdapter;
    NotificationListAdapter notificationListAdapter;
    TeacherClass teacherClass;
//    String allStudentEmailsInClass;
//    String allStudentIdsInClass;
    UserUtils userUtils;

    boolean hideMessageBox, showNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_send_message_to_one_class);

        messageEditText = (EditText) findViewById(R.id.editMessage);
        messagesListView = (ListView) findViewById(R.id.messagesList);
        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
        classNameTextView = (TextView) findViewById(R.id.className);

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(TeacherSendMessageToOneClass.this);
        userUtils = new UserUtils(this);

        hideMessageBox = getIntent().getBooleanExtra(EXTRA_HIDE_MESSAGE_BOX, false);
        showNotifications = getIntent().getBooleanExtra(EXTRA_SHOW_NOTIFICATIONS, false);


         teacherClass = new TeacherClass();
        //        int index = getIntent().getIntExtra(EXTRA_STUDENT_CLASS_INDEX, -1);
//
//        if (index != -1) {
//            teacherClass = userUtils.getUserWithDataFromSharedPrefs(Teacher.class).getTeacherClassList().get(index);
//
//            if (teacherClass == null) {
//                finish();
//            }
//        }

//        String savedMessages = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getClassCode(), null);
        String savedMessages=null;
        if (savedMessages != null) {
            classMessageArrayList = DataUtils.getMessagesArrayList(savedMessages);
        }
        setAdapter();

        sendMessageButton.setOnClickListener(this);

        classNameTextView.setText(teacherClass.getClassName());
//        allStudentEmailsInClass = StringUtils.getAllEmailsFromStudentList(teacherClass.getStudentList(), ',');
//        allStudentIdsInClass = StringUtils.getAllIdsFromStudentList(teacherClass.getStudentList(), ',');

        // get all last messages
//        updateDataAsync();

        if (hideMessageBox) {
            findViewById(R.id.linearLayout2).setVisibility(View.GONE);
        }
    }

    private void setAdapter() {
        if (showNotifications) {
            notificationListAdapter = new NotificationListAdapter(TeacherSendMessageToOneClass.this, classMessageArrayList);
            messagesListView.setAdapter(notificationListAdapter);
        } else {
            listAdapter = new ListAdapter(TeacherSendMessageToOneClass.this, classMessageArrayList);
            messagesListView.setAdapter(listAdapter);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDataAsync();
    }

    private void updateDataAsync() {

        new AsyncTask<Void, Void, Void>() {
            private String result;

            @Override
            protected Void doInBackground(Void... params) {

                HashMap<String, String> hm = new HashMap<>();
//                hm.put("user_id", teacherClass.getTeacherId());
                hm.put("user_id", "nsSANV");

                try {

                    if (showNotifications) {
//                        hm.put("class_id", teacherClass.getId());
                        hm.put("class_id", "caks");
                        result = new WebUtils().post(AppConstants.URL_CLASS_NOTIFICATION_LIST, hm);

                    } else {
//                        hm.put("class_code", teacherClass.getClassCode());
                        hm.put("class_code","cABK");
                        result = new WebUtils().post(AppConstants.URL_STUDENT_GET_NOTIFICATIONS_ONE_CLASS, hm);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {

                    if (showNotifications) {
                        classMessageArrayList = DataUtils.getNotificationsArrayList(result);
                    } else {
                        classMessageArrayList = DataUtils.getMessagesArrayList(result);
                    }

//                    sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getClassCode(), result).apply();
                    sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + "JLKCSAH", result).apply();
                    setAdapter();
                    if (classMessageArrayList.size() == 0) {
                        findViewById(R.id.noNotifications).setVisibility(View.VISIBLE);
                    }
                }
            }
        }.execute();
    }

    public void gotoBack(View view) {
        finish();
    }

    private void makeToast(String title) {
        Toast.makeText(TeacherSendMessageToOneClass.this, title, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                sendMessage();
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
        stringStringHashMap.put("message", message);
//        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("class_unique_code", teacherClass.getClassCode());
//        stringStringHashMap.put("student_id", allStudentIdsInClass);
        stringStringHashMap.put("user_id", teacherClass.getTeacherId());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                messageEditText.setText("");
                result = null;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    result = new WebUtils().post(AppConstants.URL_SEND_NOTIFICATION, stringStringHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {
                    classMessageArrayList.add(new ClassMessage(message, new Date().toString()));
                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();


    }

    private class ListAdapter extends ArrayAdapter<ClassMessage> {
        LayoutInflater inflater;

        public ListAdapter(Context context, ArrayList<ClassMessage> messagesStringArrayList) {
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
            ClassMessage classMessage = getItem(position);

            holder.message.setText(classMessage.getMessage());
            holder.time.setText(classMessage.getTime());

            return view;
        }
    }


    private class NotificationListAdapter extends ArrayAdapter<ClassMessage> {
        LayoutInflater inflater;

        public NotificationListAdapter(Context context, ArrayList<ClassMessage> messagesStringArrayList) {
            super(context, 0, messagesStringArrayList);
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        class ViewHolder {
            final TextView time;
            final TextView message;
            final ImageView deleteButton;
            final ImageView archiveButton;
            final ImageView alertButton;

            public ViewHolder(View view) {
                message = (TextView) view.findViewById(R.id.message);
                time = (TextView) view.findViewById(R.id.time);
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
            ClassMessage classMessage = getItem(position);

            holder.message.setText(classMessage.getMessage());
            holder.time.setText(classMessage.getTime());


            holder.deleteButton.setOnClickListener(onClickListener);
            holder.archiveButton.setOnClickListener(onClickListener);
            holder.alertButton.setOnClickListener(onClickListener);

            holder.deleteButton.setTag(position);
            holder.archiveButton.setTag(position);
            holder.alertButton.setTag(position);

            return view;
        }


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer index = (Integer) view.getTag();
                String notificationId = getItem(index).getId();

                if (view.getId() == R.id.deleteButton) {
                    deleteButton(notificationId);

                } else if (view.getId() == R.id.archiveButton) {
                    archiveButton(notificationId);

                } else if (view.getId() == R.id.alertButton) {
                    alertButton(notificationId);
                }
            }
        };

        private void deleteButton(final String notificationId) {
            new AlertDialog.Builder(TeacherSendMessageToOneClass.this)
                    .setMessage("Delete notification!")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            final String url = AppConstants.URL_DELETE_NOTIFICATION;

                            new AsyncTask<Void, Void, String>() {
                                ProgressDialog progressDialog = new ProgressDialog(TeacherSendMessageToOneClass.this);

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
                                    map.put("notification_id", notificationId);

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

}
