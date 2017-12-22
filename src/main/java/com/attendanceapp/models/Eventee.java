package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Eventee extends User implements Serializable {

    private String parentEmail, studentClassId, modifiedDate, classCode;

    private List<Event> eventList = new ArrayList<>();


    public Eventee(User user) {
        super(user.getUserId(), user.getUsername(), user.getSchool(), user.getUserView(), user.getUniqueCode(), user.getCreateDate(), user.getEmail(), user.getDeviceToken(), user.getStatus(), user.getUserRoles());
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
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

}
