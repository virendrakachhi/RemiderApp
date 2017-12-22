package com.attendanceapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.attendanceapp.models.Student;

import java.util.List;

public class ParentPagerAdapter  extends FragmentPagerAdapter {

    List<Student> students;

    public ParentPagerAdapter(FragmentManager supportFragmentManager, List<Student> students) {
        super(supportFragmentManager);
        this.students = students;
    }

    @Override
    public Fragment getItem(int position) {
        return new DummySectionFragment();
    }

    @Override
    public int getCount() {
        return students.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String className = " ";
        className = students.get(position).getUsername();
        if (className.length() == 1) {
            className = className.toUpperCase();
        } else {
            className = (className.substring(0, 1).toUpperCase()) + className.substring(1);
        }
        return className;
    }

    public static class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            return inflater.inflate(android.R.layout.test_list_item,container, false);
        }
    }
}
