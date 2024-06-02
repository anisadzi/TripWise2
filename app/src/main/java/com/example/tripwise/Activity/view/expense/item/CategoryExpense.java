package com.example.tripwise.Activity.view.expense.item;

public class CategoryExpense implements java.io.Serializable {
    private String name;
    private Double amount;
    private int color;


    public CategoryExpense() {
        this.name = "";
        this.amount = 0.0;
        this.color = 0;
    }

    public CategoryExpense(String name, Double amount, int color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Double getAmount() {return amount;}

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getColor() {
        return color;
    }
}
