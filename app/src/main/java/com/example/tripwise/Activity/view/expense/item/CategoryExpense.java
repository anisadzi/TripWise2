package com.example.tripwise.Activity.view.expense.item;

public class CategoryExpense implements java.io.Serializable {
    private String name;
    private Double amount;
    private int color;

    // Default constructor initializes fields with default values
    public CategoryExpense() {
        this.name = "";
        this.amount = 0.0;
        this.color = 0;
    }

    // Parameterized constructor sets values for name, amount, and color
    public CategoryExpense(String name, Double amount, int color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
    }

    // Getter for the category name
    public String getName() {
        return name;
    }

    // Getter for the category amount
    public Double getAmount() {
        return amount;
    }

    // Setter for the category amount
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    // Getter for the category color
    public int getColor() {
        return color;
    }
}
