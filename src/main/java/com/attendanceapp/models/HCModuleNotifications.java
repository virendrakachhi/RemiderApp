package com.attendanceapp.models;

public class HCModuleNotifications {
    public String senderName, receiverName,senderID, messageTime, messageDate, message,notificationID, messageLeft, locationID, archived;

    public HCModuleNotifications() {}

    public HCModuleNotifications(String senderName, String receiverName, String messageDate, String message, String messageTime
            , String messageLeft, String locationID, String archived) {
        this.message = message;
        this.messageLeft = messageLeft;
        this.messageTime = messageTime;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.messageDate = messageDate;
        this.locationID = locationID;
        this.archived = archived;
    }
}
