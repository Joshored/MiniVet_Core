package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import javafx.scene.control.Alert;

public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @FXML private TextField usuarioCampoTxt;
    @FXML private PasswordField contrasenaCampoTxt;
    @FXML private Button entrarBoton;
    @FXML private Button crearCuentaBoton;
    @FXML private Label errorTexto;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando controlador de login");

        // Verificar conexión a la base de datos al inicio
        if (!usuarioDAO.verificarConexionBD()) {
            errorTexto.setText("Error de conexión a BD. Usando modo de emergencia.");
            logger.warn("Modo de emergencia activado - BD no disponible");
        } else {
            logger.info("Conexión a BD verificada correctamente");
        }
    }

    @FXML
    public void entrarBotonOnAction(ActionEvent event) {
        String usuario = usuarioCampoTxt.getText();
        String contrasena = contrasenaCampoTxt.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            errorTexto.setText("Complete todos los campos");
            return;
        }

        try {
            // Validar usuario en la base de datos
            boolean loginValido = usuarioDAO.validarUsuario(usuario, contrasena);

            if (loginValido) {
                logger.info("Login exitoso para usuario: {}", usuario);
                abrirListaClientes();
            } else {
                logger.warn("Intento de login fallido para usuario: {}", usuario);
                errorTexto.setText("Contraseña incorrecta o Usuario incorrecto");
            }
        } catch (Exception e) {
            logger.error("Error durante el login", e);
            // Modo de emergencia: permitir acceso con credenciales por defecto
            if ("admin".equals(usuario) && "admin123".equals(contrasena)) {
                logger.warn("Acceso de emergencia concedido con credenciales por defecto");
                abrirListaClientes();
            } else {
                errorTexto.setText("Error del sistema. Intente con admin/admin123");
            }
        }
    }

    @FXML
    public void crearCuentaBotonOnAction(ActionEvent event) {
        abrirRegistro();
    }

    private void abrirListaClientes() {
        try {
            Stage stageActual = (Stage) entrarBoton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaClientes.fxml"));
            Parent root = loader.load();

            // Verificar que el controlador se cargó correctamente
            ListaClientesController controller = loader.getController();
            if (controller == null) {
                throw new IOException("No se pudo cargar el controlador ListaClientesController");
            }

            stageActual.setTitle("Lista de Clientes - MiniVet");
            stageActual.setScene(new Scene(root));
            stageActual.centerOnScreen();

            logger.info("Ventana de lista de clientes abierta exitosamente");

        } catch (IOException e) {
            logger.error("Error al abrir lista de Clientes", e);
            errorTexto.setText("Error al abrir lista de Clientes: " + e.getMessage());

            // Mostrar alerta de error
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No se pudo abrir la ventana");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
            });
        } catch (Exception e) {
            logger.error("Error inesperado al abrir lista de clientes", e);
            errorTexto.setText("Error inesperado: " + e.getMessage());
        }
    }

    private void abrirRegistro() {
        try {
            Stage stageActual = (Stage) crearCuentaBoton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();
            stageActual.setTitle("Registro de Cliente - MiniVet");
            stageActual.setScene(new Scene(root));
            stageActual.centerOnScreen();
            logger.info("Ventana de registro abierta exitosamente");
        } catch (IOException e) {
            logger.error("Error al abrir registro", e);
            errorTexto.setText("Error al abrir registro: " + e.getMessage());
        }
    }
}