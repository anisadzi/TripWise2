package com.example.tripwise.Activity.item;

public class UpcomingTrip {
    private String countdown;
    private String dateRange;
    private String nightsDays;
    private String nameTrip;
    private String locationTrip;


    public UpcomingTrip(String countdown, String dateRange, String nightsDays, String nameTrip, String locationTrip) {
        this.countdown = countdown;
        this.dateRange = dateRange;
        this.nightsDays = nightsDays;
        this.nameTrip = nameTrip;
        this.locationTrip = locationTrip;

    }

    public String getCountdown() {
        return countdown;
    }

    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getNightsDays() {
        return nightsDays;
    }

    public void setNightsDays(String nightsDays) {
        this.nightsDays = nightsDays;
    }

    public String getNameTrip() {
        return nameTrip;
    }

    public void setNameTrip(String nameTrip) {
        this.nameTrip = nameTrip;
    }

    public String getLocationTrip() {
        return locationTrip;
    }

    public void setLocationTrip(String locationTrip) {
        this.locationTrip = locationTrip;
    }

}

