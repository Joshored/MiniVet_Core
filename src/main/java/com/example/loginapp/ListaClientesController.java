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
import javafx.scene.control.Alert;
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
    @FXML private Button verMascotas;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatosDesdeBD();
        configurarBusqueda();
        configurarBotones();
    }

    private void configurarColumnas() {
        ColumnaApellidoP.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        ColumnaApellidoM.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        ColumnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        ColumnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        ColumnaDireccion.setCellValueFactory(new PropertyValueFactory<>("direccionCompleta"));
    }

    private void cargarDatosDesdeBD() {
        listaClientes.clear();
        listaClientes.addAll(clienteDAO.obtenerTodos());
    }

    private void configurarBusqueda() {
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
    }

    private void configurarBotones() {
        if (nuevoCliente != null) nuevoCliente.setOnAction(e -> nuevoClienteOnAction());
        if (editarCliente != null) editarCliente.setOnAction(e -> editarClienteOnAction());
        if (eliminarCliente != null) eliminarCliente.setOnAction(e -> eliminarClienteOnAction());
        if (verMascotas != null) verMascotas.setOnAction(e -> verMascotasOnAction());
    }

    @FXML
    public void nuevoClienteOnAction() { abrirFormularioRegistro(null); }

    @FXML
    public void editarClienteOnAction() { editarClienteSeleccionado(); }

    @FXML
    public void eliminarClienteOnAction() { eliminarClienteSeleccionado(); }

    @FXML
    public void verMascotasOnAction() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            abrirListaMascotas(clienteSeleccionado);
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un cliente para ver sus mascotas");
        }
    }

    @FXML
    public void verCitasOnAction() { abrirListaCitas(); }

    private void abrirListaCitas() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaCitas.fxml"));
            Parent root = loader.load();

            ListaCitasController controller = loader.getController();
            controller.setListaClientes(listaClientes);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Lista de Citas Veterinarias");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la lista de citas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirListaMascotas(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ListaMascotas.fxml"));
            Parent root = loader.load();

            ListaMascotasController controller = loader.getController();
            controller.setListaClientes(listaClientes);

            if (cliente != null) controller.setClienteFiltro(cliente);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(cliente != null ? "Mascotas de " + cliente.getNombreCompleto() : "Todas las Mascotas");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            cargarDatosDesdeBD(); // Recargar datos después de cerrar

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la lista de mascotas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirFormularioRegistro(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            RegistroController controller = loader.getController();

            if (cliente != null) {// Editar cliente existente
                controller.setClienteParaEditar(cliente); // Pasar el cliente existente al controlador
            } else {
                // Inicializar para nuevo cliente
                controller.inicializarFormulario("Cliente");
            }
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
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
            alert.setContentText("Esta acción también eliminará sus " +
                    clienteSeleccionado.getCantidadMascotas() + " mascota(s)");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                clienteDAO.eliminar(clienteSeleccionado.getId());
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