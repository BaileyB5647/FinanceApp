package com.example.financeapp;

import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class AppController {
    //Available Screen Space
    private final double screenWidth = Screen.getPrimary().getBounds().getWidth() - 100;
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight() - 240;

    ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    ObservableList<RecurringTransaction> recurringTransactions = FXCollections.observableArrayList();

    public MenuBar getMenuBar(){
        MenuBar menuBar = new MenuBar();
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac"))
            menuBar.useSystemMenuBarProperty().set(true);

        Menu settingsMenu = new Menu("Settings");
        MenuItem appearance = new MenuItem("Appearance");
        appearance.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MenuBarController.getAppearanceEditor();
            }
        });

        settingsMenu.getItems().addAll(appearance);

        menuBar.getMenus().addAll(settingsMenu);

        return menuBar;
    }

    public void initialiseApp(){
        transactions.addAll(TransactionsDatabase.loadTransactions());
        recurringTransactions.addAll(TransactionsDatabase.loadRecurringTransactions());
    }

    public Tab getDashboardTab(){
        // Dashboard Tab Setup
        Tab dashboard = new Tab("Dashboard");
        dashboard.setClosable(false);

        // Grid Pane Setup
        GridPane gridPane = new GridPane();
        gridPane.setId("root");

        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setHgrow(gridPane, Priority.ALWAYS);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);



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

        // - = - = - = - = - = - = - = - =
        // Card One: Balance Data
        // - = - = - = - = - = - = - = - =

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

        StringBinding balanceStringBinding = new StringBinding() {
            {
                // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                double newBalance = getBalance(transactions); // <- Your method
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                return formatter.format(newBalance);
            }
        };

        Label balanceLabel = new Label();
        balanceLabel.textProperty().bind(balanceStringBinding);
        balanceLabel.getStyleClass().add("balanceValue");

        if (getBalance(transactions) > 0 ){
            balanceLabel.setId("positive");
        } else {
            balanceLabel.setId("negative");
        }

        transactions.addListener((ListChangeListener<Transaction>) _ -> {
            double currentBalance = getBalance(transactions);
            if (currentBalance > 0) {
                balanceLabel.setId("positive");
            } else {
                balanceLabel.setId("negative");
            }
        });

        HBox balanceBox = new HBox(balanceLabel);
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

        StringBinding incomeStringBinding = new StringBinding() {
            {
                // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double getIncome = getIncome(transactions);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                return formatter.format(getIncome);
            }
        };

        Label incomeValue = new Label();
        incomeValue.textProperty().bind(incomeStringBinding);
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

        StringBinding expensesStringBinding = new StringBinding() {
            {
                // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double expenses = getExpenses(transactions);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                return formatter.format(expenses);
            }
        };

        Label expensesValue = new Label();
        expensesValue.textProperty().bind(expensesStringBinding);
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

        StringBinding plannedExpensesStringBinding = new StringBinding() {
            {
                // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double expenses = getPlannedExpenses(transactions);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                return formatter.format(expenses);
            }
        };

        Label plannedExpensesValue = new Label();
        plannedExpensesValue.textProperty().bind(plannedExpensesStringBinding);
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

        StringBinding netCashflowStringBinding = new StringBinding() {
            {
                // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double expenses = getNetCashflow(transactions);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                return formatter.format(expenses);
            }
        };

        Label netCashflowValue = new Label();
        netCashflowValue.textProperty().bind(netCashflowStringBinding);
        netCashflowValue.getStyleClass().add("valueLabel");
        netCashflowValue.setId("white");

        HBox netCashflowValueBox = new HBox(netCashflowValue);

        if (getNetCashflow(transactions) >= 0) {
            netCashflowValueBox.getStyleClass().add("greenCard");
        } else {
            netCashflowValueBox.getStyleClass().add("redCard");
        }

        netCashflowStringBinding.addListener((_) -> {
            netCashflowValueBox.getStyleClass().clear();
            if (getNetCashflow(transactions) >= 0) {
                netCashflowValueBox.getStyleClass().add("greenCard");
            } else {
                netCashflowValueBox.getStyleClass().add("redCard");
            }
        });

        gridPane.add(netCashflowValueBox, 6, 5, 2, 2);

        // Add Transaction Button:
        Button addTransaction = new Button("Add Transaction");

        addTransaction.setOnAction(_ -> newTransactionDialogue());

        addTransaction.getStyleClass().add("transactionButton");
        addTransaction.setPrefSize(screenWidth/4, screenHeight/8);

        HBox buttonsBox = new HBox(addTransaction);
        buttonsBox.getStyleClass().add("buttonsBox");

        gridPane.add(buttonsBox, 3, 7, 2, 1);

        dashboard.setContent(gridPane);

        return dashboard;
    }

    private Double getIncome(ObservableList<Transaction> transactions) {
        Double monthlyIncome = 0.0;

        for (Transaction transaction : transactions){
            if (!transaction.isExpense() && transaction.getDate().getMonth().equals(LocalDate.now().getMonth())) {
                monthlyIncome += transaction.getAmount();
            }
        }

        return monthlyIncome;
    }

    private Double getExpenses(ObservableList<Transaction> transactions){
        Double monthlyExpenses = 0.0;

        for (Transaction transaction : transactions){
            if (transaction.isExpense() && transaction.getDate().getMonth().equals(LocalDate.now().getMonth())
                && (transaction.getDate().isBefore(LocalDate.now()) || transaction.getDate().isEqual(LocalDate.now()))) {
                monthlyExpenses += transaction.getAmount();
            }
        }

        return monthlyExpenses;
    }

    private Double getPlannedExpenses(ObservableList<Transaction> transactions){
        Double monthlyPlannedExpenses = 0.0;

        for (Transaction transaction : transactions){
            if (transaction.isExpense() && transaction.getDate().getMonth().equals(LocalDate.now().getMonth())
                    && transaction.getDate().getYear() == LocalDate.now().getYear() && transaction.getDate().isAfter(LocalDate.now())) {
                monthlyPlannedExpenses += transaction.getAmount();
            }
        }

        return monthlyPlannedExpenses;
    }

    private Double getNetCashflow(ObservableList<Transaction> transactions){
        return getIncome(transactions) - (getExpenses(transactions) + getPlannedExpenses(transactions));
    }

    public Tab getTransactionsTab() {
        Tab transactionsTab = new Tab("Transactions");
        transactionsTab.setClosable(false);



        // Add Transaction Button:
        Button addTransaction = new Button("Add Transaction");

        addTransaction.setOnAction(_ -> newTransactionDialogue());

        addTransaction.getStyleClass().add("transactionButton");
        addTransaction.setPrefSize(screenWidth/6, screenHeight/12);

        HBox buttonsBox = new HBox(addTransaction);
        buttonsBox.getStyleClass().add("buttonsBox");




        // Create layout containers
        VBox container = new VBox();
        GridPane gridView = new GridPane();
        gridView.getStyleClass().add("listTab");

        ColumnConstraints col0 = new ColumnConstraints(screenWidth*0.65);
        ColumnConstraints col1 = new ColumnConstraints(screenWidth*0.35);

        RowConstraints row0 = new RowConstraints(screenHeight*0.1);
        RowConstraints row1 = new RowConstraints(screenHeight*0.8);
        RowConstraints row2 = new RowConstraints(screenHeight*0.1);


        gridView.getColumnConstraints().addAll(col0, col1);
        gridView.getRowConstraints().addAll(row0, row1, row2);


        // Header row
        HBox header = new HBox(10);
        header.getStyleClass().add("listHeader");

        Label dateHeader = createHeaderLabel("Date", 150);
        Label descHeader = createHeaderLabel("Description", 200);
        Label amountHeader = createHeaderLabel("Amount", 140);
        Label categoryHeader = createHeaderLabel("Category", 200);

        header.getChildren().addAll(dateHeader, descHeader, amountHeader,  categoryHeader);

        // ListView setup
        ListView<Transaction> transactionListView = new ListView<>();
        transactionListView.getStyleClass().add("listView");
        transactionListView.setItems(transactions);

        transactionListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Transaction transaction, boolean empty) {
                super.updateItem(transaction, empty);
                if (empty || transaction == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String formattedAmount = NumberFormat.getCurrencyInstance(Locale.getDefault())
                            .format(transaction.getAmount());

                    Label nameLabel = new Label(transaction.getDescription());
                    Label amountLabel = new Label(transaction.isExpense() ? "-" + formattedAmount : formattedAmount);

                    if (transaction.isExpense()){
                        amountLabel.getStyleClass().add("expense");
                    }

                    Label dateLabel = new Label(transaction.getDate().toString());
                    Label categoryLabel = new Label(transaction.getCategory().toString());

                    nameLabel.setPrefWidth(200);
                    amountLabel.setPrefWidth(140);
                    dateLabel.setPrefWidth(150);
                    categoryLabel.setPrefWidth(200);

                    HBox row = new HBox(10, dateLabel, nameLabel, amountLabel, categoryLabel);
                    setGraphic(row);
                }
            }
        });

        VBox transactionEditor = new VBox();

        transactionListView.getSelectionModel().selectedItemProperty().addListener((_, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (oldSelection != null){
                    gridView.getChildren().remove(transactionEditor);
                }

                transactionEditor.getChildren().setAll(getTransactionEditor(newSelection, transactionListView));
                gridView.add(transactionEditor, 1, 0, 1, 2);
            }
        });

        // Assemble the UI
        container.getChildren().addAll(header, transactionListView);


        gridView.add(header, 0, 0);
        gridView.add(transactionListView, 0, 1);
        gridView.add(buttonsBox, 0, 2);

        transactionsTab.setContent(gridView);

        return transactionsTab;
    }

    public Tab getForecastTab() {
        Tab forecastTab = new Tab("Forecast");
        forecastTab.setClosable(false);

        // Define axes
        final CategoryAxis xAxis = new CategoryAxis(); // JavaFX doesn't support LocalDate directly here
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount");

        // Convert LocalDate to String for CategoryAxis
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transactions");



        SortedList<Transaction> sortedList = transactions.sorted(Comparator.comparing(Transaction::getDate));

        for (Transaction transaction : sortedList) {
            String dateStr = transaction.getDate().toString();
            series.getData().add(new XYChart.Data<>(dateStr, transaction.getAmount()));
        }

        // Create chart
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), yAxis);
        chart.getData().add(series);
        chart.setTitle("Transaction Forecast");


        transactions.addListener((ListChangeListener<Transaction>) _ -> {
            chart.getData().clear();
            series.getData().clear();
            SortedList<Transaction> sortedList1 = transactions.sorted(Comparator.comparing(Transaction::getDate));

            for (Transaction transaction : sortedList1) {
                String dateStr = transaction.getDate().toString();
                series.getData().add(new XYChart.Data<>(dateStr, transaction.getAmount()));

            }
            chart.getData().add(series);
        });

        forecastTab.setContent(chart);
        return forecastTab;
    }

    public Tab getAccountsTab(){
        Tab accountsTab = new Tab("Accounts");
        accountsTab.setClosable(false);



        return accountsTab;
    }

    public Tab getRecurringTransactionsTab(){
        Tab recurringTransactionsTab = new Tab("Recurring Transactions");
        recurringTransactionsTab.setClosable(false);

        // Add Transaction Button:
        Button addTransaction = new Button("Add Recurring Transaction");
        addTransaction.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newRecurringTransactionDialogue();
            }
        });

        addTransaction.getStyleClass().add("transactionButton");
        addTransaction.setPrefSize(screenWidth/4, screenHeight/12);

        HBox buttonsBox = new HBox(addTransaction);
        buttonsBox.getStyleClass().add("buttonsBox");

        // Create layout containers
        VBox container = new VBox();
        GridPane gridView = new GridPane();
        gridView.getStyleClass().add("listTab");

        ColumnConstraints col0 = new ColumnConstraints(screenWidth*0.65);
        ColumnConstraints col1 = new ColumnConstraints(screenWidth*0.35);

        RowConstraints row0 = new RowConstraints(screenHeight*0.1);
        RowConstraints row1 = new RowConstraints(screenHeight*0.8);
        RowConstraints row2 = new RowConstraints(screenHeight*0.1);


        gridView.getColumnConstraints().addAll(col0, col1);
        gridView.getRowConstraints().addAll(row0, row1, row2);


        // Header row
        HBox header = new HBox(10);
        header.getStyleClass().add("listHeader");

        Label descHeader = createHeaderLabel("Description", 120);
        Label amountHeader = createHeaderLabel("Amount", 110);
        Label categoryHeader = createHeaderLabel("Category", 110);
        Label repeatFrequency = createHeaderLabel("Frequency", 110);
        Label startDateHeader = createHeaderLabel("Start Date", 110);
        Label endDateHeader = createHeaderLabel("End Date", 110);


        header.getChildren().addAll(descHeader, amountHeader,  categoryHeader, repeatFrequency, startDateHeader, endDateHeader);

        // ListView setup
        ListView<RecurringTransaction> recurringTransactionListView = new ListView<>();
        recurringTransactionListView.getStyleClass().add("listView");
        recurringTransactionListView.setItems(recurringTransactions);

        recurringTransactionListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(RecurringTransaction transaction, boolean empty) {
                super.updateItem(transaction, empty);
                if (empty || transaction == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String formattedAmount = NumberFormat.getCurrencyInstance(Locale.getDefault())
                            .format(transaction.getAmount());

                    Label nameLabel = new Label(transaction.getDescription());
                    Label amountLabel = new Label(transaction.isExpense() ? "-" + formattedAmount : formattedAmount);

                    if (transaction.isExpense()){
                        amountLabel.getStyleClass().add("expense");
                    }

                    Label startDateLabel = new Label(transaction.getStartDate().toString());

                    Label endDateLabel = new Label();
                    if (transaction.getEndDate() != null){ endDateLabel.setText(transaction.getEndDate().toString()); }

                    Label categoryLabel = new Label(transaction.getCategory().toString());
                    Label frequencyLabel = new Label((transaction.getFrequency().toString()));

                    nameLabel.setPrefWidth(120);
                    amountLabel.setPrefWidth(110);
                    startDateLabel.setPrefWidth(110);
                    endDateLabel.setPrefWidth(110);
                    categoryLabel.setPrefWidth(110);
                    frequencyLabel.setPrefWidth(110);


                    HBox row = new HBox(10, nameLabel, amountLabel, categoryLabel, frequencyLabel, startDateLabel, endDateLabel);
                    setGraphic(row);
                }
            }
        });

        VBox transactionEditor = new VBox();

        recurringTransactionListView.getSelectionModel().selectedItemProperty().addListener((_, oldSelection, newSelection) -> {
            if (newSelection != null) {
                gridView.getChildren().remove(transactionEditor);

                transactionEditor.getChildren().setAll(getRecurringTransactionEditor(newSelection, recurringTransactionListView));
                gridView.add(transactionEditor, 1, 0, 1, 2);
            }

        });

        // Assemble the UI
        container.getChildren().addAll(header, recurringTransactionListView);


        gridView.add(header, 0, 0);
        gridView.add(recurringTransactionListView, 0, 1);
        gridView.add(buttonsBox, 0, 2);

        recurringTransactionsTab.setContent(gridView);

        return recurringTransactionsTab;
    }

    private void newAccountDialogue() {
    }

    public void newTransactionDialogue() {
        // === Stage Setup ===
        Stage dialog = new Stage();
        dialog.setTitle("Add New Transaction");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // === GridPane Setup ===
        GridPane gridPane = new GridPane();
        gridPane.setId("transactionRoot");

        for (int i = 0; i < 6; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(screenHeight / 8));
        }

        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(screenWidth / 7),
                new ColumnConstraints(screenWidth / 6)
        );

        // === Scene Setup ===
        Scene scene = new Scene(gridPane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("lightMode.css")).toExternalForm());
        dialog.setScene(scene);

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("labelBox");

        ToggleButton expenseButton = createToggleButton("Expense", "redButton");
        ToggleButton incomeButton = createToggleButton("Income", "greenButton");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(expenseButton, incomeButton);
        toggleGroup.selectToggle(expenseButton);

        // Prevent deselection
        for (Toggle toggle : toggleGroup.getToggles()) {
            ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ToggleButton btn = (ToggleButton) event.getSource();
                if (btn.isSelected()) event.consume();
            });
        }

        HBox toggleButtonsBox = new HBox(expenseButton, incomeButton);
        toggleButtonsBox.getStyleClass().add("toggleButtonsBox");

        addLabeledField(gridPane, "Transaction Type:", toggleButtonsBox, 0);

        // === Start Date Picker ===
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        addLabeledField(gridPane, "Start Date:", startDatePicker, 1);

        // === Description Field ===
        TextField descriptionField = new TextField();
        addLabeledField(gridPane, "Description:", descriptionField, 2);

        // === Category ChoiceBox ===
        ChoiceBox<Category> categories = new ChoiceBox<>();
        categories.getItems().addAll(Category.values());
        categories.setPrefWidth(screenWidth / 5);
        categories.getStyleClass().add("fieldBox");
        addLabeledField(gridPane, "Category:", categories, 3);

        // === Amount Spinner ===
        Spinner<Double> amountSpinner = new Spinner<>(0, Double.MAX_VALUE - 1, 0, 0.01);
        amountSpinner.setEditable(true);
        addLabeledField(gridPane, "Amount:", amountSpinner, 4);

        // === Buttons ===
        Button confirm = new Button("Confirm");
        confirm.getStyleClass().addAll("button", "confirm");

        confirm.setOnAction(_ -> {
            Category category = categories.getValue();
            LocalDate startDate = startDatePicker.getValue();
            String description = descriptionField.getText().strip();
            Double transactionAmount = amountSpinner.getValue();
            Boolean isExpense = toggleGroup.getSelectedToggle() == expenseButton;

            if (category == null){
                getErrorAlert("Empty Category Field", "Field Error");

            } else if (startDate.toString().isBlank()) {
                getErrorAlert("Empty Start Date Field", "Field Error");

            } else if (description.isBlank()){
                getErrorAlert("Empty Description Field", "Field Error");

            } else if (transactionAmount.toString().isBlank()) {
                getErrorAlert("Empty Amount Field", "Field Error");
            } else {
                Transaction newTransaction = new Transaction(category, startDate, description, transactionAmount, isExpense);
                transactions.add(newTransaction);
                TransactionsDatabase.addTransaction(newTransaction);
                dialog.close();
            }

        });


        Button cancel = new Button("Cancel");
        cancel.getStyleClass().addAll("button", "cancel");

        cancel.setOnAction(_ -> dialog.close());

        HBox buttonBox = new HBox(confirm, cancel);
        buttonBox.getStyleClass().add("buttonsBox");
        gridPane.add(buttonBox, 0, 5, 2, 1);

        // === Finalize Dialog ===
        dialog.setOnShown(_ -> dialog.centerOnScreen());
        dialog.show();
    }

    public void newRecurringTransactionDialogue(){
        // === Stage Setup ===
        Stage dialog = new Stage();
        dialog.setTitle("Add New Transaction");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // === GridPane Setup ===
        GridPane gridPane = new GridPane();
        gridPane.setId("transactionRoot");

        for (int i = 0; i < 7; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(screenHeight / 8));
        }
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(screenWidth / 7),
                new ColumnConstraints(screenWidth / 6)
        );

        // === Scene Setup ===
        Scene scene = new Scene(gridPane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("lightMode.css")).toExternalForm());
        dialog.setScene(scene);

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("labelBox");

        ToggleButton expenseButton = createToggleButton("Expense", "redButton");
        ToggleButton incomeButton = createToggleButton("Income", "greenButton");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(expenseButton, incomeButton);
        toggleGroup.selectToggle(expenseButton);

        // Prevent deselection
        for (Toggle toggle : toggleGroup.getToggles()) {
            ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ToggleButton btn = (ToggleButton) event.getSource();
                if (btn.isSelected()) event.consume();
            });
        }

        HBox toggleButtonsBox = new HBox(expenseButton, incomeButton);
        toggleButtonsBox.getStyleClass().add("toggleButtonsBox");

        addLabeledField(gridPane, "Transaction Type:", toggleButtonsBox, 0);

        // === Start Date Picker ===
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        addLabeledField(gridPane, "Start Date:", startDatePicker, 1);

        // === End Date Picker ===
        DatePicker endDatePicker = new DatePicker();
        addLabeledField(gridPane, "End Date:", endDatePicker, 2);

        // === Description Field ===
        TextField descriptionField = new TextField();
        addLabeledField(gridPane, "Description:", descriptionField, 3);

        // === Category ChoiceBox ===
        ChoiceBox<Category> categories = new ChoiceBox<>();
        categories.getItems().addAll(Category.values());
        categories.setPrefWidth(screenWidth / 5);
        categories.getStyleClass().add("fieldBox");
        addLabeledField(gridPane, "Category:", categories, 4);

        // === Frequency ChoiceBox ===
        ChoiceBox<Frequency> frequencyChoiceBox = new ChoiceBox<>();
        frequencyChoiceBox.getItems().addAll(Frequency.values());
        frequencyChoiceBox.setPrefWidth(screenWidth/5);
        frequencyChoiceBox.getStyleClass().add("fieldBox");
        addLabeledField(gridPane, "Frequency:", frequencyChoiceBox, 5);

        // === Amount Spinner ===
        Spinner<Double> amountSpinner = new Spinner<>(0, Double.MAX_VALUE - 1, 0, 0.01);
        amountSpinner.setEditable(true);
        addLabeledField(gridPane, "Amount:", amountSpinner, 6);

        // === Buttons ===
        Button confirm = new Button("Confirm");
        confirm.getStyleClass().addAll("button", "confirm");

        confirm.setOnAction(_ -> {
            Category category = categories.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String description = descriptionField.getText().strip();
            Double transactionAmount = amountSpinner.getValue();
            boolean isExpense = toggleGroup.getSelectedToggle() == expenseButton;
            Frequency frequency = frequencyChoiceBox.getValue();

            if (category == null){
                getErrorAlert("Empty Category Field", "Field Error");

            } else if (startDate.toString().isBlank()) {
                getErrorAlert("Empty Start Date Field", "Field Error");

            } else if (description.isBlank()){
                getErrorAlert("Empty Description Field", "Field Error");

            } else if (transactionAmount.toString().isBlank()) {
                getErrorAlert("Empty Amount Field", "Field Error");
            } else if (frequency == null){
                getErrorAlert("Empty Category Field", "Field Error");
            } else {
                RecurringTransaction newTransaction;
                if (endDate == null){
                    newTransaction = new RecurringTransaction(transactionAmount, description,
                            category, startDate, frequency, isExpense);
                } else {
                    newTransaction = new RecurringTransaction(transactionAmount, description,
                            category, startDate, endDate, frequency, isExpense);
                }

                recurringTransactions.add(newTransaction);
                TransactionsDatabase.addRecurringTransaction(newTransaction);

// Only generate future transactions for the new one!
                List<Transaction> future = TransactionsDatabase.generateFutureTransactions(
                        List.of(newTransaction), LocalDate.now(), LocalDate.now().plusYears(1)
                );
                transactions.addAll(future);

                TransactionsDatabase.updateTransactionsDatabase(transactions);
                dialog.close();
            }

        });


        Button cancel = new Button("Cancel");
        cancel.getStyleClass().addAll("button", "cancel");

        cancel.setOnAction(_ -> dialog.close());

        HBox buttonBox = new HBox(confirm, cancel);
        buttonBox.getStyleClass().add("buttonsBox");
        gridPane.add(buttonBox, 0, 7, 2, 1);

        // === Finalize Dialog ===
        dialog.setOnShown(_ -> dialog.centerOnScreen());
        dialog.show();
    }

