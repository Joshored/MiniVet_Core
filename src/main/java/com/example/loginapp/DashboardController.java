package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML private StackPane contenidoPrincipal;
    @FXML private Button btnInicio;
    @FXML private Button btnClientes;
    @FXML private Button btnCitas;
    @FXML private Button btnInventario;
    @FXML private Button btnFacturas;
    @FXML private Button btnSalir;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando Dashboard");
        cargarDatos();
        mostrarInicio(); // Mostrar vista de inicio por defecto
    }

    private void cargarDatos() {
        try {
            listaClientes.clear();
            listaClientes.addAll(clienteDAO.obtenerTodos());
            logger.info("Cargados {} clientes", listaClientes.size());
        } catch (Exception e) {
            logger.error("Error cargando datos iniciales", e);
        }
    }

    @FXML
    public void mostrarInicio() {
        cargarVista("DashboardHome.fxml");
        resaltarBotonActivo(btnInicio);
    }

    @FXML
    public void mostrarClientes() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaClientes.fxml"));
            Parent vista = loader.load();

            // No necesitamos pasar datos aquí, el controlador carga sus propios datos
            contenidoPrincipal.getChildren().clear();
            contenidoPrincipal.getChildren().add(vista);

            resaltarBotonActivo(btnClientes);
            logger.info("Vista de clientes cargada");
        } catch (IOException e) {
            logger.error("Error cargando vista de clientes", e);
            mostrarError("No se pudo cargar la vista de clientes");
        }
    }

    @FXML
    public void mostrarCitas() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaCitas.fxml"));
            Parent vista = loader.load();

            ListaCitasController controller = loader.getController();
            controller.setListaClientes(listaClientes);

            contenidoPrincipal.getChildren().clear();
            contenidoPrincipal.getChildren().add(vista);

            resaltarBotonActivo(btnCitas);
            logger.info("Vista de citas cargada");
        } catch (IOException e) {
            logger.error("Error cargando vista de citas", e);
            mostrarError("No se pudo cargar la vista de citas");
        }
    }

    @FXML
    public void mostrarInventario() {
        cargarVista("Inventario.fxml");
        resaltarBotonActivo(btnInventario);
    }

    @FXML
    public void mostrarFacturas() {
        cargarVista("Facturacion.fxml");
        resaltarBotonActivo(btnFacturas);
    }

    @FXML
    public void cerrarSesion() {
        try {
            Stage stageActual = (Stage) btnSalir.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Parent root = loader.load();

            stageActual.setTitle("MiniVet - Login");
            stageActual.setScene(new Scene(root));
            stageActual.centerOnScreen();
            stageActual.setMaximized(false);

            logger.info("Sesión cerrada, volviendo a login");
        } catch (IOException e) {
            logger.error("Error al cerrar sesión", e);
        }
    }

    private void cargarVista(String nombreArchivo) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(nombreArchivo));
            Parent vista = loader.load();
            contenidoPrincipal.getChildren().clear();
            contenidoPrincipal.getChildren().add(vista);
            logger.info("Vista {} cargada", nombreArchivo);
        } catch (IOException e) {
            logger.error("Error cargando vista: {}", nombreArchivo, e);
            mostrarError("No se pudo cargar la vista: " + nombreArchivo);
        }
    }

    private void resaltarBotonActivo(Button botonActivo) {
        // Restablecer estilos de todos los botones
        String estiloNormal = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER-LEFT; -fx-padding: 12px 20px;";
        String estiloActivo = "-fx-background-color: #9f2d5b; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER-LEFT; -fx-padding: 12px 20px;";

        btnInicio.setStyle(estiloNormal);
        btnClientes.setStyle(estiloNormal);
        btnCitas.setStyle(estiloNormal);
        btnInventario.setStyle(estiloNormal);
        btnFacturas.setStyle(estiloNormal);

        // Aplicar estilo activo al botón seleccionado
        if (botonActivo != null) {
            botonActivo.setStyle(estiloActivo);
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void refrescarDatos() {
        logger.info("Refrescando datos del dashboard");
        cargarDatos();
    }
}