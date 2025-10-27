package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class HelloController {
    @FXML
    private TextField usuarioCampoTxt;

    @FXML
    private PasswordField contrasenaCampoTxt;

    @FXML
    private Button entrarBoton;

    @FXML
    private Button crearCuentaBoton;

    @FXML
    private Label errorTexto;

    @FXML
    public void entrarBotonOnAction(ActionEvent event) {
        String usuario = usuarioCampoTxt.getText();
        String contrasena = contrasenaCampoTxt.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            errorTexto.setText("Complete todos los campos");
            return;
        }

        // Por ahora, cualquier usuario/contraseña es válido
        boolean loginValido = true;

        if (loginValido) {
            abrirListaClientes();
        } else {
            errorTexto.setText("Contraseña incorrecta o Usuario incorrecto");
        }
    }

    @FXML
    public void crearCuentaBotonOnAction(ActionEvent event) {
        abrirRegistro();
    }

    private void abrirListaClientes() {
        try {
            // Obtener el Stage actual
            Stage stageActual = (Stage) entrarBoton.getScene().getWindow();

            // Cargar la nueva vista
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaClientes.fxml"));
            Parent root = loader.load();

            // Cambiar la escena en el mismo Stage
            stageActual.setTitle("Lista de Clientes");
            stageActual.setScene(new Scene(root));
            stageActual.centerOnScreen();

        } catch (IOException e) {
            errorTexto.setText("Error al abrir lista de Clientes");
            e.printStackTrace();
        }
    }

    private void abrirRegistro() {
        try {
            // Obtener el Stage actual
            Stage stageActual = (Stage) crearCuentaBoton.getScene().getWindow();

            // Cargar la nueva vista
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            // Cambiar la escena en el mismo Stage
            stageActual.setTitle("Registro de Cliente");
            stageActual.setScene(new Scene(root));
            stageActual.centerOnScreen();

        } catch (IOException e) {
            errorTexto.setText("Error al abrir registro");
            e.printStackTrace();
        }
    }
}