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

// Controlador para la ventana de lista de citas.
public class ListaCitasController {

    @FXML private TableView<Object> tablaCitas; // URIEL cambia Object por Cita
    @FXML private TableColumn<Object, String> columnaFecha;
    @FXML private TableColumn<Object, String> columnaHora;
    @FXML private TableColumn<Object, String> columnaCliente;
    @FXML private TableColumn<Object, String> columnaMascota;
    @FXML private TableColumn<Object, String> columnaTipoServicio;
    @FXML private TableColumn<Object, String> columnaVeterinario;
    @FXML private TableColumn<Object, String> columnaEstado;

    @FXML private TextField busquedaCitas;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private ComboBox<String> filtroTipoServicio;
    @FXML private DatePicker filtroFecha;

    @FXML private Button nuevaCita;
    @FXML private Button editarCita;
    @FXML private Button eliminarCita;
    @FXML private Button verDetalles;
    @FXML private Button volverMenu;

    private ObservableList<Object> listaCitas = FXCollections.observableArrayList();
    private ObservableList<Cliente> listaClientes;

    // Inicialización del controlador
    @FXML
    public void initialize() {
        configurarColumnas();
        configurarFiltros();
        configurarBusqueda();
        configurarBotones();
        cargarDatosEjemplo();
    }

    // Configura las columnas de la tabla
    private void configurarColumnas() {
        // URIEL asigna las propiedades correctas de la clase Cita
    }

    // Configura los filtros de la interfaz
    private void configurarFiltros() {
        // Filtro de estado
        if (filtroEstado != null) {
            filtroEstado.getItems().addAll(
                    "Todas",
                    "Programada",
                    "Completada",
                    "Cancelada",
                    "En Proceso"
            );
            filtroEstado.setValue("Todas");
        }

        // Filtro de tipo de servicio
        if (filtroTipoServicio != null) {
            filtroTipoServicio.getItems().addAll(
                    "Todos",
                    "Consulta Médica",
                    "Revisión Mensual",
                    "Revisión Anual",
                    "Urgencias",
                    "Seguimiento de Tratamiento",
                    "Desparasitación",
                    "Esterilización",
                    "Vacunación",
                    "Baño y Corte",
                    "Solo Baño"
            );
            filtroTipoServicio.setValue("Todos");
        }
    }

    // Configura la funcionalidad de búsqueda en la tabla
    private void configurarBusqueda() {
        FilteredList<Object> filteredData = new FilteredList<>(listaCitas, p -> true);

        if (busquedaCitas != null) {
            busquedaCitas.textProperty().addListener((obs, oldVal, newVal) -> {
                String filtro = (newVal == null) ? "" : newVal.trim().toLowerCase();
                filteredData.setPredicate(cita -> {
                    if (filtro.isEmpty()) return true;

                    // URIEL implementa las condiciones de búsqueda según los campos de la clase Cita

                    return false;
                });
            });
        }

        SortedList<Object> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaCitas.comparatorProperty());
        tablaCitas.setItems(sortedData);
    }

    // Configura los eventos de los botones
    private void configurarBotones() {
        if (nuevaCita != null) {
            nuevaCita.setOnAction(e -> nuevaCitaOnAction());
        }
        if (editarCita != null) {
            editarCita.setOnAction(e -> editarCitaOnAction());
        }
        if (eliminarCita != null) {
            eliminarCita.setOnAction(e -> eliminarCitaOnAction());
        }
        if (verDetalles != null) {
            verDetalles.setOnAction(e -> verDetallesOnAction());
        }
        if (volverMenu != null) {
            volverMenu.setOnAction(e -> volverMenuOnAction());
        }
    }

    // Establece la lista de clientes para usar en el formulario de citas
    public void setListaClientes(ObservableList<Cliente> clientes) {
        this.listaClientes = clientes;
    }

    // Carga datos de ejemplo en la tabla
    private void cargarDatosEjemplo() {
        // URIEL aqui puedes poner las citas reales
        listaCitas.clear();
        // URIEL agregar citas de ejemplo a la listaCitas
    }

    // Abre el formulario para crear una nueva cita
    @FXML
    public void nuevaCitaOnAction() {
        abrirFormularioCita(null);
    }

    // Abre el formulario para editar la cita seleccionada
    @FXML
    public void editarCitaOnAction() {
        Object citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            abrirFormularioCita(citaSeleccionada);
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una cita para editar");
        }
    }

    // Elimina la cita seleccionada
    @FXML
    public void eliminarCitaOnAction() {
        Object citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar esta cita?");
            alert.setContentText("Esta acción no se puede deshacer");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                listaCitas.remove(citaSeleccionada);
                mostrarAlerta("Éxito", "Cita eliminada correctamente");
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una cita para eliminar");
        }
    }

    // Muestra los detalles de la cita seleccionada
    @FXML
    public void verDetallesOnAction() {
        Object citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            // Aquí se puede abrir una ventana de detalles o mostrar un diálogo
            mostrarAlerta("Detalles de Cita", "Funcionalidad de detalles por implementar");
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una cita para ver sus detalles");
        }
    }

    // Vuelve al menú principal
    @FXML
    public void volverMenuOnAction() {
        Stage stage = (Stage) volverMenu.getScene().getWindow();
        stage.close();
    }

    // Abre el formulario de registro/edición de cita
    private void abrirFormularioCita(Object cita) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("registroCita-view.fxml"));
            Parent root = loader.load();

            CitaController controller = loader.getController();
            controller.setListaClientes(listaClientes);

            // Si se está editando una cita, pasar la cita al controlador

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(cita == null ? "Nueva Cita" : "Editar Cita");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recargar la tabla después de cerrar el formulario
            cargarDatosEjemplo();
            tablaCitas.refresh();

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Muestra una alerta informativa
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}