package com.attendanceapp.models;

/**
 * Created by ritesh.local on 2/5/2016.
 */
public class RKSelectEmployeeBean {
    String mName,mEmail,empID,punctuality;
    Boolean isChecked = false;

    public String getEmpID() {
        return empID;
    }

    public void setEmpID(String empID) {
        this.empID = empID;
    }

    public String getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(String punctuality) {
        this.punctuality = punctuality;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }
public RKSelectEmployeeBean()
{

}
    public RKSelectEmployeeBean(String mName, String mEmail, Boolean isChecked) {
        this.mName = mName;
        this.mEmail = mEmail;
        this.isChecked = isChecked;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }


}
