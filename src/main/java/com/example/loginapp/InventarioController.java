package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class InventarioController {

    @FXML private ComboBox<String> filtroCategoria;
    @FXML private ComboBox<String> filtroEstadoStock;

    @FXML
    public void initialize() {
        configurarComboBoxes();
    }

    private void configurarComboBoxes() {
        if (filtroCategoria != null) {
            filtroCategoria.getItems().addAll(
                    "Todas", "Medicamentos", "Alimentos",
                    "Accesorios", "Higiene", "Vacunas"
            );
            filtroCategoria.setValue("Todas");
        }

        if (filtroEstadoStock != null) {
            filtroEstadoStock.getItems().addAll(
                    "Todos", "Disponible", "Stock Bajo", "Sin Stock"
            );
            filtroEstadoStock.setValue("Todos");
        }
    }
}