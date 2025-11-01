package com.example.loginapp;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

// Controlador para la ventana de registro/edición de mascotas
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

    // Inicializa el controlador y configura los componentes del formulario
    @FXML
    public void initialize() {
        // Configurar ComboBox de sexo
        SexoMascota.getItems().addAll("Macho", "Hembra");

        // Configurar ComboBox de esterilizado
        esterilizado.getItems().addAll("Sí", "No");

        // Validar que edad solo acepte números
        EdadMascota.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                EdadMascota.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 3) {
                EdadMascota.setText(newVal.substring(0, 3));
            }
        });

        // Configurar botón guardar
        if (GuardarRegistroM != null) {
            GuardarRegistroM.setOnAction(e -> guardarMascota());
        }
    }

    // Establece la lista de clientes para el ComboBox
    public void setListaClientes(ObservableList<Cliente> clientes) {
        this.listaClientes = clientes;
        NombreDueno.setItems(clientes);
    }

    // Establece un cliente preseleccionado en el ComboBox
    public void setClientePreseleccionado(Cliente cliente) {
        if (cliente != null) {
            NombreDueno.setValue(cliente);
        }
    }

    // Configura el formulario para editar una mascota existente
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

            if (mascota.getDueno() != null) {
                NombreDueno.setValue(mascota.getDueno());
            }

            GuardarRegistroM.setText("Actualizar");
        }
    }

    // Maneja la acción del botón guardar
    @FXML
    public void guardarRegistroMascotaOnAction() {
        guardarMascota();
    }

    private void guardarMascota() {
        if (!validarFormulario()) {
            return;
        }

        try {
            // Crear nueva mascota o usar la existente
            if (mascotaEdicion == null) {
                mascotaEdicion = new Mascota();
            }

            // Asignar valores básicos
            mascotaEdicion.setNombre(nombreMascota.getText().trim());
            mascotaEdicion.setEspecie(Especie.getText().trim());

            // Convertir edad a entero
            int edad = 0;
            if (!EdadMascota.getText().trim().isEmpty()) {
                edad = Integer.parseInt(EdadMascota.getText().trim());
            }
            mascotaEdicion.setEdad(edad);

            mascotaEdicion.setRaza(Raza.getText().trim());
            mascotaEdicion.setSexo(SexoMascota.getValue());
            mascotaEdicion.setColor(ColorMascota.getText().trim());
            mascotaEdicion.setNumeroChip(numeroChip.getText().trim());

            // Convertir esterilizado a boolean
            mascotaEdicion.setEsterilizado(
                    esterilizado.getValue() != null && esterilizado.getValue().equals("Sí")
            );

            mascotaEdicion.setSintomas(sintomas.getText().trim());

            // Asignar dueño y establecer relación
            Cliente duenoSeleccionado = NombreDueno.getValue();
            if (duenoSeleccionado != null) {
                mascotaEdicion.setDueno(duenoSeleccionado);
                // Agregar mascota a la lista del cliente si es nueva
                if (!duenoSeleccionado.getMascotas().contains(mascotaEdicion)) {
                    duenoSeleccionado.agregarMascota(mascotaEdicion);
                }
            }

            // Mostrar mensaje de éxito
            MensajeAvisoRegistro.setText("Mascota guardada correctamente");
            MensajeAvisoRegistro.setStyle("-fx-text-fill: green;");

            // Cerrar ventana después de un delay
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                cerrarVentana();
            });

        } catch (NumberFormatException e) {
            mostrarError("Error al procesar la edad");
        } catch (Exception e) {
            mostrarError("Error al guardar la mascota: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Obtiene la mascota resultante después de guardar/editar
    public Mascota getMascotaResultado() {
        return mascotaEdicion;
    }

    // Valida los campos del formulario
    private boolean validarFormulario() {
        if (nombreMascota.getText().trim().isEmpty()) {
            mostrarError("El nombre de la mascota es obligatorio");
            return false;
        }

        if (Especie.getText().trim().isEmpty()) {
            mostrarError("La especie es obligatoria");
            return false;
        }

        if (EdadMascota.getText().trim().isEmpty()) {
            mostrarError("La edad es obligatoria");
            return false;
        }

        if (SexoMascota.getValue() == null) {
            mostrarError("Debe seleccionar el sexo");
            return false;
        }

        if (esterilizado.getValue() == null) {
            mostrarError("Debe indicar si está esterilizado");
            return false;
        }

        if (NombreDueno.getValue() == null) {
            mostrarError("Debe seleccionar un dueño");
            return false;
        }

        return true;
    }

    // Muestra un mensaje de error en el formulario
    private void mostrarError(String mensaje) {
        MensajeAvisoRegistro.setText(mensaje);
        MensajeAvisoRegistro.setStyle("-fx-text-fill: red;");
    }

    // Cierra la ventana actual
    private void cerrarVentana() {
        Stage stage = (Stage) GuardarRegistroM.getScene().getWindow();
        stage.close();
    }
}