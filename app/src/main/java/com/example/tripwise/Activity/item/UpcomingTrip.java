package com.example.tripwise.Activity.item;

// Class to represent an upcoming trip with details like countdown, date range, nights and days, name, and location.
public class UpcomingTrip {
    // Fields to store trip details
    private String countdown; // Countdown to the trip
    private String dateRange; // Date range of the trip
    private String nightsDays; // Number of nights and days of the trip
    private String nameTrip; // Name of the trip
    private String locationTrip; // Location of the trip

    // Constructor to initialize the fields
    public UpcomingTrip(String countdown, String dateRange, String nightsDays, String nameTrip, String locationTrip) {
        this.countdown = countdown; // Set countdown
        this.dateRange = dateRange; // Set date range
        this.nightsDays = nightsDays; // Set nights and days
        this.nameTrip = nameTrip; // Set name of the trip
        this.locationTrip = locationTrip; // Set location of the trip
    }

    // Getter method for countdown
    public String getCountdown() {
        return countdown;
    }

    // Setter method for countdown
    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    // Getter method for date range
    public String getDateRange() {
        return dateRange;
    }

    // Setter method for date range
    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    // Getter method for nights and days
    public String getNightsDays() {
        return nightsDays;
    }

    // Setter method for nights and days
    public void setNightsDays(String nightsDays) {
        this.nightsDays = nightsDays;
    }

    // Getter method for name of the trip
    public String getNameTrip() {
        return nameTrip;
    }

    // Setter method for name of the trip
    public void setNameTrip(String nameTrip) {
        this.nameTrip = nameTrip;
    }

    // Getter method for location of the trip
    public String getLocationTrip() {
        return locationTrip;
    }

    // Setter method for location of the trip
    public void setLocationTrip(String locationTrip) {
        this.locationTrip = locationTrip;
    }
}
