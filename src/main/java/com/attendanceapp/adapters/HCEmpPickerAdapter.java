package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.HCEmployee;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VICKY KUMAR on 21-01-2016.
 */
public class HCEmpPickerAdapter extends ArrayAdapter<HCEmployee> {
Context ctx;
    List<HCEmployee> empList;
    private LayoutInflater inflater;
    public HCEmpPickerAdapter(Context context, List<HCEmployee> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        empList = objects;
        ctx = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       convertView =  inflater.inflate(R.layout.hc_emp_picker_item, null);
        TextView empName = (TextView)convertView.findViewById(R.id.emp_name_txt);
        empName.setText(empList.get(position).getName());
        return convertView;
    }
}
