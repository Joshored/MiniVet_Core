package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class HelloController {
    @FXML
    private Button entrarBoton;

    @FXML
    private Label errorTexto;

    @FXML
    public void entrarBotonOnAction(ActionEvent event) {
        boolean loginValido = true;
        if (loginValido) {
            abrirListaClientes();
        } else {
            errorTexto.setText("Contraseña incorrecta o Usuario incorrecto");
        }
    }

    private void abrirListaClientes() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("listaClientes.fxml"));
            Parent root = loader.load();

            Stage stageTabla = new Stage();
            stageTabla.setTitle("Lista de Clientes");
            stageTabla.setScene(new Scene(root));
            stageTabla.show();

            Stage stageLogin = (Stage) entrarBoton.getScene().getWindow();
            stageLogin.close();
        } catch (IOException e) {
            errorTexto.setText("Error al abrir lista de Clientes");
            e.printStackTrace();
        }
    }
}