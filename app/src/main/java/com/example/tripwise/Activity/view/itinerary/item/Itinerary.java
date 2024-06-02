package com.example.tripwise.Activity.view.itinerary.item;

public class Itinerary {
    private String name;
    private String location;
    private long startTime;
    private long endTime;
    private String date;
    private long dateMilis;

    public Itinerary() {}
    public Itinerary(String name, String location, long startTime, long endTime, String date, long dateMilis) {
        this.name = name;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.dateMilis = dateMilis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public long getDateMilis() {
        return dateMilis;
    }
    public void setDateMilis(long dateMilis) {
        this.dateMilis = dateMilis;
    }

}
