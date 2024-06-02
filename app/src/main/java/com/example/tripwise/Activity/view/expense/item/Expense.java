package com.example.tripwise.Activity.view.expense.item;

import java.util.List;

public class Expense {

    private long date;
    private Double total;
    private List<CategoryExpense> categoryExpenses;

    public Expense(){}

    public Expense(long date, Double total, List<CategoryExpense> categoryExpenses) {
        this.date = date;
        this.total = total;
        this.categoryExpenses = categoryExpenses;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<CategoryExpense> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(List<CategoryExpense> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }

}
