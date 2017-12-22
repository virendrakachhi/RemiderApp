package com.attendanceapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.attendanceapp.R;
import com.attendanceapp.models.ClassEventCompany;
import com.attendanceapp.models.ManagerHCClass;

import java.util.List;

public class HCViewPagerAdapter extends FragmentPagerAdapter {

    List<? extends ManagerHCClass> classEventCompanyList;

    public HCViewPagerAdapter(FragmentManager supportFragmentManager, List<? extends ManagerHCClass> classEventCompanyList) {
        super(supportFragmentManager);
        this.classEventCompanyList = classEventCompanyList;
    }

    @Override
    public Fragment getItem(int position) {
        return new DummySectionFragment();
    }

    @Override
    public int getCount() {
        return classEventCompanyList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String className = " ";
        try {
            className = classEventCompanyList.get(position).getLocationName();
            if (className.length() == 1) {
                className = className.toUpperCase();
            } else {
                className = (className.substring(0, 1).toUpperCase()) + className.substring(1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return className;
    }

    public static class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_events,container, false);
        }
    }
}
