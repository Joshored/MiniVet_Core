package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NuevaVentaController {
    private static final Logger logger = LoggerFactory.getLogger(NuevaVentaController.class);

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Producto> comboProducto;
    @FXML private TextField txtCantidad;
    @FXML private Button btnAgregarProducto;
    @FXML private TableView<DetalleFactura> tablaDetalles;
    @FXML private TableColumn<DetalleFactura, String> columnaProducto;
    @FXML private TableColumn<DetalleFactura, Integer> columnaCantidad;
    @FXML private TableColumn<DetalleFactura, Double> columnaPrecio;
    @FXML private TableColumn<DetalleFactura, Double> columnaSubtotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblTotal;
    @FXML private ComboBox<String> comboMetodoPago;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label mensajeAviso;

    private ObservableList<DetalleFactura> detalles = FXCollections.observableArrayList();
    private Factura factura;

    private ClienteDAO clienteDAO = new ClienteDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private FacturaDAO facturaDAO = new FacturaDAO();
    private DetalleFacturaDAO detalleFacturaDAO = new DetalleFacturaDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando NuevaVentaController");

        try {
            configurarComboBoxes();
            configurarTabla();
            cargarDatos();
            configurarEventos();
            inicializarTotales();
            logger.info("NuevaVentaController inicializado correctamente");
        } catch (Exception e) {
            logger.error("Error inicializando NuevaVentaController", e);
            mostrarMensaje("Error al inicializar: " + e.getMessage(), false);
        }
    }

    private void inicializarTotales() {
        if (lblSubtotal != null) lblSubtotal.setText("$0.00");
        if (lblIVA != null) lblIVA.setText("$0.00");
        if (lblTotal != null) lblTotal.setText("$0.00");
    }

    private void configurarComboBoxes() {
        // Métodos de pago
        if (comboMetodoPago != null) {
            comboMetodoPago.getItems().addAll("Efectivo", "Tarjeta", "Transferencia");
            comboMetodoPago.setValue("Efectivo");
        }

        // Combo clientes
        if (comboCliente != null) {
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

        // Combo productos
        if (comboProducto != null) {
            comboProducto.setCellFactory(lv -> new ListCell<Producto>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        // Formato correcto del precio con $
                        String precio = String.format("$%.2f", item.getPrecioVenta());
                        setText(String.format("%s - %s (Stock: %d)",
                                item.getNombre(), precio, item.getStock()));
                    }
                }
            });
            comboProducto.setButtonCell(new ListCell<Producto>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        String precio = String.format("$%.2f", item.getPrecioVenta());
                        setText(String.format("%s - %s", item.getNombre(), precio));
                    }
                }
            });
        }
    }

    private void configurarTabla() {
        if (columnaProducto != null) {
            columnaProducto.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getProducto() != null ?
                                    cellData.getValue().getProducto().getNombre() : ""));
        }

        if (columnaCantidad != null) {
            columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        }

        if (columnaPrecio != null) {
            columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
            columnaPrecio.setCellFactory(column -> new TableCell<DetalleFactura, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            });
        }

        if (columnaSubtotal != null) {
            columnaSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
            columnaSubtotal.setCellFactory(column -> new TableCell<DetalleFactura, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            });
        }

        if (tablaDetalles != null) {
            tablaDetalles.setItems(detalles);

            // Columna de eliminar
            TableColumn<DetalleFactura, Void> columnaAcciones = new TableColumn<>("Acciones");
            columnaAcciones.setMinWidth(100);
            columnaAcciones.setPrefWidth(100);
            columnaAcciones.setCellFactory(col -> new TableCell<DetalleFactura, Void>() {
                private final Button btnEliminar = new Button("Eliminar");

                {
                    btnEliminar.setStyle("-fx-background-color: #e27e8d; -fx-text-fill: white; -fx-font-size: 12px;");
                    btnEliminar.setOnAction(e -> {
                        DetalleFactura detalle = getTableView().getItems().get(getIndex());
                        detalles.remove(detalle);
                        calcularTotales();
                        logger.info("Producto eliminado: {}", detalle.getProducto().getNombre());
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btnEliminar);
                }
            });

            tablaDetalles.getColumns().add(columnaAcciones);
        }
    }

    private void cargarDatos() {
        try {
            ObservableList<Cliente> listaClientes = FXCollections.observableArrayList(clienteDAO.obtenerTodos());
            ObservableList<Producto> listaProductos = FXCollections.observableArrayList(productoDAO.obtenerTodos());

            if (comboCliente != null) comboCliente.setItems(listaClientes);
            if (comboProducto != null) comboProducto.setItems(listaProductos);

            logger.info("Datos cargados: {} clientes, {} productos", listaClientes.size(), listaProductos.size());
        } catch (Exception e) {
            logger.error("Error cargando datos", e);
            mostrarMensaje("Error cargando datos: " + e.getMessage(), false);
        }
    }

    private void configurarEventos() {
        if (btnAgregarProducto != null) {
            btnAgregarProducto.setOnAction(e -> agregarProducto());
        }
        if (btnGuardar != null) {
            btnGuardar.setOnAction(e -> guardarVenta());
        }
        if (btnCancelar != null) {
            btnCancelar.setOnAction(e -> cerrarVentana());
        }

        // Validar solo números
        if (txtCantidad != null) {
            txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    txtCantidad.setText(oldVal);
                }
            });
        }
    }

    @FXML
    private void agregarProducto() {
        Producto producto = comboProducto.getValue();
        String cantidadTexto = txtCantidad.getText().trim();

        if (producto == null) {
            mostrarMensaje("Seleccione un producto", false);
            return;
        }

        if (cantidadTexto.isEmpty()) {
            mostrarMensaje("Ingrese la cantidad", false);
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadTexto);

            if (cantidad <= 0) {
                mostrarMensaje("La cantidad debe ser mayor a 0", false);
                return;
            }

            if (cantidad > producto.getStock()) {
                mostrarMensaje(String.format("Stock insuficiente. Disponible: %d", producto.getStock()), false);
                return;
            }

            // Crear detalle
            DetalleFactura detalle = new DetalleFactura();
            detalle.setProducto(producto);
            detalle.setProductoId(producto.getId());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.calcularSubtotal();

            detalles.add(detalle);
            calcularTotales();

            // Limpiar
            comboProducto.setValue(null);
            txtCantidad.clear();

            logger.info("Producto agregado: {} x{}", producto.getNombre(), cantidad);

        } catch (NumberFormatException e) {
            mostrarMensaje("Cantidad inválida", false);
        }
    }

    private void calcularTotales() {
        double subtotal = 0;
        for (DetalleFactura detalle : detalles) {
            subtotal += detalle.getSubtotal();
        }

        double iva = subtotal * 0.16;
        double total = subtotal + iva;

        if (lblSubtotal != null) {
            lblSubtotal.setText(String.format("$%.2f", subtotal));
        }
        if (lblIVA != null) {
            lblIVA.setText(String.format("$%.2f", iva));
        }
        if (lblTotal != null) {
            lblTotal.setText(String.format("$%.2f", total));
        }
    }

    @FXML
    private void guardarVenta() {
        if (!validarFormulario()) return;

        try {
            Cliente cliente = comboCliente.getValue();
            String numeroFactura = generarNumeroFactura();

            // Crear factura
            factura = new Factura();
            factura.setNumeroFactura(numeroFactura);
            factura.setClienteId(cliente.getId());
            factura.setCliente(cliente);
            factura.setMetodoPago(comboMetodoPago.getValue());
            factura.setFechaEmision(LocalDateTime.now());
            factura.setEstado("Pagada");

            // Calcular totales
            double subtotal = 0;
            for (DetalleFactura detalle : detalles) {
                subtotal += detalle.getSubtotal();
            }
            factura.setSubtotal(subtotal);
            factura.setIva(subtotal * 0.16);
            factura.setTotal(subtotal + (subtotal * 0.16));

            // Guardar en BD
            int facturaId = facturaDAO.guardar(factura);
            factura.setId(facturaId);

            // Guardar detalles y actualizar stock
            for (DetalleFactura detalle : detalles) {
                detalle.setFacturaId(facturaId);
                detalleFacturaDAO.guardar(detalle);

                // Actualizar stock
                Producto producto = detalle.getProducto();
                int nuevoStock = producto.getStock() - detalle.getCantidad();
                productoDAO.actualizarStock(producto.getId(), nuevoStock);
            }

            mostrarMensaje("Venta guardada - Factura: " + numeroFactura, true);
            logger.info("Venta guardada exitosamente: {}", numeroFactura);

            // Cerrar ventana
            javafx.application.Platform.runLater(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ex) {}
                cerrarVentana();
            });

        } catch (Exception e) {
            logger.error("Error guardando venta", e);
            mostrarMensaje("Error al guardar: " + e.getMessage(), false);
        }
    }

    private boolean validarFormulario() {
        if (comboCliente.getValue() == null) {
            mostrarMensaje("Seleccione un cliente", false);
            return false;
        }

        if (detalles.isEmpty()) {
            mostrarMensaje("Agregue al menos un producto", false);
            return false;
        }

        if (comboMetodoPago.getValue() == null) {
            mostrarMensaje("Seleccione un método de pago", false);
            return false;
        }

        return true;
    }

    private String generarNumeroFactura() {
        try {
            int ultimoNumero = facturaDAO.obtenerTodas().size() + 1;
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            return String.format("FAC-%s-%04d", timestamp, ultimoNumero);
        } catch (Exception e) {
            logger.error("Error generando número de factura", e);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            return String.format("FAC-%s", timestamp);
        }
    }

    private void mostrarMensaje(String mensaje, boolean esExito) {
        if (mensajeAviso != null) {
            mensajeAviso.setText(mensaje);
            mensajeAviso.setStyle(esExito ? "-fx-text-fill: green; -fx-font-size: 14px;" : "-fx-text-fill: red; -fx-font-size: 14px;");
        }
    }

    private void cerrarVentana() {
        try {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            logger.error("Error cerrando ventana", e);
        }
    }

    public Factura getFacturaResultado() {
        return factura;
    }
}