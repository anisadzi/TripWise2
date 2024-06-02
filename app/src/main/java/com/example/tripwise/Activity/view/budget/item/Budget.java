package com.example.tripwise.Activity.view.budget.item;

import java.util.List;

public class Budget {
    private double budgetAmount;
    private String tripName;
    private long startDate;
    private long endDate;
    private String tripLocation;
    private List<Category> categoryList;

    public Budget() {
    }

    public Budget(double budgetAmount, String tripName, long startDate, long endDate, String tripLocation, List<Category> categoryList) {
        this.budgetAmount = budgetAmount;
        this.tripName = tripName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tripLocation = tripLocation;
        this.categoryList = categoryList;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public void setTripLocation(String tripLocation) {
        this.tripLocation = tripLocation;
    }
    public String getTripLocation() {
        return tripLocation;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }
}
