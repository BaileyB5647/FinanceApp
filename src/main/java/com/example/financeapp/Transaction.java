package com.example.financeapp;

import java.util.Date;
import java.util.Locale;

public class Transaction {
    private final Category category;
    private final Date date;
    private final String description;
    private final Double amount;
    private final Boolean isExpense;

    public Transaction(Category category, Date date, String description,
                       Double amount, Boolean isExpense){

        this.category = category;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isExpense = isExpense;

    }

    public Category getCategory() {
        return category;
    }

    public Date getDate() {
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
