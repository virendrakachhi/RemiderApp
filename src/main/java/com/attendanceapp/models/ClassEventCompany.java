package com.attendanceapp.models;

import com.estimote.sdk.Beacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

public class ClassEventCompany implements Serializable {

    private String id, name, district, code, uniqueCode, createDate, modifiedDate, beaconsJsonString = "";
    private User maker = new User();
    private List<User> users = new ArrayList<>();
    private ArrayList<Beacon> beaconList = new ArrayList<>();
    private double latitude, longitude;
    private Integer status = 1, interval;
    private RepeatType repeatType;
    private TreeSet<Integer> repeatDates = new TreeSet<>();
    private TreeSet<SelectedDays> repeatDays = new TreeSet<>();
    private Calendar startTime, endTime, startDate, endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public User getMaker() {
        return maker;
    }

    public void setMaker(User maker) {
        this.maker = maker;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public ArrayList<Beacon> getBeaconList() {
        return beaconList;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public TreeSet<Integer> getRepeatDates() {
        return repeatDates;
    }

    public void setRepeatDates(TreeSet<Integer> repeatDates) {
        this.repeatDates = repeatDates;
    }

    public TreeSet<SelectedDays> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(TreeSet<SelectedDays> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public void setBeaconList(ArrayList<Beacon> beaconList) {
        this.beaconList = beaconList;
    }

    public String getBeaconsJsonString() {
        return beaconsJsonString;
    }

    public void setBeaconsJsonString(String beaconsJsonString) {
        this.beaconsJsonString = beaconsJsonString;
    }
}

