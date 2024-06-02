package com.example.tripwise.Activity.view.budget.item;

public class Category implements java.io.Serializable {
    private String name;
    private int color;

    // Default constructor initializes name as empty string and color as 0
    public Category() {
        this.name = "";
        this.color = 0;
    }

    // Constructor to initialize Category object with provided name and color
    public Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    // Getter method for retrieving the category name
    public String getName() {
        return name;
    }

    // Getter method for retrieving the color associated with the category
    public int getColor() {
        return color;
    }
}
