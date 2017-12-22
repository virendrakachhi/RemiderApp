package com.attendanceapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.attendanceapp.utils.UserUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by VICKY KUMAR on 03-03-2016.
 */
public class WifiActivity extends Activity {
    WifiManager wifi;
    int size = 0;
    List<ScanResult> results;
    ArrayAdapter adapter;
    WifiScanReceiver wifiReciever;
    ListView lv;
    String ITEM_KEY = "key";
    String wifis[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_module_manager_check_list);
        lv = (ListView)findViewById(R.id.dynamic_checklist);
        TextView title = (TextView)findViewById(R.id.txtTitle);
        title.setText("Choose a Wifi");
        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifi.startScan();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",wifis[i]);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
    public void gotoBack(View view) {
        onBackPressed();
    }

    class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
/* int numberOfLevels = 5;
WifiInfo wifiInfo = wifi.getConnectionInfo();
int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
String str=String.valueOf(level);
Log.e("Test->>", str);*/

            List<ScanResult> wifiScanList=new ArrayList<ScanResult>();
            wifiScanList = wifi.getScanResults();
//            SharedPreferences prefs = getSharedPreferences("User", Context.MODE_PRIVATE);
//save the user list to preference
//            SharedPreferences.Editor sEdit=prefs.edit();


            wifis = new String[wifiScanList.size()];

            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = ((wifiScanList.get(i).SSID));
                Log.d("scanResult", "Strength of " + wifiScanList.get(i).toString() + "Wifi is:-" + wifiScanList.get(i).level);
//                sEdit.putString("val" + i, wifiScanList.get(i).toString());
            }
//            sEdit.putInt("size",wifiScanList.size());
//            sEdit.commit();
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,wifis){ @Override
            public View getView(int position, View convertView,
                    ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.BLACK);

                return view;
            }});


        }
    }





}
