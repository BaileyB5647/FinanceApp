package com.example.financeapp;

public enum Theme {
    LIGHT_MODE,
    DARK_MODE;

    @Override
    public String toString() {
        String out = "";

        switch (this.name()) {
            case "LIGHT_MODE" -> out = "Light Mode";
            case "DARK_MODE" -> out = "Dark Mode";
        }

        return out;
    }

    public static Theme getTheme(String cssName){

        switch (cssName){
            case "lightMode.css" -> {
                return LIGHT_MODE;
            }
            case "darkMode.css" -> {
                return DARK_MODE;
            }
        }

        throw new IllegalArgumentException("Unknown theme CSS name: " + cssName);
    }


    public String getSheetName(){
        String out = "";

        switch (this.name()){
            case "LIGHT_MODE" -> out = "lightMode.css";
            case "DARK_MODE" -> out = "darkMode.css";
        }

        return out;
    }
}
