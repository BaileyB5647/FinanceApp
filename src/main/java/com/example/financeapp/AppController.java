package com.example.financeapp;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.swing.text.NumberFormatter;
import java.sql.Date;
import java.text.Format;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

public class AppController {
    //Available Screen Space
    private final double screenWidth = Screen.getPrimary().getBounds().getWidth() - 100;
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight() - 240;

    Transaction t1 = new Transaction(Category.Groceries, Date.valueOf(LocalDate.now()),
            "Test", 102.00, true);

    ArrayList<Transaction> transactions = new ArrayList<>();

    public Tab getDashboardTab(){
        transactions.add(t1);
        // Dashboard Tab Setup
        Tab dashboard = new Tab("Dashboard");
        dashboard.setClosable(false);


        // Grid Pane Setup
        GridPane gridPane = new GridPane();
        gridPane.setId("root");

        ColumnConstraints col0 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col1 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col2 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col3 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col4 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col5 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col6 = new ColumnConstraints(screenWidth/8);
        ColumnConstraints col7 = new ColumnConstraints(screenWidth/8);


        RowConstraints row0 = new RowConstraints(screenHeight/8);
        RowConstraints row1 = new RowConstraints(screenHeight/8);
        RowConstraints row2 = new RowConstraints(screenHeight/8);
        RowConstraints row3 = new RowConstraints(screenHeight/8);
        RowConstraints row4 = new RowConstraints(screenHeight/8);
        RowConstraints row5 = new RowConstraints(screenHeight/8);
        RowConstraints row6 = new RowConstraints(screenHeight/8);
        RowConstraints row7 = new RowConstraints(screenHeight/8);

        gridPane.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5, col6, col7);
        gridPane.getRowConstraints().addAll(row0, row1, row2, row3, row4, row5, row6, row7);

        // Card One: Balance Data

        // Current Month Label

        Label monthLabel = new Label(String.valueOf(LocalDate.now().getMonth()));
        monthLabel.getStyleClass().add("monthLabel");

        HBox monthBox = new HBox(monthLabel);
        monthBox.getStyleClass().add("monthBox");


        // Balance Title
        Label balanceTitle = new Label("Balance");
        balanceTitle.getStyleClass().add("sectionTitle");

        HBox balanceTitleBox = new HBox(balanceTitle);
        balanceTitleBox.getStyleClass().add("balanceTitleBox");

        // Balance Value

        Label balanceValue = new Label(NumberFormat.getCurrencyInstance(Locale.UK).format(getBalance(transactions)));
        balanceValue.getStyleClass().add("balanceValue");

        if (getBalance(transactions) > 0 ){
            balanceValue.setId("positive");
        } else {
            balanceValue.setId("negative");
        }

        HBox balanceBox = new HBox(balanceValue);
        balanceBox.getStyleClass().add("balanceBox");


        VBox cardOne = new VBox();

        cardOne.getChildren().addAll(monthBox, balanceTitleBox, balanceBox);
        cardOne.getStyleClass().add("card");

        gridPane.add(cardOne, 0, 0, 3,3);


        // Card Two: Budget Data

        // Budget Section Title

        Label budgetTitle = new Label("Budget");
        budgetTitle.getStyleClass().add("sectionTitle");

        HBox budgetTitleBox = new HBox(budgetTitle);
        budgetTitleBox.getStyleClass().add("budgetTitleBox");

        // Budget Display Bar
        ProgressBar budgetDisplayBar = new ProgressBar(0.5);
        budgetDisplayBar.getStyleClass().add("budgetDisplayBar");

        budgetDisplayBar.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(budgetDisplayBar, Priority.ALWAYS);

        Label spent = new Label("Spent: ");
        spent.getStyleClass().add("boldLabel");

        Label spentValue = new Label("£250.00");
        spentValue.getStyleClass().add("standardLabel");

        HBox spentBox = new HBox(spent, spentValue);
        spentBox.getStyleClass().add("spentBox");


