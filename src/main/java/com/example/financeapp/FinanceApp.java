package com.example.financeapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.Objects;

public class FinanceApp extends Application {
    private final double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight();

    @Override
    public void start(Stage stage) {
        AppController appController = new AppController();

        TransactionsDatabase.init();
        appController.initialiseApp();

        TabPane tabPane = new TabPane();
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setTop(appController.getMenuBar());

        Scene scene = new Scene(root);

        root.setPrefWidth(screenWidth);
        root.setPrefHeight(screenHeight);

        Tab dashboard = appController.getDashboardTab();
        Tab forecast = appController.getForecastTab();
        Tab transactions = appController.getTransactionsTab();
        Tab recurringTransactions = appController.getRecurringTransactionsTab();
        Tab accounts = appController.getAccountsTab();

        stage.setOnCloseRequest(_ -> TransactionsDatabase.updateTransactionsDatabase(appController.transactions));

        tabPane.getTabs().addAll(dashboard, forecast, transactions, recurringTransactions, accounts);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("lightMode.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}