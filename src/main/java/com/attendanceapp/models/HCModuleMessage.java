package com.attendanceapp.models;

public class HCModuleMessage {
    public String senderName,senderID,receiverID,senderEmail, receiverName, messageTime, messageDate, message;

    public HCModuleMessage() {}

    public HCModuleMessage(String senderName,String receiverName,String messageDate,String message, String messageTime) {
        this.message = message;
        this.messageTime = messageTime;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.messageDate = messageDate;
    }
}
