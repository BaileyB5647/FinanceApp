package com.example.financeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class TransactionsDatabase {
    private static final String DB_URL = "jdbc:sqlite:transactions.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void init() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            String createTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category TEXT NOT NULL," +
                    "date TEXT NOT NULL," +
                    "description TEXT," +
                    "amount REAL NOT NULL," +
                    "is_expense INTEGER NOT NULL)";
            stmt.execute(createTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void clearDatabase(){
        String deleteSQL = "DELETE FROM transactions";
        String resetSQL = "DELETE FROM sqlite_sequence WHERE name = 'transactions'";


        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

                conn.setAutoCommit(false); // Optional but safer for batch operations

                stmt.executeUpdate(deleteSQL);
                stmt.executeUpdate(resetSQL);

                conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateDatabase(ObservableList<Transaction> transactions){
        clearDatabase();

        for (Transaction transaction : transactions){
            addTransaction(transaction);
        }
    }

    public static void addTransaction(Transaction transaction){
        String sql = "INSERT INTO transactions(category, date, description, amount, is_expense) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getCategory().toString());
            pstmt.setString(2, transaction.getDate().toString()); // LocalDate
            pstmt.setString(3, transaction.getDescription());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setInt(5, transaction.isExpense() ? 1 : 0);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ObservableList<Transaction> loadTransactions() {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();

        String sql = "SELECT * FROM transactions";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = Category.valueOf(rs.getString("category"));
                LocalDate date = LocalDate.parse(rs.getString("date"));
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                boolean isExpense = rs.getInt("is_expense") == 1;

                transactions.add(new Transaction(category, date, description, amount, isExpense));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

}

