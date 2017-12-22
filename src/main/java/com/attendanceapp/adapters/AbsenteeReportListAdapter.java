package com.attendanceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.Attendance;

import java.util.ArrayList;
import java.util.List;

public class AbsenteeReportListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Attendance> attendanceList = new ArrayList<>();

    public AbsenteeReportListAdapter(Context context, List<Attendance> attendanceList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.attendanceList = attendanceList;
    }

    @Override
    public int getCount() {
        return attendanceList.size();
    }

    @Override
    public Attendance getItem(int position) {
        return attendanceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        final TextView dateTextView;

        ViewHolder(View view) {
            dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view, position);
        return view;
    }


    private void bind(Attendance attendance, View view, int position) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.dateTextView.setText(attendance.getCreateDate());
    }


    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_absent, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

}
