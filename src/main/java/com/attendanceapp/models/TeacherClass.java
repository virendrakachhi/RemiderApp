package com.attendanceapp.models;

import com.estimote.sdk.Beacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TeacherClass implements Serializable {

    private User user;
    private String id, teacherId, className, district, code, classCode, startTime, endTime, startDate, endDate, createDate, modifiedDate;
    private List<Student> studentList = new ArrayList<>();
    private transient ArrayList<Beacon> beaconList = new ArrayList<>();
    private Double latitude, longitude;
    private Integer status, interval;
    private RepeatType repeatType;
    private TreeSet<Integer> repeatDates = new TreeSet<>();
    private TreeSet<SelectedDays> repeatDays = new TreeSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public TreeSet<Integer> getRepeatDates() {
        return repeatDates;
    }

    public void setRepeatDates(TreeSet<Integer> repeatDates) {
        this.repeatDates = repeatDates;
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

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
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

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public TreeSet<SelectedDays> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(TreeSet<SelectedDays> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public ArrayList<Beacon> getBeaconList() {
        return beaconList;
    }

    public void setBeaconList(ArrayList<Beacon> beaconList) {
        this.beaconList = beaconList;
    }
}

