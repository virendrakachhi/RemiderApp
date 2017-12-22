package com.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.models.ClassMessage;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.StudentClass;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.DataUtils;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class StudentNotificationActivity extends Activity {
    public static final String EXTRA_STUDENT_CLASS = "extra_student_class";

    ListView notificationListView;
    TextView className;
    SharedPreferences publicPreferences;

    ListAdapter listAdapter;
    ArrayList<ClassMessage> messageArrayList = new ArrayList<>();

    StudentClass studentClass;
    Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notifcation);

        notificationListView = (ListView) findViewById(R.id.notificationListView);
        className = (TextView) findViewById(R.id.className);

        studentClass = (StudentClass) getIntent().getSerializableExtra(EXTRA_STUDENT_CLASS);
        className.setText(studentClass.getClassName());

        publicPreferences = AndroidUtils.getCommonSharedPrefs(getApplicationContext());
        student = new Student(new UserUtils(this).getUserFromSharedPrefs());

        String messagesString = publicPreferences.getString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + student.getUserId() + studentClass.getClassUniqueCode() , null);
        if (messagesString != null) {
            messageArrayList = DataUtils.getMessagesArrayList(messagesString);
        }

        /* get new message from notification */
        //        Intent intent = getIntent();
        //        Bundle bundle = intent.getExtras();
        //        String message = bundle.getString("m");
        //        classCode = bundle.getString("classCode");
        //        if (message != null && classCode != null) {
        //
        //        }

        setAdapter();
        updateDataAsync();
    }

    private void setAdapter() {
        listAdapter = new ListAdapter(StudentNotificationActivity.this, messageArrayList);
        notificationListView.setAdapter(listAdapter);
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
                hm.put("email", student.getEmail());
                hm.put("class_code", studentClass.getClassUniqueCode());

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
                    try {
                        JSONObject object = new JSONObject(result);

                        if (object.has("Error")) {
                            Toast.makeText(StudentNotificationActivity.this, object.getString("Error"), Toast.LENGTH_SHORT).show();

                        } else {
                            messageArrayList = DataUtils.getMessagesArrayList(result);
                            publicPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_NOTIFICATIONS + student.getUserId() + studentClass.getClassUniqueCode(), result).apply();
                            setAdapter();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public void gotoBack(View view) {
        finish();
    }

}
