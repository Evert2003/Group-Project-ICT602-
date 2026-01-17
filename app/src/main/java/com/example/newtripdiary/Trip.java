package com.example.newtripdiary;

public class Trip {

    private String tripId;
    private String tripName;
    private String note;
    private String tripDate;

    // Default constructor required for Firebase
    public Trip() {
    }

    // Constructor to create a new Trip
    public Trip(String tripId, String tripName, String note, String tripDate) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.note = note;
        this.tripDate = tripDate;
    }

    // Getters and setters
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }
}
