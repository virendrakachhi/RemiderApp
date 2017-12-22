package com.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.attendanceapp.API.APIHandler;
import com.attendanceapp.adapters.HCManagerCheckListAdapter;
import com.attendanceapp.models.HCEmpCheckList;
import com.attendanceapp.models.MediaBean;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.CommonMethods;
import com.attendanceapp.utils.MobileConnectivity;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;


/**
 * Created by ritesh.local on 1/22/2016.
 */
public class HcModule_Manager_Check_List_Activity extends Activity {


    // Adapter coding

    List<HCEmpCheckList> empCheckList = new ArrayList<>();
    List<MediaBean> arrMedia = new ArrayList<MediaBean>();
    List<HCEmpCheckList> empValuesCheckList = new ArrayList<>();
    // HCEmpCheckListAdapter checkListAdapter = null;
    HCManagerCheckListAdapter checkListAdapter = null;

    public static String employeeChecklist_id;

    private Boolean isBackPressed = false;
    //
    String empName = "";
    private TextView txtTitle;
    UserUtils userUtils;
    String locationID, empID;
    ProgressDialog mDialog;

    Context mContext;
    ListView mCheckList;
    ProgressBar mProgress;
    EditText notes_edit;
    ImageView pic1, pic2, pic3;
    Button saveBtn;

    private String scheduleID = "";

    private String media = "";
    private boolean isImage = false;

    // Image
    private Uri mImageUri;
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_TAKE_GALLERY = 2;
    String mCurrentPhotoPath;
    String mSelectedImagePath = null;
    String ImgPath = "emulated/0/Notify";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_module_manager_check_list);
        initView();

        media = "";
        isImage = false;

        mContext = HcModule_Manager_Check_List_Activity.this;
        int index = getIntent().getExtras().getInt("Index");
        // mDialog = new ProgressDialog(HcModule_Employee_Check_List_Activity.this);
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(HcModule_Manager_Check_List_Activity.this);

        sharedPreferences.edit().putBoolean("Image Status", false).commit();
        userUtils = new UserUtils(HcModule_Manager_Check_List_Activity.this);

        locationID = getIntent().getExtras().getString("locationID");
        scheduleID = getIntent().getExtras().getString("schedule_id");
