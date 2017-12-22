package com.attendanceapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.API.APIHandler;
import com.attendanceapp.adapters.HCEmpCheckListAdapter;
import com.attendanceapp.models.HCEmpCheckList;
import com.attendanceapp.models.User;
import com.attendanceapp.utils.AndroidMultiPartEntity;
import com.attendanceapp.utils.AndroidUtils;
import com.attendanceapp.utils.CommonMethods;
import com.attendanceapp.utils.MobileConnectivity;
import com.attendanceapp.utils.UserUtils;
import com.attendanceapp.utils.WebUtils;
import com.attendanceapp.webserviceCommunicator.WebServiceHandler;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.entity.mime.HttpMultipartMode;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;


public class HcModule_Employee_Check_List_Activity extends Activity implements View.OnClickListener {


    private Uri fileUri;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int REQUEST_CAMERA = 0;
    private static final int SELECT_FILE = 1;
    //    file path
    String tempFilePath;

    // Adapter coding

    List<HCEmpCheckList> empCheckList = new ArrayList<>();
    List<HCEmpCheckList> empValuesCheckList = new ArrayList<>();
    HCEmpCheckListAdapter checkListAdapter = null;

    public static String employeeChecklist_id;

    private Boolean isBackPressed = false;

    UserUtils userUtils;
    String locationID, empID;
    ProgressDialog mDialog;
    List<EditText> allTimeEds = new ArrayList<EditText>();
    List<EditText> allCommentEds = new ArrayList<EditText>();
    Context mContext;
    ListView mCheckList;
    ProgressBar mProgress;
    SharedPreferences sharedPreferences;
    EditText notes_edit;
    ImageView pic1, pic2, pic3;
    private FrameLayout navigationLayout;
    protected ImageView navigationButton;
    private Animation textAnimation;
    Button saveBtn;
//    private Button buttonCompleteChecklist;

    ArrayList<String> checklist_option = null, checked_attribute = null, time_range = null, comment = null;

    private String scheduleID = "";

    private String isComplete = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_module_employee_check_list);
        initView();
        isComplete = "0";
        mContext = HcModule_Employee_Check_List_Activity.this;
        locationID = getIntent().getExtras().getString("locationID");
        scheduleID = getIntent().getExtras().getString("schedule_id");
//        int index = getIntent().getExtras().getInt("Index");
        // mDialog = new ProgressDialog(HcModule_Employee_Check_List_Activity.this);
        sharedPreferences = AndroidUtils.getCommonSharedPrefs(HcModule_Employee_Check_List_Activity.this);
//        buttonCompleteChecklist = (Button) findViewById(R.id.button_complete_checklist);

        sharedPreferences.edit().putBoolean("Image Status", false).commit();
        userUtils = new UserUtils(HcModule_Employee_Check_List_Activity.this);
//        navigationLayout = (FrameLayout) findViewById(R.id.navigation);
//        navigationButton = (ImageView) findViewById(R.id.navigationButton);
//        navigationButton.setOnClickListener(this);

//        HCEmployee teacherClass = userUtils.getUserWithDataFromSharedPrefs(Employee.class).getManagerLocationList().get(index);
//        locationID = teacherClass.getLocationId();
        User user = userUtils.getUserFromSharedPrefs();

        empID = user.getUserId();
