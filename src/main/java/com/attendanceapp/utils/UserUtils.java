package com.attendanceapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.attendanceapp.AppConstants;
import com.attendanceapp.EmployeeHCDashboardActivity;
import com.attendanceapp.GCMIntentService;
import com.attendanceapp.SendLocationService;
import com.attendanceapp.SplashActivity;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;
import com.attendanceapp.services.BeaconMonitorService;
import com.attendanceapp.services.HCEmployeeService;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;
import com.google.android.gcm.GCMRegistrar;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public final class UserUtils {

    private Activity activity;
    private Geocoder gCoder;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    public UserUtils(Activity activity) {
        this.activity = activity;
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(this.activity);
        gCoder = new Geocoder(activity);
    }

    /**
     * Logout user, clear shared prefs and stop all services
     */
    public void userLogout() {
        sharedPreferences.edit().clear().apply();
        activity.stopService(new Intent(activity, SendLocationService.class));
        activity.stopService(new Intent(activity, BeaconMonitorService.class));
        activity.stopService(new Intent(activity, HCEmployeeService.class));
        GCMRegistrar.unregister(activity);
        AndroidUtils.openActivity(activity, SplashActivity.class, true);
        activity.finish();

    }

    /**
     * Toggle current state of shared prefs
     *
     * @return return new state after saving to shared prefs
     */
    public boolean toggleNotification() {
        sharedPreferences.edit().putBoolean(AppConstants.IS_NOTIFICATIONS_ON, !sharedPreferences.getBoolean(AppConstants.IS_NOTIFICATIONS_ON, true)).apply();
        return sharedPreferences.getBoolean(AppConstants.IS_NOTIFICATIONS_ON, true);
    }

    @Nullable
    public static UserRole userRoleFromString(String userRoleString) {
        UserRole[] values = UserRole.values();
        for (UserRole value : values) {
            if (value.getRole() == Integer.valueOf(userRoleString)) {
                return value;
            }
        }
        return null;
    }

    public User getUserFromSharedPrefs() {
        String userString = sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER, null);

        if (userString == null) {
            userLogout();
            activity.finish();
        }

        return gson.fromJson(userString, User.class);
    }

    public String getUserDataFromSharedPrefs() {
        return sharedPreferences.getString(AppConstants.KEY_LOGGED_IN_USER_DATA, null);
    }

    public void saveUserToSharedPrefs(User user) {
        sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER, gson.toJson(user)).apply();
    }

    public void clearSharedPrefs() {
        sharedPreferences.edit().clear().apply();
    }

    public void saveUserWithDataToSharedPrefs(Object o, Class type) {
        sharedPreferences.edit().putString(AppConstants.KEY_LOGGED_IN_USER_DATA, new Gson().toJson(o, type)).apply();
    }

    public <T> T getUserWithDataFromSharedPrefs(Class<T> t) {
        return new Gson().fromJson(getUserDataFromSharedPrefs(), t);
    }

    @Nullable
    public Address getAddress(double lat, double lng) {
        try {
            List<Address> addresses = gCoder.getFromLocation(lat, lng, 1);
            if (addresses == null) {
                return null;
            } else if (addresses.size() > 0) {
                return addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public String getAddressString(Address address) {
        if (address != null) {
            StringBuilder addressString = new StringBuilder();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressString.append(address.getAddressLine(i));
                // add comma
                if (i < (address.getMaxAddressLineIndex() - 1)) {
                    addressString.append(", ");
                }
            }

            return addressString.toString();
        }
        return "Please check location services";
    }

    public void toggleClassNotifications(String classCode) {
        sharedPreferences.edit()
                .putBoolean(AppConstants.IS_CLASS_NOTIFICATIONS_ON + classCode,
                        !sharedPreferences.getBoolean(AppConstants.IS_CLASS_NOTIFICATIONS_ON + classCode, true))
                .apply();
    }

    public boolean isClassNotificationOn(String classCode) {
        return sharedPreferences.getBoolean(AppConstants.IS_CLASS_NOTIFICATIONS_ON + classCode, true);
    }

}
