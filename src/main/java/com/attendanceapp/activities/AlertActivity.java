package com.attendanceapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.EmployeeHCSendMessageToOneLocation;
import com.attendanceapp.R;
import com.attendanceapp.models.HCModuleNotifications;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.ManagerHCClass;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 12/5/16.
 */
public class AlertActivity extends Activity implements View.OnClickListener {

    private TextView txtAlert, txtArchivedAlert;
    private ImageView navigationButton;
    private ImageView backButton;
    private ListView listAlert;

    private AlertListAdapter alertListAdapter;
//    private ArrayList<AlertBean> beanArrayList;

    private String locID = "";

    private boolean isArchivedAlerts = false;

    private Intent intent;
    private String intentJSON = "";
    SharedPreferences sharedPreferences;
    private Animation textAnimation;
    private FrameLayout navigationLayout;
    UserUtils userUtils;
    User user;
    private int index = 0;
    ManagerHCClass managerLocData;
    private ArrayList<HCModuleNotifications> hcModuleNotificationsArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        initView();
    }

    /**
     * This method is used to initialize all views
     */
    private void initView() {

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(AlertActivity.this);
        sharedPreferences.edit().putBoolean("Image Status", false).commit();

        userUtils = new UserUtils(this);
        user = userUtils.getUserFromSharedPrefs();

        navigationLayout = (FrameLayout) findViewById(R.id.navigation);

        txtAlert = (TextView) findViewById(R.id.txt_alert);
        txtArchivedAlert = (TextView) findViewById(R.id.txt_archived_alert);
        navigationButton = (ImageView) findViewById(R.id.navigationButton);
        backButton = (ImageView) findViewById(R.id.back_button_alert);
        backButton.setOnClickListener(this);
        navigationButton.setOnClickListener(this);
        isArchivedAlerts = false;

        listAlert = (ListView) findViewById(R.id.alert_list);
//        beanArrayList = new ArrayList<AlertBean>();
        alertListAdapter = new AlertListAdapter(AlertActivity.this, hcModuleNotificationsArrayList);
        listAlert.setAdapter(alertListAdapter);

        txtAlert.setOnClickListener(this);
        txtArchivedAlert.setOnClickListener(this);

        try {
            intent = getIntent();
//            intentJSON = intent.getStringExtra(AppConstants.INTENT_NOTIFICATION_JSON);
//            setListAdapter(intentJSON, "0");

            index = getIntent().getExtras().getInt("Index");
            locID = getIntent().getStringExtra("location_id");

            if (locID.trim().length() > 0){
//                locationID = location_id;
            } else {
                managerLocData = userUtils.getUserWithDataFromSharedPrefs(Manager.class).getManagerLocationList().get(index);
                locID = managerLocData.getId();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        new AsyncArchiveAlert().execute(locID);

    }

    /**
     * This methos is used to perform all click events
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.txt_alert:

                txtArchivedAlert.setBackgroundColor(Color.parseColor("#1B7DBE"));
                txtAlert.setBackgroundColor(0xF6F5F1);
                txtArchivedAlert.setTextColor(Color.parseColor("#FFFFFF"));
                txtAlert.setTextColor(Color.parseColor("#1B7DBE"));
                navigationButton.setImageResource(R.drawable.question_mark);
                isArchivedAlerts = false;
                setListAdapter(intentJSON, "0");
                sharedPreferences.edit().putBoolean("Image Status", false).commit();
//                alertListAdapter = new AlertListAdapter(AlertActivity.this, hcModuleNotificationsArrayList);
//                listAlert.setAdapter(alertListAdapter);
                break;
            case R.id.txt_archived_alert:
                txtAlert.setBackgroundColor(Color.parseColor("#1B7DBE"));
                txtArchivedAlert.setTextColor(Color.parseColor("#1B7DBE"));
                txtAlert.setTextColor(Color.parseColor("#FFFFFF"));
                txtArchivedAlert.setBackgroundColor(0xF6F5F1);
                navigationButton.setImageResource(R.drawable.per);
                isArchivedAlerts = true;
                setListAdapter(intentJSON, "1");
                new NavigationPage(this, userUtils.getUserFromSharedPrefs());
//                alertListAdapter = new AlertListAdapter(AlertActivity.this, hcModuleNotificationsArrayList);
//                listAlert.setAdapter(alertListAdapter);
                break;
            case R.id.back_button_alert:
                finish();
                break;
            case R.id.navigationButton:
                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
                navigationLayout.setAnimation(textAnimation);
                navigationLayout.setVisibility(View.VISIBLE);
                break;

        }

    }

    /**
     * This class is used to set all recoed in a list.
     */
    public class AlertListAdapter extends BaseAdapter {
        class ViewHolder {

            TextView txtUserName, txtTime;
            TextView message;
            TextView messageLeft;
            ImageView imgDelete;
            ImageView imgArchive;
            ImageView imgInfo;

        }

        private Context mContext;
        private LayoutInflater mInflater = null;
        private List<HCModuleNotifications> mItems;

        public AlertListAdapter(Context context) {
            mContext = context;
        }

        public AlertListAdapter(Context context, ArrayList<HCModuleNotifications> arrayList) {
            mContext = context;
            if (mItems != null && mItems.size() > 0) {
                mItems.clear();
                mItems = null;
            }
            mItems = arrayList;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {

            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup arg2) {
            ViewHolder holder = null;
            final HCModuleNotifications entry = mItems.get(position);

            mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.list_item_alert_activity, null);
            holder = new ViewHolder();
            holder.txtUserName = (TextView) convertView.findViewById(R.id.emp_name);
            holder.txtTime = (TextView) convertView.findViewById(R.id.time);
            holder.imgDelete = (ImageView) convertView.findViewById(R.id.delete_button_arc_alt);
            holder.imgArchive = (ImageView) convertView.findViewById(R.id.archive_button_arc_alt);
            holder.imgInfo = (ImageView) convertView.findViewById(R.id.alert_button_arc_alt);
            holder.message = (TextView) convertView.findViewById(R.id.message_alert);
            holder.messageLeft = (TextView) convertView.findViewById(R.id.messageLeft_alert);
            convertView.setTag(holder);

            if (isArchivedAlerts) {
                holder.imgArchive.setVisibility(View.GONE);
                holder.imgInfo.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imgDelete.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.imgDelete.setLayoutParams(params);
            }

            holder.txtUserName.setText("" + entry.senderName);
            holder.txtTime.setText("" + changeDateFormat(entry.messageDate));

            holder.message.setText(entry.message);
            holder.messageLeft.setText(entry.messageLeft);

            holder.imgArchive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncArchiveAlert().execute(entry.locationID, entry.notificationID, "archive");
                }
            });

            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteButton(entry.notificationID);
                }
            });

            holder.imgInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AlertActivity.this, EmployeeHCSendMessageToOneLocation.class);
                    Bundle bun = new Bundle();
                    bun.putInt("Index", index);
                    bun.putInt("UserType", 10);
                    bun.putString("ChatType", "Single");
                    bun.putString("User", entry.senderName);
                    bun.putString("ReceiverID", entry.senderID);
                    intent.putExtras(bun);
