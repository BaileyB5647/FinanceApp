module com.example.financeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.financeapp to javafx.fxml;
    exports com.example.financeapp;
}