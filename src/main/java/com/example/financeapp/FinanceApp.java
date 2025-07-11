package com.example.financeapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class FinanceApp extends Application {
    private final double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight();

    @Override
    public void start(Stage stage) {
        Database.init();

        AppController appController = new AppController();
        stage.setTitle("Finance App");

        appController.initialiseApp();

        TabPane tabPane = new TabPane();
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setTop(appController.getMenuBar());


        root.setPrefWidth(screenWidth);
        root.setPrefHeight(screenHeight);

        Tab dashboard = appController.getDashboardTab();
        Tab forecast = appController.getForecastTab();
        Tab transactions = appController.getTransactionsTab();
        Tab recurringTransactions = appController.getRecurringTransactionsTab();

        stage.setOnCloseRequest(_ -> Database.updateTransactionsDatabase(appController.transactions));

        tabPane.getTabs().addAll(dashboard, forecast, transactions, recurringTransactions);

        Scene scene = appController.setupScene(root);

        stage.setOnCloseRequest(_ -> ThemeManager.saveTheme(appController.getActiveStylesheet()));

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}