//        HCEmployee teacherClass = userUtils.getUserWithDataFromSharedPrefs(Employee.class).getManagerLocationList().get(index);
//        locationID = teacherClass.getLocationId();
        User user = userUtils.getUserFromSharedPrefs();

        empID = getIntent().getExtras().getString("EmployeeID");

        employeeChecklist_id = getIntent().getExtras().getString("CheckListID");
        empName = getIntent().getExtras().getString("EmployeeName");
        txtTitle.setText(empName);

        mCheckList.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        notes_edit.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        if (MobileConnectivity.checkNetworkConnections(mContext).isIntenetConnectionactive()) {
            getUpdatedDataAsync();
        } else {
            CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_internet));
            finish();
        }

    }

    /*
    Initialize views in this method
     */
    void initView() {

        mCheckList = (ListView) findViewById(R.id.dynamic_checklist);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        notes_edit = (EditText) findViewById(R.id.notes_edit);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        pic1 = (ImageView) findViewById(R.id.pic1);
        pic2 = (ImageView) findViewById(R.id.pic2);

        saveBtn = (Button) findViewById(R.id.saveBtn);


    }

    /*
    Hit API
     */


    private View.OnClickListener ClickListenerDelegate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

            }
        }
    };

    private void getDesignAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                if (empID != null) {
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("id", employeeChecklist_id);
//                    hm.put("id", "49d025d1331bd29fcbd62ed6f2ab08cf");

                    hm.put("emp_id", empID);


                    try {

                        return new WebUtils().post(AppConstants.URL_GET_CHECKLIST, hm);
//                        return new WebUtils().post("http://182.71.22.43/attendance/Mobiles/getCheckList", hm);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                  /*  if(employeeChecklist_id!=null) // starting  employeeChecklist_id!=null
                    {*/

                    try {

                        JSONObject jsn = new JSONObject(result);
                        JSONArray jsnArr = jsn.getJSONArray("Data");
                        for (int i = 0; i < jsnArr.length(); i++) {
                            JSONObject jsonObj = jsnArr.getJSONObject(i).getJSONObject("location_checklists");


                            String
                                    id,
                                    location_id,
                                    emp_id,
                                    location_checklists,
                                    valueHeading;

                            Boolean showYesNo,
                                    showNa,
                                    showComment,
                                    showTimeRange;

                            valueHeading = jsonObj.getString("key");

                            showYesNo = Boolean.parseBoolean(jsonObj.getString("yes_no"));
                            showNa = Boolean.parseBoolean(jsonObj.getString("na"));
                            showComment = Boolean.parseBoolean(jsonObj.getString("comment"));
                            showTimeRange = Boolean.parseBoolean(jsonObj.getString("time_range"));

                            HCEmpCheckList obj = new HCEmpCheckList();

                            obj.valueHeading = valueHeading;
                            obj.showYesNo = showYesNo;
                            obj.showNa = showNa;
                            obj.showComment = showComment;
                            obj.showTimeRange = showTimeRange;

                            empCheckList.add(obj);
                        }
                        if (empCheckList.size() > 0) {
                            setAdapter();
                        } else {
                            CommonMethods.getInstance().DisplayToast(mContext, "No item is list");
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


       /*         }
                  else{

                        CommonMethods.getInstance().DisplayToast(mContext, "No Checklist assigned to you right now, " +
                                "Ask your manager to assign you a Checklist ");
                    }// closeing  of if(employeeChecklist_id!=null)*/
                } else {
                    CommonMethods.getInstance().DisplayToast(mContext, "API not Seponding ");
                    Log.v("API not Seponding ", "Response Not available".toString());
                }


            }
        }.execute();

    }

    private void getUpdatedDataAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mProgress != null) {
                    mProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected String doInBackground(Void... params) {

                if (empID != null) {
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("checklist_id", employeeChecklist_id);
                    // hm.put("checklist_id", employeeChecklist_id);
                    hm.put("emp_id", empID);
                    hm.put("location_id", locationID);
                    hm.put("schedule_id", scheduleID);
//                    hm.put("emp_id", "47");
                    try {
                        return new WebUtils().post(AppConstants.URL_GET_UPDATED_CHECKLIST, hm);
//                        return new WebUtils().post("http://182.71.22.43/attendance/Mobiles/getUpdatedList/", hm);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                if (result != null) {

                    try {

                        JSONObject jsn = new JSONObject(result);
                        JSONArray jsnArr = jsn.getJSONArray("Data");
                        if (jsnArr.length() == 0) {
                            CommonMethods.getInstance().DisplayToast(mContext, "No item in list");
                            return;
                        }
                        for (int i = 0; i < jsnArr.length(); i++) {
                            JSONObject jsonObj = jsnArr.getJSONObject(i).getJSONObject("schedule_checklists_dones");


                            String
                                    id,
                                    location_id,
                                    emp_id,
                                    location_checklists,
                                    valueHeading,
                                    valueYesNo,
                                    valueNa,
                                    valueComment,
                                    valueTimeRange;

                            Boolean showYesNo,
                                    showNa,
                                    showComment,
                                    showTimeRange;

                            valueHeading = jsonObj.getString("checklist_option");
                            valueYesNo = jsonObj.getString("checked_attribute");
                            // valueNa = jsonObj.getString("key");
                            valueComment = jsonObj.getString("comment");
                            valueTimeRange = jsonObj.getString("time_range");
/*
                            showYesNo =Boolean.parseBoolean(jsonObj.getString("yes_no"));
                            showNa =Boolean.parseBoolean(jsonObj.getString("na"));
                            showComment =Boolean.parseBoolean(jsonObj.getString("comment"));
                            showTimeRange =Boolean.parseBoolean(jsonObj.getString("time_range"));*/

                            HCEmpCheckList objGetData = new HCEmpCheckList();


                            objGetData.valueHeading = valueHeading;
                            objGetData.valueYesNo = valueYesNo;
                            // objGetData.showNa=showNa;
                            objGetData.valueComment = valueComment;
                            objGetData.valueTimeRange = valueTimeRange;

                            String yes_no_val = valueYesNo.trim();

                            if (valueYesNo.equalsIgnoreCase("yes")) {
                                objGetData.setSelectedRadio("1"); // If yes item is seleted
                            } else if (valueYesNo.equalsIgnoreCase("no")) {
                                objGetData.setSelectedRadio("0"); // If no seleted
                            } else if (valueYesNo.equalsIgnoreCase("na")) {
                                objGetData.setSelectedRadio("-1"); // If NA item is seleted
                            } else {
                                objGetData.setSelectedRadio("0"); // If no item is seleted
                            }

                            empValuesCheckList.add(objGetData);
                        }


                        getDesignAsync();

                        /*{

                            "schedule_checklists_media":{
                            "id":"6",
                                    "emp_id":"890",
                                    "image":"",
                                    "video":"http:\/\/abdevs.com\/abdevs_beacon\/attendance\/app\/webroot\/img\/1467294999VID_20160630_095633.3gp",
                                    "checklist_id":"fb1498907be13d4ba7c1d92f004fa53a",
                                    "schedule_id":"644",
                                    "location_id":"287"
                        }

                        },*/
                        arrMedia.clear();
                        JSONArray jsnArrMedia = jsn.optJSONArray("Media");
                        for (int i = 0; i < jsnArrMedia.length(); i++) {
                            JSONObject objectMedia = jsnArrMedia.optJSONObject(i);
                            JSONObject objectMediaDetail = objectMedia.optJSONObject("schedule_checklists_media");
                            media = objectMediaDetail.optString("image");
                            isImage = true;
                            MediaBean mediaBean = new MediaBean();

                            if (media.length() == 0){
                                isImage = false;
                                media = objectMediaDetail.optString("video");
                                int slashPos = media.lastIndexOf("/");
                                String mediaName = media.substring(slashPos + 1, media.length());
                                mediaBean.setStrMediaName(mediaName);
                                mediaBean.setStrMediaType("video");
                                mediaBean.setStrMediaURL(media);
                            } else {
                                int slashPos = media.lastIndexOf("/");
                                String mediaName = media.substring(slashPos + 1, media.length());
                                mediaBean.setStrMediaName(mediaName);
                                mediaBean.setStrMediaType("image");
                                mediaBean.setStrMediaURL(media);
                            }
                            arrMedia.add(mediaBean);
                        }

//                        setAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    CommonMethods.getInstance().DisplayToast(mContext, "API not Seponding ");
                    Log.v("API not Seponding ", "Response Not available".toString());
                }


            }
        }.execute();

    }


    /*private void updateCheckListAsync(final String timeKey, final String CommentKey) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationID);
                hm.put("emp_id", empID);
                hm.put(CommentKey, "No Comments");
                hm.put(timeKey, "0");
                try {
                    return new WebUtils().post(AppConstants.URL_SAVE_CHECKLIST_DATA, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if ((mDialog != null) && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                if (result != null) {
                    try {
                        Toast.makeText(getApplicationContext(), new JSONObject(result).getString("Message"), Toast.LENGTH_LONG).show();
                        updateDataAsync();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }*/

    /*private void updateCheckListNAAsync(final String timeKey, final String CommentKey) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationID);
                hm.put("emp_id", empID);
                hm.put(CommentKey, "NA");
                hm.put(timeKey, "NA");
                try {
                    return new WebUtils().post(AppConstants.URL_SAVE_CHECKLIST_DATA, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if ((mDialog != null) && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                if (result != null) {
                    try {
                        Toast.makeText(getApplicationContext(), new JSONObject(result).getString("Message"), Toast.LENGTH_LONG).show();
                        updateDataAsync();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }

    private void updateCheckListRadioAsync(final String radioKey, final String radioValue) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                mDialog.setMessage("Please wait...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationID);
                hm.put("emp_id", empID);
                hm.put(radioKey, radioValue);
                try {
                    return new WebUtils().post(AppConstants.URL_SAVE_CHECKLIST_DATA, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if ((mDialog != null) && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                if (result != null) {
                    try {
                        Toast.makeText(getApplicationContext(), new JSONObject(result).getString("Message"), Toast.LENGTH_LONG).show();
                        updateDataAsync();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }

    ProgressDialog mUpdateDialog;

    private void updateCheckList() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                mUpdateDialog = new ProgressDialog(mContext);
                mUpdateDialog.setMessage("Updating..");
                mUpdateDialog.setCancelable(false);
                mUpdateDialog.show();
//                mDialog.setMessage("Please wait...");
//                mDialog.setCancelable(false);
//                mDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {


                HashMap<String, String> hm = new HashMap<>();
                hm.put("location_id", locationID);
                hm.put("emp_id", empID);


                try {
                    return new WebUtils().post(AppConstants.URL_SAVE_CHECKLIST_DATA, hm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
//                if ((mDialog != null) && mDialog.isShowing()) {
//                    mDialog.dismiss();
//                }
                if (mUpdateDialog != null) {
                    if (mUpdateDialog.isShowing()) mUpdateDialog.dismiss();
                }
                if (result != null) {
                    try {

                        Toast.makeText(getApplicationContext(), new JSONObject(result).getString("Message"), Toast.LENGTH_LONG).show();
                        updateDataAsync();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }*/

////////////////////////////////////


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*
    Update CheckList
     */
    void updateCheckList_Retrofit() {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //Parse API response in My Response class
        APIHandler handler = retrofit.create(APIHandler.class);

        String[] checklist_option = null, checked_attribute = null, time_range = null, comment = null;
        //Get updated List from adapter
        if (checkListAdapter != null) {
            List<HCEmpCheckList> updatedList = checkListAdapter.getUpdatedCheckListValue();
            if (updatedList == null) return;

            int size = updatedList.size();
            checklist_option = new String[size];
            checked_attribute = new String[size];
            time_range = new String[size];
            comment = new String[size];
        /*
        Insert all array values according to their info
         */
            for (int i = 0; i < updatedList.size(); i++) {
                checklist_option[i] = updatedList.get(i).getValueHeading();
                //Add checked Attribute on behalf of selected item yes, no or NA
                checked_attribute[i] = updatedList.get(i).getSelectedRadio();
                /*if (empCheckList.get(i).getShowYesNo() == false && empCheckList.get(i).getShowNa() == false) {
                    checked_attribute[i] = "0";
                } else {
                    // insert timerange item
                    checked_attribute[i] = updatedList.get(i).getSelectedRadio();
                }*/
                //Add Time Range if visible otherwise add 0
                if (empCheckList.get(i).getShowTimeRange() == false) {
                    time_range[i] = "0";
                } else {
                    // insert timerange item
                    time_range[i] = updatedList.get(i).getValueTimeRange();
                }
                //Add comment if visible otherwise add 0
                if (empCheckList.get(i).getShowComment() == false) {
                    comment[i] = "0";
                } else {
                    // insert comment
                    comment[i] = updatedList.get(i).getValueComment();
                }
                CommonMethods.getInstance().e("", "checklist_option-> " + checklist_option[i]);
            }


        } else {
            CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_something));
            return;
        }


