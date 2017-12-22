package com.attendanceapp.models;

import java.io.Serializable;

public class StudentClass implements Serializable {
    private String studentClassId, teacherId, className, classUniqueCode;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getClassUniqueCode() {
        return classUniqueCode;
    }

    public void setClassUniqueCode(String classUniqueCode) {
        this.classUniqueCode = classUniqueCode;
    }
}

