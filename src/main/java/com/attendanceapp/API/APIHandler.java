
package com.attendanceapp.API;



import com.attendanceapp.API.Response.SaveCheckListResponse;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;


/**
 * Created by Raj on 1/6/2016.
 */

public interface APIHandler {
//Get CheckList Data
    @FormUrlEncoded
    @POST("/attendance/Mobiles/checklistupdate")
    Call<SaveCheckListResponse>saveCheckListData(
            @Field("checklist_id") String checklist_id,
            @Field("checklist_option[]") ArrayList<String> checklist_option,
            @Field("checked_attribute[]") ArrayList<String> checked_attribute,
            @Field("emp_id") String emp_id,
            @Field("time_range[]") ArrayList<String> time_range,
            @Field("comment[]") ArrayList<String> comment);




}

