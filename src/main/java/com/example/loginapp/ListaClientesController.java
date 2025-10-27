// ListaClientesController.java
package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ListaClientesController {
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> ColumnaApellidoP;
    @FXML private TableColumn<Cliente, String> ColumnaApellidoM;
    @FXML private TableColumn<Cliente, String> ColumnaTelefono;
    @FXML private TableColumn<Cliente, String> ColumnaEmail;
    @FXML private TableColumn<Cliente, String> ColumnaDireccion;
    @FXML private TextField busquedaClientes;
    @FXML private Button nuevoCliente;
    @FXML private Button editarCliente;
    @FXML private Button eliminarCliente;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar columnas
        ColumnaApellidoP.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        ColumnaApellidoM.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        ColumnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        ColumnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        ColumnaDireccion.setCellValueFactory(new PropertyValueFactory<>("direccionCompleta"));

        cargarDatosEjemplo();

        // Configurar búsqueda
        FilteredList<Cliente> filteredData = new FilteredList<>(listaClientes, p -> true);

        if (busquedaClientes != null) {
            busquedaClientes.textProperty().addListener((obs, oldVal, newVal) -> {
                String filtro = (newVal == null) ? "" : newVal.trim().toLowerCase();
                filteredData.setPredicate(cliente -> {
                    if (filtro.isEmpty()) return true;

                    if (cliente.getApellidoPaterno() != null &&
                            cliente.getApellidoPaterno().toLowerCase().contains(filtro)) return true;
                    if (cliente.getApellidoMaterno() != null &&
                            cliente.getApellidoMaterno().toLowerCase().contains(filtro)) return true;
                    if (cliente.getTelefono() != null &&
                            cliente.getTelefono().toLowerCase().contains(filtro)) return true;
                    if (cliente.getEmail() != null &&
                            cliente.getEmail().toLowerCase().contains(filtro)) return true;
                    if (cliente.getDireccionCompleta() != null &&
                            cliente.getDireccionCompleta().toLowerCase().contains(filtro)) return true;
                    if (cliente.getNombre() != null &&
                            cliente.getNombre().toLowerCase().contains(filtro)) return true;

                    return false;
                });
            });
        }

        SortedList<Cliente> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaClientes.comparatorProperty());
        tablaClientes.setItems(sortedData);

        // Los manejadores ya están en el FXML pero los agregamos aquí también
        if (nuevoCliente != null) {
            nuevoCliente.setOnAction(e -> nuevoClienteOnAction());
        }
        if (editarCliente != null) {
            editarCliente.setOnAction(e -> editarClienteOnAction());
        }
        if (eliminarCliente != null) {
            eliminarCliente.setOnAction(e -> eliminarClienteOnAction());
        }
    }

    private void cargarDatosEjemplo() {
        listaClientes.clear();
        listaClientes.add(new Cliente("Juan", "García", "López", "555-1234",
                "garcia@email.com", "Calle Primavera 123, Centro"));
        listaClientes.add(new Cliente("María", "Martínez", "Rodríguez", "555-5678",
                "martinez@email.com", "Av. Central 456, Norte"));
    }

    // Métodos llamados desde el FXML
    @FXML
    public void nuevoClienteOnAction() {
        abrirFormularioRegistro(null);
    }

    @FXML
    public void editarClienteOnAction() {
        editarClienteSeleccionado();
    }

    @FXML
    public void eliminarClienteOnAction() {
        eliminarClienteSeleccionado();
    }

    private void abrirFormularioRegistro(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            RegistroController controller = loader.getController();
            if (cliente != null) {
                controller.setClienteParaEditar(cliente);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Modal
            stage.setTitle(cliente == null ? "Nuevo Cliente" : "Editar Cliente");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Cliente resultado = controller.getClienteResultado();
            if (resultado != null) {
                if (cliente == null) {
                    listaClientes.add(resultado);
                } else {
                    tablaClientes.refresh();
                }
            }

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el formulario de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editarClienteSeleccionado() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            abrirFormularioRegistro(clienteSeleccionado);
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un cliente para editar");
        }
    }

    private void eliminarClienteSeleccionado() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar al cliente?");
            alert.setContentText("Esta acción no se puede deshacer");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                listaClientes.remove(clienteSeleccionado);
                mostrarAlerta("Éxito", "Cliente eliminado correctamente");
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un cliente para eliminar");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}