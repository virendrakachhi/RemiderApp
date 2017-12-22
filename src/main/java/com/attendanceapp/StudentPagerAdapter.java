package com.attendanceapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.attendanceapp.models.StudentClass;

import java.util.List;

/**
 * Created by Hemant Sharma on 6/3/2015.
 */
public class StudentPagerAdapter extends FragmentPagerAdapter {

    List<StudentClass> studentClassList;

    public StudentPagerAdapter(FragmentManager supportFragmentManager, List<StudentClass> studentClassList) {
        super(supportFragmentManager);
        this.studentClassList = studentClassList;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new StudentClassViewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(StudentClassViewFragment.EXTRA_STUDENT_CLASS, studentClassList.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return studentClassList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String className = " ";
        className = studentClassList.get(position).getClassName();
        if (className.length() == 1) {
            className = className.toUpperCase();
        } else {
            className = (className.substring(0, 1).toUpperCase()) + className.substring(1);
        }
        return className;
    }
}
