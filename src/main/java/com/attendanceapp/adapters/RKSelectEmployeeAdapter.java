package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.R;
import com.attendanceapp.activities.RKSelectEmployeeActivity;
import com.attendanceapp.models.RKSelectEmployeeBean;

import java.util.List;

/**
 * Created by ritesh.local on 2/5/2016.
 */
public class RKSelectEmployeeAdapter extends ArrayAdapter<RKSelectEmployeeBean> {

    Activity context;
    List<RKSelectEmployeeBean> rowItems;

    public RKSelectEmployeeAdapter(Activity context, int resourceId, List<RKSelectEmployeeBean> items)
    {
        super(context, resourceId, items);
        this.context = context;
        this.rowItems = items;
    }

    /* private view holder class */
    private class ViewHolder {
        TextView name;
        TextView email;
        CheckBox empSelected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        RKSelectEmployeeBean rowItem = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView==null)
        {
            convertView = mInflater.inflate(R.layout.rk_select_employee_data_modle,null);
            viewHolder = new ViewHolder();

            viewHolder.name = (TextView) convertView.findViewById(R.id.emp_name_txt);
            viewHolder.email = (TextView) convertView.findViewById(R.id.emp_email_txt);
            viewHolder.empSelected = (CheckBox) convertView.findViewById(R.id.select_emp_ckb);

            convertView.setTag(viewHolder);

// net Code
            viewHolder.empSelected.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v ;
                    RKSelectEmployeeBean BeanData = rowItems.get(position);
                    if(!cb.isChecked()) {
                        BeanData.setIsChecked(false);
                    }
                    else
                    {
                        BeanData.setIsChecked(true);
                    }
                    RKSelectEmployeeActivity.rowItems.set(position,BeanData);
                    Toast.makeText(context,"Clicked on Checkbox: " + cb.getText() +
                                    " is " + RKSelectEmployeeActivity.rowItems.get(position).getIsChecked(),
                            Toast.LENGTH_LONG).show();

// BeanData.setIsChecked(cb.isChecked());
                }
            });
        }

        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(rowItem.getmName());
        viewHolder.email.setText(rowItem.getmEmail());
        viewHolder.empSelected.setChecked(rowItem.getIsChecked());
        return convertView;
    }
}
