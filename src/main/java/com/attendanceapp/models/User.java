package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String userId, username, school, userView, uniqueCode, createDate;
    //    private String imageName, email, parentEmail, deviceToken, securityQuestion, securityQuestionAnswer;
    private String imageName, email, deviceToken, securityQuestion, securityQuestionAnswer;
    private Integer status;
    private List<UserRole> userRoles = new ArrayList<>();
    private ArrayList<ClassEventCompany> classEventCompanyArrayList = new ArrayList<>();
    private ArrayList<Attendance> attendances = new ArrayList<>();
    private ArrayList<String> parentEmailList = new ArrayList<>();


    public User() {
    }

    public User(String userId, String username, String school, String userView, String uniqueCode,
                String createDate, String email, String deviceToken, Integer status, List<UserRole> userRoles) {

        this.userId = userId;
        this.username = username;
        this.school = school;
        this.userView = userView;
        this.uniqueCode = uniqueCode;
        this.createDate = createDate;
        this.email = email;
        this.deviceToken = deviceToken;
        this.status = status;
        this.userRoles = userRoles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getUserView() {
        return userView;
    }

    public void setUserView(String userView) {
        this.userView = userView;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityQuestionAnswer() {
        return securityQuestionAnswer;
    }

    public void setSecurityQuestionAnswer(String securityQuestionAnswer) {
        this.securityQuestionAnswer = securityQuestionAnswer;
    }

    public ArrayList<ClassEventCompany> getClassEventCompanyArrayList() {
        return classEventCompanyArrayList;
    }

    public ArrayList<Attendance> getAttendances() {
        return attendances;
    }

//    public String getParentEmail() {
//        return parentEmail;
//    }

//    public void setParentEmail(String parentEmail) {
//        this.parentEmail = parentEmail;
//    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public ArrayList<String> getParentEmailList() {
        return parentEmailList;
    }
}
