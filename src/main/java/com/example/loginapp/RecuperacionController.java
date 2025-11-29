package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecuperacionController {
    private static final Logger logger = LoggerFactory.getLogger(RecuperacionController.class);

    @FXML private VBox paso1Container;
    @FXML private VBox paso2Container;
    @FXML private TextField usuarioField;
    @FXML private PasswordField nuevaContrasenaField;
    @FXML private PasswordField confirmarContrasenaField;
    @FXML private Label mensajePaso1;
    @FXML private Label mensajePaso2;
    @FXML private Button validarUsuarioButton;
    @FXML private Button cambiarContrasenaButton;
    @FXML private Button cancelarButton;

    private String usuarioValidado;

    @FXML
    public void initialize() {
        // Configurar visibilidad inicial
        paso1Container.setVisible(true);
        paso2Container.setVisible(false);

        // Configurar eventos
        if (validarUsuarioButton != null) validarUsuarioButton.setOnAction(e -> validarUsuario());
        if (cambiarContrasenaButton != null) cambiarContrasenaButton.setOnAction(e -> cambiarContrasena());
        if (cancelarButton != null) cancelarButton.setOnAction(e -> cancelar());
    }

    @FXML
    public void validarUsuario() {
        String usuario = usuarioField.getText().trim();

        if (usuario.isEmpty()) {
            mostrarMensajePaso1("Por favor ingresa tu nombre de usuario", false);
            return;
        }

        try {
            // Verificar si el usuario existe
            if (!usuarioExiste(usuario)) {
                mostrarMensajePaso1("El usuario no existe en el sistema", false);
                return;
            }

            // Usuario válido, avanzar al paso 2
            usuarioValidado = usuario;
            mostrarMensajePaso1("Usuario verificado correctamente", true);

            // Cambiar a paso 2
            paso1Container.setVisible(false);
            paso2Container.setVisible(true);

            logger.info("Usuario validado para recuperación: {}", usuario);

        } catch (Exception e) {
            logger.error("Error validando usuario: {}", usuario, e);
            mostrarMensajePaso1("Error del sistema: " + e.getMessage(), false);
        }
    }

    @FXML
    public void cambiarContrasena() {
        if (usuarioValidado == null) {
            mostrarMensajePaso2("Error: Usuario no validado", false);
            return;
        }

        String nuevaContrasena = nuevaContrasenaField.getText();
        String confirmarContrasena = confirmarContrasenaField.getText();

        // Validaciones
        if (nuevaContrasena.isEmpty()) {
            mostrarMensajePaso2("Por favor ingresa la nueva contraseña", false);
            return;
        }

        if (confirmarContrasena.isEmpty()) {
            mostrarMensajePaso2("Por favor confirma la nueva contraseña", false);
            return;
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            mostrarMensajePaso2("Las contraseñas no coinciden", false);
            return;
        }

        if (nuevaContrasena.length() < 4) {
            mostrarMensajePaso2("La contraseña debe tener al menos 4 caracteres", false);
            return;
        }

        try {
            // Actualizar la contraseña en la base de datos
            if (actualizarContrasena(usuarioValidado, nuevaContrasena)) {
                mostrarExito();
                logger.info("Contraseña actualizada para usuario: {}", usuarioValidado);
            } else {
                mostrarMensajePaso2("Error al actualizar la contraseña", false);
            }

        } catch (Exception e) {
            logger.error("Error cambiando contraseña para usuario: {}", usuarioValidado, e);
            mostrarMensajePaso2("Error del sistema: " + e.getMessage(), false);
        }
    }

    private boolean usuarioExiste(String usuario) {
        String sql = "SELECT COUNT(*) as count FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt("count") > 0;

        } catch (SQLException e) {
            logger.error("Error verificando existencia de usuario: {}", usuario, e);
            return false;
        }
    }

    private boolean actualizarContrasena(String usuario, String nuevaContrasena) {
        String sql = "UPDATE usuarios SET password = ? WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevaContrasena);
            pstmt.setString(2, usuario);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error actualizando contraseña para usuario: {}", usuario, e);
            return false;
        }
    }

    private void mostrarExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contraseña Actualizada");
        alert.setHeaderText("Contraseña actualizada exitosamente");
        alert.setContentText("Tu contraseña ha sido cambiada. Ahora puedes iniciar sesión con tu nueva contraseña.");
        alert.showAndWait();

        // Cerrar ventana después de mostrar el mensaje
        cerrarVentana();
    }

    @FXML
    public void cancelar() {
        cerrarVentana();
    }

    private void mostrarMensajePaso1(String mensaje, boolean esExito) {
        mensajePaso1.setText(mensaje);
        mensajePaso1.setStyle(esExito ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void mostrarMensajePaso2(String mensaje, boolean esExito) {
        mensajePaso2.setText(mensaje);
        mensajePaso2.setStyle(esExito ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void cerrarVentana() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }
}