//        employeeChecklist_id= teacherClass.getCheckListId();
        employeeChecklist_id = getIntent().getExtras().getString("CheckListID");

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

        Log.d(">>>>>>>>>>>>>", ">>>>>>>>>>>>" + empID + "   " + employeeChecklist_id);

    }

    /*
    Initialize views in this method
     */
    void initView() {

        mCheckList = (ListView) findViewById(R.id.dynamic_checklist);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        notes_edit = (EditText) findViewById(R.id.notes_edit);

        pic1 = (ImageView) findViewById(R.id.pic1);
        pic2 = (ImageView) findViewById(R.id.pic2);

        saveBtn = (Button) findViewById(R.id.saveBtn);
    }

    /*
    Hit API
     */

    private void getDesignAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                if (empID != null) {
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("id", employeeChecklist_id);
//                    hm.put("id", "49d025d1331bd29fcbd62ed6f2ab08cf");

//                    hm.put("emp_id", empID);


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


                            String valueHeading;

                            Boolean showYesNo,
                                    showNa,
                                    showComment,
                                    showTimeRange;

                            valueHeading = jsonObj.getString("key");

                            showYesNo = jsonObj.getBoolean("yes_no");
                            showNa = jsonObj.getBoolean("na");
                            showComment = jsonObj.getBoolean("comment");
                            showTimeRange = jsonObj.getBoolean("time_range");

                            HCEmpCheckList obj = new HCEmpCheckList();
                            obj.setValueHeading(valueHeading);
                            obj.setShowYesNo(showYesNo);
                            obj.setShowComment(showComment);
                            obj.setShowNa(showNa);
                            obj.setShowTimeRange(showTimeRange);
//                            obj.valueHeading = valueHeading;
//                            obj.showYesNo = showYesNo;
//                            obj.showNa = showNa;
//                            obj.showComment = showComment;
//                            obj.showTimeRange = showTimeRange;

                            empCheckList.add(obj);
                        }
                        if (empCheckList.size() > 0) {
//                            setAdapter();

                            setChecklists();
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
                    CommonMethods.getInstance().DisplayToast(mContext, "API not Responding ");
                    Log.v("API not Responding ", "Response Not available".toString());
                }


            }
        }.execute();
    }

    private void setChecklists() {
        mCheckList.setVisibility(View.GONE);
        RelativeLayout parent = (RelativeLayout) findViewById(R.id.dynamic_layout);
        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

// CREATE CHILD LAYOUTS
        LinearLayout newLLayout = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams LLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newLLayout.setLayoutParams(LLayoutParams);
        LLayoutParams.setMargins(5, 5, 5, 5);
        Resources res = getResources();

        newLLayout.setOrientation(LinearLayout.VERTICAL);
        newLLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        for (int l = 0; l < empCheckList.size(); l++) {
//            for(int l = 0; l<10; l++) {

            TextView checkHeading = new TextView(this);
            checkHeading.setBackgroundColor(res.getColor(R.color.dark_blue));
            checkHeading.setTextColor(Color.WHITE);
            checkHeading.setTextSize(16.0f);
            checkHeading.setPadding(10, 10, 10, 10);
            LinearLayout.LayoutParams prm = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            prm.setMargins(10, 10, 10, 5);
            checkHeading.setLayoutParams(prm);
            RadioGroup rbGrp = new RadioGroup(this);
            rbGrp.setLayoutParams(LLayoutParams);
            final RadioButton yesRB = new RadioButton(this);
            yesRB.setText("Yes");
            checkHeading.setLayoutParams(LLayoutParams);
            final RadioButton noRB = new RadioButton(this);
            noRB.setText("No");
            checkHeading.setLayoutParams(LLayoutParams);
            final RadioButton naRB = new RadioButton(this);
            naRB.setText("NA");
            checkHeading.setLayoutParams(LLayoutParams);
            LinearLayout timeLayout = new LinearLayout(this);
            timeLayout.setOrientation(LinearLayout.HORIZONTAL);
            timeLayout.setLayoutParams(LLayoutParams);
            final TextView timeLabelET = new TextView(this);
            timeLabelET.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            timeLabelET.setText("Time Range");
            timeLabelET.setTextSize(14.0f);
            timeLabelET.setTextColor(res.getColor(R.color.darkblue));
            timeLayout.addView(timeLabelET);

            final EditText timeET = new EditText(this);
            timeET.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            timeET.setBackgroundResource(R.drawable.edittext_bg);
            timeLayout.addView(timeET);
            allTimeEds.add(timeET);
            LinearLayout commentLayout = new LinearLayout(this);
            commentLayout.setOrientation(LinearLayout.HORIZONTAL);
            commentLayout.setLayoutParams(LLayoutParams);
            final TextView commentLabelET = new TextView(this);
            commentLabelET.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            commentLabelET.setText("Comments");
            commentLabelET.setTextSize(14.0f);
            commentLabelET.setTextColor(res.getColor(R.color.darkblue));

            final EditText commentET = new EditText(this);
            commentET.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            commentET.setBackgroundResource(R.drawable.edittext_bg);

            commentLayout.addView(commentLabelET);
            commentLayout.addView(commentET);
            allCommentEds.add(commentET);

            newLLayout.addView(checkHeading);
            rbGrp.addView(yesRB);
            rbGrp.addView(noRB);
            rbGrp.addView(naRB);
            newLLayout.addView(rbGrp);
            newLLayout.addView(timeLayout);
            newLLayout.addView(commentLayout);

            if (l == empCheckList.size() - 1) {

                TextView txtAttachImageVideo = new TextView(this);
                LinearLayout.LayoutParams UploadImageLP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                UploadImageLP.setMargins(20, 20, 20, 5);
                txtAttachImageVideo.setLayoutParams(UploadImageLP);
                txtAttachImageVideo.setText("Attach");
                txtAttachImageVideo.setTextSize(20);
                txtAttachImageVideo.setTextColor(res.getColor(R.color.white));
                txtAttachImageVideo.setPadding(20, 10, 20, 10);
                txtAttachImageVideo.setBackgroundColor(res.getColor(R.color.blue));
                newLLayout.addView(txtAttachImageVideo);
                txtAttachImageVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addImageAttachment();
                    }
                });

                Button buttonComplete = new Button(this);
                LinearLayout.LayoutParams completeLP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                completeLP.setMargins(20, 40, 20, 40);
                buttonComplete.setLayoutParams(completeLP);
