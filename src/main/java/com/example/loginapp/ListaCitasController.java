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

public class ListaCitasController {
    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, String> columnaFecha;
    @FXML private TableColumn<Cita, String> columnaHora;
    @FXML private TableColumn<Cita, String> columnaCliente;
    @FXML private TableColumn<Cita, String> columnaMascota;
    @FXML private TableColumn<Cita, String> columnaTipoServicio;
    @FXML private TableColumn<Cita, String> columnaVeterinario;
    @FXML private TableColumn<Cita, String> columnaEstado;

    @FXML private TextField busquedaCitas;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private ComboBox<String> filtroTipoServicio;
    @FXML private DatePicker filtroFecha;

    @FXML private Button nuevaCita;
    @FXML private Button editarCita;
    @FXML private Button eliminarCita;
    @FXML private Button verDetalles;
    @FXML private Button volverMenu;

    private ObservableList<Cita> listaCitas = FXCollections.observableArrayList();
    private ObservableList<Cliente> listaClientes;
    private CitaDAO citaDAO = new CitaDAO();

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarFiltros();
        configurarBusqueda();
        configurarBotones();
        cargarDatosDesdeBD();
    }

    private void configurarColumnas() {
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columnaHora.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        columnaCliente.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCliente().getNombreCompleto()));
        columnaMascota.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMascota().getNombre()));
        columnaTipoServicio.setCellValueFactory(new PropertyValueFactory<>("tipoServicio"));
        columnaVeterinario.setCellValueFactory(new PropertyValueFactory<>("veterinario"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void configurarFiltros() {
        if (filtroEstado != null) {
            filtroEstado.getItems().addAll("Todas", "Programada", "Completada", "Cancelada", "En Proceso");
            filtroEstado.setValue("Todas");
            filtroEstado.setOnAction(e -> aplicarFiltros());
        }

        if (filtroTipoServicio != null) {
            filtroTipoServicio.getItems().addAll("Todos", "Consulta Médica", "Revisión Mensual",
                    "Revisión Anual", "Urgencias", "Seguimiento de Tratamiento", "Desparasitación",
                    "Esterilización", "Vacunación", "Baño y Corte", "Solo Baño");
            filtroTipoServicio.setValue("Todos");
            filtroTipoServicio.setOnAction(e -> aplicarFiltros());
        }

        if (filtroFecha != null) filtroFecha.setOnAction(e -> aplicarFiltros());
    }

    private void configurarBusqueda() {
        FilteredList<Cita> filteredData = new FilteredList<>(listaCitas, p -> true);

        if (busquedaCitas != null) {
            busquedaCitas.textProperty().addListener((obs, oldVal, newVal) -> {
                aplicarFiltros();
            });
        }

        SortedList<Cita> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaCitas.comparatorProperty());
        tablaCitas.setItems(sortedData);
    }

    private void aplicarFiltros() {
        String textoBusqueda = busquedaCitas.getText().toLowerCase();
        String estadoFiltro = filtroEstado.getValue();
        String tipoServicioFiltro = filtroTipoServicio.getValue();

        FilteredList<Cita> filteredData = new FilteredList<>(listaCitas, cita -> {
            // Filtro por texto de búsqueda
            if (!textoBusqueda.isEmpty()) {
                boolean coincide = cita.getCliente().getNombreCompleto().toLowerCase().contains(textoBusqueda) ||
                        cita.getMascota().getNombre().toLowerCase().contains(textoBusqueda) ||
                        cita.getTipoServicio().toLowerCase().contains(textoBusqueda) ||
                        cita.getVeterinario().toLowerCase().contains(textoBusqueda);
                if (!coincide) return false;
            }

            // Filtro por estado
            if (!"Todas".equals(estadoFiltro) && !cita.getEstado().equals(estadoFiltro)) return false;

            // Filtro por tipo de servicio
            if (!"Todos".equals(tipoServicioFiltro) && !cita.getTipoServicio().equals(tipoServicioFiltro)) return false;

            // Filtro por fecha
            if (filtroFecha.getValue() != null && !cita.getFecha().equals(filtroFecha.getValue())) return false;

            return true;
        });

        SortedList<Cita> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaCitas.comparatorProperty());
        tablaCitas.setItems(sortedData);
    }

    private void configurarBotones() {
        if (nuevaCita != null) nuevaCita.setOnAction(e -> nuevaCitaOnAction());
        if (editarCita != null) editarCita.setOnAction(e -> editarCitaOnAction());
        if (eliminarCita != null) eliminarCita.setOnAction(e -> eliminarCitaOnAction());
        if (verDetalles != null) verDetalles.setOnAction(e -> verDetallesOnAction());
        if (volverMenu != null) volverMenu.setOnAction(e -> volverMenuOnAction());
    }

    public void setListaClientes(ObservableList<Cliente> clientes) {
        this.listaClientes = clientes;
    }

    private void cargarDatosDesdeBD() {
        listaCitas.clear();
        listaCitas.addAll(citaDAO.obtenerTodas());
    }

    @FXML
    public void nuevaCitaOnAction() { abrirFormularioCita(null); }

    @FXML
    public void editarCitaOnAction() {
        Cita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) abrirFormularioCita(citaSeleccionada);
        else mostrarAlerta("Advertencia", "Por favor seleccione una cita para editar");
    }

    @FXML
    public void eliminarCitaOnAction() {
        Cita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar esta cita?");
            alert.setContentText("Esta acción no se puede deshacer");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                citaDAO.eliminar(citaSeleccionada.getId());
                listaCitas.remove(citaSeleccionada);
                mostrarAlerta("Éxito", "Cita eliminada correctamente");
            }
        } else mostrarAlerta("Advertencia", "Por favor seleccione una cita para eliminar");
    }

    @FXML
    public void verDetallesOnAction() {
        Cita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            StringBuilder detalles = new StringBuilder();
            detalles.append("Cliente: ").append(citaSeleccionada.getCliente().getNombreCompleto()).append("\n");
            detalles.append("Mascota: ").append(citaSeleccionada.getMascota().getNombre()).append("\n");
            detalles.append("Servicio: ").append(citaSeleccionada.getTipoServicio()).append("\n");
            detalles.append("Fecha: ").append(citaSeleccionada.getFecha()).append("\n");
            detalles.append("Hora: ").append(citaSeleccionada.getHoraInicio()).append("\n");
            detalles.append("Veterinario: ").append(citaSeleccionada.getVeterinario()).append("\n");
            detalles.append("Estado: ").append(citaSeleccionada.getEstado()).append("\n");
            if (citaSeleccionada.getSintomas() != null && !citaSeleccionada.getSintomas().isEmpty()) {
                detalles.append("Síntomas: ").append(citaSeleccionada.getSintomas()).append("\n");
            }
            if (citaSeleccionada.getDescripcion() != null && !citaSeleccionada.getDescripcion().isEmpty()) {
                detalles.append("Descripción: ").append(citaSeleccionada.getDescripcion());
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalles de la Cita");
            alert.setHeaderText("Información completa de la cita");
            alert.setContentText(detalles.toString());
            alert.showAndWait();
        } else mostrarAlerta("Advertencia", "Por favor seleccione una cita para ver sus detalles");
    }

    @FXML
    public void volverMenuOnAction() {
        Stage stage = (Stage) volverMenu.getScene().getWindow();
        stage.close();
    }

    private void abrirFormularioCita(Cita cita) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registroCita-view.fxml"));
            Parent root = loader.load();

            CitaController controller = loader.getController();
            controller.setListaClientes(listaClientes);

            if (cita != null) controller.setCitaParaEditar(cita);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(cita == null ? "Nueva Cita" : "Editar Cita");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Cita resultado = controller.getCitaResultado();
            if (resultado != null) {
                if (cita == null) {
                    // Nueva cita
                    listaCitas.add(resultado);
                } else {
                    // Editar cita existente
                    tablaCitas.refresh();
                }
            }

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
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