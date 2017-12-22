package com.attendanceapp.models;

import com.estimote.sdk.Beacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by VICKY KUMAR on 17-01-2016.
 */
public class ManagerHCClass implements Serializable {
    private User user;
    private String id;
    private String managerId;
    private String locationName;
    private String companyName;
    private String distance;
    private String companyCode;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    private String startTime;
    private String endTime;
    private String createDate;
    private String modifiedDate;
    private List<HCEmployee> employeeList = new ArrayList<>();
    private List<HCFamily> familyList = new ArrayList<>();
    private transient ArrayList<Beacon> beaconList = new ArrayList<>();
    private String latitude, longitude;
    private List<User> users = new ArrayList<>();
    public User getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public List<HCEmployee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<HCEmployee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<HCFamily> getFamilyList() {
        return familyList;
    }

    public void setFamilyList(List<HCFamily> familyList) {
        this.familyList = familyList;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
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

    public void setBeaconList(ArrayList<Beacon> beaconList) {
        this.beaconList = beaconList;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
