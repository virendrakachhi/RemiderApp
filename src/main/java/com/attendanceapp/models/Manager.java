package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Manager extends User implements Serializable {

    private List<Company> companies = new ArrayList<>();
    private List<ManagerHCClass> managerLocationList = new ArrayList<>();

    public Manager(User user) {
        super(user.getUserId(),user.getUsername(),user.getSchool(),user.getUserView(),user.getUniqueCode(),user.getCreateDate(),user.getEmail(),user.getDeviceToken(),user.getStatus(),user.getUserRoles());
    }

    public List<ManagerHCClass> getManagerLocationList() {
        return managerLocationList;
    }

    public void setManagerLocationList(List<ManagerHCClass> managerLocationList) {
        this.managerLocationList = managerLocationList;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }
}
