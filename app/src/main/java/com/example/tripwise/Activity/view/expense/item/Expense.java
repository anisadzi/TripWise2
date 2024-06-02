package com.example.tripwise.Activity.view.expense.item;

import java.util.List;

public class Expense {

    private long date;
    private Double total;
    private List<CategoryExpense> categoryExpenses;

    // Default constructor
    public Expense(){}

    // Parameterized constructor
    public Expense(long date, Double total, List<CategoryExpense> categoryExpenses) {
        this.date = date;
        this.total = total;
        this.categoryExpenses = categoryExpenses;
    }

    // Getter for the date of the expense
    public long getDate() {
        return date;
    }

    // Setter for the date of the expense
    public void setDate(long date) {
        this.date = date;
    }

    // Getter for the total amount of the expense
    public Double getTotal() {
        return total;
    }

    // Setter for the total amount of the expense
    public void setTotal(Double total) {
        this.total = total;
    }

    // Getter for the list of category expenses associated with the expense
    public List<CategoryExpense> getCategoryExpenses() {
        return categoryExpenses;
    }

    // Setter for the list of category expenses associated with the expense
    public void setCategoryExpenses(List<CategoryExpense> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }

}