//                buttonComplete.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                buttonComplete.setBackgroundColor(res.getColor(R.color.blue));
                buttonComplete.setText("Complete");
                buttonComplete.setTextSize(20);
                buttonComplete.setTextColor(res.getColor(R.color.white));
                buttonComplete.setPadding(10, 10, 10, 10);
                newLLayout.addView(buttonComplete);

                buttonComplete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (MobileConnectivity.checkNetworkConnections(mContext).isIntenetConnectionactive()) {
                            isComplete = "1";
                            updateCheckList_Retrofit("1");
                        }
                    }
                });

            }

            final HCEmpCheckList obj = empValuesCheckList.get(l);
            HCEmpCheckList showobj = empCheckList.get(l);
            String valueHeading = null,
                    valueYesNo = null,
                    valueNa = null,
                    valueComment = null,
                    valueTimeRange = null;

            Boolean showYesNo = false,
                    showNa = false,
                    showComment = false,
                    showTimeRange = false;


            valueHeading = (obj.getValueHeading());
            valueYesNo = (obj.getValueYesNo());
            valueNa = (obj.getValueNa());
            valueComment = (obj.getValueComment());
            valueTimeRange = (obj.getValueTimeRange());
            showYesNo = (showobj.getShowYesNo());
            showNa = (showobj.getShowNa());
            showComment = (showobj.getShowComment());
            showTimeRange = (showobj.getShowTimeRange());

//            yes_no_na_layout.setVisibility(View.VISIBLE);
//            checkList_data.setVisibility(View.VISIBLE);

            // Heading
            if (valueHeading != null) {
                checkHeading.setText(valueHeading);
            }

            // Radio buttons
            if (showYesNo) {
                yesRB.setVisibility(View.VISIBLE);
                noRB.setVisibility(View.VISIBLE);
            } else {
                yesRB.setVisibility(View.GONE);
                noRB.setVisibility(View.GONE);
            }

            if (showNa) {
                naRB.setVisibility(View.VISIBLE);
            } else {
                naRB.setVisibility(View.GONE);
            }

            // Texts
