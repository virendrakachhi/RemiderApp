package com.attendanceapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.attendanceapp.activities.Attendee_DashboardActivity;
import com.attendanceapp.activities.Employee_DashboardActivity;
import com.attendanceapp.activities.EventHost_DashboardActivity;
import com.attendanceapp.activities.Manager_DashboardActivity;
import com.attendanceapp.models.ScheduleStatus;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;

import org.json.JSONObject;

import static com.attendanceapp.AppConstants.SENDER_ID;
import static com.attendanceapp.utils.AndroidUtils.displayMessage;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";

    private static boolean IS_CHAT_NOTI = false;

    public GCMIntentService() {
        super(SENDER_ID);
    }

    /**
     * Method called on device registered
     */
    @Override
    protected void onRegistered(Context context, String registrationId) {
        try {
            Log.e(TAG, "Device registered: regId = " + registrationId);

            String userString = AndroidUtils.getCommonSharedPrefs(getApplicationContext()).getString(AppConstants.KEY_LOGGED_IN_USER, null);
            User user = new Gson().fromJson(userString, User.class);
            ServerUtilities.register(context, registrationId, user.getUserId(), user.getDeviceToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Method called on device un registred
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        IS_CHAT_NOTI = false;
        String message, senderName, receiverID, locationID, senderID = "";
        User user = null;
        if (bundle != null && bundle.containsKey("classCode")) {
            String classCode = bundle.getString("classCode");
            SharedPreferences sp = AndroidUtils.getCommonSharedPrefs(context);
            String userString = sp.getString(AppConstants.KEY_LOGGED_IN_USER, null);
            if (userString == null) {
                return;
            }
            user = new Gson().fromJson(userString, User.class);

            int count = sp.getInt(user.getUserId() + classCode, 0);
            sp.edit().putInt(user.getUserId() + classCode, ((count + 1))).apply();
        }

        if (AndroidUtils.getCommonSharedPrefs(context).getBoolean(AppConstants.IS_NOTIFICATIONS_ON, true)) {

            if (bundle != null && bundle.containsKey("m")) {
                message = bundle.getString("m");
                senderName = bundle.getString("sender_name");
                receiverID = bundle.getString("receiver_id");
                locationID = bundle.getString("location_id");
                if (message.contains("location_id")) {
                    try {
                        JSONObject object = new JSONObject(message);
                        message = object.optString("message");
                        locationID = object.optString("location_id");
                        if (object.toString().contains("sender_name")) {
                            message = object.optString("m");
                            locationID = object.optString("location_id");
                            senderName = object.optString("sender_name");
                            senderID = object.optString("sender_id");
                            receiverID = object.optString("receiver_id");
                            IS_CHAT_NOTI = true;

//                            Broadcast chat message
                            if (EmployeeHCSendMessageToOneLocation.receiverIDNotify.equalsIgnoreCase(senderID)){
                                if (receiverID.equalsIgnoreCase(user.getUserId())){
                                    Intent intentToManager = new Intent();
                                    intentToManager.setAction("MY_MESSAGE_ACTION");
                                    intentToManager.putExtra("message", message);
                                    sendBroadcast(intentToManager);
                                }
                            }
                        }
//                        {"receiver_id":"568","sender_name":"we222","m":"hdhchzhfzhfz jhcjhxjx vmjgxjgx","location_id":"281"}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                displayMessage(context, message);
                generateNotification(context, message, senderName, receiverID, locationID, bundle, senderID);
            } else if (bundle.containsKey("active")) {
                /*{"state":"active","location_id":"250"}*/
                try {
                    String activeJsonStr = bundle.getString("active");
                    JSONObject objectActive = new JSONObject(activeJsonStr);
                    String state = objectActive.optString("state");
                    String locationId = objectActive.optString("location_id");
                    boolean isLocIdAvailable = false;
                    try {
                        for (int i = 0; i < ManagerHCDashboardActivity.statusArrayList.size(); i++) {
                            if (ManagerHCDashboardActivity.statusArrayList.get(i).getLocationId().equalsIgnoreCase(locationId)) {
                                ManagerHCDashboardActivity.statusArrayList.remove(i);
                                ScheduleStatus scheduleStatus = new ScheduleStatus();
                                scheduleStatus.setStatus(state);
                                scheduleStatus.setLocationId(locationId);
                                ManagerHCDashboardActivity.statusArrayList.add(scheduleStatus);
                                isLocIdAvailable = true;
                            }
                        }

                        if (!isLocIdAvailable) {
                            ScheduleStatus scheduleStatus = new ScheduleStatus();
                            scheduleStatus.setStatus(state);
                            scheduleStatus.setLocationId(locationId);
                            ManagerHCDashboardActivity.statusArrayList.add(scheduleStatus);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intentToManager = new Intent();
                    intentToManager.setAction("MY_ACTION");
                    intentToManager.putExtra("state", state);
                    intentToManager.putExtra("location_id", locationId);
                    sendBroadcast(intentToManager);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

//        ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
//         String message = intent.getExtras().getString("price");
//         notifies user
    }

    /**
     * Method called on receiving a deleted message
     */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message, null, null, null, null, null);
    }

    /**
     * Method called on Error
     */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        generateNotification(context, message, null, null, null, null, null);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message, String sName, String recID, String locID, Bundle bundle, String senderID) {
        String title = context.getString(R.string.app_name);
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();

        boolean showChatNotification = false;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        Intent notificationIntent = new Intent(context, StudentNotificationActivity.class);

        // check for logged in user
        SharedPreferences sharedPreferences = AndroidUtils.getCommonSharedPrefs(context);
        String userString = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);
//        String msg[] = message.split(",");
//        for(int i=0; i<msg.length; i++) {
//            int m = i+1;
//            if(m<msg.length)
//                message = msg[i] + "\n" + msg[m];
//
//
//        }

        if (userString != null) {

            User user = new Gson().fromJson(userString, User.class);
            String userRole = String.valueOf(user.getUserRoles().get(0).getRole());

            if ("1".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, TeacherDashboardActivity.class);
            } else if ("2".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, StudentDashboardActivity.class);
            } else if ("3".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, ParentDashboardActivity.class);
            } else if ("4".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, Manager_DashboardActivity.class);
            } else if ("5".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, Employee_DashboardActivity.class);
            } else if ("6".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, EventHost_DashboardActivity.class);
            } else if ("7".equalsIgnoreCase(userRole)) {
                notificationIntent = new Intent(context, Attendee_DashboardActivity.class);
            } else if ("10".equalsIgnoreCase(userRole)) {
//                notificationIntent = new Intent(context,ManagerHCDashboardActivity.class);

                if (recID == null) {
                    if (message.contains("left early")) {
                        notificationIntent = new Intent(context, HCManagerSendNotificationActivity.class);
                        notificationIntent.putExtra("location_id", locID);
                    } else {
                        notificationIntent = new Intent(context, ManagerHCDashboardActivity.class);
                    }
                } else {

                    if (user.getUserId().equalsIgnoreCase(recID)){
                        showChatNotification = true;
                    }

                    title = sName;
                    notificationIntent = new Intent(context, EmployeeHCSendMessageToOneLocation.class);
                    Bundle bun = new Bundle();
                    bun.putInt("Index", 0);
                    bun.putInt("UserType", 10);
                    bun.putString("ChatType", "Single");
                    bun.putString("User", sName);
                    bun.putString("ReceiverID", senderID);
                    bun.putString("LocationID", locID);

                    notificationIntent.putExtras(bun);
                }

            } else if ("11".equalsIgnoreCase(userRole)) {
//                notificationIntent = new Intent(context,EmployeeHCDashboardActivity.class);
                if (user.getUserId().equalsIgnoreCase(recID)){
                    showChatNotification = true;
                }
                title = sName;
                notificationIntent = new Intent(context, EmployeeHCSendMessageToOneLocation.class);
                Bundle bun = new Bundle();

                bun.putInt("Index", 0);
                bun.putInt("UserType", 11);
                bun.putString("ChatType", "Multiple");
                bun.putString("LocationID", locID);
                notificationIntent.putExtras(bun);


            } else if ("12".equalsIgnoreCase(userRole)) {
//                notificationIntent = new Intent(context,FamilyHCDashboardActivity.class);
                notificationIntent = new Intent(context, EmployeeHCSendMessageToOneLocation.class);
                Bundle bun = new Bundle();

                bun.putInt("Index", 0);
                bun.putInt("UserType", 12);
                bun.putString("ChatType", "Multiple");
                bun.putString("LocationID", locID);
                notificationIntent.putExtras(bun);


            }
        }
        notificationIntent.putExtra("push", true);

        if (bundle != null) {
            notificationIntent.putExtras(bundle);
        }

        // set intent so it does not start a new activity
        notificationIntent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        //notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "your_sound_file_name.mp3");

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        try {
            if (!(EmployeeHCSendMessageToOneLocation.isOnChatScreen && IS_CHAT_NOTI &&
                    EmployeeHCSendMessageToOneLocation.receiverIDNotify.equalsIgnoreCase(senderID))) {
                if (showChatNotification){
                    notificationManager.notify(0, notification);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (showChatNotification){
                notificationManager.notify(0, notification);
            }
        }


    }

}
