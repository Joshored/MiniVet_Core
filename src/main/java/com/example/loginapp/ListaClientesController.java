package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class ListaClientesController {

    @FXML
    private TextField busquedaClientes;

    @FXML
    private TableView<?> tableView;

    @FXML
    private TableColumn<?, ?> ColumnaApellidoP;

    @FXML
    private TableColumn<?, ?> ColumnaApellidoM;

    @FXML
    private TableColumn<?, ?> ColumnaTelefono;

    @FXML
    private TableColumn<?, ?> ColumnaEmail;

    @FXML
    private TableColumn<?, ?> ColumnaDireccion;

    @FXML
    private Button nuevoCliente;

    @FXML
    private Button editarCliente;

    @FXML
    private Button eliminarCliente;

    @FXML
    public void initialize() {
        // Aquí puedes inicializar las columnas de la tabla
        // Por ahora dejamos la tabla vacía
    }

    @FXML
    public void nuevoClienteOnAction(ActionEvent event) {
        abrirRegistro(false);
    }

    @FXML
    public void editarClienteOnAction(ActionEvent event) {
        // Verificar si hay un cliente seleccionado
        abrirRegistro(true);
    }

    @FXML
    public void eliminarClienteOnAction(ActionEvent event) {
        // Lógica para eliminar cliente
        System.out.println("Eliminar cliente");
    }

    private void abrirRegistro(boolean esEdicion) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            RegistroController controller = loader.getController();
            if (esEdicion) {
                controller.setModoEdicion(true);
                // Aquí pasarías los datos del cliente seleccionado
            }

            Stage stageRegistro = new Stage();
            stageRegistro.setTitle(esEdicion ? "Editar Cliente" : "Nuevo Cliente");
            stageRegistro.setScene(new Scene(root));
            stageRegistro.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir ventana de registro");
        }
    }
}