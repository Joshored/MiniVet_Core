package com.example.loginapp;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Inicializar la base de datos antes de lanzar la aplicación
        System.out.println("Inicializando base de datos...");

        // Esta línea inicializará la base de datos automáticamente
        // a través del bloque static en DatabaseConfigtg

        Application.launch(HelloApplication.class, args);
    }
}