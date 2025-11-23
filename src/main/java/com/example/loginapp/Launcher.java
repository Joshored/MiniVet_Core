package com.example.loginapp;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        //
        System.out.println("Inicializando base de datos...");

        Application.launch(HelloApplication.class, args);
    }
}