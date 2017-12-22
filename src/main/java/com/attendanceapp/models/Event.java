package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event extends ClassEventCompany implements Serializable {

    private List<Eventee> eventeeList = new ArrayList<>();

    public List<Eventee> getEventeeList() {
        return eventeeList;
    }

    public void setEventeeList(List<Eventee> eventeeList) {
        this.eventeeList = eventeeList;
    }
}
