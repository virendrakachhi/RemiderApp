package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VICKY KUMAR on 20-01-2016.
 */

public class HCEmployee implements Serializable {
    public String getProfilePicName() {
        return profilePicName;
    }

    public String getCheckListName() {
        return checkListName;
    }

    public void setCheckListName(String checkListName) {
        this.checkListName = checkListName;
    }

    public void setProfilePicName(String profilePicName) {
        this.profilePicName = profilePicName;
    }

    private String id;
    private String name;
    private String managerID;
    private String profilePicName;
    private String checkListId;
    private String checkListName;
    private String email;
    private String phone;
    private String locationName;
    private String locationId;
    private String locationSetup;
    private String locationType;
    private String employeeId;
    private String companyName;
    private String companyCode;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;
    private String longitude;
    private String latitude;
    private String checklist_id;
    private String distance;
    private List<HCEmployee> employeeList = new ArrayList<>();
    private List<HCFamily> familyList = new ArrayList<>();
    private List<HCBeaconEmployee> employeeBeaconList = new ArrayList<>();


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<HCBeaconEmployee> getEmployeeBeaconList() {
        return employeeBeaconList;
    }

    public void setEmployeeBeaconList(List<HCBeaconEmployee> employeeBeaconList) {
        this.employeeBeaconList = employeeBeaconList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCheckListId() {
        return checkListId;
    }

    public void setCheckListId(String checkListId) {
        this.checkListId = checkListId;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getLocationSetup() {
        return locationSetup;
    }

    public void setLocationSetup(String locationSetup) {
        this.locationSetup = locationSetup;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

 public String getChecklist_id() {
        return checklist_id;
    }

    public void setChecklist_id(String checklist_id) {
        this.checklist_id = checklist_id;
    }



    public List<HCFamily> getFamilyList() {
        return familyList;
    }

    public void setFamilyList(List<HCFamily> familyList) {
        this.familyList = familyList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public List<HCEmployee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<HCEmployee> employeeList) {
        this.employeeList = employeeList;
    }


    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }
}
