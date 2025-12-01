package com.example.loginapp;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import javafx.collections.FXCollections;

public class CitaController {
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Mascota> comboMascota;
    @FXML private ComboBox<String> comboTipoServicio;
    @FXML private DatePicker fechaCita;
    @FXML private TextField horaInicio;
    @FXML private TextField horaFin;
    @FXML private TextArea sintomas;
    @FXML private TextArea descripcion;
    @FXML private ComboBox<String> comboVeterinario;
    @FXML private ComboBox<String> comboEstado;
    @FXML private Button btnGuardar;
    @FXML private Label mensajeAviso;

    private ObservableList<Cliente> listaClientes;
    private Cita citaEdicion;
    private CitaDAO citaDAO = new CitaDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private VeterinarioDAO veterinarioDAO = new VeterinarioDAO();

    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarValidaciones();
        configurarEventos();
        cargarDatosFrescos();
    }

    private void cargarDatosFrescos() {
        try {
            // Obtenemos la lista directamente de la BD
            listaClientes = FXCollections.observableArrayList(clienteDAO.obtenerTodos());

            if (comboCliente != null) {
                comboCliente.setItems(listaClientes);

                // Configuración visual del ComboBox (CellFactory)
                comboCliente.setCellFactory(lv -> new ListCell<Cliente>() {
                    @Override protected void updateItem(Cliente item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : item.getNombreCompleto());
                    }
                });
                comboCliente.setButtonCell(new ListCell<Cliente>() {
                    @Override protected void updateItem(Cliente item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : item.getNombreCompleto());
                    }
                });
            }
        } catch (Exception e) {
            mensajeAviso.setText("Error al cargar clientes");
        }
    }

    private void configurarComboBoxes() {
        if (comboTipoServicio != null) comboTipoServicio.getItems().addAll(
                "Consulta Médica", "Revisión Mensual", "Revisión Anual", "Urgencias",
                "Seguimiento de Tratamiento", "Desparasitación", "Esterilización",
                "Vacunación", "Baño y Corte", "Solo Baño"
        );

        if (comboEstado != null) {
            comboEstado.getItems().addAll("Programada", "Completada", "Cancelada", "En Proceso");
            comboEstado.setValue("Programada");
        }

        if (comboVeterinario != null) {
            comboVeterinario.getItems().clear();

            // Obtener la lista real de la base de datos
            var listaVets = veterinarioDAO.obtenerTodos();

            if (listaVets.isEmpty()) {
                comboVeterinario.setPromptText("No hay veterinarios registrados");
            } else {
                // Agregar los nombres completos al combo
                for (Veterinario vet : listaVets) {
                    comboVeterinario.getItems().add(vet.getNombreCompleto());
                }
            }
        }
    }

    private void configurarValidaciones() {
        if (horaInicio != null) horaInicio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9:]*")) horaInicio.setText(oldVal);
        });

        if (horaFin != null) horaFin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9:]*")) horaFin.setText(oldVal);
        });
    }

    private void configurarEventos() {
        if (comboCliente != null) comboCliente.setOnAction(e -> cargarMascotasDelCliente());
        if (btnGuardar != null) btnGuardar.setOnAction(e -> guardarCita());
    }

    public void setCitaParaEditar(Cita cita) {
        this.citaEdicion = cita;
        if (cita != null) {
            comboCliente.setValue(cita.getCliente());
            cargarMascotasDelCliente();
            comboMascota.setValue(cita.getMascota());
            comboTipoServicio.setValue(cita.getTipoServicio());
            fechaCita.setValue(cita.getFecha());
            horaInicio.setText(cita.getHoraInicio());
            horaFin.setText(cita.getHoraFin());
            sintomas.setText(cita.getSintomas());
            descripcion.setText(cita.getDescripcion());
            comboVeterinario.setValue(cita.getVeterinario());
            comboEstado.setValue(cita.getEstado());
            btnGuardar.setText("Actualizar Cita");
        }
    }

    private void cargarMascotasDelCliente() {
        Cliente clienteSeleccionado = comboCliente.getValue();
        if (clienteSeleccionado != null && comboMascota != null) {
            comboMascota.getItems().clear();
            comboMascota.getItems().addAll(clienteSeleccionado.getMascotas());
            comboMascota.setCellFactory(lv -> new ListCell<Mascota>() {
                @Override protected void updateItem(Mascota item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.toString());
                }
            });
            comboMascota.setButtonCell(new ListCell<Mascota>() {
                @Override protected void updateItem(Mascota item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.toString());
                }
            });
        }
    }

    @FXML
    public void guardarCita() {
        if (!validarFormulario()) return;

        try {
            if (citaEdicion == null) citaEdicion = new Cita();
            else citaEdicion.setId(citaEdicion.getId()); // Mantener ID existente

            citaEdicion.setCliente(comboCliente.getValue());
            citaEdicion.setMascota(comboMascota.getValue());
            citaEdicion.setTipoServicio(comboTipoServicio.getValue());
            citaEdicion.setFecha(fechaCita.getValue());
            citaEdicion.setHoraInicio(horaInicio.getText().trim());
            citaEdicion.setHoraFin(horaFin.getText().trim());
            citaEdicion.setSintomas(sintomas.getText().trim());
            citaEdicion.setDescripcion(descripcion.getText().trim());
            citaEdicion.setVeterinario(comboVeterinario.getValue());
            citaEdicion.setEstado(comboEstado.getValue());

            if (citaEdicion.getId() == 0) {
                int nuevoId = citaDAO.guardar(citaEdicion);
                citaEdicion.setId(nuevoId);
            } else citaDAO.actualizar(citaEdicion);

            mostrarMensaje("Cita guardada correctamente", true);

            javafx.application.Platform.runLater(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                cerrarVentana();
            });

        } catch (Exception e) {
            mostrarMensaje("Error al guardar la cita: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    public Cita getCitaResultado() { return citaEdicion; }

    private boolean validarFormulario() {
        if (comboCliente.getValue() == null) {
            mostrarMensaje("Debe seleccionar un cliente", false); return false;
        }
        if (comboMascota.getValue() == null) {
            mostrarMensaje("Debe seleccionar una mascota", false); return false;
        }
        if (comboTipoServicio.getValue() == null) {
            mostrarMensaje("Debe seleccionar el tipo de servicio", false); return false;
        }
        if (fechaCita.getValue() == null) {
            mostrarMensaje("Debe seleccionar una fecha", false); return false;
        }
        if (fechaCita.getValue().isBefore(LocalDate.now())) {
            mostrarMensaje("La fecha no puede ser en el pasado", false); return false;
        }

        if (sintomas.getText() == null || sintomas.getText().trim().isEmpty()) {
            mostrarMensaje("Es obligatorio describir los síntomas o el motivo de la consulta", false);
            sintomas.requestFocus();
            return false;
        }

        if (horaInicio.getText().trim().isEmpty()) {
            mostrarMensaje("Debe ingresar la hora de inicio", false); return false;
        }
        if (comboVeterinario.getValue() == null) {
            mostrarMensaje("Debe seleccionar un veterinario", false); return false;
        }
        return true;
    }

    private void mostrarMensaje(String mensaje, boolean esExito) {
        mensajeAviso.setText(mensaje);
        mensajeAviso.setStyle(esExito ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}