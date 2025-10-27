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

        // Validación simple - puedes mejorarla después
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
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaClientes.fxml"));
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

    private void abrirRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            Stage stageRegistro = new Stage();
            stageRegistro.setTitle("Registro de Cliente");
            stageRegistro.setScene(new Scene(root));
            stageRegistro.show();

            Stage stageLogin = (Stage) crearCuentaBoton.getScene().getWindow();
            stageLogin.close();
        } catch (IOException e) {
            errorTexto.setText("Error al abrir registro");
            e.printStackTrace();
        }
    }
}