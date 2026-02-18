package com.HerbertSantos.TH_brain.domain.model;

public class User {
    private String from;
    private String text;
    private Long timeStamp;
    private String name;
    private String menssageId;
    private boolean isgroup;
    private MessageType messageType;
    private String deviceType;

    public User(String from, String text, Long timeStamp, String name, String menssageId, boolean isgroup, MessageType messageType, String deviceType) {
        this.from = from;
        this.text = text;
        this.timeStamp = timeStamp;
        this.name = name;
        this.menssageId = menssageId;
        this.isgroup = isgroup;
        this.messageType = messageType;
        this.deviceType = deviceType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMenssageId() {
        return menssageId;
    }

    public void setMenssageId(String menssageId) {
        this.menssageId = menssageId;
    }

    public boolean isIsgroup() {
        return isgroup;
    }

    public void setIsgroup(boolean isgroup) {
        this.isgroup = isgroup;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
