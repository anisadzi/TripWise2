package com.example.tripwise.Activity.view.budget.item;

public class Category implements java.io.Serializable {
    private String name;
    private int color;


    public Category() {

        this.name = "";
        this.color = 0;
    }

    public Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
}
