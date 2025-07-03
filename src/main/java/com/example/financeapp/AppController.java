package com.example.financeapp;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.*;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;

public class AppController {
    //Available Screen Space
    private final double screenWidth = Screen.getPrimary().getBounds().getWidth() - 100;
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight() - 240;

    public ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    public ObservableList<RecurringTransaction> recurringTransactions = FXCollections.observableArrayList();

    public SimpleDoubleProperty budgetLimit = new SimpleDoubleProperty(Database.getBudgetAmount(LocalDate.now().getYear(), LocalDate.now().getMonthValue()));

    public SimpleStringProperty activeStyleSheet = new SimpleStringProperty(ThemeManager.loadTheme());

    /**
     * Initializes the application by loading transactions and recurring transactions
     * from the database and populating the respective collections.
     */
    public void initialiseApp(){

        transactions.addAll(Database.loadTransactions());
        recurringTransactions.addAll(Database.loadRecurringTransactions());
    }


    /**
     * Sets up and returns a new {@link Scene} for the given root pane.
     * The scene will apply the active stylesheet and listen for changes
     * to the stylesheet, updating the scene stylesheets dynamically.
     *
     * @param root the root {@link Pane} to be used in the scene
     * @return a configured {@link Scene} with the active stylesheet applied
     */
    public Scene setupScene(Pane root){
        Scene scene = new Scene(root);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(getActiveStylesheet())).toExternalForm());

        activeStyleSheet.addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                scene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource(getActiveStylesheet())).toExternalForm());
            }
        });

        return scene;
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                     MENU BAR SETUP METHODS                   \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\

    /**
     * Creates and returns the main application {@link MenuBar} with platform-specific settings.
     *
     * <p>On Mac systems, the menu bar is integrated into the system menu bar.
     * The menu bar includes:
     * <ul>
     *     <li>A "Settings" menu with an "Appearance" item that opens the appearance editor.</li>
     *     <li>A "New" menu with options to create a new transaction or a new recurring transaction.</li>
     * </ul>
     * </p>
     *
     * @return a configured {@link MenuBar} with application menus and actions
     */
    public MenuBar getMenuBar(){
        MenuBar menuBar = new MenuBar();
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac"))
            menuBar.useSystemMenuBarProperty().set(true);

        // Settings

        Menu settingsMenu = new Menu("Settings");
        MenuItem appearance = new MenuItem("Appearance");
        appearance.setOnAction(_ -> getAppearanceEditor());

        settingsMenu.getItems().addAll(appearance);

        // New
        Menu newMenu = new Menu("New");
        MenuItem newTransaction = new MenuItem("New Transaction");
        newTransaction.setOnAction(_ -> newTransactionDialogue());
        MenuItem newRecurringTransaction = new MenuItem("New Recurring Transaction");
        newRecurringTransaction.setOnAction(_ -> newRecurringTransactionDialogue());

        newMenu.getItems().addAll(newTransaction, newRecurringTransaction);
        menuBar.getMenus().addAll(settingsMenu, newMenu);

        return menuBar;
    }




    private void getAppearanceEditor() {
        Stage appearanceEditor = new Stage();
        appearanceEditor.initModality(Modality.APPLICATION_MODAL);
        appearanceEditor.setTitle("Appearance Editor");

        BorderPane root = new BorderPane();
        root.setMinSize(500, 500);
        root.setId("root");

        // === Theme Selector ===

        Label themeLabel = new Label("Theme: ");
        themeLabel.getStyleClass().add("standardLabel");
        themeLabel.setPrefWidth(120);

        ComboBox<Theme> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(Theme.values());

        themeSelector.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> activeStyleSheet.set(newValue.getSheetName()));

        themeSelector.setValue(Theme.getTheme(activeStyleSheet.getValue()));

        HBox themeSelectorBox = new HBox(themeLabel, themeSelector);
        themeSelectorBox.getStyleClass().add("centerBox");

        // === UI SETUP ===
        VBox UIBox = new VBox(themeSelectorBox);
        root.setCenter(UIBox);

        Scene scene = setupScene(root);

        appearanceEditor.setScene(scene);
        appearanceEditor.show();
    }

    /**
     * Returns the path of the currently active stylesheet.
     *
     * @return the active stylesheet file path as a {@link String}
     */
    public String getActiveStylesheet(){
        return activeStyleSheet.getValue();
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                      TAB SETUP METHODS                       \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\

    /**
     * Creates and returns the main dashboard tab for the application UI.
     * <p>
     * This dashboard displays an overview of the user's financial status including:
     * <ul>
     *     <li>Current balance for the selected month</li>
     *     <li>Budget usage with progress indicator</li>
     *     <li>Income, expenses, planned expenses, and net cash flow</li>
     * </ul>
     * Each section is visually represented using styled cards and updated dynamically based on
     * the list of transactions. The dashboard also includes a button to add a new transaction.
     *
     * @return the {@link Tab} containing the complete dashboard layout and content
     */
    public Tab getDashboardTab(){
        // - = - = - = - = - = - = - = - =
        // Card One: Balance Data
        // - = - = - = - = - = - = - = - =

        // Current Month Label
        Label monthLabel = new Label(String.valueOf(LocalDate.now().getMonth()));
        HBox monthBox = new HBox(monthLabel);

        monthLabel.getStyleClass().add("monthLabel");
        monthBox.getStyleClass().add("monthBox");

        // Balance Title
        Label balanceTitle = new Label("Balance");
        balanceTitle.getStyleClass().add("sectionTitle");


        HBox balanceTitleBox = new HBox(balanceTitle);
        balanceTitleBox.getStyleClass().add("balanceTitleBox");

        // Balance Value
        StringBinding balanceStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions list
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


        // Balance Conditional Styling
        if (getBalance(transactions) > 0 ){ balanceLabel.setId("positive");}
        else { balanceLabel.setId("negative"); }

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

        // Card Setup
        VBox cardOne = new VBox();

        cardOne.getChildren().addAll(monthBox, balanceTitleBox, balanceBox);
        cardOne.getStyleClass().add("card");


        // - = - = - = - = - = - = - = - =
        // Budget Data Card
        // - = - = - = - = - = - = - = - =

        Label budgetTitle   = new Label("Budget");
        HBox budgetTitleBox = new HBox(budgetTitle);

        budgetTitle.getStyleClass().add("sectionTitle");
        budgetTitleBox.getStyleClass().add("budgetTitleBox");


        // Budget Display Bar
        ProgressBar budgetDisplayBar = new ProgressBar();
        budgetDisplayBar.getStyleClass().add("budgetDisplayBar");
        budgetDisplayBar.setMinHeight(50);

        budgetDisplayBar.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(budgetDisplayBar, Priority.ALWAYS);


        // Spent Data
        StringBinding spentStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double expenses = getExpenses(transactions) + getPlannedExpenses(transactions);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());


                return formatter.format(expenses);
            }
        };

        Label spent = new Label("Spent: ");
        Label spentValueLabel = new Label();
        spentValueLabel.textProperty().bind(spentStringBinding);

        HBox spentBox = new HBox(spent, spentValueLabel);

        spent.getStyleClass().addAll("standardLabel", "bold");
        spentValueLabel.getStyleClass().add("standardLabel");
        spentBox.getStyleClass().add("leftBox");


        // Remaining Data
        StringBinding remainingStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double remaining = budgetLimit.doubleValue() - (getExpenses(transactions) + getPlannedExpenses(transactions));
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                Double spent = budgetLimit.getValue() - remaining;

                budgetDisplayBar.setProgress(spent / budgetLimit.getValue());

                return formatter.format(remaining);
            }
        };


        Label remaining = new Label("Remaining: ");
        Label remainingValueLabel = new Label();
        remainingValueLabel.textProperty().bind(remainingStringBinding);

        HBox remainingBox = new HBox(remaining, remainingValueLabel);

        remaining.getStyleClass().addAll("bold", "standardLabel");
        remainingValueLabel.getStyleClass().add("standardLabel");
        remainingBox.getStyleClass().add("rightBox");


        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Card Setup
        HBox budgetDisplayLabels = new HBox(spentBox, spacer, remainingBox);
        budgetDisplayLabels.getStyleClass().add("budgetDisplayLabels");

        VBox cardTwo = new VBox(budgetTitleBox, budgetDisplayBar, budgetDisplayLabels);

        cardTwo.getStyleClass().add("card");


        cardTwo.setOnMouseClicked(_ -> newBudgetDialogue());

        // - = - = - = - = - = - = - = - =
        // Income Card
        // - = - = - = - = - = - = - = - =

        Label incomeLabel   = new Label("Income");
        HBox incomeLabelBox = new HBox(incomeLabel);

        incomeLabel.getStyleClass().add("dataTitle");
        incomeLabelBox.getStyleClass().add("centerBox");



        StringBinding incomeStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions list
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

        // - = - = - = - = - = - = - = - =
        // Expenses Card
        // - = - = - = - = - = - = - = - =

        Label expensesLabel   = new Label("Expenses");
        HBox expensesLabelBox = new HBox(expensesLabel);

        expensesLabel.getStyleClass().add("dataTitle");
        expensesLabelBox.getStyleClass().add("centerBox");


        StringBinding expensesStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions list
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


        // - = - = - = - = - = - = - = - =
        // Planned Expenses Card
        // - = - = - = - = - = - = - = - =

        Label plannedExpensesLabel   = new Label("Planned Expenses");
        HBox plannedExpensesLabelBox = new HBox(plannedExpensesLabel);

        plannedExpensesLabel.getStyleClass().add("dataTitle");
        plannedExpensesLabelBox.getStyleClass().add("centerBox");


        StringBinding plannedExpensesStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions list
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
        plannedExpensesValue.setId("white");
        HBox plannedExpensesValueBox = new HBox(plannedExpensesValue);
        plannedExpensesValueBox.getStyleClass().add("yellowCard");



        // - = - = - = - = - = - = - = - =
        // Net Cash Flow Card
        // - = - = - = - = - = - = - = - =

        Label netCashFlowLabel   = new Label("Net Cash Flow");
        HBox netCashFlowLabelBox = new HBox(netCashFlowLabel);

        netCashFlowLabel.getStyleClass().add("dataTitle");
        netCashFlowLabelBox.getStyleClass().add("centerBox");


        StringBinding netCashflowStringBinding = new StringBinding() {
            {   // Bind to changes in the transactions list
                bind(transactions);
            }

            @Override
            protected String computeValue() {
                Double expenses = getNetCashFlow(transactions);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

                return formatter.format(expenses);
            }
        };


        Label netCashFlowValue = new Label();
        netCashFlowValue.textProperty().bind(netCashflowStringBinding);

        netCashFlowValue.getStyleClass().add("valueLabel");
        netCashFlowValue.setId("white");

        HBox netCashFlowValueBox = new HBox(netCashFlowValue);

        // Conditional Styling for the Net Cash Flow Card:

        if (getNetCashFlow(transactions) >= 0) { netCashFlowValueBox.getStyleClass().add("greenCard"); }
        else { netCashFlowValueBox.getStyleClass().add("redCard"); }

        // For any changes in the string binding, check to see if style needs to be updated
        netCashflowStringBinding.addListener((_) -> {
            netCashFlowValueBox.getStyleClass().clear();

            if (getNetCashFlow(transactions) >= 0) { netCashFlowValueBox.getStyleClass().add("greenCard"); }
            else { netCashFlowValueBox.getStyleClass().add("redCard"); }
        });


        // - = - = - = - = - = - = - = - =
        // Add Transaction Button
        // - = - = - = - = - = - = - = - =

        Button addTransaction = new Button("Add Transaction");
        addTransaction.setPrefSize(screenWidth/4, screenHeight/8);
        addTransaction.getStyleClass().add("transactionButton");

        addTransaction.setOnAction(_ -> newTransactionDialogue());


        HBox buttonsBox = new HBox(addTransaction);
        buttonsBox.getStyleClass().add("buttonsBox");


        // - = - = - = - = - = - = - = - =
        // Grid Pane Setup
        // - = - = - = - = - = - = - = - =

        GridPane gridPane = new GridPane();
        gridPane.setId("root");

        // Column Constraints
        ColumnConstraints col0 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col1 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col2 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col3 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col4 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col5 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col6 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);
        ColumnConstraints col7 = new ColumnConstraints(100, screenWidth/8, screenWidth/8);

        // Row Constraints
        RowConstraints row0 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row1 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row2 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row3 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row4 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row5 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row6 = new RowConstraints(65, screenHeight/8, screenHeight/8);
        RowConstraints row7 = new RowConstraints(65, screenHeight/8, screenHeight/8);

        gridPane.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5, col6, col7);
        gridPane.getRowConstraints().addAll(row0, row1, row2, row3, row4, row5, row6, row7);

        // Add Tab Content to the Grid Pane:

        gridPane.add(cardOne,0, 0, 3, 3);
        gridPane.add(cardTwo,4, 0, 4, 3);

        gridPane.add(incomeLabelBox,0, 4, 2, 1);
        gridPane.add(incomeValueBox,0, 5, 2, 2);

        gridPane.add(expensesLabelBox,2, 4, 2, 1);
        gridPane.add(expensesValueBox,2, 5, 2, 2);

        gridPane.add(plannedExpensesLabelBox,4, 4, 2, 1);
        gridPane.add(plannedExpensesValueBox,4, 5, 2, 2);

        gridPane.add(netCashFlowLabelBox,6, 4, 2, 1);
        gridPane.add(netCashFlowValueBox,6, 5, 2, 2);

        gridPane.add(buttonsBox,3, 7, 2, 1);

        // - = - = - = - = - = - = - = - =
        // Tab Setup
        // - = - = - = - = - = - = - = - =
        Tab dashboard = new Tab("Dashboard");
        dashboard.setClosable(false);

        dashboard.setContent(gridPane);

        return dashboard;
    }

    /**
     * Creates and returns the transactions tab for the application UI.
     * <p>
     * This tab displays a list of the user's transactions including and provides the user
     * with the ability to edit existing transactions.
     *
     * @return the {@link Tab} containing the complete transactions layout and content
     */
    public Tab getTransactionsTab() {
        // - = - = - = - = - = - = - = - =
        // Layout Containers
        // - = - = - = - = - = - = - = - =

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


        // - = - = - = - = - = - = - = - =
        // Header Row
        // - = - = - = - = - = - = - = - =
        HBox header = new HBox(10);

        header.getStyleClass().add("listHeader");

        // Create Header Titles, Set Header Widths
        Label dateHeader     = createHeaderLabel("Date",        150);
        Label descHeader     = createHeaderLabel("Description", 200);
        Label amountHeader   = createHeaderLabel("Amount",      140);
        Label categoryHeader = createHeaderLabel("Category",    200);

        header.getChildren().addAll(dateHeader, descHeader, amountHeader,  categoryHeader);

        // - = - = - = - = - = - = - = - =
        // Transactions Sorted by Date
        // - = - = - = - = - = - = - = - =

        SortedList<Transaction> sortedTransactions = transactions.sorted(Comparator.comparing(Transaction::getDate));

        // - = - = - = - = - = - = - = - =
        // Month Selector
        // - = - = - = - = - = - = - = - =
        SearchableComboBox<Year> yearComboBox = new SearchableComboBox<>();
        yearComboBox.setMaxSize(120, 30);
        yearComboBox.getStyleClass().add("comboBox");

        if (!transactions.isEmpty()) {
            int startYear = sortedTransactions.getFirst().getDate().getYear();
            int endYear = sortedTransactions.getLast().getDate().getYear();

            for (int i = startYear; i <= endYear; i++) {
                yearComboBox.getItems().add(Year.of(i));
            }

            if (!yearComboBox.getItems().isEmpty()){
                yearComboBox.getSelectionModel().selectFirst();
            }
        }

        transactions.addListener((ListChangeListener<Transaction>) _ -> {
            if (!yearComboBox.getItems().isEmpty()){
                yearComboBox.getSelectionModel().clearSelection();
                yearComboBox.getItems().clear();
            }

            if (!transactions.isEmpty()) {
                int startYear = sortedTransactions.getFirst().getDate().getYear();
                int endYear = sortedTransactions.getLast().getDate().getYear();

                for (int i = startYear; i <= endYear; i++) {
                    yearComboBox.getItems().add(Year.of(i));
                }


                if (!yearComboBox.getItems().isEmpty()){
                    yearComboBox.getSelectionModel().selectFirst();
                }
            }

        });

        SearchableComboBox<Month> monthComboBox = new SearchableComboBox<>();
        monthComboBox.setValue(LocalDate.now().getMonth());
        monthComboBox.getStyleClass().add("comboBox");
        monthComboBox.setMaxSize(120, 30);

        monthComboBox.getItems().addAll(Month.values());

        ObservableList<Transaction> displayTransactions = FXCollections.observableArrayList();

        for (Transaction transaction : transactions){
            if (yearComboBox.getValue() != null && monthComboBox.getValue() != null
                    && transaction.getDate().getYear() == yearComboBox.getValue().getValue()
                    && transaction.getDate().getMonth() == monthComboBox.getValue()){
                displayTransactions.add(transaction);
            }
        }


        // - = - = - = - = - = - = - = - =
        // Add Transaction Button
        // - = - = - = - = - = - = - = - =
        Button addTransaction = new Button("Add Transaction");

        addTransaction.setPrefSize(screenWidth/6, screenHeight/12);
        addTransaction.getStyleClass().add("transactionButton");

        addTransaction.setOnAction(_ -> newTransactionDialogue());

        HBox buttonsBox = new HBox(addTransaction, yearComboBox, monthComboBox);
        buttonsBox.getStyleClass().add("buttonsBox");

        // - = - = - = - = - = - = - = - =
        // ListView Setup
        // - = - = - = - = - = - = - = - =
        ListView<Transaction> transactionListView = new ListView<>();
        transactionListView.getStyleClass().add("listView");

        transactionListView.setItems(displayTransactions);

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


                    Label nameLabel     = new Label(transaction.getDescription());
                    nameLabel.getStyleClass().add("standardLabel");
                    Label dateLabel     = new Label(transaction.getDate().toString());
                    dateLabel.getStyleClass().add("standardLabel");
                    Label categoryLabel = new Label(transaction.getCategory().toString());
                    categoryLabel.getStyleClass().add("standardLabel");

                    // Amount Label Styling
                    Label amountLabel   = new Label(transaction.isExpense() ? "-" + formattedAmount : formattedAmount); // if the amount is an expense, put a negative sign in front of it
                    if (transaction.isExpense()){ amountLabel.getStyleClass().add("expense"); } else { amountLabel.getStyleClass().add("standardLabel"); }
                    // Set the Column Widths
                    nameLabel.setPrefWidth(200);
                    amountLabel.setPrefWidth(140);
                    dateLabel.setPrefWidth(150);
                    categoryLabel.setPrefWidth(200);

                    // Assemble Row
                    HBox row = new HBox(10, dateLabel, nameLabel, amountLabel, categoryLabel);
                    setGraphic(row);
                }
            }
        });

        transactions.addListener((ListChangeListener<Transaction>) _ -> {
            displayTransactions.clear();
            transactionListView.getItems().clear();

            if (yearComboBox.getValue() == null && !transactions.isEmpty()) {
                yearComboBox.setValue(Year.of(transactions.getFirst().getDate().getYear())); // will trigger the listener of year
            } else {
                for (Transaction transaction : transactions){
                    if (yearComboBox.getValue() != null && monthComboBox.getValue() != null
                            && transaction.getDate().getYear() == yearComboBox.getValue().getValue()
                            && transaction.getDate().getMonth() == monthComboBox.getValue()){
                        displayTransactions.add(transaction);
                    }
                }
            }
        });

        yearComboBox.valueProperty().addListener((_) -> {
            transactionListView.getItems().clear();
            displayTransactions.clear();

            for (Transaction transaction : transactions){
                if (yearComboBox.getValue() != null && monthComboBox.getValue() != null
                        && transaction.getDate().getYear() == yearComboBox.getValue().getValue()
                        && transaction.getDate().getMonth() == monthComboBox.getValue()){
                    displayTransactions.add(transaction);
                }
            }
        });

        monthComboBox.valueProperty().addListener((_) -> {
            transactionListView.getItems().clear();
            displayTransactions.clear();

            for (Transaction transaction : transactions){
                if (yearComboBox.getValue() != null && monthComboBox.getValue() != null
                        && transaction.getDate().getYear() == yearComboBox.getValue().getValue()
                        && transaction.getDate().getMonth() == monthComboBox.getValue()){
                    displayTransactions.add(transaction);
                }
            }
        });

        // - = - = - = - = - = - = - = - =
        // Transaction Editor
        // - = - = - = - = - = - = - = - =

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

        // - = - = - = - = - = - = - = - =
        // Assemble the UI
        // - = - = - = - = - = - = - = - =
        Tab transactionsTab = new Tab("Transactions");
        transactionsTab.setClosable(false);

        container.getChildren().addAll(header, transactionListView);

        gridView.add(header, 0, 0);
        gridView.add(transactionListView, 0, 1);
        gridView.add(buttonsBox, 0, 2);

        transactionsTab.setContent(gridView);

        return transactionsTab;
    }

    /**
     * Creates and returns the forecast tab for the application UI.
     * <p>
     * This tab displays a graph of the user's spending over the next 12 months.
     * This is represented by a line graph of the user's balance after every transaction.
     *
     * @return the {@link Tab} containing the complete forecast tab layout and content
     */
    public Tab getForecastTab() {
        Tab forecastTab = new Tab("Forecast");
        forecastTab.setClosable(false);

        // Define axes
        final CategoryAxis xAxis = new CategoryAxis(); // JavaFX doesn't support LocalDate directly here
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balance");

        SortedList<Transaction> sortedList = transactions.sorted(Comparator.comparing(Transaction::getDate));

        LocalDate previousDate = null;
        double runningBalance = 0.0;

        for (Transaction transaction : sortedList) {
            LocalDate currentDate = transaction.getDate();

            // If we're still on the same date, just update the balance
            if (currentDate.equals(previousDate)) {
                runningBalance += transaction.isExpense() ? -transaction.getAmount() : transaction.getAmount();
            } else {
                // If it's a new date and not the first iteration, add the previous date's balance
                if (previousDate != null) {
                    series.getData().add(new XYChart.Data<>(previousDate.toString(), runningBalance));
                }

                // Update running balance for the new date
                runningBalance += transaction.isExpense() ? -transaction.getAmount() : transaction.getAmount();
                previousDate = currentDate;
            }
        }

        // Add the final date's balance after loop ends
        if (previousDate != null) {
            series.getData().add(new XYChart.Data<>(previousDate.toString(), runningBalance));
        }

        // Create chart
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), yAxis);
        chart.getData().add(series);
        chart.setTitle("Balance Forecast");


        transactions.addListener((ListChangeListener<Transaction>) _ -> {
            chart.getData().clear();
            series.getData().clear();

            LocalDate previousDate1 = null;
            double runningBalance1 = 0.0;

            for (Transaction transaction : sortedList) {
                LocalDate currentDate = transaction.getDate();

                // If we're still on the same date, just update the balance
                if (currentDate.equals(previousDate1)) {
                    runningBalance1 += transaction.isExpense() ? -transaction.getAmount() : transaction.getAmount();
                } else {
                    // If it's a new date and not the first iteration, add the previous date's balance
                    if (previousDate1 != null) {
                        series.getData().add(new XYChart.Data<>(previousDate1.toString(), runningBalance1));
                    }

                    // Update running balance for the new date
                    runningBalance1 += transaction.isExpense() ? -transaction.getAmount() : transaction.getAmount();
                    previousDate1 = currentDate;
                }
            }

            // Add the final date's balance after loop ends
            if (previousDate1 != null) {
                series.getData().add(new XYChart.Data<>(previousDate1.toString(), runningBalance1));
            }



            chart.getData().add(series);
        });

        forecastTab.setContent(chart);
        return forecastTab;
    }

    /**
     * Creates and returns the recurring transactions tab for the application UI.
     * <p>
     * This tab displays a list of the user's active recurring transactions
     * and provides the user with the ability to edit/delete active recurring transactions.
     *
     * @return the {@link Tab} containing the complete recurring transactions layout and content
     */
    public Tab getRecurringTransactionsTab(){
        Tab recurringTransactionsTab = new Tab("Recurring Transactions");
        recurringTransactionsTab.setClosable(false);

        // - = - = - = - = - = - = - = - =
        // Add Transaction Button
        // - = - = - = - = - = - = - = - =
        Button addTransaction = new Button("Add Recurring Transaction");
        addTransaction.setOnAction(_ -> newRecurringTransactionDialogue());

        addTransaction.getStyleClass().add("transactionButton");
        addTransaction.setPrefSize(screenWidth/4, screenHeight/12);

        HBox buttonsBox = new HBox(addTransaction);
        buttonsBox.getStyleClass().add("buttonsBox");

        // - = - = - = - = - = - = - = - =
        // Create Layout Containers
        // - = - = - = - = - = - = - = - =
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


        // - = - = - = - = - = - = - = - =
        // Header Row
        // - = - = - = - = - = - = - = - =
        HBox header = new HBox(10);
        header.getStyleClass().add("listHeader");

        Label descHeader      = createHeaderLabel("Description", 120);
        Label amountHeader    = createHeaderLabel("Amount",      110);
        Label categoryHeader  = createHeaderLabel("Category",    110);
        Label repeatFrequency = createHeaderLabel("Frequency",   110);
        Label startDateHeader = createHeaderLabel("Start Date",  110);
        Label endDateHeader   = createHeaderLabel("End Date",    110);

        header.getChildren().addAll(descHeader, amountHeader,  categoryHeader, repeatFrequency, startDateHeader, endDateHeader);

        // - = - = - = - = - = - = - = - =
        // ListView Setup
        // - = - = - = - = - = - = - = - =
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

                    Label nameLabel      = new Label(transaction.getDescription());
                    nameLabel.getStyleClass().add("standardLabel");
                    Label startDateLabel = new Label(transaction.getStartDate().toString());
                    startDateLabel.getStyleClass().add("standardLabel");
                    Label categoryLabel  = new Label(transaction.getCategory().toString());
                    categoryLabel.getStyleClass().add("standardLabel");
                    Label frequencyLabel = new Label((transaction.getFrequency().toString()));
                    frequencyLabel.getStyleClass().add("standardLabel");

                    // End Date Label Setup
                    Label endDateLabel = new Label();
                    if (transaction.getEndDate() != null){ endDateLabel.setText(transaction.getEndDate().toString()); }

                    // Amount Label Setup & Conditional Styling
                    Label amountLabel = new Label(transaction.isExpense() ? "-" + formattedAmount : formattedAmount);

                    if (transaction.isExpense()){ amountLabel.getStyleClass().add("expense"); } else {amountLabel.getStyleClass().add("standardLabel");}

                    // Column Width
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

        // - = - = - = - = - = - = - = - =
        // Transaction Editor
        // - = - = - = - = - = - = - = - =

        VBox transactionEditor = new VBox();

        recurringTransactionListView.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                gridView.getChildren().remove(transactionEditor);

                transactionEditor.getChildren().setAll(getRecurringTransactionEditor(newSelection, recurringTransactionListView));
                gridView.add(transactionEditor, 1, 0, 1, 2);
            }

        });

        // - = - = - = - = - = - = - = - =
        // Assemble the UI
        // - = - = - = - = - = - = - = - =
        container.getChildren().addAll(header, recurringTransactionListView);


        gridView.add(header, 0, 0);
        gridView.add(recurringTransactionListView, 0, 1);
        gridView.add(buttonsBox, 0, 2);

        recurringTransactionsTab.setContent(gridView);

        return recurringTransactionsTab;
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                     EDITOR SETUP METHODS                     \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\

    /**
     * Builds and returns a styled editor pane (as a {@link VBox}) for modifying or deleting an existing {@link Transaction}.
     * <p>
     * This editor includes form fields for transaction type, date, description, category, and amount, as well as
     * buttons to confirm changes or delete the transaction. Changes are applied directly to the provided transaction object.
     *
     * @param transaction the {@link Transaction} to be edited
     * @param listView the {@link ListView} containing all transactions, used to refresh the UI after changes
     * @return a {@link VBox} node containing the editor interface
     */
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
        typeLabelBox.getStyleClass().add("leftBox");

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
        toggleButtonsBox.getStyleClass().add("rightBox");

        // === Form Fields ===
        DatePicker datePicker = new DatePicker(transaction.getDate());

        TextField descriptionField = new TextField(transaction.getDescription());

        SearchableComboBox<Category> categories = new SearchableComboBox<>();
        categories.setEditable(true);
        categories.setValue(transaction.getCategory());
        categories.getItems().addAll(Category.values());
        categories.setMaxSize(screenWidth / 5, screenHeight / 19);
        categories.getStyleClass().add("centerBox");

        Spinner<Double> amountSpinner = new Spinner<>(0, Double.MAX_VALUE - 1, transaction.getAmount(), 0.01);
        amountSpinner.setEditable(true);




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

            Database.updateTransactionsDatabase(transactions);

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

                Database.updateTransactionsDatabase(transactions);

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

    /**
     * Builds and returns a styled editor interface for modifying or deleting a {@link RecurringTransaction}.
     * <p>
     * The editor allows users to change the transaction's type (income/expense), start and end dates, description,
     * category, and amount. Users can also delete the recurring transaction, with options to remove all occurrences
     * or only future ones.
     * <p>
     * The method also ensures:
     * <ul>
     *     <li>Transaction toggle buttons can't be deselected</li>
     *     <li>End date validation prevents a start date after the end date</li>
     *     <li>ListView is refreshed to reflect updates</li>
     *     <li>Changes are persisted via {@code TransactionsDatabase}</li>
     * </ul>
     *
     * @param transaction the {@link RecurringTransaction} to edit
     * @param listView the {@link ListView} containing all recurring transactions (used for UI refresh)
     * @return a {@link VBox} node containing the editing form and controls
     */
    private VBox getRecurringTransactionEditor(RecurringTransaction transaction, ListView<RecurringTransaction> listView){
        GridPane gridPane = new GridPane();
        gridPane.setId("transactionRoot");

        for (int i = 0; i < 7; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(screenHeight*(0.8/6)));
        }
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(screenWidth * 0.15),
                new ColumnConstraints(screenWidth * 0.15)
        );

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("leftBox");

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

        SearchableComboBox<Category> categories = new SearchableComboBox<>();
        categories.setEditable(true);
        categories.setValue(transaction.getCategory());
        categories.getItems().addAll(Category.values());
        categories.setMaxSize(screenWidth / 5, screenHeight /19);
        categories.getStyleClass().add("centerBox");

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

                Database.updateRecurringTransactionsDatabase(recurringTransactions);
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

                if (!transactions.isEmpty()){
                    transactions.removeIf(transaction1 -> transaction1.getRecurringId().intValue() == transaction.getId().intValue());
                }

                recurringTransactions.remove(transaction);


                Database.updateRecurringTransactionsDatabase(recurringTransactions);
                Database.updateTransactionsDatabase(transactions);


                gridPane.getChildren().clear();
            } else if (result.isPresent() && result.get() == futureButton){
                transactions.removeIf(transaction1 -> (transaction1.getRecurringId().intValue() == transaction.getId().intValue()) &&
                        transaction1.getDate().isAfter(LocalDate.now()));


                Database.updateRecurringTransactionsDatabase(recurringTransactions);
                Database.updateTransactionsDatabase(transactions);

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

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                  CARD SETUP HELPER METHODS                   \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\

    /**
     * Calculates the total income from a list of transactions for the current month.
     * <p>
     * This method filters transactions to include only those marked as income
     * (i.e., not expenses) and whose date falls within the current month.
     *
     * @param transactions the list of all {@link Transaction} objects to evaluate
     * @return the total income as a {@link Double} for the current month
     */
    private Double getIncome(ObservableList<Transaction> transactions) {
        Double monthlyIncome = 0.0;

        for (Transaction transaction : transactions){
            if (!transaction.isExpense() && transaction.getDate().getMonth().equals(LocalDate.now().getMonth())) {
                monthlyIncome += transaction.getAmount();
            }
        }

        return monthlyIncome;
    }

    /**
     * Calculates the total expenses from a list of transactions for the current month to date.
     * <p>
     * This method filters transactions to include only those marked as expenses
     * (i.e., not income) and whose date falls within the current month and before (or equal)
     * to the current date.
     *
     * @param transactions the list of all {@link Transaction} objects to evaluate
     * @return the total expenses as a {@link Double} for the current month to date
     */
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

    /**
     * Calculates the total planned expenses from a list of transactions for the current month.
     * <p>
     * This method filters transactions to include only those marked as expenses
     * (i.e., not income) and whose date falls within the current month and after the current date.
     *
     * @param transactions the list of all {@link Transaction} objects to evaluate
     * @return the total planned expenses as a {@link Double} for the current month
     */
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

    /**
     * Calculates the net cash flow for the current month.
     * <p>
     * Net cash flow is computed as the total income minus both actual and planned expenses
     * for the current month.
     *
     * @param transactions the list of all {@link Transaction} objects to evaluate
     * @return the net cash flow as a {@link Double} for the current month
     */
    private Double getNetCashFlow(ObservableList<Transaction> transactions){
        return getIncome(transactions) - (getExpenses(transactions) + getPlannedExpenses(transactions));
    }

    /**
     * Calculates the current balance based on the provided list of transactions.
     * <p>
     * Transactions dated today or earlier are included in the calculation.
     * Expenses decrease the balance, while income increases it.
     *
     * @param transactions the list of {@link Transaction} objects to evaluate
     * @return the computed balance as a {@code double}
     */
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

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                        DIALOGUE METHODS                      \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\

    /**
     * Launches a modal dialog allowing the user to create a new {@link Transaction}.
     * <p>
     * The form includes fields for transaction type (expense/income), date, description, category, and amount.
     * Upon confirmation and validation, the transaction is added to the observable transaction list
     * and persisted to the {@link Database}.
     * <p>
     * Displays error alerts if any required fields are empty or invalid.
     */
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
        Scene scene = setupScene(gridPane);
        dialog.setScene(scene);

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("leftBox");

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

        // === Category ComboBox ===
        SearchableComboBox<Category> categories = new SearchableComboBox<>();
        categories.setEditable(true);
        categories.getItems().addAll(Category.values());
        categories.setMaxSize(screenWidth / 5, screenHeight / 19);
        categories.getStyleClass().add("centerBox");
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
            boolean isExpense = toggleGroup.getSelectedToggle() == expenseButton;

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
                Database.addTransaction(newTransaction);
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

    /**
     * Launches a modal dialog allowing the user to create a new {@link RecurringTransaction}.
     * <p>
     * The form includes fields for transaction type (expense/income), start date, end date, description, frequency,
     * category, and amount. Upon confirmation and validation, the transaction is added to the observable transaction list
     * and persisted to the {@link Database}.
     * <p>
     * Displays error alerts if any required fields are empty or invalid.
     */
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
        Scene scene = setupScene(gridPane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(getActiveStylesheet())).toExternalForm());
        dialog.setScene(scene);

        // === Transaction Type Toggle ===
        Label typeLabel = new Label("Transaction Type:");
        typeLabel.getStyleClass().add("standardLabel");

        HBox typeLabelBox = new HBox(typeLabel);
        typeLabelBox.getStyleClass().add("leftBox");

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

        // === Category ComboBox ===
        SearchableComboBox<Category> categories = new SearchableComboBox<>();
        categories.setEditable(true);
        categories.getItems().addAll(Category.values());
        categories.setMaxSize(screenWidth / 5, screenHeight /19);
        categories.getStyleClass().add("centerBox");
        addLabeledField(gridPane, "Category:", categories, 4);

        // === Frequency ComboBox ===
        ComboBox<Frequency> frequencyComboBox = new ComboBox<>();
        frequencyComboBox.getItems().addAll(Frequency.values());
        frequencyComboBox.setPrefWidth(screenWidth/5);
        frequencyComboBox.getStyleClass().addAll("comboBox");
        addLabeledField(gridPane, "Frequency:", frequencyComboBox, 5);

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
            Frequency frequency = frequencyComboBox.getValue();

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
                Database.addRecurringTransaction(newTransaction);

      // Only generate future transactions for the new one!
                List<Transaction> future = Database.generateFutureTransactions(
                        List.of(newTransaction), LocalDate.now(), LocalDate.now().plusYears(1)
                );
                transactions.addAll(future);

                Database.updateTransactionsDatabase(transactions);
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

    /**
     * Launches a modal dialog allowing the user to edit the budget limit for that month.
     */
    public void newBudgetDialogue(){
        Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setTitle("Budget Limit");


        BorderPane borderPane = new BorderPane();
        borderPane.setId("root");
        borderPane.setMinSize(420, 120);
        borderPane.setPrefSize(420, 180);

        Label budgetLimitLabel = new Label("Budget Limit: ");
        budgetLimitLabel.getStyleClass().add("standardLabel");

        Region spacer = new Region();
        spacer.setMinWidth(30);
        spacer.setMaxWidth(100);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Spinner<Double> limitSpinner;

        if (budgetLimit.doubleValue() != -1.0){
            limitSpinner = new Spinner<>(0.0, Double.MAX_VALUE, budgetLimit.doubleValue(), 0.01);
        } else {
            limitSpinner = new Spinner<>(0.0, Double.MAX_VALUE, 0.0, 0.01);
        }

        limitSpinner.setEditable(true);
        limitSpinner.getStyleClass().addAll("entryField");

        HBox limitBox = new HBox(budgetLimitLabel, spacer, limitSpinner);
        limitBox.getStyleClass().add("centerBox");

        // === Buttons ===
        Button confirm = new Button("Confirm");
        confirm.getStyleClass().addAll("button", "confirm");

        confirm.setOnAction(_ -> {
            if (budgetLimit.doubleValue() == -1.0){
                Database.addBudget(limitSpinner.getValue(), LocalDate.now().getYear(), LocalDate.now().getMonthValue());
            } else {
                Database.editBudgetLimit(limitSpinner.getValue(), LocalDate.now().getYear(), LocalDate.now().getMonthValue());
            }
            budgetLimit.set(limitSpinner.getValue());

            Transaction temp = new Transaction(Category.Groceries, LocalDate.now(), "Temp", 0.0, true );

            transactions.addLast(temp);
            transactions.removeLast();

            newStage.close();
        });

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().addAll("button", "cancel");

        cancel.setOnAction(_ -> newStage.close());

        HBox buttonsBox = new HBox(confirm, cancel);
        buttonsBox.getStyleClass().add("buttonsBox");

        // === vSpacer ===
        Region vSpacer = new Region();
        vSpacer.setMinHeight(30);
        vSpacer.setMaxHeight(70);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        // === UI Setup ===
        VBox UIBox = new VBox(limitBox, vSpacer, buttonsBox);

        borderPane.setCenter(UIBox);

        Scene scene = setupScene(borderPane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(getActiveStylesheet())).toExternalForm());

        newStage.setScene(scene);
        newStage.show();
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                   DIALOGUE HELPER METHODS                    \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
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
        labelBox.getStyleClass().add("leftBox");

        HBox inputBox = new HBox(input);
        inputBox.getStyleClass().add("centerBox");

        if (input instanceof Control) {
            ((Control) input).setPrefWidth(screenWidth / 5);
        }

        grid.add(labelBox, 0, rowIndex);
        grid.add(inputBox, 1, rowIndex);
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    //                     OTHER HELPER METHODS                     \\
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- \\
    private void getErrorAlert(String contentText, String title){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(contentText);
        alert.setTitle(title);

        alert.show();
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.getStyleClass().addAll("bold", "standardLabel");
        return label;
    }

}
