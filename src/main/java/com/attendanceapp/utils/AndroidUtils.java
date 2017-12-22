package com.attendanceapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.Toast;

import com.attendanceapp.AppConstants;

public final class AndroidUtils {

    public AndroidUtils() {
    }

    private Activity activity;

    public AndroidUtils(Activity activity) {
        this.activity = activity;
    }

    /**
     * Notifies UI to display a message.
     * <p/>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(AppConstants.DISPLAY_MESSAGE_ACTION);
        intent.putExtra(AppConstants.EXTRA_NOTIFICATION_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    /**
     * Open a new activity
     *
     * @param fromActivity current activity.
     * @param toActivity   activity to open.
     * @param finishThis   close this activity
     */
    public static void openActivity(Activity fromActivity, Class toActivity, boolean finishThis) {
        openActivity(fromActivity, toActivity, null, finishThis);
    }

    /**
     * Open a new activity
     *
     * @param fromActivity current activity.
     * @param toActivity   activity to open.
     * @param bundle       bundle for activity.
     * @param finishThis   close this activity
     */
    public static void openActivity(Activity fromActivity, Class toActivity, Bundle bundle, boolean finishThis) {
        Intent intent = new Intent(fromActivity, toActivity);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        fromActivity.startActivity(intent);
        if (finishThis) {
            fromActivity.finish();
        }
    }

    /**
     * Open a new activity
     *
     * @param context pass context to get shared prefs
     */
    public static SharedPreferences getCommonSharedPrefs(Context context) {
        return context.getSharedPreferences(AppConstants.SP_COMMON, Context.MODE_MULTI_PROCESS);
    }

    public static void selectImage(Activity context, int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        context.startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    public static String getImagePathFromUri(Uri uri, Context context) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    public void showToast(String showText) {
        Toast.makeText(activity, showText, Toast.LENGTH_LONG).show();
    }

    public void showToast(int stringId) {
        Toast.makeText(activity, stringId, Toast.LENGTH_LONG).show();
    }

    public static void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }

    public static int convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = (int) (px / (metrics.densityDpi / 160f));
        return dp;
    }


}
