package com.example.loginapp;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

// Controlador para la ventana de registro y edición de citas
public class CitaController {

    // Información del Cliente y Mascota
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Mascota> comboMascota;

    // Información de la Cita
    @FXML private ComboBox<String> comboTipoServicio;
    @FXML private DatePicker fechaCita;
    @FXML private TextField horaInicio;
    @FXML private TextField horaFin;

    // Descripción y Síntomas
    @FXML private TextArea sintomas;
    @FXML private TextArea descripcion;

    // Veterinario
    @FXML private ComboBox<String> comboVeterinario;

    // Estado de la cita
    @FXML private ComboBox<String> comboEstado;

    // Botones y mensajes
    @FXML private Button btnGuardar;
    @FXML private Label mensajeAviso;

    private ObservableList<Cliente> listaClientes;

    // Inicialización del controlador
    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarValidaciones();
        configurarEventos();
    }

   // Configura los ComboBoxes con sus opciones
    private void configurarComboBoxes() {
        // Tipos de servicio
        if (comboTipoServicio != null) {
            comboTipoServicio.getItems().addAll(
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
        }

        // Estados de la cita
        if (comboEstado != null) {
            comboEstado.getItems().addAll(
                    "Programada",
                    "Completada",
                    "Cancelada",
                    "En Proceso"
            );
            comboEstado.setValue("Programada");
        }

        // Uriel poner la lista de veterinarios disponibles
        if (comboVeterinario != null) {
            comboVeterinario.getItems().addAll(
                    "Dr. Juan Pérez",
                    "Dra. María González",
                    "Dr. Carlos Rodríguez",
                    "Dra. Ana Martínez"
            );
        }
    }

    // Configura las validaciones de los campos de texto
    private void configurarValidaciones() {
        // Validar formato de hora (HH:MM)
        if (horaInicio != null) {
            horaInicio.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("[0-9:]*")) {
                    horaInicio.setText(oldVal);
                }
            });
        }

        if (horaFin != null) {
            horaFin.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("[0-9:]*")) {
                    horaFin.setText(oldVal);
                }
            });
        }
    }

    // Configura los eventos de los controles
    private void configurarEventos() {
        // Cuando se selecciona un cliente, cargar sus mascotas
        if (comboCliente != null) {
            comboCliente.setOnAction(e -> cargarMascotasDelCliente());
        }

        // Configurar botón guardar
        if (btnGuardar != null) {
            btnGuardar.setOnAction(e -> guardarCita());
        }
    }

    // Establece la lista de clientes para el ComboBox
    public void setListaClientes(ObservableList<Cliente> clientes) {
        this.listaClientes = clientes;
        if (comboCliente != null && listaClientes != null) {
            comboCliente.setItems(listaClientes);

            // Configurar cómo se muestran los clientes
            comboCliente.setCellFactory(lv -> new ListCell<Cliente>() {
                @Override
                protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNombreCompleto());
                }
            });

            comboCliente.setButtonCell(new ListCell<Cliente>() {
                @Override
                protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNombreCompleto());
                }
            });
        }
    }

   // Carga las mascotas del cliente seleccionado en el ComboBox de mascotas
    private void cargarMascotasDelCliente() {
        Cliente clienteSeleccionado = comboCliente.getValue();
        if (clienteSeleccionado != null && comboMascota != null) {
            comboMascota.getItems().clear();
            comboMascota.getItems().addAll(clienteSeleccionado.getMascotas());

            // Configurar cómo se muestran las mascotas
            comboMascota.setCellFactory(lv -> new ListCell<Mascota>() {
                @Override
                protected void updateItem(Mascota item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.toString());
                }
            });

            comboMascota.setButtonCell(new ListCell<Mascota>() {
                @Override
                protected void updateItem(Mascota item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.toString());
                }
            });
        }
    }

    //Guarda la cita después de validar el formulario
    @FXML
    public void guardarCita() {
        if (!validarFormulario()) {
            return;
        }

        // URIEL aqui implemeta la lógica para guardar la cita en la base de datos o en la lista correspondiente

        mostrarMensaje("Cita guardada correctamente", true);

        // Cerrar ventana después de guardar
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            cerrarVentana();
        });
    }

    //Valida que todos los campos obligatorios estén completos
    private boolean validarFormulario() {
        if (comboCliente.getValue() == null) {
            mostrarMensaje("Debe seleccionar un cliente", false);
            return false;
        }

        if (comboMascota.getValue() == null) {
            mostrarMensaje("Debe seleccionar una mascota", false);
            return false;
        }

        if (comboTipoServicio.getValue() == null) {
            mostrarMensaje("Debe seleccionar el tipo de servicio", false);
            return false;
        }

        if (fechaCita.getValue() == null) {
            mostrarMensaje("Debe seleccionar una fecha", false);
            return false;
        }

        // Validar que la fecha no sea en el pasado
        if (fechaCita.getValue().isBefore(LocalDate.now())) {
            mostrarMensaje("La fecha no puede ser en el pasado", false);
            return false;
        }

        if (horaInicio.getText().trim().isEmpty()) {
            mostrarMensaje("Debe ingresar la hora de inicio", false);
            return false;
        }

        if (comboVeterinario.getValue() == null) {
            mostrarMensaje("Debe seleccionar un veterinario", false);
            return false;
        }

        return true;
    }

    //Muestra mensajes de éxito o error
    private void mostrarMensaje(String mensaje, boolean esExito) {
        mensajeAviso.setText(mensaje);
        if (esExito) {
            mensajeAviso.setStyle("-fx-text-fill: green;");
        } else {
            mensajeAviso.setStyle("-fx-text-fill: red;");
        }
    }

    //Cierra la ventana actual
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}