package com.attendanceapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.ClassMessage;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.StringUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class UserSendMessageToOneClass extends Activity implements View.OnClickListener {

    public static final String EXTRA_SELECTED_CLASS_INDEX = "EXTRA_STUDENT_CLASS_INDEX";
    public static final String EXTRA_HIDE_MESSAGE_BOX = "EXTRA_HIDE_MESSAGE_BOX";

    ListView messagesListView;
    Button sendMessageButton;
    EditText messageEditText;
    TextView classNameTextView;
    SharedPreferences sharedPreferences;

    ArrayList<ClassMessage> classMessageArrayList = new ArrayList<>();
    ListAdapter listAdapter;
    String allStudentEmailsInClass;
    String allStudentIdsInClass;
    boolean hideMessageBox;
    UserUtils userUtils;

    private ClassEventCompany teacherClass;
    private User user;
    private UserRole userRole;

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

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(UserSendMessageToOneClass.this);
        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        userRole = UserRole.valueOf(getIntent().getIntExtra(AppConstants.EXTRA_USER_ROLE, -1));

        hideMessageBox = getIntent().getBooleanExtra(EXTRA_HIDE_MESSAGE_BOX, false);
        int index = getIntent().getIntExtra(EXTRA_SELECTED_CLASS_INDEX, -1);

        teacherClass = userUtils.getUserFromSharedPrefs().getClassEventCompanyArrayList().get(index);


        String savedMessages = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getUniqueCode(), null);
        if (savedMessages != null) {
            classMessageArrayList = DataUtils.getMessagesArrayList(savedMessages);
        }
        setAdapter();

        sendMessageButton.setOnClickListener(this);

        classNameTextView.setText(teacherClass.getName());
        allStudentEmailsInClass = StringUtils.getAllEmailsFromStudentList(teacherClass.getUsers(), ',');
        allStudentIdsInClass = StringUtils.getAllIdsFromStudentList(teacherClass.getUsers(), ',');

        // get all last messages
        updateDataAsync();

        if (hideMessageBox) {
            findViewById(R.id.linearLayout2).setVisibility(View.GONE);
        }
    }

    private void setAdapter() {
        listAdapter = new ListAdapter(UserSendMessageToOneClass.this, classMessageArrayList);
        messagesListView.setAdapter(listAdapter);

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
                // (user_id, class_code)if user login
                // (email, class_code)student_login
                HashMap<String, String> hm = new HashMap<>();
                hm.put("user_id", user.getUserId());
                hm.put("class_code", teacherClass.getUniqueCode());

                try {
                    result = new WebUtils().post(AppConstants.URL_STUDENT_GET_NOTIFICATIONS_ONE_CLASS, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (result != null) {
                    classMessageArrayList = DataUtils.getMessagesArrayList(result);
                    sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + teacherClass.getUniqueCode(), result).apply();
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
        Toast.makeText(UserSendMessageToOneClass.this, title, Toast.LENGTH_LONG).show();
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
        stringStringHashMap.put("student_emails", allStudentEmailsInClass);
        stringStringHashMap.put("class_unique_code", teacherClass.getUniqueCode());
        stringStringHashMap.put("student_id", allStudentIdsInClass);
        stringStringHashMap.put("user_id", user.getUserId());

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

    private class ListAdapter extends BaseAdapter {
        private ArrayList<ClassMessage> messagesStringArrayList;
        LayoutInflater inflater;
        Context context;

        public ListAdapter(Context context, ArrayList<ClassMessage> messagesStringArrayList) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.messagesStringArrayList = messagesStringArrayList;
        }


        @Override
        public int getCount() {
            return messagesStringArrayList.size();
        }

        @Override
        public ClassMessage getItem(int position) {
            return messagesStringArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView message, time;
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

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ClassMessage classMessage = messagesStringArrayList.get(position);

            holder.message.setText(classMessage.getMessage());
            holder.time.setText(classMessage.getTime());

            return view;
        }
    }

}
