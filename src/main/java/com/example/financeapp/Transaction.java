package com.example.financeapp;

import java.time.LocalDate;
import java.util.Date;

public class Transaction {
    private Category category;
    private LocalDate date;
    private String description;
    private Double amount;
    private Boolean isExpense;

    public Transaction(Category category, LocalDate date, String description,
                       Double amount, Boolean isExpense){

        this.category = category;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isExpense = isExpense;

    }

    public void setExpense(Boolean expense) {
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

    public Boolean isExpense() {
        return isExpense;
    }




}
