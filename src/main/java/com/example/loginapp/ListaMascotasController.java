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

/**
 * Controlador para la ventana de lista de mascotas.
 * Permite ver, crear, editar y eliminar mascotas.
 */
public class ListaMascotasController {

    @FXML private TableView<Mascota> tablaMascotas;
    @FXML private TableColumn<Mascota, String> columnaNombre;
    @FXML private TableColumn<Mascota, String> columnaEspecie;
    @FXML private TableColumn<Mascota, Integer> columnaEdad;
    @FXML private TableColumn<Mascota, String> columnaRaza;
    @FXML private TableColumn<Mascota, String> columnaSexo;
    @FXML private TableColumn<Mascota, String> columnaDueno;
    @FXML private TextField busquedaMascotas;
    @FXML private Button nuevaMascota;
    @FXML private Button editarMascota;
    @FXML private Button eliminarMascota;
    @FXML private Button volverClientes;
    @FXML private Label tituloVentana;

    private ObservableList<Mascota> listaMascotas = FXCollections.observableArrayList();
    private ObservableList<Cliente> listaClientes;
    private Cliente clienteFiltro;

    @FXML
    public void initialize() {
        // Configurar columnas
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaEspecie.setCellValueFactory(new PropertyValueFactory<>("especie"));
        columnaEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        columnaRaza.setCellValueFactory(new PropertyValueFactory<>("raza"));
        columnaSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        columnaDueno.setCellValueFactory(new PropertyValueFactory<>("nombreDueno"));

        // Configurar búsqueda
        FilteredList<Mascota> filteredData = new FilteredList<>(listaMascotas, p -> true);

        if (busquedaMascotas != null) {
            busquedaMascotas.textProperty().addListener((obs, oldVal, newVal) -> {
                String filtro = (newVal == null) ? "" : newVal.trim().toLowerCase();
                filteredData.setPredicate(mascota -> {
                    if (filtro.isEmpty()) return true;

                    if (mascota.getNombre() != null &&
                            mascota.getNombre().toLowerCase().contains(filtro)) return true;
                    if (mascota.getEspecie() != null &&
                            mascota.getEspecie().toLowerCase().contains(filtro)) return true;
                    if (mascota.getRaza() != null &&
                            mascota.getRaza().toLowerCase().contains(filtro)) return true;
                    if (mascota.getNombreDueno() != null &&
                            mascota.getNombreDueno().toLowerCase().contains(filtro)) return true;

                    return false;
                });
            });
        }

        SortedList<Mascota> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaMascotas.comparatorProperty());
        tablaMascotas.setItems(sortedData);

        // Configurar botones
        if (nuevaMascota != null) {
            nuevaMascota.setOnAction(e -> nuevaMascotaOnAction());
        }
        if (editarMascota != null) {
            editarMascota.setOnAction(e -> editarMascotaOnAction());
        }
        if (eliminarMascota != null) {
            eliminarMascota.setOnAction(e -> eliminarMascotaOnAction());
        }
        if (volverClientes != null) {
            volverClientes.setOnAction(e -> volverClientesOnAction());
        }
    }

    public void setListaClientes(ObservableList<Cliente> clientes) {
        this.listaClientes = clientes;
        cargarMascotasDeClientes();
    }

    public void setClienteFiltro(Cliente cliente) {
        this.clienteFiltro = cliente;
        if (tituloVentana != null) {
            tituloVentana.setText("Mascotas de " + cliente.getNombreCompleto());
        }
        cargarMascotasDeClientes();
    }

    private void cargarMascotasDeClientes() {
        listaMascotas.clear();

        if (listaClientes != null) {
            if (clienteFiltro != null) {
                listaMascotas.addAll(clienteFiltro.getMascotas());
            } else {
                for (Cliente cliente : listaClientes) {
                    listaMascotas.addAll(cliente.getMascotas());
                }
            }
        }
    }

    @FXML
    public void nuevaMascotaOnAction() {
        abrirFormularioMascota(null);
    }

    @FXML
    public void editarMascotaOnAction() {
        Mascota mascotaSeleccionada = tablaMascotas.getSelectionModel().getSelectedItem();
        if (mascotaSeleccionada != null) {
            abrirFormularioMascota(mascotaSeleccionada);
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una mascota para editar");
        }
    }

    @FXML
    public void eliminarMascotaOnAction() {
        Mascota mascotaSeleccionada = tablaMascotas.getSelectionModel().getSelectedItem();
        if (mascotaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar esta mascota?");
            alert.setContentText(mascotaSeleccionada.getNombre() + " - " + mascotaSeleccionada.getEspecie());

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (mascotaSeleccionada.getDueno() != null) {
                    mascotaSeleccionada.getDueno().eliminarMascota(mascotaSeleccionada);
                }
                listaMascotas.remove(mascotaSeleccionada);
                mostrarAlerta("Éxito", "Mascota eliminada correctamente");
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una mascota para eliminar");
        }
    }

    /**
     * Cierra la ventana actual y vuelve a la lista de clientes
     */
    @FXML
    public void volverClientesOnAction() {
        Stage stage = (Stage) volverClientes.getScene().getWindow();
        stage.close();
    }

    private void abrirFormularioMascota(Mascota mascota) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("registroMascota-view.fxml"));
            Parent root = loader.load();

            MascotaController controller = loader.getController();
            controller.setListaClientes(listaClientes);

            if (clienteFiltro != null && mascota == null) {
                controller.setClientePreseleccionado(clienteFiltro);
            }

            if (mascota != null) {
                controller.setMascotaParaEditar(mascota);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(mascota == null ? "Nueva Mascota" : "Editar Mascota");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Mascota resultado = controller.getMascotaResultado();
            if (resultado != null) {
                cargarMascotasDeClientes();
                tablaMascotas.refresh();
            }

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }
//Se viene lo chido
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}