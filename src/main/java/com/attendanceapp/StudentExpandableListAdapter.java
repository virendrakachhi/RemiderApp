package com.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.attendanceapp.models.Student;
import com.attendanceapp.models.TeacherClass;

import java.util.List;

public class StudentExpandableListAdapter extends BaseExpandableListAdapter {
    private TeacherClass teacherClass;
    private List<Student> studentList;
    private Activity context;
    private LayoutInflater inflater;

    public StudentExpandableListAdapter(Activity context, TeacherClass teacherClass) {
        this.teacherClass = teacherClass;
        this.studentList = teacherClass.getStudentList();
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
    public Student getGroup(int groupPosition) {
        return studentList.get(groupPosition);
    }

    @Override
    public Student getChild(int groupPosition, int childPosition) {
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

        Student student = studentList.get(groupPosition);

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

        Student student = studentList.get(groupPosition);

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
//        holder.parentEmailList.setAdapter(new ParentAdapter(context, student.getParentEmailList()));

        holder.editStudentEmail.setTag(student);
        holder.editStudentEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Student student1 = (Student) v.getTag();
                Intent updateStudent = new Intent(context, TeacherInviteStudent.class);
                updateStudent.putExtra(TeacherInviteStudent.EXTRA_CLASS_CODE, teacherClass.getClassCode());
                updateStudent.putExtra(TeacherInviteStudent.EXTRA_CLASS_ID, teacherClass.getId());
                updateStudent.putExtra(TeacherInviteStudent.EXTRA_STUDENT_ID, student1.getUserId());
                updateStudent.putExtra(TeacherInviteStudent.EXTRA_STUDENT_EMAIL, student1.getEmail());
                updateStudent.putStringArrayListExtra(TeacherInviteStudent.EXTRA_PARENT_EMAIL, student1.getParentEmailList());
                updateStudent.putExtra(TeacherInviteStudent.EXTRA_IS_FIRST_TIME, false);
                context.startActivityForResult(updateStudent, TeacherShowClassStudentsActivity.REQUEST_UPDATE_STUDENT);
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
        Student student;
        int position;
    }

    public class ItemViewHolder {
        //        TextView parentEmail;
        TextView studentEmail;
        ImageButton editStudentEmail;
        LinearLayout studentPanel, parentEmailLayout;
        //        ListView parentEmailList;
        Student student;
        int position;
    }


    class ParentAdapter extends ArrayAdapter<String> {
        private Context context;
        private List<String> objects;

        public ParentAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = new TextView(context);
            view.setText(objects.get(position));
            view.setTextColor(context.getResources().getColor(R.color.dark_blue));
            view.setPadding(5, 5, 5, 5);

            return view;
        }
    }
}
