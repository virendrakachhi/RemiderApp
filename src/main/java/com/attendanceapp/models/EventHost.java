package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventHost extends User implements Serializable {

    List<Event> eventList = new ArrayList<>();

    public EventHost() {
    }

    public EventHost(User u) {
        super(u.getUserId(), u.getUsername(), u.getSchool(), u.getUserView(), u.getUniqueCode(),
                u.getCreateDate(), u.getEmail(), u.getDeviceToken(), u.getStatus(), u.getUserRoles());
    }

    public List<Event> getTeacherClassList() {
        return eventList;
    }

}
