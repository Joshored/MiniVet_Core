package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class DashboardHomeController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardHomeController.class);

    @FXML private Label lblCantidadCitas;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalMascotas;
    @FXML private ListView<Cita> listaCitasHoy;

    @FXML private TextArea areaNotasVeterinario;
    @FXML private TextArea areaNotasRecepcion;
    @FXML private Button btnGuardarNotasVet;
    @FXML private Button btnGuardarNotasRecep;

    private ClienteDAO clienteDAO = new ClienteDAO();
    private CitaDAO citaDAO = new CitaDAO();
    private MascotaDAO mascotaDAO = new MascotaDAO();
    private NotaDAO notaDAO = new NotaDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando DashboardHome");
        configurarBotonesNotas();
        cargarNotas(); // Cargar notas PRIMERO
        cargarEstadisticas();
        cargarCitasDelDia();
        configurarListaCitas();
    }

    private void configurarBotonesNotas() {
        if (btnGuardarNotasVet != null) {
            btnGuardarNotasVet.setOnAction(e -> guardarNotasVeterinario());
        }
        if (btnGuardarNotasRecep != null) {
            btnGuardarNotasRecep.setOnAction(e -> guardarNotasRecepcion());
        }
    }

    private void cargarEstadisticas() {
        try {
            // Total de clientes
            List<Cliente> clientes = clienteDAO.obtenerTodos();
            lblTotalClientes.setText(String.valueOf(clientes.size()));

            // Total de mascotas
            List<Mascota> mascotas = mascotaDAO.obtenerTodas();
            lblTotalMascotas.setText(String.valueOf(mascotas.size()));

            logger.info("Estadísticas cargadas: {} clientes, {} mascotas", clientes.size(), mascotas.size());
        } catch (Exception e) {
            logger.error("Error cargando estadísticas", e);
            lblTotalClientes.setText("Error");
            lblTotalMascotas.setText("Error");
        }
    }

    private void cargarCitasDelDia() {
        try {
            List<Cita> todasCitas = citaDAO.obtenerTodas();
            LocalDate hoy = LocalDate.now();

            // Filtrar citas de hoy
            List<Cita> citasHoy = todasCitas.stream()
                    .filter(cita -> cita.getFecha().equals(hoy))
                    .toList();

            lblCantidadCitas.setText(String.valueOf(citasHoy.size()));

            ObservableList<Cita> citasObservable = FXCollections.observableArrayList(citasHoy);
            listaCitasHoy.setItems(citasObservable);

            logger.info("Citas del día cargadas: {} citas", citasHoy.size());
        } catch (Exception e) {
            logger.error("Error cargando citas del día", e);
            lblCantidadCitas.setText("0");
        }
    }

    private void configurarListaCitas() {
        listaCitasHoy.setCellFactory(lv -> new ListCell<Cita>() {
            @Override
            protected void updateItem(Cita cita, boolean empty) {
                super.updateItem(cita, empty);
                if (empty || cita == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String texto = String.format("%s - %s (%s) - %s",
                            cita.getHoraInicio(),
                            cita.getCliente().getNombreCompleto(),
                            cita.getMascota().getNombre(),
                            cita.getTipoServicio());
                    setText(texto);

                    setStyle("-fx-font-size: 14px; -fx-padding: 8;");

                    // Colorear según el estado
                    switch (cita.getEstado()) {
                        case "Completada":
                            setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-font-size: 14px; -fx-padding: 8;");
                            break;
                        case "En Proceso":
                            setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #f57f17; -fx-font-size: 14px; -fx-padding: 8;");
                            break;
                        case "Cancelada":
                            setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-size: 14px; -fx-padding: 8;");
                            break;
                        default: // Programada
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-size: 14px; -fx-padding: 8;");
                    }
                }
            }
        });
    }

    private void cargarNotas() {
        logger.info("Iniciando carga de notas...");

        // Verificar si la tabla de notas existe
        if (!notaDAO.tablaNotasExiste()) {
            logger.error("La tabla 'notas' no existe en la base de datos");
            mostrarError("Error: La tabla de notas no está disponible. Las notas no se guardarán.");
            // NO cargar notas de ejemplo - dejar vacías
            limpiarNotas();
            return;
        }

        try {
            // Cargar notas del veterinario desde la base de datos
            String notasVet = notaDAO.obtenerNota("veterinario");
            if (areaNotasVeterinario != null) {
                if (notasVet != null && !notasVet.isEmpty()) {
                    areaNotasVeterinario.setText(notasVet);
                    logger.info("Notas del veterinario cargadas desde BD: {} caracteres", notasVet.length());
                } else {
                    // NO cargar ejemplo - dejar vacío
                    areaNotasVeterinario.setText("");
                    logger.info("No hay notas del veterinario en BD - campo vacío");
                }
                areaNotasVeterinario.setStyle("-fx-font-size: 14px; -fx-background-color: #fffde7;");
            }

            // Cargar notas de recepción desde la base de datos
            String notasRecep = notaDAO.obtenerNota("recepcion");
            if (areaNotasRecepcion != null) {
                if (notasRecep != null && !notasRecep.isEmpty()) {
                    areaNotasRecepcion.setText(notasRecep);
                    logger.info("Notas de recepción cargadas desde BD: {} caracteres", notasRecep.length());
                } else {
                    // NO cargar ejemplo - dejar vacío
                    areaNotasRecepcion.setText("");
                    logger.info("No hay notas de recepción en BD - campo vacío");
                }
                areaNotasRecepcion.setStyle("-fx-font-size: 14px; -fx-background-color: #e3f2fd;");
            }

            logger.info("Carga de notas completada");

        } catch (Exception e) {
            logger.error("Error crítico cargando notas", e);
            mostrarError("Error cargando notas: " + e.getMessage());
            // NO cargar notas de ejemplo - dejar vacías
            limpiarNotas();
        }
    }

    private void limpiarNotas() {
        // Limpiar ambos campos de notas
        if (areaNotasVeterinario != null) {
            areaNotasVeterinario.setText("");
        }
        if (areaNotasRecepcion != null) {
            areaNotasRecepcion.setText("");
        }
        logger.info("Campos de notas limpiados");
    }

    @FXML
    private void guardarNotasVeterinario() {
        try {
            String contenido = areaNotasVeterinario.getText().trim();

            // Permitir guardar incluso si está vacío (para limpiar notas)
            boolean exito = notaDAO.guardarNota("veterinario", contenido);

            if (exito) {
                if (contenido.isEmpty()) {
                    mostrarConfirmacion("Notas del veterinario limpiadas correctamente");
                    logger.info("Notas del veterinario limpiadas (vacías)");
                } else {
                    mostrarConfirmacion("Notas del veterinario guardadas correctamente");
                    logger.info("Notas del veterinario guardadas exitosamente: {} caracteres", contenido.length());
                }
            } else {
                mostrarError("Error: No se pudieron guardar las notas del veterinario");
            }

        } catch (Exception e) {
            logger.error("Error guardando notas del veterinario", e);
            mostrarError("Error al guardar notas del veterinario: " + e.getMessage());
        }
    }

    @FXML
    private void guardarNotasRecepcion() {
        try {
            String contenido = areaNotasRecepcion.getText().trim();

            // Permitir guardar incluso si está vacío (para limpiar notas)
            boolean exito = notaDAO.guardarNota("recepcion", contenido);

            if (exito) {
                if (contenido.isEmpty()) {
                    mostrarConfirmacion("Notas de recepción limpiadas correctamente");
                    logger.info("Notas de recepción limpiadas (vacías)");
                } else {
                    mostrarConfirmacion("Notas de recepción guardadas correctamente");
                    logger.info("Notas de recepción guardadas exitosamente: {} caracteres", contenido.length());
                }
            } else {
                mostrarError("Error: No se pudieron guardar las notas de recepción");
            }

        } catch (Exception e) {
            logger.error("Error guardando notas de recepción", e);
            mostrarError("Error al guardar notas de recepción: " + e.getMessage());
        }
    }

    private void mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guardado exitoso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void refrescar() {
        logger.info("Refrescando dashboard home");
        cargarEstadisticas();
        cargarCitasDelDia();
        cargarNotas(); // Recargar notas también
    }
}