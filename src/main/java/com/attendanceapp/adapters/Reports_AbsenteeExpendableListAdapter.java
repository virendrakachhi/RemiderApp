package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.Attendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class Reports_AbsenteeExpendableListAdapter extends BaseExpandableListAdapter {

    private TreeMap<String, List<Attendance>> classAttendanceMap = new TreeMap<>();
    private String[] groupKeys;

    private Activity context;
    private LayoutInflater inflater;

    public Reports_AbsenteeExpendableListAdapter(Activity context, TreeMap<String, List<Attendance>> classAttendanceMap) {
        this.classAttendanceMap = classAttendanceMap;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void update(TreeMap<String, List<Attendance>> newClassAttendanceMap) {
        classAttendanceMap.clear();
        classAttendanceMap.putAll(newClassAttendanceMap);
        groupKeys = classAttendanceMap.keySet().toArray(new String[classAttendanceMap.size()]);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return classAttendanceMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String key = groupKeys[groupPosition];
        boolean have = classAttendanceMap.containsKey(key);
        List<Attendance> list = classAttendanceMap.get(key);
        int size = list.size();
        return size;
    }

    @Override
    public String getGroup(int groupPosition) {
        return groupKeys[groupPosition];
    }

    @Override
    public Attendance getChild(int groupPosition, int childPosition) {
        return classAttendanceMap.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView;
        GroupViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.list_group_student, null, false);
            holder = new GroupViewHolder();

            holder.studentName = (TextView) view.findViewById(R.id.studentName);
            view.setTag(holder);
        } else {
            holder = (GroupViewHolder) view.getTag();
        }

        holder.position = groupPosition;
        holder.studentName.setText(groupKeys[groupPosition]);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        ItemViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_absent, null, false);
            holder = new ItemViewHolder();

            holder.deleteImageButton = (ImageButton) view.findViewById(R.id.deleteImageButton);
            holder.deleteImageButton.setVisibility(View.GONE);
            holder.dateTextView = (TextView) view.findViewById(R.id.dateTextView);
            holder.status = (TextView) view.findViewById(R.id.status);
            holder.status.setVisibility(View.VISIBLE);

            view.setTag(holder);
        } else {
            holder = (ItemViewHolder) view.getTag();
        }

        String key = groupKeys[groupPosition];
        List<Attendance> list = classAttendanceMap.get(key);
        int size = list.size();

        Attendance attendance = list.get(childPosition);

        holder.position = childPosition;

        String dateString = attendance.getCreateDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateString);
            format.applyPattern("MMMM dd, yyyy");
            holder.dateTextView.setText("" + format.format(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.status.setText(attendance.isPresent() ? "P" : "A");

//        holder.editStudentEmail.setTag(student);
//        holder.editStudentEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Student student1 = (Student) v.getTag();
//                Intent updateStudent = new Intent(context, TeacherInviteStudent.class);
//                updateStudent.putExtra(TeacherInviteStudent.EXTRA_CLASS_CODE, classEventCompany.getClassCode());
//                updateStudent.putExtra(TeacherInviteStudent.EXTRA_CLASS_ID, classEventCompany.getId());
//                updateStudent.putExtra(TeacherInviteStudent.EXTRA_STUDENT_ID, student1.getUserId());
//                updateStudent.putExtra(TeacherInviteStudent.EXTRA_STUDENT_EMAIL, student1.getEmail());
//                updateStudent.putExtra(TeacherInviteStudent.EXTRA_PARENT_EMAIL, student1.getParentEmail());
//                updateStudent.putExtra(AppConstants.EXTRA_IS_FIRST_TIME, false);
//                context.startActivityForResult(updateStudent, TeacherShowClassStudentsActivity.REQUEST_UPDATE_STUDENT);
//            }
//        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public class GroupViewHolder {
        TextView studentName;
        int position;
    }

    public class ItemViewHolder {
        TextView dateTextView, status;
        ImageButton deleteImageButton;
        int position;
    }

}