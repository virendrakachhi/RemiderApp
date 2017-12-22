package com.attendanceapp.models;

public class HCModuleDayWeekMonthYearViewBean {
    public String attendance,scheduleDetails,scheduleID, date,heading, clientID, employeeID,empName, managerID,day, week, month, year,startDate,endDate,startTime,endTime,user, schedule,locationID,locationName;

    public HCModuleDayWeekMonthYearViewBean() {}

    public HCModuleDayWeekMonthYearViewBean(String day,String week, String month,String yearString,String date,String user,String schedule) {
        this.day = day;
        this.week = week;
        this.month = month;
        this.year = year;
        this.user = user;
        this.schedule = schedule;

    }
}
