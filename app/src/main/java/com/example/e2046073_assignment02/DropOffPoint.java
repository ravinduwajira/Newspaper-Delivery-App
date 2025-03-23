package com.example.e2046073_assignment02;
import java.util.List;

public class DropOffPoint {
    private final double latitude;
    private final double longitude;
    private final List<String> paperTypes;
    private String deliveryStatus;
    private String deliveryTimestamp;
    private boolean ShouldRenewalReminderIssued;
    private boolean isRenewalReminderIssued;
    private String note;
    private final String subscriberId;
    private final String name;
    private final String address;
    private final String phone;
    private final List<String> deliveryHistory;

    public DropOffPoint(double latitude, double longitude, List<String> paperTypes, String deliveryStatus, String deliveryTimestamp, boolean ShouldRenewalReminderIssued, boolean isRenewalReminderIssued, String note, String subscriberId, String name, String address, String phone, List<String> deliveryHistory) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.paperTypes = paperTypes;
        this.deliveryStatus = deliveryStatus;
        this.deliveryTimestamp = deliveryTimestamp;
        this.ShouldRenewalReminderIssued= ShouldRenewalReminderIssued;
        this.isRenewalReminderIssued =isRenewalReminderIssued;
        this.note = note;
        this.subscriberId = subscriberId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.deliveryHistory = deliveryHistory;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getPaperTypes() {
        return paperTypes;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveryTimestamp() {
        return deliveryTimestamp;
    }

    public void setDeliveryTimestamp(String deliveryTimestamp) {
        this.deliveryTimestamp = deliveryTimestamp;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getDeliveryHistory() {
        return deliveryHistory;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    public boolean getShouldRenewalReminderIssued() {
        return ShouldRenewalReminderIssued;
    }

    public boolean isRenewalReminderIssued() {
        return isRenewalReminderIssued;
    }
    public void setShouldRenewalReminderIssued(boolean shouldRenewalReminderIssued) {
        ShouldRenewalReminderIssued = shouldRenewalReminderIssued;
    }

    public void getRenewalReminderIssued(boolean renewalReminderIssued) {
        isRenewalReminderIssued = renewalReminderIssued;
    }
}

