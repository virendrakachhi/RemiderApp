package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.attendanceapp.AppConstants;
import com.attendanceapp.R;
import com.attendanceapp.TeacherInviteStudent;
import com.attendanceapp.TeacherShowClassStudentsActivity;
import com.attendanceapp.activities.InviteUser;
import com.attendanceapp.activities.ShowClassEventCompanyUsersActivity;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;

import java.util.List;

public class UserExpandableListAdapter extends BaseExpandableListAdapter {
    private ClassEventCompany teacherClass;
    private List<User> studentList;
    private Activity context;
    private LayoutInflater inflater;

    public UserExpandableListAdapter(Activity context, ClassEventCompany teacherClass) {
        this.teacherClass = teacherClass;
        this.studentList = teacherClass.getUsers();
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return studentList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public User getGroup(int groupPosition) {
        return studentList.get(groupPosition);
    }

    @Override
    public User getChild(int groupPosition, int childPosition) {
        return studentList.get(childPosition);
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

        User student = studentList.get(groupPosition);

        holder.student = student;
        holder.position = groupPosition;
        holder.studentName.setText(student.getEmail().split("@")[0]);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        ItemViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_student, null, false);
            holder = new ItemViewHolder();

            holder.editStudentEmail = (ImageButton) view.findViewById(R.id.editStudentEmail);
            holder.studentEmail = (TextView) view.findViewById(R.id.studentEmail);
//            holder.parentEmail = (TextView) view.findViewById(R.id.parentEmail);
//            holder.parentEmailList = (ListView) view.findViewById(R.id.parentEmailList);
            holder.studentPanel = (LinearLayout) view.findViewById(R.id.studentPanel);
            holder.parentEmailLayout = (LinearLayout) view.findViewById(R.id.parentEmailLayout);

            view.setTag(holder);
        } else {
            holder = (ItemViewHolder) view.getTag();
        }

        User student = studentList.get(groupPosition);

        holder.student = student;
        holder.position = childPosition;
        holder.studentEmail.setText(student.getEmail());


        for (String s : student.getParentEmailList()) {
            TextView textView = new TextView(context);
            textView.setText(s);
            textView.setTextColor(context.getResources().getColor(R.color.dark_blue));
            textView.setPadding(5, 5, 5, 5);
            holder.parentEmailLayout.addView(textView);
        }

//        holder.parentEmail.setText(student.getParentEmail());
//        holder.parentEmailList.setAdapter(new ParentAdapter(context, android.R.layout.simple_list_item_1, student.getParentEmailList()));

        holder.editStudentEmail.setTag(student);
        holder.editStudentEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User student1 = (User) v.getTag();

                Bundle bundle = new Bundle();
                bundle.putInt(AppConstants.EXTRA_USER_ROLE, UserRole.Manager.getRole());
                bundle.putString(InviteUser.EXTRA_CLASS_CODE, teacherClass.getUniqueCode());
                bundle.putString(InviteUser.EXTRA_CLASS_ID, teacherClass.getId());
                bundle.putBoolean(InviteUser.EXTRA_IS_FIRST_TIME, false);
                bundle.putString(InviteUser.EXTRA_STUDENT_ID, student1.getUserId());
                bundle.putString(InviteUser.EXTRA_STUDENT_EMAIL, student1.getEmail());
//                bundle.putString(InviteUser.EXTRA_PARENT_EMAIL, student1.getParentEmail());
                bundle.putStringArrayList(InviteUser.EXTRA_PARENT_EMAIL, student1.getParentEmailList());

                Intent updateStudent = new Intent(context, InviteUser.class);
                updateStudent.putExtras(bundle);
                context.startActivityForResult(updateStudent, ShowClassEventCompanyUsersActivity.REQUEST_UPDATE_STUDENT);
            }
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public class GroupViewHolder {
        TextView studentName;
        User student;
        int position;
    }

    public class ItemViewHolder {
        //        TextView studentEmail, parentEmail;
        ImageButton editStudentEmail;
        TextView studentEmail;
        LinearLayout studentPanel, parentEmailLayout;
        ListView parentEmailList;

        User student;
        int position;
    }

    class ParentAdapter extends ArrayAdapter<String> {
        public ParentAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }
    }
}
