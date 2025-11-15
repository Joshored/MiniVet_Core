package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class FacturacionController {
    private static final Logger logger = LoggerFactory.getLogger(FacturacionController.class);

    // Componentes de la interfaz
    @FXML private TableView<Factura> tablaFacturas;
    @FXML private TableColumn<Factura, String> columnaFolio;
    @FXML private TableColumn<Factura, String> columnaFecha;
    @FXML private TableColumn<Factura, String> columnaHora;
    @FXML private TableColumn<Factura, String> columnaCliente;
    @FXML private TableColumn<Factura, String> columnaProductos;
    @FXML private TableColumn<Factura, Double> columnaSubtotal;
    @FXML private TableColumn<Factura, Double> columnaDescuento;
    @FXML private TableColumn<Factura, Double> columnaTotal;
    @FXML private TableColumn<Factura, String> columnaMetodoPago;
    @FXML private TableColumn<Factura, String> columnaEstado;

    @FXML private TextField busquedaFacturas;
    @FXML private ComboBox<String> filtroPeriodo;
    @FXML private DatePicker filtroDesde;
    @FXML private DatePicker filtroHasta;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblTotalMes;

    private ObservableList<Factura> listaFacturas = FXCollections.observableArrayList();
    private FacturaDAO facturaDAO = new FacturaDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private DetalleFacturaDAO detalleFacturaDAO = new DetalleFacturaDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando controlador de facturación");
        configurarColumnas();
        configurarFiltros();
        configurarBusqueda();
        cargarDatos();
        actualizarEstadisticas();
    }

    private void configurarColumnas() {
        // Configurar las columnas de la tabla
        columnaFolio.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));

        // Columna de fecha (solo fecha)
        columnaFecha.setCellValueFactory(cellData -> {
            Factura factura = cellData.getValue();
            String fecha = factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return new javafx.beans.property.SimpleStringProperty(fecha);
        });

        // Columna de hora (solo hora)
        columnaHora.setCellValueFactory(cellData -> {
            Factura factura = cellData.getValue();
            String hora = factura.getFechaEmision().format(DateTimeFormatter.ofPattern("HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(hora);
        });

        // Columna cliente (nombre completo)
        columnaCliente.setCellValueFactory(cellData -> {
            Factura factura = cellData.getValue();
            String cliente = factura.getCliente() != null ?
                    factura.getCliente().getNombreCompleto() : "Cliente no encontrado";
            return new javafx.beans.property.SimpleStringProperty(cliente);
        });

        // Columna productos (cantidad de productos)
        columnaProductos.setCellValueFactory(cellData -> {
            Factura factura = cellData.getValue();
            int cantidadProductos = factura.getDetalles() != null ? factura.getDetalles().size() : 0;
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(cantidadProductos));
        });

        columnaSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        columnaTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        columnaMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Columna descuento (por ahora fija en 0)
        columnaDescuento.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(0.0).asObject());

        // Formatear columnas numéricas
        columnaSubtotal.setCellFactory(column -> new TableCell<Factura, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        columnaTotal.setCellFactory(column -> new TableCell<Factura, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        columnaDescuento.setCellFactory(column -> new TableCell<Factura, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // Colorear filas según estado
        tablaFacturas.setRowFactory(tv -> new TableRow<Factura>() {
            @Override
            protected void updateItem(Factura factura, boolean empty) {
                super.updateItem(factura, empty);
                if (empty || factura == null) {
                    setStyle("");
                } else {
                    switch (factura.getEstado()) {
                        case "Pagada":
                            setStyle("-fx-background-color: #c8e6c9;"); // Verde claro
                            break;
                        case "Cancelada":
                            setStyle("-fx-background-color: #ffcdd2;"); // Rojo claro
                            break;
                        case "Pendiente":
                            setStyle("-fx-background-color: #fff9c4;"); // Amarillo claro
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void configurarFiltros() {
        // Configurar filtro de período
        filtroPeriodo.getItems().addAll("Hoy", "Esta semana", "Este mes", "Este año", "Todos");
        filtroPeriodo.setValue("Este mes");
        filtroPeriodo.setOnAction(e -> aplicarFiltros());

        // Configurar date pickers
        filtroDesde.setValue(LocalDate.now().withDayOfMonth(1)); // Primer día del mes
        filtroHasta.setValue(LocalDate.now()); // Hoy

        filtroDesde.setOnAction(e -> aplicarFiltros());
        filtroHasta.setOnAction(e -> aplicarFiltros());
    }

    private void configurarBusqueda() {
        FilteredList<Factura> filteredData = new FilteredList<>(listaFacturas, p -> true);

        busquedaFacturas.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltros();
        });

        SortedList<Factura> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaFacturas.comparatorProperty());
        tablaFacturas.setItems(sortedData);
    }

    private void aplicarFiltros() {
        String textoBusqueda = busquedaFacturas.getText().toLowerCase();
        String periodoFiltro = filtroPeriodo.getValue();
        LocalDate desde = filtroDesde.getValue();
        LocalDate hasta = filtroHasta.getValue();

        FilteredList<Factura> filteredData = new FilteredList<>(listaFacturas, factura -> {
            // Filtro por texto de búsqueda
            if (!textoBusqueda.isEmpty()) {
                boolean coincide = factura.getNumeroFactura().toLowerCase().contains(textoBusqueda) ||
                        (factura.getCliente() != null &&
                                factura.getCliente().getNombreCompleto().toLowerCase().contains(textoBusqueda));
                if (!coincide) return false;
            }

            // Filtro por período
            LocalDate fechaFactura = factura.getFechaEmision().toLocalDate();
            if (desde != null && fechaFactura.isBefore(desde)) {
                return false;
            }
            if (hasta != null && fechaFactura.isAfter(hasta)) {
                return false;
            }

            // Filtro por período predefinido
            if (periodoFiltro != null) {
                LocalDate hoy = LocalDate.now();
                switch (periodoFiltro) {
                    case "Hoy":
                        if (!fechaFactura.equals(hoy)) return false;
                        break;
                    case "Esta semana":
                        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                        if (fechaFactura.isBefore(inicioSemana)) return false;
                        break;
                    case "Este mes":
                        LocalDate inicioMes = hoy.withDayOfMonth(1);
                        if (fechaFactura.isBefore(inicioMes)) return false;
                        break;
                    case "Este año":
                        LocalDate inicioAnio = hoy.withDayOfYear(1);
                        if (fechaFactura.isBefore(inicioAnio)) return false;
                        break;
                    // "Todos" no aplica filtro
                }
            }

            return true;
        });

        SortedList<Factura> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaFacturas.comparatorProperty());
        tablaFacturas.setItems(sortedData);
    }

    private void cargarDatos() {
        try {
            listaFacturas.clear();
            listaFacturas.addAll(facturaDAO.obtenerTodas());
            logger.info("Cargadas {} facturas", listaFacturas.size());
        } catch (Exception e) {
            logger.error("Error cargando facturas", e);
            mostrarAlerta("Error", "No se pudieron cargar las facturas: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        try {
            double ventasHoy = facturaDAO.obtenerTotalVentasHoy();
            double ventasMes = facturaDAO.obtenerTotalVentasMes();

            lblVentasHoy.setText(String.format("$%.2f", ventasHoy));
            lblTotalMes.setText(String.format("$%.2f", ventasMes));

            logger.info("Estadísticas actualizadas - Hoy: ${}, Mes: ${}", ventasHoy, ventasMes);
        } catch (Exception e) {
            logger.error("Error actualizando estadísticas", e);
            lblVentasHoy.setText("0.00");
            lblTotalMes.setText("0.00");
        }
    }

    @FXML
    private void nuevaVenta() {
        try {
            // Crear nueva factura
            String nuevoNumero = generarNumeroFactura();
            Factura nuevaFactura = new Factura(nuevoNumero, 0, "Efectivo"); // Cliente 0 temporal

            // Aquí deberías abrir un formulario modal para crear la factura
            // Por ahora, creamos una factura de ejemplo
            mostrarAlerta("Información", "Funcionalidad de nueva venta en desarrollo\nSe crearían: " + nuevoNumero);
            logger.info("Iniciando nueva venta con número: {}", nuevoNumero);

        } catch (Exception e) {
            logger.error("Error creando nueva venta", e);
            mostrarAlerta("Error", "No se pudo crear la nueva venta: " + e.getMessage());
        }
    }

    @FXML
    private void verDetalle() {
        Factura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            mostrarDetallesFactura(facturaSeleccionada);
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una factura para ver sus detalles");
        }
    }

    @FXML
    private void imprimirTicket() {
        Factura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            // Simulación de impresión
            String ticket = generarTicket(facturaSeleccionada);

            TextArea textArea = new TextArea(ticket);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(400, 500);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket de Venta");
            alert.setHeaderText("Ticket: " + facturaSeleccionada.getNumeroFactura());
            alert.getDialogPane().setContent(scrollPane);
            alert.showAndWait();

            logger.info("Ticket impreso para factura: {}", facturaSeleccionada.getNumeroFactura());
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una factura para imprimir");
        }
    }

    @FXML
    private void generarReporte() {
        try {
            StringBuilder reporte = new StringBuilder();
            reporte.append("REPORTE DE VENTAS - MINIVET\n");
            reporte.append("============================\n\n");

            reporte.append("Estadísticas:\n");
            reporte.append("-------------").append("\n");
            reporte.append("Ventas de hoy: ").append(lblVentasHoy.getText()).append("\n");
            reporte.append("Ventas del mes: ").append(lblTotalMes.getText()).append("\n");
            reporte.append("Total de facturas: ").append(listaFacturas.size()).append("\n\n");

            reporte.append("Últimas ventas:\n");
            reporte.append("---------------").append("\n");

            int contador = 0;
            for (Factura factura : listaFacturas) {
                if (contador >= 10) break; // Mostrar solo las últimas 10
                reporte.append(factura.getNumeroFactura())
                        .append(" | ")
                        .append(factura.getCliente().getNombreCompleto())
                        .append(" | ")
                        .append(String.format("$%.2f", factura.getTotal()))
                        .append(" | ")
                        .append(factura.getEstado())
                        .append("\n");
                contador++;
            }

            TextArea textArea = new TextArea(reporte.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(500, 400);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reporte de Ventas");
            alert.setHeaderText("Reporte Generado - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            alert.getDialogPane().setContent(scrollPane);
            alert.showAndWait();

            logger.info("Reporte de ventas generado");

        } catch (Exception e) {
            logger.error("Error generando reporte", e);
            mostrarAlerta("Error", "No se pudo generar el reporte: " + e.getMessage());
        }
    }

    @FXML
    private void cancelarVenta() {
        Factura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            if ("Cancelada".equals(facturaSeleccionada.getEstado())) {
                mostrarAlerta("Información", "Esta venta ya está cancelada");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar cancelación");
            alert.setHeaderText("¿Está seguro de cancelar esta venta?");
            alert.setContentText("Factura: " + facturaSeleccionada.getNumeroFactura() +
                    "\nCliente: " + facturaSeleccionada.getCliente().getNombreCompleto() +
                    "\nTotal: $" + facturaSeleccionada.getTotal());

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    facturaSeleccionada.setEstado("Cancelada");
                    facturaDAO.actualizar(facturaSeleccionada);
                    tablaFacturas.refresh();
                    actualizarEstadisticas();
                    mostrarAlerta("Éxito", "Venta cancelada correctamente");
                    logger.info("Venta cancelada: {}", facturaSeleccionada.getNumeroFactura());
                } catch (Exception e) {
                    logger.error("Error cancelando venta", e);
                    mostrarAlerta("Error", "No se pudo cancelar la venta: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione una factura para cancelar");
        }
    }

    private void mostrarDetallesFactura(Factura factura) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("FACTURA: ").append(factura.getNumeroFactura()).append("\n");
        detalles.append("FECHA: ").append(factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        detalles.append("CLIENTE: ").append(factura.getCliente().getNombreCompleto()).append("\n");
        detalles.append("TELÉFONO: ").append(factura.getCliente().getTelefono()).append("\n");
        detalles.append("MÉTODO PAGO: ").append(factura.getMetodoPago()).append("\n");
        detalles.append("ESTADO: ").append(factura.getEstado()).append("\n");
        detalles.append("----------------------------------------\n");
        detalles.append("PRODUCTOS/SERVICIOS:\n");

        for (DetalleFactura detalle : factura.getDetalles()) {
            detalles.append("• ").append(detalle.getCantidad()).append(" x ")
                    .append(detalle.getProducto().getNombre())
                    .append(" - $").append(String.format("%.2f", detalle.getPrecioUnitario()))
                    .append(" c/u = $").append(String.format("%.2f", detalle.getSubtotal())).append("\n");
        }

        detalles.append("----------------------------------------\n");
        detalles.append("SUBTOTAL: $").append(String.format("%.2f", factura.getSubtotal())).append("\n");
        detalles.append("IVA (16%): $").append(String.format("%.2f", factura.getIva())).append("\n");
        detalles.append("TOTAL: $").append(String.format("%.2f", factura.getTotal())).append("\n");

        TextArea textArea = new TextArea(detalles.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(400, 300);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de Factura");
        alert.setHeaderText("Detalles completos de la factura");
        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }

    private String generarTicket(Factura factura) {
        StringBuilder ticket = new StringBuilder();
        ticket.append("================================\n");
        ticket.append("        MINIVET - VETERINARIA\n");
        ticket.append("================================\n");
        ticket.append("Ticket: ").append(factura.getNumeroFactura()).append("\n");
        ticket.append("Fecha: ").append(factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        ticket.append("Cliente: ").append(factura.getCliente().getNombreCompleto()).append("\n");
        ticket.append("--------------------------------\n");
        ticket.append("CANT  DESCRIPCIÓN       IMPORTE\n");
        ticket.append("--------------------------------\n");

        for (DetalleFactura detalle : factura.getDetalles()) {
            String cantidad = String.format("%-4d", detalle.getCantidad());
            String descripcion = String.format("%-16s",
                    detalle.getProducto().getNombre().length() > 16 ?
                            detalle.getProducto().getNombre().substring(0, 16) :
                            detalle.getProducto().getNombre());
            String importe = String.format("%7.2f", detalle.getSubtotal());
            ticket.append(cantidad).append(descripcion).append(importe).append("\n");
        }

        ticket.append("--------------------------------\n");
        ticket.append(String.format("SUBTOTAL: %17.2f\n", factura.getSubtotal()));
        ticket.append(String.format("IVA: %22.2f\n", factura.getIva()));
        ticket.append(String.format("TOTAL: %20.2f\n", factura.getTotal()));
        ticket.append("--------------------------------\n");
        ticket.append("Método pago: ").append(factura.getMetodoPago()).append("\n");
        ticket.append("Estado: ").append(factura.getEstado()).append("\n");
        ticket.append("================================\n");
        ticket.append("¡Gracias por su compra!\n");
        ticket.append("================================\n");

        return ticket.toString();
    }

    private String generarNumeroFactura() {
        // Generar número de factura secuencial: FAC-001, FAC-002, etc.
        try {
            int ultimoNumero = facturaDAO.obtenerTodas().size() + 1;
            return String.format("FAC-%03d", ultimoNumero);
        } catch (Exception e) {
            return "FAC-001"; // Fallback
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void refrescar() {
        logger.info("Refrescando módulo de facturación");
        cargarDatos();
        actualizarEstadisticas();
    }
}