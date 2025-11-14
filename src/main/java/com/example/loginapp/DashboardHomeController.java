package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
    @FXML private ListView<String> listaNotasVeterinario;
    @FXML private ListView<String> listaNotasRecepcion;

    private ClienteDAO clienteDAO = new ClienteDAO();
    private CitaDAO citaDAO = new CitaDAO();
    private MascotaDAO mascotaDAO = new MascotaDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando DashboardHome");
        cargarEstadisticas();
        cargarCitasDelDia();
        cargarNotas();
        configurarListaCitas();
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
                } else {
                    String texto = String.format("%s - %s (%s) - %s",
                            cita.getHoraInicio(),
                            cita.getCliente().getNombreCompleto(),
                            cita.getMascota().getNombre(),
                            cita.getTipoServicio());
                    setText(texto);

                    // Colorear según el estado
                    switch (cita.getEstado()) {
                        case "Completada":
                            setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
                            break;
                        case "En Proceso":
                            setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #f57f17;");
                            break;
                        case "Cancelada":
                            setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
                            break;
                        default: // Programada
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;");
                    }
                }
            }
        });
    }

    private void cargarNotas() {
        // Notas de ejemplo para veterinario
        ObservableList<String> notasVet = FXCollections.observableArrayList(
                "🐕 Perro en observación - Sala 2",
                "💉 Verificar inventario de vacunas",
                "📋 Revisar resultados de análisis - Max (Cliente: García)",
                "⚠️ Gato en recuperación post-cirugía"
        );
        listaNotasVeterinario.setItems(notasVet);

        // Notas de ejemplo para recepción
        ObservableList<String> notasRecep = FXCollections.observableArrayList(
                "📞 Llamar a cliente Pérez - confirmar cita",
                "📦 Pedido de alimento llegará mañana",
                "💰 Revisar pagos pendientes",
                "🔔 Recordatorios de vacunas por enviar"
        );
        listaNotasRecepcion.setItems(notasRecep);

        logger.info("Notas cargadas");
    }

    public void refrescar() {
        logger.info("Refrescando dashboard home");
        cargarEstadisticas();
        cargarCitasDelDia();
    }
}