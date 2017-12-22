package com.attendanceapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by anuj_sharma on 8/7/2015.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CommonMethods {
    private static CommonMethods mCommonmethods;
    private static AlertDialog alert;
    private static Pattern pattern;
    private static Matcher matcher;
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    LinearLayout product_parent;

    Fragment fragment = null;

    private CommonMethods() {

    }

    public static CommonMethods getInstance() {
        if (mCommonmethods == null)
            mCommonmethods = new CommonMethods();
        return mCommonmethods;
    }

    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }
    public void el(String tag, String msg) {
        Log.e(tag, msg);
    }

    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public void DisplayToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
   /* public void displaySnackBar(Context ctx, String msg, View view) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }*/
    private void EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    public boolean validateEmail(final String EmailID) {
        EmailValidator();
        matcher = pattern.matcher(EmailID);
        return matcher.matches();

    }

    /*
    Change Image Orientation portrate after camera intent
     */
    public Bitmap changeToPortrate(String photoPath,Bitmap bp) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        Bitmap bitmap = null;
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

        if (orientation ==ExifInterface.ORIENTATION_NORMAL) {

// Do nothing. The original image is fine.
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {

            bitmap = rotateImage(bp, 90);

        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {

            bitmap = rotateImage(bp, 180);

        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {

            bitmap = rotateImage(bp, 270);

        }


        /*int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bitmap = rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                bitmap = rotateImage(bitmap, 180);
                break;
            // etc.
        }*/
        return bitmap;
    }
    public Bitmap rotateImage(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /*
	 * Hide Keyboard
	 */
    void hideKeyboard(Context ctx){
       // CommonMethods.e("", "hide soft keyboard");
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
      //  imm.hideSoftInputFromWindow(ctx.getApplicationContext().getApplicationWindowToken(), 0);

    }

/*
Read Image  Orientation
 */
public final int exifToDegrees(int exifOrientation) {
    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
    else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
    else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
    return 0;
}
    /*
    Async Task method
     */

    public void runonAsynTask(AsyncTask<String, Void, String> mAsynctask) {
        if (mAsynctask != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                mAsynctask.executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });
            else
                mAsynctask.execute();
        }

    }
    /*
    Enter Reveal Animation
     */
    public void enterReveal(View view) {
        // previously invisible view
        final View myView = view;

        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);

        anim.start();
    }

    /*
    Exit Reveal Animation
     */
    public void exitReveal(View view) {
        // previously visible view
        final View myView = view;

        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = myView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
    }
    /*
    Split text from string
     */
    public String getMeNthParamInString(String p_text,
                                               String p_seperator, int nThParam) { // / "TOTRPIDS=101=104" returns
        // "101" If nThParam ==
        // 2.
        String retStrThirdParam = new String("");
        int index = -1;
        int prevIdx = 0;
        int loopNM = 1;
        boolean loopBool = true;
        while (loopBool) {
            try {
                index = p_text.indexOf(p_seperator, prevIdx);
                if (loopNM >= nThParam) {
                    if (index >= 0) {
                        retStrThirdParam = p_text.substring(prevIdx, index);
                    } else // /-1
                    {
                        retStrThirdParam = p_text.substring(prevIdx);
                    }
                    loopBool = false;
                    break;
                } else {
                    if (index < 0) // /-1
                    {
                        loopBool = false;
                        retStrThirdParam = "";
                        break;
                    }
                }
                loopNM++;
                prevIdx = index + 1;
            } catch (Exception ex) {
                loopBool = false;
                retStrThirdParam = "";
                break;
            }
        } // /while
        if (retStrThirdParam.trim().length() <= 0) {
            retStrThirdParam = "";
        }
        return retStrThirdParam;
    }

    public final String getPath(Uri uri, Context context) {
        if (uri == null) {
            CommonMethods.getInstance().e("", "uri is null");
            return null;
        }
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        CommonMethods.getInstance().e("", "get path method->> after cursor init");
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
//        CommonMethods.getInstance().e("", "get path method->> after cursor");
//        CommonMethods.getInstance().e("", "get path method->> " + uri.getPath());
        return uri.getPath();
    }

    /*
    Custom Dialog with XML
     */
    /*public static final void CreateCustomDialog(final Context context, final String url, final String user_url, String title)
    {

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.customdialog_layout);
//					dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        ImageView customimage = (ImageView)dialog.findViewById(R.id.customimg);
        CircleImageView userImg = (CircleImageView)dialog.findViewById(R.id.user_image);
        TextView userName = (TextView)dialog.findViewById(R.id.user_name);
        final ProgressBar progresBar = (ProgressBar)dialog.findViewById(R.id.progress);

        //set image to view
        Picasso.with(context).load(url).into(customimage, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                progresBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                progresBar.setVisibility(View.GONE);
            }
        });
        Picasso.with(context).load(user_url).into(userImg);
        userName.setText(title);
        dialog.show();
    }*/

    public Boolean filterByPackageName(Context context, Intent intent, String prefix) {
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(prefix)) {
                intent.setPackage(info.activityInfo.packageName);
                return true;
            }
        }
        return false;
    }

    public String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf("", "UTF-8 should always be supported", e);
            return "";
        }
    }

    /*
         * Alert Box
         */
    /*public static final void showOkDialog(String dlgText, Context context) {

        if (context == null && GlobalActivity.getGlobalContext() != null) {
            context = GlobalActivity.getGlobalContext();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                AlertDialog.THEME_HOLO_LIGHT);
        builder.setMessage(dlgText).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (alert != null)
                    alert.dismiss();
            }

        });
        alert = builder.create();
        alert.show();
    }*/


}
