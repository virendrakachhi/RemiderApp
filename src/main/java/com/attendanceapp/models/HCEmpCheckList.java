package com.attendanceapp.models;

public class HCEmpCheckList {
    public String
            id;
    public String location_id;
    public String emp_id;
    public String location_checklists;

    public String valueHeading;
    public String valueYesNo;
    public String valueNa;
    public String valueComment;
    public String valueTimeRange;

    public String getCmntChanged() {
        return cmntChanged;
    }

    public void setCmntChanged(String cmntChanged) {
        this.cmntChanged = cmntChanged;
    }

    private String cmntChanged = "no";

    public Boolean

            showYesNo,
            showNa,
            showComment,
            showTimeRange;

    public HCEmpCheckList() {
    }

    public HCEmpCheckList(String id,
                          String location_id,
                          String emp_id,
                          String location_checklists,

                          Boolean showYesNo,
                          Boolean showNa,
                          Boolean showComment,
                          Boolean showTimeRange,

                          String valueHeading,
                          String valueYesNo,
                          String valueNa,
                          String valueComment,
                          String valueTimeRange) {

        this.id = id;
        this.location_id = location_id;
        this.emp_id = emp_id;
        this.location_checklists = location_checklists;

        this.showYesNo = showYesNo;
        this.showNa = showNa;
        this.showComment = showComment;
        this.showTimeRange = showTimeRange;

        this.valueHeading = valueHeading;
        this.valueYesNo = valueYesNo;
        this.valueNa = valueNa;
        this.valueComment = valueComment;
        this.valueTimeRange = valueTimeRange;
    }

    public String getSelectedRadio() {
        return selectedRadio;
    }

    public void setSelectedRadio(String selectedRadio) {
        this.selectedRadio = selectedRadio;
    }

    public String selectedRadio;

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    public String getLocation_checklists() {
        return location_checklists;
    }

    public void setLocation_checklists(String location_checklists) {
        this.location_checklists = location_checklists;
    }

    public String getValueHeading() {
        return valueHeading;
    }

    public void setValueHeading(String valueHeading) {
        this.valueHeading = valueHeading;
    }

    public String getValueYesNo() {
        return valueYesNo;
    }

    public void setValueYesNo(String valueYesNo) {
        this.valueYesNo = valueYesNo;
    }

    public String getValueNa() {
        return valueNa;
    }

    public void setValueNa(String valueNa) {
        this.valueNa = valueNa;
    }

    public String getValueComment() {
        return valueComment;
    }

    public void setValueComment(String valueComment) {
        this.valueComment = valueComment;
    }

    public String getValueTimeRange() {
        return valueTimeRange;
    }

    public void setValueTimeRange(String valueTimeRange) {
        this.valueTimeRange = valueTimeRange;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getShowYesNo() {
        return showYesNo;
    }

    public void setShowYesNo(Boolean showYesNo) {
        this.showYesNo = showYesNo;
    }

    public Boolean getShowNa() {
        return showNa;
    }

    public void setShowNa(Boolean showNa) {
        this.showNa = showNa;
    }

    public Boolean getShowComment() {
        return showComment;
    }

    public void setShowComment(Boolean showComment) {
        this.showComment = showComment;
    }

    public Boolean getShowTimeRange() {
        return showTimeRange;
    }

    public void setShowTimeRange(Boolean showTimeRange) {
        this.showTimeRange = showTimeRange;
    }


}