//            checkList_data.setVisibility(View.VISIBLE);
            if (showComment) {
                commentLayout.setVisibility(View.VISIBLE);

            } else {
                commentLayout.setVisibility(View.GONE);
            }
            if (showTimeRange) {
                timeLayout.setVisibility(View.VISIBLE);
            } else {
                timeLayout.setVisibility(View.GONE);
            }

            // Setting the Values

            String yes_no_val = obj.getValueYesNo().trim();
            if (showYesNo && yes_no_val.equalsIgnoreCase("yes")) {

                yesRB.setChecked(true);
                obj.setSelectedRadio("yes");
            } else if (showYesNo && yes_no_val.equalsIgnoreCase("no")) {
                noRB.setChecked(true);
                obj.setSelectedRadio("no");
            } else if (showNa && yes_no_val.equalsIgnoreCase("na")) {
                naRB.setChecked(true);
                obj.setSelectedRadio("na");
            } else {
                obj.setSelectedRadio("0");
            }
            if (showComment && (valueComment != null)) {
                commentET.setText(obj.getValueComment());
            }

            if (showTimeRange && (valueTimeRange != null)) {
                if (obj.getCmntChanged().equalsIgnoreCase("yes")) {
                    timeET.setText(obj.getValueTimeRange());
                } else {
                    timeET.setText(obj.getValueTimeRange());
                }
//                timeRange_edt.setText(obj.getValueTimeRange());
            }
            empValuesCheckList.set(l, obj);

            final int temp = l;
//            =  /*
//            Get which radio button is selected and update the array list
//             */
            yesRB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (yesRB.isChecked()) {
                        empValuesCheckList.get(temp).setSelectedRadio("yes");

                    }

                }
            });
            yesRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        empValuesCheckList.get(temp).setSelectedRadio("yes");
                    } else {
                        empValuesCheckList.get(temp).setSelectedRadio("0");

                    }

                }
            });


            noRB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (noRB.isChecked()) {
                        empValuesCheckList.get(temp).setSelectedRadio("no");

                    }
                }
            });

            noRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        empValuesCheckList.get(temp).setSelectedRadio("no");
                    } else {
                        empValuesCheckList.get(temp).setSelectedRadio("0");

                    }

                }
            });

            naRB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (naRB.isChecked()) {
                        empValuesCheckList.get(temp).setSelectedRadio("na");

                    }
                }
            });

            naRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        empValuesCheckList.get(temp).setSelectedRadio("na");
                    } else {
                        empValuesCheckList.get(temp).setSelectedRadio("0");

                    }

                }
            });
