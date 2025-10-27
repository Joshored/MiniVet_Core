package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class RegistroController {

    @FXML
    private TextField ApellidoPaterno;

    @FXML
    private TextField ApellidoMaterno;

    @FXML
    private TextField NombreCliente;

    @FXML
    private TextField Dia;

    @FXML
    private ComboBox<String> Mes;

    @FXML
    private TextField Ano;

    @FXML
    private TextField NumeroTel;

    @FXML
    private TextField eMail;

    @FXML
    private TextField Calle;

    @FXML
    private TextField numCalle;

    @FXML
    private TextField Colonia;

    @FXML
    private TextField username;

    @FXML
    private PasswordField contrasena;

    @FXML
    private PasswordField contrasenaConfirmacion;

    @FXML
    private Button GuardarRegistro;

    @FXML
    private Label MensajeAvisoRegistro;

    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        // Inicializar el ComboBox de meses
        Mes.getItems().addAll(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
    }

    public void setModoEdicion(boolean modo) {
        this.modoEdicion = modo;
    }

    @FXML
    public void guardarRegistroOnAction(ActionEvent event) {
        // Validar que los campos no estén vacíos
        if (!validarCampos()) {
            return;
        }

        // Validar que las contraseñas coincidan
        if (!contrasena.getText().equals(contrasenaConfirmacion.getText())) {
            MensajeAvisoRegistro.setText("Las contraseñas no coinciden");
            return;
        }

        // Aquí guardarías los datos
        MensajeAvisoRegistro.setStyle("-fx-text-fill: green;");
        MensajeAvisoRegistro.setText("Cliente guardado exitosamente");

        // Cerrar ventana después de 2 segundos (opcional)
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> cerrarVentana());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean validarCampos() {
        if (ApellidoPaterno.getText().isEmpty() ||
                NombreCliente.getText().isEmpty() ||
                NumeroTel.getText().isEmpty() ||
                username.getText().isEmpty() ||
                contrasena.getText().isEmpty()) {

            MensajeAvisoRegistro.setText("Complete los campos obligatorios");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) GuardarRegistro.getScene().getWindow();
        stage.close();
    }
}