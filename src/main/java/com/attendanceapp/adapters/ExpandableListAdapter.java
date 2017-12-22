package com.attendanceapp.adapters;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.Attendance;
import com.attendanceapp.models.EmployeeSubAttendance;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    private List<String> _listPunctuality; //Punctuality
    // child data in format of header title, child title
    private HashMap<String, List<Attendance>> _listDataChild;
    ViewHolder holder = null;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<Attendance>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    /* private view holder class */
    private static class ViewHolder {
        TextView statusListItem;
        TextView  dateListItem;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {

        Attendance rowItem = (Attendance) getChild(groupPosition,childPosition);
        ViewHolder holder = null;
        LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.employee_sub_attendance, null);
            holder = new ViewHolder();
            holder.statusListItem = (TextView) convertView.findViewById(R.id.statusListItem);
            holder.dateListItem = (TextView) convertView.findViewById(R.id.dateListItem);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(rowItem.isPresent())
        {
            holder.statusListItem.setText("A");
        }
        else
        {
            if(rowItem.getAttend().matches("P"))
            {
                holder.statusListItem.setText("P");
            }
//            holder.statusListItem.setText("A");
        }

        holder.dateListItem.setText(rowItem.getCreateDate());
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }




    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {


        long l =getGroupId(groupPosition);
        int pos =(int) l;

            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.employee_super_attendance, null);

        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setText(_listDataHeader.get(pos));

//        TextView lblPunctuality = (TextView) convertView.findViewById(R.id.lblPunctuality);

//        lblPunctuality.setText("Punctuality 8"+pos+"%");

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}