//        intent.putExtra(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    private String changeDateFormat(String dateString) {
        String formatedDate = "";
        try {
            SimpleDateFormat sdfOldDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat sdfNewDate = new SimpleDateFormat("M/d/yyyy hh:mm:ss");
            Date date = sdfOldDate.parse(dateString);
            formatedDate = sdfNewDate.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return formatedDate;
    }

    private void deleteButton(final String notificationId) {
        new AlertDialog.Builder(AlertActivity.this)
                .setMessage("Delete alert!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        final String url = AppConstants.URL_DELETE_NOTIFICATION;

                        new AsyncTask<Void, Void, String>() {
                            ProgressDialog progressDialog = new ProgressDialog(AlertActivity.this);

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
                                            new AsyncArchiveAlert().execute(locID);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
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

    private void makeToast(String title) {
        Toast.makeText(AlertActivity.this, title, Toast.LENGTH_LONG).show();
    }

    private void setListAdapter(String jSONString, String isArchive) {
        try {
            hcModuleNotificationsArrayList.clear();
            JSONObject jsonObject = new JSONObject(jSONString);
            // result= {"Error":"No Record Found"}

            JSONArray jsnArr = jsonObject.getJSONArray("Data");

            for (int i = 0; i < jsnArr.length(); i++) {
                String chatMessage = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("message");
                String chatMessageLeft = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("message_left");
                String sentDate = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("created");
                String notificationID = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("id");
                String senderName = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("employee_name");
                String senderID = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("hc_employee_id");
                String locationID = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("location_id");
                String archived = jsnArr.getJSONObject(i).getJSONObject("notifications").getString("archived");
                locID = locationID;
                HCModuleNotifications obj = new HCModuleNotifications();
                obj.message = chatMessage;
                obj.messageLeft = chatMessageLeft;
                obj.senderName = senderName;
                obj.messageDate = sentDate;
                obj.notificationID = notificationID;
                obj.senderID = senderID;
                obj.locationID = locationID;
                obj.archived = archived;
                if (isArchive.equalsIgnoreCase(archived)) {
                    hcModuleNotificationsArrayList.add(obj);
                }
            }

            alertListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            alertListAdapter.notifyDataSetChanged();
        }
    }


    private class AsyncArchiveAlert extends AsyncTask<String, String, String> {

        String response = "";
        private ProgressDialog progressDialog = new ProgressDialog(AlertActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (strings[1] != null) {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("location_id", strings[0]));
                    nameValuePairs.add(new BasicNameValuePair("id", strings[1]));
                    response = new WebServiceHandler().webServiceCall(nameValuePairs, AppConstants.URL_ARCHIVE_ALERT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<NameValuePair> nameValuePairsNew = new ArrayList<NameValuePair>();
            nameValuePairsNew.add(new BasicNameValuePair("location_id", strings[0]));
            response = new WebServiceHandler().webServiceCall(nameValuePairsNew, AppConstants.URL_GET_SAVED_HC_NOTIFICATION);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s != null) {
                try {
                    intentJSON = s;
                    if (isArchivedAlerts) {
                        setListAdapter(intentJSON, "1");
                    } else {
                        setListAdapter(intentJSON, "0");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
//        new NavigationPage(this, userUtils.getUserFromSharedPrefs());
    }
}
