package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Teacher extends User implements Serializable {

    private List<TeacherClass> teacherClassList = new ArrayList<>();

    public Teacher() {
    }

    public Teacher(User user) {
        super(user.getUserId(), user.getUsername(), user.getSchool(), user.getUserView(), user.getUniqueCode(), user.getCreateDate(), user.getEmail(), user.getDeviceToken(), user.getStatus(), user.getUserRoles());
    }

    public List<TeacherClass> getTeacherClassList() {
        return teacherClassList;
    }

    public void setTeacherClassList(List<TeacherClass> teacherClassList) {
        this.teacherClassList = teacherClassList;
    }

}
