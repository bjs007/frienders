package com.frienders.main.db.model;

public class GroupMessage
{
    private String from, message, type, messageId, time, date, fileName, groupId, senderDisplayName;
    private Long likes;

    public GroupMessage()
    {

    }

    public GroupMessage(String from, String senderDisplayName, String message, String type, String messageId, String time, String date, String fileName, String groupId) {
        this.from = from;
        this.senderDisplayName = senderDisplayName;
        this.message = message;
        this.type = type;
        this.messageId = messageId;
        this.time = time;
        this.date = date;
        this.fileName = fileName;
        this.groupId = groupId;
    }

    public GroupMessage(String messageId, String time, String date, String fileName) {
        this.messageId = messageId;
        this.time = time;
        this.date = date;
        this.fileName = fileName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }
}
