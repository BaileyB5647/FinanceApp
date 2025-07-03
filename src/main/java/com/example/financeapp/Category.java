package com.example.financeapp;

public enum Category {
    // Income Categories:
    Salary("Salary"),
    Wages("Wages"),
    Bonuses("Bonuses"),
    Commission("Commission"),
    Freelance("Freelance"),
    Side_Gig("Side Gig"),
    Investments("Investments"),
    Dividends("Dividends"),
    Interest("Interest"),
    Rental_Income("Rental Income"),
    Gifts("Gifts"),
    Inheritance("Inheritance"),
    Tax_Refund("Tax Refund"),
    Government_Benefits("Government Benefits"),
    Other_Income("Other Income"),

    // Expense Categories
    Rent("Rent"),
    Mortgage_Payment("Mortgage Payment"),
    Property_Taxes("Property Taxes"),
    Insurance("Insurance"),
    Utilities("Utilities"),
    Internet("Internet"),
    Phone("Phone"),
    Maintenance_Repairs("Maintenance & Repairs"),
    Home_Improvement("Home Improvement"),
    Transport("Transport"),
    Groceries("Groceries"),
    Dining_Out("Dining Out"),
    Takeout("Takeout"),
    Health("Health"),
    Haircut("Haircut"),
    Cosmetics("Cosmetics"),
    Clothing("Clothing"),
    Streaming("Streaming"),
    Hobbies("Hobbies"),
    Holiday("Holiday/Vacation"),
    Tuition("Tuition"),
    Student_Loan_Payment("Student Loan Payment"),
    Subscription("Subscription"),
    Debt_Repayment("Debt Repayment"),
    Savings_Contribution("Savings Contribution"),
    Investment_Contribution("Investment Contribution"),
    Retirement_Contribution("Retirement Contribution"),
    Other_Expenses("Other Expenses");


    private final String displayName;

    Category(String displayName){
        this.displayName = displayName;
    }

    @Override public String toString() {
        return displayName;
    }

}
