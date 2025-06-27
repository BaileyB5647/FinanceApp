package com.example.financeapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class FinanceApp extends Application {

    private final double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private final double screenHeight = Screen.getPrimary().getBounds().getHeight();

    @Override
    public void start(Stage stage) throws IOException {
        AppController appController = new AppController();


        TabPane root = new TabPane();
        Scene scene = new Scene(root);

        root.setPrefWidth(screenWidth);
        root.setPrefHeight(screenHeight);

        Tab dashboard = appController.getDashboardTab();
        Tab forecast = appController.getForecastTab();
        Tab transactions = appController.getTransactionsTab();


        root.getTabs().addAll(dashboard, forecast, transactions);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("lightMode.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}