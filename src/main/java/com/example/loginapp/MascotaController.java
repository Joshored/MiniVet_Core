package com.example.loginapp;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MascotaController {
    @FXML private TextField nombreMascota;
    @FXML private TextField Especie;
    @FXML private TextField EdadMascota;
    @FXML private TextField Raza;
    @FXML private ComboBox<String> SexoMascota;
    @FXML private TextField ColorMascota;
    @FXML private TextField numeroChip;
    @FXML private ComboBox<String> esterilizado;
    @FXML private TextArea sintomas;
    @FXML private ComboBox<Cliente> NombreDueno;
    @FXML private Button GuardarRegistroM;
    @FXML private Label MensajeAvisoRegistro;

    private Mascota mascotaEdicion;
    private ObservableList<Cliente> listaClientes;

    @FXML
    public void initialize() {
        SexoMascota.getItems().addAll("Macho", "Hembra");
        esterilizado.getItems().addAll("Sí", "No");

        EdadMascota.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) EdadMascota.setText(newVal.replaceAll("\\D", ""));
            if (newVal.length() > 3) EdadMascota.setText(newVal.substring(0, 3));
        });

        if (GuardarRegistroM != null) GuardarRegistroM.setOnAction(e -> guardarMascota());
    }

    public void setListaClientes(ObservableList<Cliente> clientes) {
        this.listaClientes = clientes;
        if (NombreDueno != null && listaClientes != null) {
            NombreDueno.setItems(listaClientes);
            NombreDueno.setCellFactory(lv -> new ListCell<Cliente>() {
                @Override protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNombreCompleto());
                }
            });
            NombreDueno.setButtonCell(new ListCell<Cliente>() {
                @Override protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNombreCompleto());
                }
            });
        }
    }

    public void setClientePreseleccionado(Cliente cliente) {
        if (cliente != null) NombreDueno.setValue(cliente);
    }

    public void setMascotaParaEditar(Mascota mascota) {
        this.mascotaEdicion = mascota;
        if (mascota != null) {
            nombreMascota.setText(mascota.getNombre());
            Especie.setText(mascota.getEspecie());
            EdadMascota.setText(String.valueOf(mascota.getEdad()));
            Raza.setText(mascota.getRaza());
            SexoMascota.setValue(mascota.getSexo());
            ColorMascota.setText(mascota.getColor());
            numeroChip.setText(mascota.getNumeroChip());
            esterilizado.setValue(mascota.isEsterilizado() ? "Sí" : "No");
            sintomas.setText(mascota.getSintomas());
            if (mascota.getDueno() != null) NombreDueno.setValue(mascota.getDueno());
            GuardarRegistroM.setText("Actualizar");
        }
    }

    @FXML
    public void guardarRegistroMascotaOnAction() { guardarMascota(); }

    private void guardarMascota() {
        if (!validarFormulario()) return;

        try {
            if (mascotaEdicion == null) mascotaEdicion = new Mascota();
            else mascotaEdicion.setId(mascotaEdicion.getId()); // Mantener ID existente

            mascotaEdicion.setNombre(nombreMascota.getText().trim());
            mascotaEdicion.setEspecie(Especie.getText().trim());

            int edad = 0;
            if (!EdadMascota.getText().trim().isEmpty()) edad = Integer.parseInt(EdadMascota.getText().trim());
            mascotaEdicion.setEdad(edad);

            mascotaEdicion.setRaza(Raza.getText().trim());
            mascotaEdicion.setSexo(SexoMascota.getValue());
            mascotaEdicion.setColor(ColorMascota.getText().trim());
            mascotaEdicion.setNumeroChip(numeroChip.getText().trim());
            mascotaEdicion.setEsterilizado(esterilizado.getValue() != null && esterilizado.getValue().equals("Sí"));
            mascotaEdicion.setSintomas(sintomas.getText().trim());

            Cliente duenoSeleccionado = NombreDueno.getValue();
            if (duenoSeleccionado != null) mascotaEdicion.setDueno(duenoSeleccionado);

            MensajeAvisoRegistro.setText("Mascota guardada correctamente");
            MensajeAvisoRegistro.setStyle("-fx-text-fill: green;");

            javafx.application.Platform.runLater(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                cerrarVentana();
            });

        } catch (NumberFormatException e) {
            mostrarError("Error al procesar la edad");
        } catch (Exception e) {
            mostrarError("Error al guardar la mascota: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Mascota getMascotaResultado() { return mascotaEdicion; }

    private boolean validarFormulario() {
        if (nombreMascota.getText().trim().isEmpty()) {
            mostrarError("El nombre de la mascota es obligatorio"); return false;
        }
        if (Especie.getText().trim().isEmpty()) {
            mostrarError("La especie es obligatoria"); return false;
        }
        if (EdadMascota.getText().trim().isEmpty()) {
            mostrarError("La edad es obligatoria"); return false;
        }
        if (SexoMascota.getValue() == null) {
            mostrarError("Debe seleccionar el sexo"); return false;
        }
        if (esterilizado.getValue() == null) {
            mostrarError("Debe indicar si está esterilizado"); return false;
        }
        if (NombreDueno.getValue() == null) {
            mostrarError("Debe seleccionar un dueño"); return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        MensajeAvisoRegistro.setText(mensaje);
        MensajeAvisoRegistro.setStyle("-fx-text-fill: red;");
    }

    private void cerrarVentana() {
        Stage stage = (Stage) GuardarRegistroM.getScene().getWindow();
        stage.close();
    }
}