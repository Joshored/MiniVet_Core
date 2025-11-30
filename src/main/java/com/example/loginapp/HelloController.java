package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @FXML private TextField usuarioCampoTxt;
    @FXML private PasswordField contrasenaCampoTxt;
    @FXML private TextField contrasenaVisibleTxt;
    @FXML private CheckBox mostrarContrasenaCheckBox;
    @FXML private Label errorTexto;
    @FXML private Button entrarBoton;
    @FXML private Button crearCuentaBoton;
    @FXML private Button olvideContrasenaBoton;

    @FXML
    public void initialize() {
        // Inicializar la visibilidad de la contraseña
        contrasenaVisibleTxt.setVisible(false);
        contrasenaCampoTxt.setVisible(true);
        mostrarContrasenaCheckBox.setSelected(false);
        contrasenaCampoTxt.textProperty().bindBidirectional(contrasenaVisibleTxt.textProperty());

        // Configurar eventos
        contrasenaCampoTxt.textProperty().bindBidirectional(contrasenaVisibleTxt.textProperty());
        if (entrarBoton != null) entrarBoton.setOnAction(e -> entrarBotonOnAction());
        if (crearCuentaBoton != null) crearCuentaBoton.setOnAction(e -> crearCuentaBotonOnAction());
        if (olvideContrasenaBoton != null) olvideContrasenaBoton.setOnAction(e -> olvideContrasenaBotonOnAction());
        if (mostrarContrasenaCheckBox != null) mostrarContrasenaCheckBox.setOnAction(e -> toggleVisibilidadContrasena());
    }

    @FXML
    public void entrarBotonOnAction() {
        String usuario = usuarioCampoTxt.getText();
        String contrasena = contrasenaCampoTxt.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            errorTexto.setText("Por favor complete todos los campos");
            return;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            if (usuarioDAO.validarUsuario(usuario, contrasena)) {
                logger.info("Usuario autenticado: {}", usuario);

                // Cargar dashboard
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Dashboard.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) entrarBoton.getScene().getWindow();
                stage.setTitle("MiniVet - Dashboard");
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.centerOnScreen();

            } else {
                errorTexto.setText("Usuario o contraseña incorrectos");
                logger.warn("Intento de login fallido para usuario: {}", usuario);
            }
        } catch (Exception e) {
            logger.error("Error durante el login", e);
            errorTexto.setText("Error del sistema: " + e.getMessage());
        }
    }

    @FXML
    public void crearCuentaBotonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del registro y pasar el stage actual (login)
            RegistroController registroController = loader.getController();
            registroController.setStageLogin((Stage) crearCuentaBoton.getScene().getWindow());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Registro - MiniVet");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            logger.error("Error abriendo ventana de registro", e);
            errorTexto.setText("Error al abrir registro");
        }
    }

    @FXML
    public void olvideContrasenaBotonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("recuperacion-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Recuperar Contraseña - MiniVet");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            logger.error("Error abriendo ventana de recuperación", e);
            errorTexto.setText("Error al abrir recuperación de contraseña");
        }
    }

    @FXML
    public void toggleVisibilidadContrasena() {
        boolean mostrar = mostrarContrasenaCheckBox.isSelected();

        if (mostrar) {
            // Mostramos el texto plano y ocultamos el password field
            contrasenaCampoTxt.setVisible(false);
            contrasenaVisibleTxt.setVisible(true);
        } else {
            // Ocultamos el texto plano y mostramos el password field
            contrasenaVisibleTxt.setVisible(false);
            contrasenaCampoTxt.setVisible(true);
        }
    }
}