        Label remaining = new Label("Remaining: ");
        remaining.getStyleClass().add("boldLabel");

        Label remainingValue = new Label("£250.00");
        remainingValue.getStyleClass().add("standardLabel");

        HBox remainingBox = new HBox(remaining, remainingValue);
        remainingBox.getStyleClass().add("remainingBox");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        HBox budgetDisplayLabels = new HBox(spentBox, spacer, remainingBox);
        budgetDisplayLabels.getStyleClass().add("budgetDisplayLabels");

        VBox cardTwo = new VBox(budgetTitleBox, budgetDisplayBar, budgetDisplayLabels);

        cardTwo.getStyleClass().add("card");

        gridPane.add(cardTwo, 4, 0, 4, 3);

        // Line Break

        Line lineBreak = new Line();
        lineBreak.setEndX(screenWidth);
        lineBreak.getStyleClass().add("line");
        gridPane.add(lineBreak, 0, 3, 8, 1);

        // Card 4: Income Data

        Label incomeLabel = new Label("Income");
        incomeLabel.getStyleClass().add("dataTitle");
        HBox incomeLabelBox = new HBox(incomeLabel);
        incomeLabelBox.getStyleClass().add("dataTitleBox");

        gridPane.add(incomeLabelBox, 0, 4, 2, 1);

        Label incomeValue = new Label("£500.00");
        incomeValue.getStyleClass().add("valueLabel");
        incomeValue.setId("white");

        HBox incomeValueBox = new HBox(incomeValue);
        incomeValueBox.getStyleClass().add("greenCard");

        gridPane.add(incomeValueBox, 0, 5, 2, 2);

        // Card 5: Expenses Data

        Label expensesLabel = new Label("Expenses");
        expensesLabel.getStyleClass().add("dataTitle");

        HBox expensesLabelBox = new HBox(expensesLabel);
        expensesLabelBox.getStyleClass().add("dataTitleBox");

        gridPane.add(expensesLabelBox, 2, 4, 2, 1);

        Label expensesValue = new Label("£460.00");
        expensesValue.getStyleClass().add("valueLabel");
        expensesValue.setId("white");

        HBox expensesValueBox = new HBox(expensesValue);
        expensesValueBox.getStyleClass().add("redCard");

        gridPane.add(expensesValueBox, 2, 5, 2, 2);

        // Card 6: Planned Expenses

        Label plannedExpensesLabel = new Label("Planned Expenses");
        plannedExpensesLabel.getStyleClass().add("dataTitle");

        HBox plannedExpensesLabelBox = new HBox(plannedExpensesLabel);
        plannedExpensesLabelBox.getStyleClass().add("dataTitleBox");

        gridPane.add(plannedExpensesLabelBox, 4, 4, 2, 1);

        Label plannedExpensesValue = new Label("£500.00");
        plannedExpensesValue.getStyleClass().add("valueLabel");

        HBox plannedExpensesValueBox = new HBox(plannedExpensesValue);
        plannedExpensesValueBox.getStyleClass().add("yellowCard");

        gridPane.add(plannedExpensesValueBox, 4, 5, 2, 2);


        // Card 7: Net Cashflow

        Label netCashflowLabel = new Label("Net Cashflow");
        netCashflowLabel.getStyleClass().add("dataTitle");

        HBox netCashflowLabelBox = new HBox(netCashflowLabel);
        netCashflowLabelBox.getStyleClass().add("dataTitleBox");

        gridPane.add(netCashflowLabelBox, 6, 4, 2, 1);

        Label netCashflowValue = new Label("+£40.00");
        netCashflowValue.getStyleClass().add("valueLabel");
        netCashflowValue.setId("white");

        HBox netCashflowValueBox = new HBox(netCashflowValue);
        netCashflowValueBox.getStyleClass().add("greenCard");

        gridPane.add(netCashflowValueBox, 6, 5, 2, 2);