//            rbGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//
//                @Override
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    if (checkedId == R.id.yes_cb) {
//                        empValuesCheckList.get(temp).setSelectedRadio("yes");
//                    } else if (checkedId == R.id.no_cb) {
//                        empValuesCheckList.get(temp).setSelectedRadio("no");
//                    } else if (checkedId == R.id.na_cb) {
//                        empValuesCheckList.get(temp).setSelectedRadio("na");
//                    } else {
//
//                    }
//                }
//            });

            timeET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    try {
                        if (!hasFocus) {
                            int itemIndex = v.getId();
                            String enteredValue = timeET.getText().toString();
                            obj.setValueTimeRange(enteredValue);
//                            empValuesCheckList.get(temp).setValueTimeRange(enteredValue);
                        } else {
                            int itemIndex = v.getId();
                            String enteredValue = timeET.getText().toString();
                            obj.setValueTimeRange(enteredValue);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            });


//            timeET.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    obj.setCmntChanged("no");
//                }
//            });
            /*
        Text watcher to reflect changes in Time Range section
         */
//            timeET.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    obj.setCmntChanged("yes");
//                    empValuesCheckList.get(temp).setValueTimeRange(s.toString());
////                    updatedRange.put(position,s.toString());
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
////                    if(obj.getCmntChanged().equalsIgnoreCase("yes")){
////                        obj.setValueTimeRange(s.toString());
////                    }
//                    empValuesCheckList.get(temp).setValueTimeRange(s.toString());
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
////                    obj.setCmntChanged("done");
////                    notifyDataSetChanged();
//                }
//            });

            commentET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    try {
                        if (!hasFocus) {
                            int itemIndex = v.getId();
                            String enteredValue = commentET.getText().toString();
                            obj.setValueComment(enteredValue);
                        } else {
                            int itemIndex = v.getId();
                            String enteredValue = commentET.getText().toString();
                            obj.setValueComment(enteredValue);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }


        sv.addView(newLLayout, LLayoutParams);
        parent.addView(sv);

//        buttonCompleteChecklist.setVisibility(View.VISIBLE);
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
//                    hm.put("checklist_id", "49d025d1331bd29fcbd62ed6f2ab08cf");
                    hm.put("checklist_id", employeeChecklist_id);
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
                                    valueHeading,
                                    valueYesNo,

                                    valueComment,
                                    valueTimeRange;

                            valueHeading = jsonObj.getString("checklist_option");
                            valueYesNo = jsonObj.getString("checked_attribute");
                            valueComment = jsonObj.getString("comment");
                            valueTimeRange = jsonObj.getString("time_range");

//                            scheduleID = jsonObj.getString("schedule_id");

                            HCEmpCheckList objGetData = new HCEmpCheckList();


                            objGetData.setValueHeading(valueHeading);
                            objGetData.setValueYesNo(valueYesNo);

                            objGetData.setValueComment(valueComment);
                            objGetData.setValueTimeRange(valueTimeRange);

                            if (valueYesNo.equalsIgnoreCase("yes")) {
                                objGetData.setSelectedRadio("yes"); // If yes item is seleted
                            } else if (valueYesNo.equalsIgnoreCase("no")) {
                                objGetData.setSelectedRadio("no"); // If no seleted
                            } else if (valueYesNo.equalsIgnoreCase("na")) {
                                objGetData.setSelectedRadio("na"); // If NA item is seleted
                            } else {
                                objGetData.setSelectedRadio("0"); // If no item is seleted
                            }
                            objGetData.setCmntChanged("no");

                            empValuesCheckList.add(objGetData);
                        }
                        getDesignAsync();
//                        setAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    CommonMethods.getInstance().DisplayToast(mContext, "API not Responding ");
                    Log.v("API not Responding ", "Response Not available".toString());
                }
            }
        }.execute();
    }


    @Override
    public void onBackPressed() {
//        if (navigationLayout.getVisibility() == View.VISIBLE) {
//            textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
//            navigationLayout.setAnimation(textAnimation);
//            navigationLayout.setVisibility(View.GONE);
//        }
        if (checkListAdapter == null || empValuesCheckList == null) {
            super.onBackPressed();
        }
        if (MobileConnectivity.checkNetworkConnections(mContext).isIntenetConnectionactive()) {
//            updateCheckList();
            if (!isBackPressed) {
                isBackPressed = true;
                updateCheckList_Retrofit("0");

            } else {
                CommonMethods.getInstance().DisplayToast(mContext, "Please wait..");
            }
        } else {
            CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_internet));
        }

