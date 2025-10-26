package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.awt.event.ActionEvent;

public class HelloController {
    @FXML
    private Button entrarBoton;

    @FXML
    public void entrarBotonOnAction() {
        entrarBoton.setText("Entrar");
    }
    @FXML
    private Label errorTexto;

    private void entrarBotonOnAction(ActionEvent e) {
        errorTexto.setText("Estas logeandote");
    }

   }