        // Add Transaction Button:
        Button addTransaction = new Button("Add Transaction");

        addTransaction.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newTransactionDialogue();
            }
        });

        addTransaction.getStyleClass().add("transactionButton");
        addTransaction.setPrefSize(screenWidth/4, screenHeight/8);

        HBox buttonsBox = new HBox(addTransaction);
        buttonsBox.getStyleClass().add("buttonsBox");

        gridPane.add(buttonsBox, 3, 7, 2, 1);


        dashboard.setContent(gridPane);

        return dashboard;
    }

    public void newTransactionDialogue(){
        Stage dialog = new Stage();
        dialog.setTitle("Add New Transaction");
        GridPane gridPane = new GridPane();
        gridPane.setId("transactionRoot");

        Scene addTransactionScene = new Scene(gridPane);
        addTransactionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("lightMode.css")).toExternalForm());


        ColumnConstraints col0 = new ColumnConstraints(screenWidth/6);
        ColumnConstraints col1 = new ColumnConstraints(screenWidth/6);

        RowConstraints row0 = new RowConstraints(screenHeight/8);
        RowConstraints row1 = new RowConstraints(screenHeight/8);
        RowConstraints row2 = new RowConstraints(screenHeight/8);
        RowConstraints row3 = new RowConstraints(screenHeight/8);
        RowConstraints row4 = new RowConstraints(screenHeight/8);
        RowConstraints row5 = new RowConstraints(screenHeight/8);

        gridPane.getRowConstraints().addAll(row0, row1, row2, row3, row4, row5);
        gridPane.getColumnConstraints().addAll(col0, col1);

        ToggleButton expenseButton = new ToggleButton("Expense");
        expenseButton.setPrefSize(screenWidth/6, screenHeight/8);
        expenseButton.getStyleClass().add("toggleButton");
        expenseButton.setId("redButton");

        ToggleButton incomeButton = new ToggleButton("Income");
        incomeButton.setPrefSize(screenWidth/6, screenHeight/8);
        incomeButton.getStyleClass().add("toggleButton");
        incomeButton.setId("greenButton");

        ToggleGroup buttonGroup = new ToggleGroup();
        buttonGroup.getToggles().addAll(expenseButton, incomeButton);

        HBox toggleButtons = new HBox(expenseButton, incomeButton);
        toggleButtons.getStyleClass().add("toggleButtonsBox");

        gridPane.add(toggleButtons, 0, 0, 2,1 );

        Label dateLabel = new Label("Date:");
        dateLabel.getStyleClass().add("standardLabel");

        HBox dateLabelBox = new HBox(dateLabel);
        dateLabelBox.getStyleClass().add("labelBox");
        gridPane.add(dateLabelBox, 0, 1);

        DatePicker datePicker = new DatePicker();
        gridPane.add(datePicker, 1, 1);

        Label descriptionLabel = new Label("Description:");
        HBox descriptionLabelBox = new HBox(descriptionLabel);

        gridPane.add(descriptionLabelBox, 0, 2);

        TextField descriptionTextField = new TextField();

        gridPane.add(descriptionTextField, 1, 2);

        dialog.setScene(addTransactionScene);
        dialog.setOnShown(e -> dialog.centerOnScreen());
        dialog.show();
    }

    public static Tab getForecastTab(){
        Tab forecastTab = new Tab("Forecast");
        forecastTab.setClosable(false);

        return forecastTab;
    }

    public static Tab getTransactionsTab(){
        Tab transactionsTab = new Tab("Transactions");
        transactionsTab.setClosable(false);

        return transactionsTab;
    }


    public double getBalance(ArrayList<Transaction> transactions){
        double runningBalance = 0;

        for (Transaction transaction : transactions){
            if (transaction.isExpense()) {
                runningBalance -= transaction.getAmount();
            } else {
                runningBalance += transaction.getAmount();
            }
        }

        return runningBalance;
    }

}
