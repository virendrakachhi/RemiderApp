package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.HCModuleDayWeekMonthYearViewBean;

import java.util.ArrayList;
import java.util.List;

public class HCModuleDayWeekMonthYearListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HCModuleDayWeekMonthYearViewBean> scheduleList = new ArrayList<>();

    public HCModuleDayWeekMonthYearListAdapter(Activity context, List<HCModuleDayWeekMonthYearViewBean> schedulingList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.scheduleList = schedulingList;
    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public HCModuleDayWeekMonthYearViewBean getItem(int position) {
        return scheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        final TextView  timeTypeHeader,selectedTimeType,details;


        ViewHolder(View view) {
            //  othersDateTextView = (TextView) view.findViewById(R.id.other);
            timeTypeHeader = (TextView) view.findViewById(R.id.timeTypeHeader);
            selectedTimeType = (TextView) view.findViewById(R.id.selectedTimeType);
            details = (TextView) view.findViewById(R.id.details);



        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view, position);
        return view;
    }



    private void bind(HCModuleDayWeekMonthYearViewBean schedule, View view, int position) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.timeTypeHeader.setText(schedule.heading);
        holder.selectedTimeType.setText(schedule.date);
        holder.details.setText(schedule.scheduleDetails);
/*
        holder.timeTypeHeader.setText(scheduleList.get(position).heading);
        holder.selectedTimeType.setText(scheduleList.get(position).heading);
        holder.details.setText(scheduleList.get(position).scheduleDetails);
*/
    }


    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_hc_module_day_week_month_year,  null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }
}