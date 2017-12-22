package com.attendanceapp.models;

/**
 * Created by canopus-pc on 26/5/16.
 */
public class ScheduleStatus {

    String status = "";
    String locationId = "";

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
