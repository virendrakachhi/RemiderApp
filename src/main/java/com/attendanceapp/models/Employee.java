package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee extends User implements Serializable {

    private String parentEmail, childIdForLocation, studentClassId, modifiedDate, classCode, checkList_id;
    private List<HCEmployee> managerLocationList = new ArrayList<>();
    private List<HCBeaconEmployee> managerBeaconList = new ArrayList<>();

    public List<HCBeaconEmployee> getManagerBeaconList() {
        return managerBeaconList;
    }

    public void setManagerBeaconList(List<HCBeaconEmployee> managerBeaconList) {
        this.managerBeaconList = managerBeaconList;
    }


    public List<HCEmployee> getManagerLocationList() {
        return managerLocationList;
    }

    public void setManagerLocationList(List<HCEmployee> managerLocationList) {
        this.managerLocationList = managerLocationList;
    }

    private List<StudentClass> studentClassList = new ArrayList<>();


    public Employee(User user) {
        super(user.getUserId(),user.getUsername(),user.getSchool(),user.getUserView(),user.getUniqueCode(),user.getCreateDate(),user.getEmail(),user.getDeviceToken(),user.getStatus(),user.getUserRoles());
    }

    public List<StudentClass> getStudentClassList() {
        return studentClassList;
    }

    public void setStudentClassList(List<StudentClass> studentClassList) {
        this.studentClassList = studentClassList;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public String getChildIdForLocation() {
        return childIdForLocation;
    }

    public void setChildIdForLocation(String childIdForLocation) {
        this.childIdForLocation = childIdForLocation;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getCheckList_id() {
        return checkList_id;
    }

    public void setCheckList_id(String checkList_id) {
        this.checkList_id = classCode;
    }
}


