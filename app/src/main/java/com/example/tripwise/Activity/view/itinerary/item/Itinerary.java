package com.example.tripwise.Activity.view.itinerary.item;

public class Itinerary {
    private String name;
    private String location;
    private long startTime;
    private long endTime;
    private String date;
    private long dateMilis;

    // Empty constructor required for Firebase
    public Itinerary() {}

    // Constructor with parameters to initialize the Itinerary object
    public Itinerary(String name, String location, long startTime, long endTime, String date, long dateMilis) {
        this.name = name;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.dateMilis = dateMilis;
    }

    // Getter and setter methods for the name property
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter methods for the location property
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and setter methods for the startTime property
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    // Getter and setter methods for the endTime property
    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    // Getter and setter methods for the date property
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Getter and setter methods for the dateMilis property
    public long getDateMilis() {
        return dateMilis;
    }

    public void setDateMilis(long dateMilis) {
        this.dateMilis = dateMilis;
    }
}
