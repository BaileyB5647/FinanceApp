package com.example.financeapp;

import java.time.LocalDate;

public class RecurringTransaction {
    private double amount;
    private String description;
    private Category category;
    private LocalDate startDate;
    private LocalDate endDate;
    private final Frequency frequency;
    private boolean isExpense;
    private int recurringID;

    public RecurringTransaction(double amount, String description, Category category,
                                LocalDate startDate, Frequency frequency, boolean isExpense) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.startDate = startDate;
        this.frequency = frequency;
        this.isExpense = isExpense;
    }

    public RecurringTransaction(double amount, String description, Category category,
                                LocalDate startDate, LocalDate endDate, Frequency frequency,
                                boolean isExpense) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
        this.isExpense = isExpense;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    public Integer getId(){
        return recurringID;
    }

    public void setId(int recurringID){
        this.recurringID = recurringID;
    }
}
