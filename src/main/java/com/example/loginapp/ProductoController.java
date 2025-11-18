package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProductoController {
    @FXML private TextField codigo;
    @FXML private TextField nombre;
    @FXML private TextArea descripcion;
    @FXML private ComboBox<String> categoria;
    @FXML private TextField stock;
    @FXML private TextField stockMinimo;
    @FXML private TextField precioCompra;
    @FXML private TextField precioVenta;
    @FXML private TextField proveedor;
    @FXML private Button btnGuardar;
    @FXML private Label mensajeAviso;

    private Producto productoEdicion;

    @FXML
    public void initialize() {
        // Configurar categorías
        categoria.getItems().addAll(
                "Medicamentos", "Alimentos", "Accesorios",
                "Higiene", "Vacunas", "Equipamiento"
        );

        // Validar solo números en campos numéricos
        stock.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) stock.setText(oldVal);
        });

        stockMinimo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) stockMinimo.setText(oldVal);
        });

        precioCompra.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) precioCompra.setText(oldVal);
        });

        precioVenta.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) precioVenta.setText(oldVal);
        });

        if (btnGuardar != null) btnGuardar.setOnAction(e -> guardarProducto());
    }

    public void setProductoParaEditar(Producto producto) {
        this.productoEdicion = producto;
        if (producto != null) {
            codigo.setText(producto.getCodigo());
            nombre.setText(producto.getNombre());
            descripcion.setText(producto.getDescripcion());
            categoria.setValue(producto.getCategoria());
            stock.setText(String.valueOf(producto.getStock()));
            stockMinimo.setText(String.valueOf(producto.getStockMinimo()));
            precioCompra.setText(String.valueOf(producto.getPrecioCompra()));
            precioVenta.setText(String.valueOf(producto.getPrecioVenta()));
            proveedor.setText(producto.getProveedor());
            btnGuardar.setText("Actualizar");
        }
    }

    @FXML
    public void guardarProducto() {
        if (!validarFormulario()) return;

        try {
            if (productoEdicion == null) productoEdicion = new Producto();

            productoEdicion.setCodigo(codigo.getText().trim());
            productoEdicion.setNombre(nombre.getText().trim());
            productoEdicion.setDescripcion(descripcion.getText().trim());
            productoEdicion.setCategoria(categoria.getValue());
            productoEdicion.setStock(Integer.parseInt(stock.getText().trim()));
            productoEdicion.setStockMinimo(Integer.parseInt(stockMinimo.getText().trim()));
            productoEdicion.setPrecioCompra(Double.parseDouble(precioCompra.getText().trim()));
            productoEdicion.setPrecioVenta(Double.parseDouble(precioVenta.getText().trim()));
            productoEdicion.setProveedor(proveedor.getText().trim());

            mostrarMensaje("Producto guardado correctamente", true);

            javafx.application.Platform.runLater(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                cerrarVentana();
            });

        } catch (NumberFormatException e) {
            mostrarMensaje("Error en los valores numéricos", false);
        } catch (Exception e) {
            mostrarMensaje("Error al guardar: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    public Producto getProductoResultado() { return productoEdicion; }

    private boolean validarFormulario() {
        if (codigo.getText().trim().isEmpty()) {
            mostrarMensaje("El código es obligatorio", false); return false;
        }
        if (nombre.getText().trim().isEmpty()) {
            mostrarMensaje("El nombre es obligatorio", false); return false;
        }
        if (categoria.getValue() == null) {
            mostrarMensaje("Debe seleccionar una categoría", false); return false;
        }
        if (stock.getText().trim().isEmpty()) {
            mostrarMensaje("El stock es obligatorio", false); return false;
        }
        if (stockMinimo.getText().trim().isEmpty()) {
            mostrarMensaje("El stock mínimo es obligatorio", false); return false;
        }
        if (precioCompra.getText().trim().isEmpty()) {
            mostrarMensaje("El precio de compra es obligatorio", false); return false;
        }
        if (precioVenta.getText().trim().isEmpty()) {
            mostrarMensaje("El precio de venta es obligatorio", false); return false;
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