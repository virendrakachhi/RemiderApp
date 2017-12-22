package com.attendanceapp.models;

import java.io.Serializable;

/**
 * Created by canopus-pc on 31/3/16.
 */
public class HCBeaconEmployee implements Serializable{

    String beaconName = "";
    String beaconMinor = "";
    String beaconMajor = "";

    public String getBeaconName() {
        return beaconName;
    }

    public void setBeaconName(String beaconName) {
        this.beaconName = beaconName;
    }

    public String getBeaconMinor() {
        return beaconMinor;
    }

    public void setBeaconMinor(String beaconMinor) {
        this.beaconMinor = beaconMinor;
    }

    public String getBeaconMajor() {
        return beaconMajor;
    }

    public void setBeaconMajor(String beaconMajor) {
        this.beaconMajor = beaconMajor;
    }

}
