package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Student extends User implements Serializable {

    private String childIdForLocation, allClassesId, studentClassId, modifiedDate, classCode;

    private List<StudentClass> studentClassList = new ArrayList<>();

    public Student() {
    }

    public Student(User user) {
        super(user.getUserId(), user.getUsername(), user.getSchool(), user.getUserView(), user.getUniqueCode(), user.getCreateDate(), user.getEmail(), user.getDeviceToken(), user.getStatus(), user.getUserRoles());
    }


    public List<StudentClass> getStudentClassList() {
        return studentClassList;
    }

    public void setStudentClassList(List<StudentClass> studentClassList) {
        this.studentClassList = studentClassList;
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

    public String getAllClassesId() {
        return allClassesId;
    }

    public void setAllClassesId(String allClassesId) {
        this.allClassesId = allClassesId;
    }
}


