package com.attendanceapp.models;

/**
 * Created by ritesh.local on 2/2/2016.
 */
public class EmployeeSubAttendance {

    String Status,Date;

    public EmployeeSubAttendance(String Status, String Date) {
        this.Status = Status;
        this.Date = Date;
    }


    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }


}
