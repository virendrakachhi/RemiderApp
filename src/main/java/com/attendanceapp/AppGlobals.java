package com.attendanceapp;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.support.multidex.MultiDex;

import com.attendanceapp.models.Employee;
import com.attendanceapp.models.EventHost;
import com.attendanceapp.models.Eventee;
import com.attendanceapp.models.Family;
import com.attendanceapp.models.Manager;
import com.attendanceapp.models.Parent;
import com.attendanceapp.models.Student;
import com.attendanceapp.models.Teacher;
import com.attendanceapp.models.User;
import com.estimote.sdk.EstimoteSDK;

public class AppGlobals extends Application {
    private static Context context;

    public static Location mCurrentLocation;
    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        EstimoteSDK.initialize(this, "app_1e2ims1641", "2ec3ee5dce97647cdd50d19a7fbe4561");
        // Configure verbose debug logging.
        EstimoteSDK.enableDebugLogging(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public static Context getContext() {
        return context;
    }


    private User user;
    private Teacher teacher;
    private Student student;
    private Parent parent;

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    private Family family;
    private Manager manager;
    private Employee employee;
    private EventHost eventHost;
    private Eventee eventee;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public EventHost getEventHost() {
        return eventHost;
    }

    public void setEventHost(EventHost eventHost) {
        this.eventHost = eventHost;
    }

    public Eventee getEventee() {
        return eventee;
    }

    public void setEventee(Eventee eventee) {
        this.eventee = eventee;
    }
}
