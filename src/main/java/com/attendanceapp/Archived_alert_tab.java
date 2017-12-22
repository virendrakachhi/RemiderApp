package com.attendanceapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class Archived_alert_tab extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_archived_tab);

        List<String> names = new ArrayList<String>();

        names.add("Thomas");
        names.add("Thomas");

        ListView listview = (ListView) this.findViewById(R.id.listView1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(), R.layout.lv_alert_archived,
                R.id.textView1, names);
        listview.setAdapter(adapter);
    }

}
