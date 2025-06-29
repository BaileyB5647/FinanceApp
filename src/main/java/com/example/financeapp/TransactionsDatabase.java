package com.example.financeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionsDatabase {
    private static final String DB_URL = "jdbc:sqlite:transactions.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void init() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category TEXT NOT NULL," +
                    "date TEXT NOT NULL," +
                    "description TEXT," +
                    "amount REAL NOT NULL," +
                    "is_expense INTEGER NOT NULL," +
                    "recurring_id INTEGER)";
            stmt.execute(createTransactionsTable);

            String createRecurringTable = "CREATE TABLE IF NOT EXISTS recurring_transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL," +
                    "description TEXT," +
                    "category TEXT NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "end_date TEXT," +
                    "frequency TEXT NOT NULL," +
                    "is_expense INTEGER NOT NULL)";
            stmt.execute(createRecurringTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void clearTransactionsDatabase() {
        String deleteTransactions = "DELETE FROM transactions";
        String resetSeq = "DELETE FROM sqlite_sequence WHERE name = 'transactions'";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.executeUpdate(deleteTransactions);
            stmt.executeUpdate(resetSeq);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTransactionsDatabase(ObservableList<Transaction> transactions) {
        clearTransactionsDatabase();
        for (Transaction t : transactions) {
            addTransaction(t);
        }
    }

    public static void addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions(category, date, description, amount, is_expense, recurring_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getCategory().toString());
            pstmt.setString(2, transaction.getDate().toString());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setInt(5, transaction.isExpense() ? 1 : 0);

            if (transaction.getRecurringId() != null) {
                pstmt.setInt(6, transaction.getRecurringId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

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
                int recurringId = rs.getInt("recurring_id");
                Transaction transaction = new Transaction(category, date, description, amount, isExpense);
                transaction.setRecurringId(recurringId);
                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // =============== RECURRING TRANSACTIONS ==================

    public static void addRecurringTransaction(RecurringTransaction rt) {
        String sql = "INSERT INTO recurring_transactions (amount, description, category, start_date, end_date, frequency, is_expense) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, rt.getAmount());  // 💥 This is likely where NULL was being passed
            pstmt.setString(2, rt.getDescription());
            pstmt.setString(3, rt.getCategory().name());
            pstmt.setString(4, rt.getStartDate().toString());
            pstmt.setString(5, rt.getEndDate() != null ? rt.getEndDate().toString() : null);
            pstmt.setString(6, rt.getFrequency().name());
            pstmt.setInt(7, rt.isExpense() ? 1 : 0);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int rtId = rs.getInt(1);
                    rt.setId(rtId);  // So we can reference this later
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<RecurringTransaction> loadRecurringTransactions() {
        List<RecurringTransaction> list = new ArrayList<>();

        String sql = "SELECT * FROM recurring_transactions";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                Category category = Category.valueOf(rs.getString("category"));
                LocalDate start = LocalDate.parse(rs.getString("start_date"));
                String endDateStr = rs.getString("end_date");
                LocalDate end = (endDateStr != null && !endDateStr.isBlank()) ? LocalDate.parse(endDateStr) : null;
                Frequency frequency = Frequency.valueOf(rs.getString("frequency"));
                boolean isExpense = rs.getInt("is_expense") == 1;

                RecurringTransaction rt = (end != null)
                        ? new RecurringTransaction(amount, description, category, start, end, frequency, isExpense)
                        : new RecurringTransaction(amount, description, category, start, frequency, isExpense);

                list.add(rt);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void updateRecurringTransactionsDatabase(ObservableList<RecurringTransaction> transactions) {
        clearRecurringDatabase();
        for (RecurringTransaction t : transactions) {
            addRecurringTransaction(t);
        }
    }

    private static void clearRecurringDatabase() {
        String deleteTransactions = "DELETE FROM recurring_transactions";
        String resetSeq = "DELETE FROM sqlite_sequence WHERE name = 'recurring_transactions'";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.executeUpdate(deleteTransactions);
            stmt.executeUpdate(resetSeq);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============== GENERATE FUTURE TRANSACTIONS ==================

    public static List<Transaction> generateFutureTransactions(List<RecurringTransaction> recurringList, LocalDate from, LocalDate to) {
        List<Transaction> result = new ArrayList<>();

        for (RecurringTransaction rt : recurringList) {
            int rtId = rt.getId();  // You need to store the ID when loading from DB or after inserting

            LocalDate next = rt.getStartDate();
            LocalDate until = rt.getEndDate() != null ? rt.getEndDate() : to;

            while (!next.isAfter(to) && !next.isAfter(until)) {
                if (!next.isBefore(from)) {
                    Transaction generated = new Transaction(
                            rt.getCategory(),
                            next,
                            rt.getDescription(),
                            rt.getAmount(),
                            rt.isExpense()
                    );
                    generated.setRecurringId(rtId); // Link it back
                    result.add(generated);         // ⬅️ Add to result
                }

                // Step to next date
                switch (rt.getFrequency()) {
                    case Daily -> next = next.plusDays(1);
                    case Weekly -> next = next.plusWeeks(1);
                    case Biweekly -> next = next.plusWeeks(2);
                    case Monthly -> next = next.plusMonths(1);
                    case Quarterly -> next = next.plusMonths(3);
                    case Yearly -> next = next.plusYears(1);
                }
            }
        }

        return result;
    }


}


