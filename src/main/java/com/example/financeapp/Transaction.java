package com.example.financeapp;

import java.time.LocalDate;

public class Transaction {
    private Category category;
    private LocalDate date;
    private String description;
    private Double amount;
    private boolean isExpense;
    private Integer recurringId; // nullable

    public Transaction(Category category, LocalDate date, String description,
                       Double amount, boolean isExpense){

        this.category = category;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isExpense = isExpense;

    }

    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCategory(Category category){
        this.category = category;
    }


    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public Integer getRecurringId(){
        return recurringId;
    }

    public void setRecurringId(int id){
        recurringId = id;
    }


}
