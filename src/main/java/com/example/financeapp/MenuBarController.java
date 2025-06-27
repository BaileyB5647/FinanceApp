package com.example.financeapp;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MenuBarController {

    public static void getAppearanceEditor(){
        Stage newStage = new Stage();
        newStage.setTitle("Appearance Editor");

        BorderPane root = new BorderPane();
        root.setPrefSize(150, 150);

        Scene scene = new Scene(root);
        newStage.setScene(scene);
        newStage.show();
    }

}