// === Helpers ===

    private ToggleButton createToggleButton(String text, String id) {
        ToggleButton button = new ToggleButton(text);
        button.setPrefSize(screenWidth / 10, screenHeight / 10);
        button.setId(id);
        button.getStyleClass().add("toggleButton");
        return button;
    }

    private void addLabeledField(GridPane grid, String labelText, Node input, int rowIndex) {
        Label label = new Label(labelText);
        label.getStyleClass().add("standardLabel");

        HBox labelBox = new HBox(label);
        labelBox.getStyleClass().add("labelBox");

        HBox inputBox = new HBox(input);
        inputBox.getStyleClass().add("fieldBox");

        if (input instanceof Control) {
            ((Control) input).setPrefWidth(screenWidth / 5);
        }

        grid.add(labelBox, 0, rowIndex);
        grid.add(inputBox, 1, rowIndex);
    }

    private void getErrorAlert(String contentText, String title){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(contentText);
        alert.setTitle(title);

        alert.show();
    }

    private VBox getTransactionEditor(Transaction transaction, ListView<Transaction> listView) {
        GridPane gridPane = new GridPane();
        gridPane.setId("transactionRoot");

        for (int i = 0; i < 6; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(screenHeight*(0.9/6)));
        }
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(screenWidth * 0.15),
                new ColumnConstraints(screenWidth * 0.15)
        );

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("labelBox");

        ToggleButton expenseButton = createToggleButton("Expense", "redButton");
        ToggleButton incomeButton = createToggleButton("Income", "greenButton");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(expenseButton, incomeButton);

        if (transaction.isExpense()) {
            toggleGroup.selectToggle(expenseButton);
        } else {
            toggleGroup.selectToggle(incomeButton);
        }

        // Prevent deselection
        for (Toggle toggle : toggleGroup.getToggles()) {
            ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ToggleButton btn = (ToggleButton) event.getSource();
                if (btn.isSelected()) event.consume();
            });
        }

        HBox toggleButtonsBox = new HBox(expenseButton, incomeButton);
        toggleButtonsBox.getStyleClass().add("toggleButtonsBox");

        // === Form Fields ===
        DatePicker datePicker = new DatePicker(transaction.getDate());


        TextField descriptionField = new TextField(transaction.getDescription());

        ChoiceBox<Category> categories = new ChoiceBox<>();
        categories.setValue(transaction.getCategory());
        categories.getItems().addAll(Category.values());
        categories.setPrefWidth(screenWidth / 5);
        categories.getStyleClass().add("fieldBox");

        Spinner<Double> amountSpinner = new Spinner<>(0, Double.MAX_VALUE - 1, transaction.getAmount(), 0.01);
        amountSpinner.setEditable(true);

        // === Submit Button ===


        // === Submit Button ===
        Button confirm = new Button("Confirm");
        confirm.getStyleClass().addAll("button", "confirm");

        confirm.setOnAction(_ -> {
            transaction.setCategory(categories.getValue());
            transaction.setDate(datePicker.getValue());
            transaction.setDescription(descriptionField.getText().strip());
            transaction.setAmount(amountSpinner.getValue());
            transaction.setExpense(toggleGroup.getSelectedToggle() == expenseButton);

            Transaction temp = new Transaction(Category.Groceries, LocalDate.now(), "TEMP", 0.0, false);
            listView.refresh();
            transactions.addLast(temp);
            transactions.removeLast();

            TransactionsDatabase.updateTransactionsDatabase(transactions);

        });

        // === Delete Button ===
        Button deleteButton = new Button("Delete Transaction");
        deleteButton.getStyleClass().addAll("button", "cancel");
        deleteButton.setOnAction(_ -> {
            Alert warningAlert = new Alert(Alert.AlertType.CONFIRMATION);
            warningAlert.setTitle("Delete Transaction?");
            warningAlert.setContentText("Do you want to delete this transaction?");

            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

            warningAlert.getButtonTypes().setAll(yesButton, noButton);
            Optional<ButtonType> result = warningAlert.showAndWait();

            if (result.isPresent() && result.get() == yesButton) {
                // User clicked Yes
                transactions.remove(transaction);

                TransactionsDatabase.updateTransactionsDatabase(transactions);

                gridPane.getChildren().clear();
            }
        });


        HBox buttonBox = new HBox(confirm, deleteButton);
        buttonBox.getStyleClass().add("buttonsBox");

        // === Layout Grid ===
        addLabeledField(gridPane, "Transaction Type:", toggleButtonsBox, 0);
        addLabeledField(gridPane, "Date:", datePicker, 1);
        addLabeledField(gridPane, "Description:", descriptionField, 2);
        addLabeledField(gridPane, "Category:", categories, 3);
        addLabeledField(gridPane, "Amount:", amountSpinner, 4);

        gridPane.add(buttonBox, 0, 5, 2, 1);

        // === Wrap gridPane in a Card ===
        VBox card = new VBox(gridPane);
        card.getStyleClass().add("card");
        card.setId("transactionEditor");

        return card;
    }

    private VBox getRecurringTransactionEditor(RecurringTransaction transaction, ListView<RecurringTransaction> listView){
        GridPane gridPane = new GridPane();
        gridPane.setId("transactionRoot");

        for (int i = 0; i < 7; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(screenHeight*(0.9/6)));
        }
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(screenWidth * 0.15),
                new ColumnConstraints(screenWidth * 0.15)
        );

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("labelBox");

        ToggleButton expenseButton = createToggleButton("Expense", "redButton");
        ToggleButton incomeButton = createToggleButton("Income", "greenButton");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(expenseButton, incomeButton);

        if (transaction.isExpense()) {
            toggleGroup.selectToggle(expenseButton);
        } else {
            toggleGroup.selectToggle(incomeButton);
        }

        // Prevent deselection
        for (Toggle toggle : toggleGroup.getToggles()) {
            ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ToggleButton btn = (ToggleButton) event.getSource();
                if (btn.isSelected()) event.consume();
            });
        }

        HBox toggleButtonsBox = new HBox(expenseButton, incomeButton);
        toggleButtonsBox.getStyleClass().add("toggleButtonsBox");

        // === Form Fields ===
        DatePicker startDatePicker = new DatePicker(transaction.getStartDate());

        DatePicker endDatePicker = transaction.getEndDate() != null
                ? new DatePicker(transaction.getEndDate())
                : new DatePicker();

        TextField descriptionField = new TextField(transaction.getDescription());

        ChoiceBox<Category> categories = new ChoiceBox<>();
        categories.setValue(transaction.getCategory());
        categories.getItems().addAll(Category.values());
        categories.setPrefWidth(screenWidth / 5);
        categories.getStyleClass().add("fieldBox");

        Spinner<Double> amountSpinner = new Spinner<>(0, Double.MAX_VALUE - 1, transaction.getAmount(), 0.01);
        amountSpinner.setEditable(true);

        // === Submit Button ===
        Button confirm = new Button("Confirm");
        confirm.getStyleClass().addAll("button", "confirm");

        confirm.setOnAction(_ -> {
            transaction.setCategory(categories.getValue());
            transaction.setStartDate(startDatePicker.getValue());
            transaction.setDescription(descriptionField.getText().strip());
            transaction.setEndDate(endDatePicker.getValue());
            transaction.setAmount(amountSpinner.getValue());
            transaction.setExpense(toggleGroup.getSelectedToggle() == expenseButton);

            if (endDatePicker.getValue() != null && startDatePicker.getValue().isAfter(endDatePicker.getValue())){
                getErrorAlert("Start Date is after End Date!", "Date Error");
            } else {
                listView.refresh();

                TransactionsDatabase.updateRecurringTransactionsDatabase(recurringTransactions);
            }
        });

        // === Delete Button ===
        Button deleteButton = new Button("Delete Transaction");
        deleteButton.getStyleClass().addAll("button", "cancel");
        deleteButton.setOnAction(_ -> {
            Alert warningAlert = new Alert(Alert.AlertType.CONFIRMATION);
            warningAlert.setTitle("Delete Transaction?");
            warningAlert.setContentText("Do you want to delete this transaction?");


            ButtonType allButton = new ButtonType("All Transactions", ButtonBar.ButtonData.YES);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
            ButtonType futureButton = new ButtonType("Future Transactions", ButtonBar.ButtonData.HELP);

            warningAlert.getButtonTypes().setAll(allButton, futureButton, cancelButton);
            Optional<ButtonType> result = warningAlert.showAndWait();

            if (result.isPresent() && result.get() == allButton) {
                // User clicked Yes
                recurringTransactions.remove(transaction);

                TransactionsDatabase.updateRecurringTransactionsDatabase(recurringTransactions);

                transactions.removeIf(transaction1 -> Objects.equals(transaction1.getRecurringId(), transaction.getId()));

                TransactionsDatabase.updateTransactionsDatabase(transactions);


                gridPane.getChildren().clear();
            } else if (result.isPresent() && result.get() == futureButton){
                recurringTransactions.remove(transaction);

                TransactionsDatabase.updateRecurringTransactionsDatabase(recurringTransactions);

                transactions.removeIf(transaction1 -> (Objects.equals(transaction1.getRecurringId(), transaction.getId()) &&
                        transaction1.getDate().isAfter(LocalDate.now())));

                TransactionsDatabase.updateTransactionsDatabase(transactions);

                gridPane.getChildren().clear();
            }
        });


        HBox buttonBox = new HBox(confirm, deleteButton);
        buttonBox.getStyleClass().add("buttonsBox");

        // === Layout Grid ===
        addLabeledField(gridPane, "Transaction Type:", toggleButtonsBox, 0);
        addLabeledField(gridPane, "Start Date:", startDatePicker, 1);
        addLabeledField(gridPane, "End Date:", endDatePicker, 2);
        addLabeledField(gridPane, "Description:", descriptionField, 3);
        addLabeledField(gridPane, "Category:", categories, 4);
        addLabeledField(gridPane, "Amount:", amountSpinner, 5);

        gridPane.add(buttonBox, 0, 6, 2, 1);

        // === Wrap gridPane in a Card ===
        VBox card = new VBox(gridPane);
        card.getStyleClass().add("card");
        card.setId("transactionEditor");

        return card;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        return label;
    }

    public double getBalance(ObservableList<Transaction> transactions){
        double runningBalance = 0;

        for (Transaction transaction : transactions){
            if (transaction.getDate().isBefore(LocalDate.now()) || transaction.getDate().isEqual(LocalDate.now())){
                if (transaction.isExpense()) {
                    runningBalance -= transaction.getAmount();
                } else {
                    runningBalance += transaction.getAmount();
                }
            }
        }

        return runningBalance;
    }

}
