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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class InventarioController {
    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> columnaCodigo;
    @FXML private TableColumn<Producto, String> columnaNombre;
    @FXML private TableColumn<Producto, String> columnaCategoria;
    @FXML private TableColumn<Producto, Integer> columnaStock;
    @FXML private TableColumn<Producto, Integer> columnaStockMinimo;
    @FXML private TableColumn<Producto, Double> columnaPrecioCompra;
    @FXML private TableColumn<Producto, Double> columnaPrecioVenta;
    @FXML private TableColumn<Producto, String> columnaEstado;
    @FXML private TableColumn<Producto, String> columnaProveedor;

    @FXML private TextField busquedaProductos;
    @FXML private ComboBox<String> filtroCategoria;
    @FXML private ComboBox<String> filtroEstadoStock;
    @FXML private Label lblStockBajo;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    public void initialize() {
        logger.info("Inicializando controlador de inventario");
        configurarComboBoxes();
        configurarColumnas();
        configurarBusqueda();
        cargarDatos();
        actualizarContadorStockBajo();
    }

    private void configurarComboBoxes() {
        if (filtroCategoria != null) {
            filtroCategoria.getItems().addAll(
                    "Todas", "Medicamentos", "Alimentos",
                    "Accesorios", "Higiene", "Vacunas"
            );
            filtroCategoria.setValue("Todas");
            filtroCategoria.setOnAction(e -> aplicarFiltros());
        }

        if (filtroEstadoStock != null) {
            filtroEstadoStock.getItems().addAll(
                    "Todos", "Disponible", "Stock Bajo", "Sin Stock"
            );
            filtroEstadoStock.setValue("Todos");
            filtroEstadoStock.setOnAction(e -> aplicarFiltros());
        }
    }

    private void configurarColumnas() {
        columnaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        columnaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        columnaStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        columnaPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        columnaPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        columnaProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));

        // Columna estado calculada
        columnaEstado.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado()));

        // Formatear precios
        columnaPrecioCompra.setCellFactory(column -> new TableCell<Producto, Double>() {
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

        columnaPrecioVenta.setCellFactory(column -> new TableCell<Producto, Double>() {
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

        // Colorear filas según estado de stock
        tablaProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setStyle("");
                } else {
                    switch (producto.getEstado()) {
                        case "Agotado":
                            setStyle("-fx-background-color: #ffcdd2;");
                            break;
                        case "Stock Bajo":
                            setStyle("-fx-background-color: #fff9c4;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void configurarBusqueda() {
        FilteredList<Producto> filteredData = new FilteredList<>(listaProductos, p -> true);

        busquedaProductos.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltros();
        });

        SortedList<Producto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(sortedData);
    }

    private void aplicarFiltros() {
        String textoBusqueda = busquedaProductos.getText().toLowerCase();
        String categoriaFiltro = filtroCategoria.getValue();
        String estadoFiltro = filtroEstadoStock.getValue();

        FilteredList<Producto> filteredData = new FilteredList<>(listaProductos, producto -> {
            // Filtro por texto de búsqueda
            if (!textoBusqueda.isEmpty()) {
                boolean coincide = producto.getCodigo().toLowerCase().contains(textoBusqueda) ||
                        producto.getNombre().toLowerCase().contains(textoBusqueda) ||
                        producto.getCategoria().toLowerCase().contains(textoBusqueda) ||
                        producto.getProveedor().toLowerCase().contains(textoBusqueda);
                if (!coincide) return false;
            }

            // Filtro por categoría
            if (!"Todas".equals(categoriaFiltro) && !producto.getCategoria().equals(categoriaFiltro)) {
                return false;
            }

            // Filtro por estado de stock
            if (!"Todos".equals(estadoFiltro)) {
                switch (estadoFiltro) {
                    case "Stock Bajo":
                        if (!"Stock Bajo".equals(producto.getEstado())) return false;
                        break;
                    case "Sin Stock":
                        if (!"Agotado".equals(producto.getEstado())) return false;
                        break;
                    case "Disponible":
                        if (!"Activo".equals(producto.getEstado())) return false;
                        break;
                }
            }

            return true;
        });

        SortedList<Producto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(sortedData);
    }

    private void cargarDatos() {
        try {
            listaProductos.clear();
            listaProductos.addAll(productoDAO.obtenerTodos());
            logger.info("Cargados {} productos", listaProductos.size());
        } catch (Exception e) {
            logger.error("Error cargando productos", e);
            mostrarAlerta("Error", "No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    private void actualizarContadorStockBajo() {
        try {
            int cantidad = productoDAO.contarProductosConStockBajo();
            lblStockBajo.setText("📦 Productos con stock bajo: " + cantidad);
        } catch (Exception e) {
            logger.error("Error actualizando contador de stock bajo", e);
        }
    }

    @FXML
    private void nuevoProducto() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registroProducto-view.fxml"));
            Parent root = loader.load();

            ProductoController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nuevo Producto");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Producto resultado = controller.getProductoResultado();
            if (resultado != null) {
                try {
                    int nuevoId = productoDAO.guardar(resultado);
                    resultado.setId(nuevoId);
                    listaProductos.add(resultado);
                    actualizarContadorStockBajo();
                    mostrarAlerta("Éxito", "Producto guardado correctamente");
                    logger.info("Nuevo producto creado: {}", resultado.getNombre());
                } catch (Exception e) {
                    logger.error("Error guardando producto", e);
                    mostrarAlerta("Error", "No se pudo guardar el producto: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.error("Error abriendo formulario de producto", e);
            mostrarAlerta("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void verDetalles() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            StringBuilder detalles = new StringBuilder();
            detalles.append("Código: ").append(productoSeleccionado.getCodigo()).append("\n");
            detalles.append("Nombre: ").append(productoSeleccionado.getNombre()).append("\n");
            detalles.append("Categoría: ").append(productoSeleccionado.getCategoria()).append("\n");
            detalles.append("Stock: ").append(productoSeleccionado.getStock()).append("\n");
            detalles.append("Stock Mínimo: ").append(productoSeleccionado.getStockMinimo()).append("\n");
            detalles.append("Precio Compra: $").append(String.format("%.2f", productoSeleccionado.getPrecioCompra())).append("\n");
            detalles.append("Precio Venta: $").append(String.format("%.2f", productoSeleccionado.getPrecioVenta())).append("\n");
            detalles.append("Proveedor: ").append(productoSeleccionado.getProveedor()).append("\n");
            detalles.append("Estado: ").append(productoSeleccionado.getEstado());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalles del Producto");
            alert.setHeaderText("Información completa del producto");
            alert.setContentText(detalles.toString());
            alert.showAndWait();
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un producto para ver sus detalles");
        }
    }

    @FXML
    private void ajustarStock() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(productoSeleccionado.getStock()));
            dialog.setTitle("Ajustar Stock");
            dialog.setHeaderText("Ajustar stock para: " + productoSeleccionado.getNombre());
            dialog.setContentText("Nuevo stock:");

            Optional<String> resultado = dialog.showAndWait();
            if (resultado.isPresent()) {
                try {
                    int nuevoStock = Integer.parseInt(resultado.get());
                    productoDAO.actualizarStock(productoSeleccionado.getId(), nuevoStock);
                    productoSeleccionado.setStock(nuevoStock);
                    tablaProductos.refresh();
                    actualizarContadorStockBajo();
                    mostrarAlerta("Éxito", "Stock actualizado correctamente");
                    logger.info("Stock ajustado para {}: {}", productoSeleccionado.getNombre(), nuevoStock);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Por favor ingrese un número válido");
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un producto para ajustar stock");
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar este producto?");
            alert.setContentText(productoSeleccionado.getNombre() + " (" + productoSeleccionado.getCodigo() + ")");

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    productoDAO.eliminar(productoSeleccionado.getId());
                    listaProductos.remove(productoSeleccionado);
                    actualizarContadorStockBajo();
                    mostrarAlerta("Éxito", "Producto eliminado correctamente");
                    logger.info("Producto eliminado: {}", productoSeleccionado.getNombre());
                } catch (Exception e) {
                    logger.error("Error eliminando producto", e);
                    mostrarAlerta("Error", "No se pudo eliminar el producto: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un producto para eliminar");
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