//        super.onBackPressed();
    }

    /*
    Update CheckList
     */
    void updateCheckList_Retrofit(String isComplete) {
        try {
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

//        ArrayList<String> checklist_option = null, checked_attribute = null, time_range = null, comment = null;
            //Get updated List from adapter
//        if (checkListAdapter != null) {
//            List<HCEmpCheckList> updatedList = checkListAdapter.getUpdatedCheckListValue();
            List<HCEmpCheckList> updatedList = empValuesCheckList;

            if (updatedList == null) return;

            int size = updatedList.size();
            checklist_option = new ArrayList<String>();
            checked_attribute = new ArrayList<String>();
            time_range = new ArrayList<String>();
            comment = new ArrayList<String>();
        /*
        Insert all array values according to their info
         */

            for (int i = 0; i < updatedList.size(); i++) {
                Log.v("items== ", "=>>>>  " + updatedList.get(i).getValueHeading());
                checklist_option.add(updatedList.get(i).getValueHeading());
                Log.v("checked_attribute==== ", updatedList.get(i).getSelectedRadio());

                //Add checked Attribute on behalf of selected item yes, no or NA
                checked_attribute.add(updatedList.get(i).getSelectedRadio());
                /*if (empCheckList.get(i).getShowYesNo() == false && empCheckList.get(i).getShowNa() == false) {
                    checked_attribute[i] = "0";
                } else {
                    // insert timerange item
                    checked_attribute[i] = updatedList.get(i).getSelectedRadio();
                }*/
                //Add Time Range if visible otherwise add 0
                if (empCheckList.get(i).getShowTimeRange() == false) {
                    time_range.add("0");
                } else {
                    // insert timerange item
//                    time_range.add(updatedList.get(i).getValueTimeRange());
                    time_range.add(allTimeEds.get(i).getText().toString());
                }
                //Add comment if visible otherwise add 0
                if (empCheckList.get(i).getShowComment() == false) {
                    comment.add("0");
                } else {
                    // insert comment
//                    comment.add(updatedList.get(i).getValueComment());
                    comment.add(allCommentEds.get(i).getText().toString());
                }
                CommonMethods.getInstance().e("", "checklist_option-> " + checklist_option.get(i));
            }
//        } else {
//            CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_something));
//            return;
//        }


            new AsyncUpdateCheckList().execute(isComplete);


       /* Call call = handler.saveCheckListData(employeeChecklist_id, checklist_option,
                checked_attribute, empID,
                time_range, comment);

        call.enqueue(new Callback<SaveCheckListResponse>() {
            @Override
            public void onResponse(Response<SaveCheckListResponse> response, Retrofit retrofit) {
                isBackPressed = false;
                SaveCheckListResponse obj = response.body();
                if (obj != null) {
                    //404 or the response cannot be converted to HomeScreenResponse.
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_something));
                    } else {
                        CommonMethods.getInstance().e("tag", response.body().toString());
                        // Success Now parse the response and Reflect it on UI.
                        CommonMethods.getInstance().DisplayToast(mContext, obj.getMessage());
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
//                view.onFailure(t.getMessage());
                CommonMethods.getInstance().e("", "exception-> " + t.toString());
                CommonMethods.getInstance().DisplayToast(mContext, mContext.getResources().getString(R.string.error_api));
                isBackPressed = false;
                finish();
            }
        });*/
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    protected void onResume() {
        super.onResume();

//        new NavigationPage(this, userUtils.getUserFromSharedPrefs());

    }


    private void setAdapter() {
        if (checkListAdapter == null) {
            checkListAdapter = new HCEmpCheckListAdapter(HcModule_Employee_Check_List_Activity.this, empCheckList, empValuesCheckList);
            mCheckList.setAdapter(checkListAdapter);
        } else {
            checkListAdapter.notifyDataSetChanged();
        }

    }

    public void gotoBack(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.navigationButton:
//                textAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
//                navigationLayout.setAnimation(textAnimation);
//                navigationLayout.setVisibility(View.VISIBLE);
//                break;
//
//        }
    }


    private class AsyncUpdateCheckList extends AsyncTask<String, String, String> {

        String response = "";
        String isComplete = "";
        private ProgressDialog progressDialog = new ProgressDialog(HcModule_Employee_Check_List_Activity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isComplete.equalsIgnoreCase("1")) {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        //        location_id, user_id, employee_id, startDate (yyyy-mm-dd) ,attends
        @Override
        protected String doInBackground(String... strings) {
            /*@POST("/attendance/Mobiles/checklistupdate")
            Call<SaveCheckListResponse>saveCheckListData(
                    @Field("checklist_id") String checklist_id,
                    @Field("checklist_option[]") ArrayList<String> checklist_option,
                    @Field("checked_attribute[]") ArrayList<String> checked_attribute,
                    @Field("emp_id") String emp_id,
                    @Field("time_range[]") ArrayList<String> time_range,
                    @Field("comment[]") ArrayList<String> comment);*/
            String strCheckListOption = checklist_option.toString();
            String strCheckListAttribute = checked_attribute.toString();
            String strCheckTimeRange = time_range.toString();
            String strCheckListComment = comment.toString();

            strCheckListOption = strCheckListOption.substring(1, strCheckListOption.length() - 1);
            strCheckListAttribute = strCheckListAttribute.substring(1, strCheckListAttribute.length() - 1);
            strCheckTimeRange = strCheckTimeRange.substring(1, strCheckTimeRange.length() - 1);
            strCheckListComment = strCheckListComment.substring(1, strCheckListComment.length() - 1);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("checklist_id", employeeChecklist_id));
            nameValuePairs.add(new BasicNameValuePair("checklist_option", strCheckListOption));
            nameValuePairs.add(new BasicNameValuePair("checked_attribute", strCheckListAttribute));
            nameValuePairs.add(new BasicNameValuePair("emp_id", empID));
            nameValuePairs.add(new BasicNameValuePair("time_range", strCheckTimeRange));
            nameValuePairs.add(new BasicNameValuePair("comment", strCheckListComment));
            nameValuePairs.add(new BasicNameValuePair("schedule_id", scheduleID));
            nameValuePairs.add(new BasicNameValuePair("is_complete", strings[0]));
            isComplete = strings[0];

            response = new WebServiceHandler().webServiceCall(nameValuePairs, AppConstants.URL_CHECKLIST_UPDATE);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(HcModule_Employee_Check_List_Activity.this, "Checklist saved", Toast.LENGTH_LONG).show();
            if (isComplete.equalsIgnoreCase("1")) {
                finish();
            }
        }
    }


    private void addImageAttachment() {

        final CharSequence[] items = {"Take image", "Record video", "Select from library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HcModule_Employee_Check_List_Activity.this);
        builder.setTitle("Add attachment");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take image")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Record video")) {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    // create a file to save the video
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    // set the image file name
                    java.util.Date date = new java.util.Date();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory().getPath() + timeStamp + "videocapture_example.mp4");
                    // set the video image quality to high
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                    // start the Video Capture Intent
                    startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
                    dialog.dismiss();
                } else if (items[item].equals("Select from library")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*,video/*");
                        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            Uri selectedImageUri = null;

            switch (requestCode) {
                case REQUEST_CAMERA:
                    try {
                        bitmap = (Bitmap) data.getExtras().get("data");
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        tempFilePath = saveImage(bitmap, timeStamp + ".jpg");

                        tempFilePath = saveImage(bitmap, timeStamp + ".jpg");

//                        sendMsg(tempFilePath, "Image");
                        popUpConfirmSendImage(tempFilePath);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SELECT_FILE:
                    try {
                        selectedImageUri = data.getData();
                        tempFilePath = getPath(selectedImageUri, HcModule_Employee_Check_List_Activity.this);


                        popUpConfirmSendImage(tempFilePath);
                        /*File f = new File(tempFilePath);
                        double bytes = f.length();
                        double kilobytes = (bytes / 1024);
                        double megabytes = (kilobytes / 1024);
                        if (megabytes < 10) {
                            popUpConfirmSendImage(tempFilePath);
                        } else {
                            Toast.makeText(HcModule_Employee_Check_List_Activity.this, "File size must be less then 10mb", Toast.LENGTH_LONG).show();
                        }*/
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE:
                    try {
                        selectedImageUri = data.getData();
                        tempFilePath = getPath(selectedImageUri, HcModule_Employee_Check_List_Activity.this);

                        popUpConfirmSendImage(tempFilePath);
                        /*File f = new File(tempFilePath);
                        double bytes = f.length();
                        double kilobytes = (bytes / 1024);
                        double megabytes = (kilobytes / 1024);
                        if (megabytes < 10) {
                            sendMsg(tempFilePath, "Video");
                        } else {
                            Toast.makeText(HcModule_Employee_Check_List_Activity.this, "Recorded video size must be less then 10mb", Toast.LENGTH_LONG).show();
                        }*/

//                        sendMsg(tempFilePath, 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
//            popUpConfirmSendImage(tempFilePath);
        }
    }

    public String getPath(Uri uri, Activity activity) {

        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }


    private static File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NotifyApp");

        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {
                Log.d("NotifyApp", "Failed to create directory NotifyApp.");
                return null;
            }
        }

        // Create a media file name
        // For unique file name appending current timeStamp with file name
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());

        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO) {
            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    private Boolean isSDPresent;
    private File myfileNew;
    private String root = Environment.getExternalStorageDirectory().toString() + "/NotifyApp";
    private File myDir = new File(root);

    public String saveImage(Bitmap finalBitmap, String fileName) {
        isSDPresent = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (isSDPresent) {
            myDir.mkdirs();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            myfileNew = new File(myDir, fileName);
            if (myfileNew.exists()) myfileNew.delete();

            try {
                FileOutputStream out = new FileOutputStream(myfileNew);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (myfileNew.exists())
                myfileNew.delete();
            try {
                FileOutputStream fos = new FileOutputStream(myfileNew);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MediaScannerConnection.scanFile(HcModule_Employee_Check_List_Activity.this,
                new String[]{myfileNew.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v("imageeee", "file " + path
                                + " was scanned seccessfully: " + uri);
                    }
                });

        return myfileNew.toString();
    }


    public void popUpConfirmSendImage(final String fileToSend) {
        final Dialog dialogPopUp = new Dialog(HcModule_Employee_Check_List_Activity.this);
        dialogPopUp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPopUp.setCancelable(false);
        dialogPopUp.setContentView(R.layout.popup_send_image_confirm);
        dialogPopUp.show();

        TextView textSendImage = (TextView) dialogPopUp.findViewById(R.id.txt_delete_chat);
        TextView textCancel = (TextView) dialogPopUp.findViewById(R.id.txt_cancel_delete);

        textSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {


                    dialogPopUp.dismiss();
                    if (fileToSend.contains(".mp4.") || fileToSend.contains(".3gp.") || fileToSend.contains(".mkv.")
                            || fileToSend.contains(".mov.") || fileToSend.contains(".mp4.")) {
//                        sendMsg(fileToSend, "Image");
                        new AsyncUploadFile().execute("video", fileToSend);
                    } else if (fileToSend.contains(".mp4") || fileToSend.contains(".3gp") || fileToSend.contains(".mkv")
                            || fileToSend.contains(".mov") || fileToSend.contains(".mp4")) {
//                        sendMsg(fileToSend, "Video");
                        new AsyncUploadFile().execute("video", fileToSend);
                    } else {
//                        sendMsg(fileToSend, "Image");
                        new AsyncUploadFile().execute("image", fileToSend);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HcModule_Employee_Check_List_Activity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPopUp.dismiss();
            }
        });
    }


    private class AsyncUploadFile extends AsyncTask<String, String, String> {

        boolean userLogin = false;
        long totalSize = 0;
        String responseString = "";
        String mediaType = "2";
        private String msgIdStatus = "";
        private ProgressDialog progressDialog = new ProgressDialog(HcModule_Employee_Check_List_Activity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(final String... strings) {
            try {

                        HttpParams httpParams = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams,
                                30000);
                        int timeoutSocket = 30000;
                        HttpConnectionParams.setSoTimeout(httpParams,
                                timeoutSocket);
                        try {


                            HttpClient httpClient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(AppConstants.URL_ADD_CHECKLIST_MEDIA);

                            String BOUNDARY = "Boundary-8B33EF29-2436-47F6-A415-62EF61F62D14";

							/*MultipartEntity entity = new MultipartEntity(
									HttpMultipartMode.BROWSER_COMPATIBLE,
									BOUNDARY, Charset.defaultCharset());*/

                            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                                    HttpMultipartMode.BROWSER_COMPATIBLE,
                                    BOUNDARY, Charset.defaultCharset(),new AndroidMultiPartEntity.ProgressListener() {

                                @Override
                                public void transferred(long num) {
                                    // TODO Auto-generated method stub
                                    publishProgress(""+(int) ((num / (float) totalSize) * 100));
                                }
                            });

                            httppost.setHeader("Accept", "application/json");
                            httppost.setHeader("Content-Type",
                                    "multipart/form-data; boundary=" + BOUNDARY);

//                            Param:checklist_id,employee_id,schedule_id,location_id,image,video

                                File myFile = new File(strings[1]);
                                FileBody fileBody = new FileBody(myFile);
                                entity.addPart(new FormBodyPart(strings[0], fileBody));


                            entity.addPart("checklist_id", new StringBody(employeeChecklist_id));
                            entity.addPart("employee_id", new StringBody(empID));
                            entity.addPart("schedule_id", new StringBody(scheduleID));
                            entity.addPart("location_id", new StringBody(locationID));


                            totalSize = entity.getContentLength();

                            httppost.setEntity(entity);
                            httpClient = new DefaultHttpClient(httpParams);
                            HttpResponse response;
                            response = httpClient.execute(httppost);
                            responseString = EntityUtils.toString(response
                                    .getEntity());

                            JSONObject jsonObjectRes = new JSONObject(responseString);
                            String strStatus = jsonObjectRes
                                    .getString("status");


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0] +" % uploaded");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                progressDialog.dismiss();

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


}