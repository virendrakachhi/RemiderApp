package com.attendanceapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.AppConstants;
import com.attendanceapp.Contact_us;
import com.attendanceapp.EmployeeHCDashboardActivity;
import com.attendanceapp.FamilyHCDashboardActivity;
import com.attendanceapp.Faq;
import com.attendanceapp.ManagerHCDashboardActivity;
import com.attendanceapp.OnSwipeTouchListener;
import com.attendanceapp.R;
import com.attendanceapp.RegisterActivity;
import com.attendanceapp.activities.AddUserViewActivity;
import com.attendanceapp.activities.SearchActivity;
import com.attendanceapp.adapters.UserViewsAdapter;
import com.attendanceapp.models.CircleTransform;
import com.attendanceapp.models.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavigationPage implements View.OnClickListener {
    private static final int REQUEST_EDIT_ACCOUNT = 100;

    private Activity activity;
    private User user;
    private UserUtils userUtils;

    private ImageView navigationBackButton;
    private ImageView navigationButton;
    private TextView faqButton, contactUsButton, editAccountButton, addViewButton, storeButton, logoutButton, notificationStatus, schoolDisttCompanyName, viewNotifications;
    private FrameLayout navigationLayout, navigationSwipe;
    private LinearLayout notificationsButton;
    private SharedPreferences sharedPreferences;


    public interface NavigationFunctions {
//        void viewNavigationNotifications();
    }


    public NavigationPage(Activity activity, User user) {
        this.activity = activity;
        this.user = user;

        navigationBackButton = (ImageView) activity.findViewById(R.id.navigationBackButton);
        navigationButton = (ImageView) activity.findViewById(R.id.navigationButton);
        notificationStatus = (TextView) activity.findViewById(R.id.notificationStatus);
        schoolDisttCompanyName = (TextView) activity.findViewById(R.id.schoolDisttCompanyName);
        viewNotifications = (TextView) activity.findViewById(R.id.viewNotifications);

        notificationsButton = (LinearLayout) activity.findViewById(R.id.notificationsButton);
        navigationLayout = (FrameLayout) activity.findViewById(R.id.navigation);
        faqButton = (TextView) activity.findViewById(R.id.faqButton);
        contactUsButton = (TextView) activity.findViewById(R.id.contactUsButton);
        editAccountButton = (TextView) activity.findViewById(R.id.editAccountButton);
        addViewButton = (TextView) activity.findViewById(R.id.addViewButton);
        storeButton = (TextView) activity.findViewById(R.id.storeButton);
        logoutButton = (TextView) activity.findViewById(R.id.logoutButton);
        navigationSwipe = (FrameLayout) activity.findViewById(R.id.navigationSwipe);

        notificationsButton.setOnClickListener(this);
        navigationBackButton.setOnClickListener(this);
        navigationButton.setOnClickListener(this);
        faqButton.setOnClickListener(this);
        contactUsButton.setOnClickListener(this);
        editAccountButton.setOnClickListener(this);
        addViewButton.setOnClickListener(this);
        storeButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        navigationSwipe.setOnClickListener(this);
        viewNotifications.setOnClickListener(this);
        schoolDisttCompanyName.setOnClickListener(this);

        navigationSwipe.setOnTouchListener(onSwipeTouchListener);
        schoolDisttCompanyName.setText(StringUtils.changeFirstLetterToUppercase(user.getUserView()));

        sharedPreferences = AndroidUtils.getCommonSharedPrefs(activity);
        userUtils = new UserUtils(activity);
        notificationStatus.setText(sharedPreferences.getBoolean(AppConstants.IS_NOTIFICATIONS_ON, true) ? "On" : "Off");
        new UserViewsAdapter(activity, user);

//        if(user.getUserRoles().get(0).getRole()==10)
//        {
//            if(ManagerHCDashboardActivity.picUrl==null)
//            getImage();
//            else
//            {
//                System.out.println("Inside Else in Manager");
//            }
//
//
//        }
//        else if(user.getUserRoles().get(0).getRole()==11)
//        {
//            if(EmployeeHCDashboardActivity.picUrl==null)
//                getImage();
//            else
//            {
//                System.out.println("Inside Else in Employee");
//            }
//        }
//        else if(user.getUserRoles().get(0).getRole()==12)
//        {
//            if(FamilyHCDashboardActivity.picUrl==null)
//                getImage();
//            else
//            {
//                System.out.println("Inside Else in Family");
//            }
//        }
//        else
//        {
//            System.out.println("Inside Else in Navigation");
//        }

        boolean bn = sharedPreferences.getBoolean("Image Status", false);
        if (!bn)
            getImage();


    }

    private void makeToast(String title) {
        Toast.makeText(activity, title, Toast.LENGTH_LONG).show();
    }

    private void getImage() {
        Map<String, String> keysValues = new HashMap<>();
        keysValues.put("user_id", user.getUserId());

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
                            String pic = AppConstants.IMAGE_BASE_URL + jsonObject.getJSONObject("data").getJSONObject("userdata").getString("profile_pic");
                            if (user.getUserRoles().get(0).getRole() == 10)
                                ManagerHCDashboardActivity.picUrl = pic;
                            else if (user.getUserRoles().get(0).getRole() == 11)
                                EmployeeHCDashboardActivity.picUrl = pic;
                            else if (user.getUserRoles().get(0).getRole() == 12)
                                FamilyHCDashboardActivity.picUrl = pic;
//                             Target mTarget = new Target() {
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                    // Perform simple file operation to store this bitmap to your sd card
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Drawable errorDrawable) {
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                }
//                            };


                            sharedPreferences.edit().putBoolean("Image Status", true).commit();
//                            Picasso.with(activity).load(pic).transform(new CircleTransform()).placeholder(R.drawable.per).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.per).into(navigationButton);
                            Picasso.with(activity).load(pic).transform(new CircleTransform()).placeholder(R.drawable.per).error(R.drawable.per).into(navigationButton);

                            System.out.println("User image is following" + pic);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void toggleNavigation() {
        Animation textAnimation = AnimationUtils.loadAnimation(activity, R.anim.left_in);
        navigationLayout.setAnimation(textAnimation);
        navigationLayout.setVisibility(navigationLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener() {
        @Override
        public boolean onSwipeLeft() {
            if (navigationLayout.getVisibility() == View.VISIBLE) {
                return false;
            }
            toggleNavigation();
            return true;
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.navigationBackButton:
                activity.onBackPressed();
                break;
            case R.id.navigationButton:
                toggleNavigation();
                break;
            case R.id.faqButton:
                openActivity(Faq.class, false);
                break;
            case R.id.contactUsButton:
                openActivity(Contact_us.class, false);
                break;
            case R.id.editAccountButton:
                editAccountButton();
                toggleNavigation();
                break;
            case R.id.addViewButton:
                openActivity(AddUserViewActivity.class, false);
                break;
            case R.id.notificationsButton:
                toggleNotification();
                break;
            case R.id.storeButton:
                break;
            case R.id.logoutButton:
                userUtils.userLogout();
                break;

            case R.id.viewNotifications:
                viewNotifications();
                break;

            case R.id.schoolDisttCompanyName:
                schoolDisttCompanyName();
                break;
        }
    }

    private void schoolDisttCompanyName() {
        activity.startActivity(new Intent(activity, SearchActivity.class));
    }

    private void viewNotifications() {

    }

    private void editAccountButton() {
        //TODO edit account
        Intent editAccountIntent = new Intent(activity, RegisterActivity.class);

        editAccountIntent.putExtra(RegisterActivity.EXTRA_USER, user);
        editAccountIntent.putExtra(RegisterActivity.EXTRA_EDIT, true);
        activity.startActivityForResult(editAccountIntent, REQUEST_EDIT_ACCOUNT);
    }

    private void openActivity(Class aClass, boolean finishThis) {
        AndroidUtils.openActivity(activity, aClass, finishThis);
    }

    private void toggleNotification() {
        boolean afterToggle = userUtils.toggleNotification();
        notificationStatus.setText(afterToggle ? "On" : "Off");
    }


}
