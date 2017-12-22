package com.attendanceapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VICKY KUMAR on 17-01-2016.
 */
public class Family extends User implements Serializable {
User familyType;
    private List<HCFamily> managerLocationList = new ArrayList<>();

    public List<HCFamily> getManagerLocationList() {
        return managerLocationList;
    }

    public void setManagerLocationList(List<HCFamily> managerLocationList) {
        this.managerLocationList = managerLocationList;
    }

    public Family(User familyType)
    {
        this.familyType = familyType;
    }
}
