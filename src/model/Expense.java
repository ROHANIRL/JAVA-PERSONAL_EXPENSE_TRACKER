package model;

import java.time.LocalDate;

public class Expense {
    private int expenseId;
    private int categoryId;
    private String categoryName;
    private double amount;
    private String description;
    private LocalDate date;

    public Expense() {
    }

    public Expense(int categoryId, double amount, String description, LocalDate date) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-12s Rs.%-10.2f %-20s %s",
                expenseId, categoryName, amount, description, date);
    }
}