//        Call call = handler.saveCheckListData("7b83ce6b4258c36753ae", checklist_option,
//                checked_attribute, "2",
//                time_range, comment);
//
//        call.enqueue(new Callback<SaveCheckListResponse>() {
//            @Override
//            public void onResponse(Response<SaveCheckListResponse> response, Retrofit retrofit) {
//                isBackPressed = false;
//                SaveCheckListResponse obj = response.body();
//                if (obj != null) {
//                    //404 or the response cannot be converted to HomeScreenResponse.
//                    ResponseBody responseBody = response.errorBody();
//                    if (responseBody != null) {
//                        CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_something));
//                    } else {
//                        CommonMethods.getInstance().e("tag", response.body().toString());
//                        // Success Now parse the response and Reflect it on UI.
//                        CommonMethods.getInstance().DisplayToast(mContext, obj.getMessage());
//                        finish();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
////                view.onFailure(t.getMessage());
//                CommonMethods.getInstance().e("","exception-> " + t.toString());
//                CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_api));
//                isBackPressed = false;
//                finish();
//            }
//        });
    }

    private void setAdapter() {
        if (checkListAdapter == null) {
            checkListAdapter = new HCManagerCheckListAdapter(this, empCheckList, empValuesCheckList, arrMedia);
            mCheckList.setAdapter(checkListAdapter);
        } else {
            checkListAdapter.notifyDataSetChanged();
        }

    }

    public void gotoBack(View view) {
        onBackPressed();
    }

   /* public void gotoBack(View view) {

        updateCheckList();
        super.onBackPressed();
    }*/

}