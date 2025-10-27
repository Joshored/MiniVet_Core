// RegistroController.java
package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroController {
    @FXML private TextField ApellidoPaterno;
    @FXML private TextField ApellidoMaterno;
    @FXML private TextField NombreCliente;
    @FXML private TextField Dia;
    @FXML private ComboBox<String> Mes;
    @FXML private TextField Ano;
    @FXML private TextField NumeroTel;
    @FXML private TextField eMail;
    @FXML private TextField Calle;
    @FXML private TextField numCalle;
    @FXML private TextField Colonia;
    @FXML private TextField username;
    @FXML private PasswordField contrasena;
    @FXML private PasswordField contrasenaConfirmacion;
    @FXML private Button GuardarRegistro;
    @FXML private Label MensajeAvisoRegistro;

    private Cliente clienteEdicion;

    @FXML
    public void initialize() {
        // Configurar combo box de meses
        Mes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        // El evento se maneja desde el FXML con onAction="#guardarRegistroOnAction"
        // pero agregamos el manejador aquí también por si acaso
        if (GuardarRegistro != null) {
            GuardarRegistro.setOnAction(e -> guardarCliente());
        }
    }

    public void setClienteParaEditar(Cliente cliente) {
        this.clienteEdicion = cliente;
        if (cliente != null) {
            // Llenar todos los campos con los datos del cliente
            if (cliente.getApellidoPaterno() != null) {
                ApellidoPaterno.setText(cliente.getApellidoPaterno());
            }
            if (cliente.getApellidoMaterno() != null) {
                ApellidoMaterno.setText(cliente.getApellidoMaterno());
            }
            if (cliente.getNombre() != null) {
                NombreCliente.setText(cliente.getNombre());
            }
            if (cliente.getTelefono() != null) {
                NumeroTel.setText(cliente.getTelefono());
            }
            if (cliente.getEmail() != null) {
                eMail.setText(cliente.getEmail());
            }
            if (cliente.getDireccion() != null) {
                // Intentar separar la dirección (esto es simplificado)
                String dir = cliente.getDireccion();
                String[] partes = dir.split(",");
                if (partes.length > 0) {
                    String[] calleNum = partes[0].trim().split(" ");
                    if (calleNum.length > 1) {
                        Calle.setText(calleNum[0]);
                        numCalle.setText(calleNum[calleNum.length - 1]);
                    } else {
                        Calle.setText(partes[0].trim());
                    }
                }
                if (partes.length > 1) {
                    Colonia.setText(partes[1].trim());
                }
            }
        }
    }

    // Método que se llama desde el botón (onAction en FXML)
    @FXML
    public void guardarRegistroOnAction() {
        guardarCliente();
    }

    private void guardarCliente() {
        if (validarFormulario()) {
            if (clienteEdicion == null) {
                clienteEdicion = new Cliente();
            }

            // Asignar todos los valores
            clienteEdicion.setApellidoPaterno(ApellidoPaterno.getText().trim());
            clienteEdicion.setApellidoMaterno(ApellidoMaterno.getText().trim());
            clienteEdicion.setNombre(NombreCliente.getText().trim());
            clienteEdicion.setTelefono(NumeroTel.getText().trim());
            clienteEdicion.setEmail(eMail.getText().trim());

            // Construir dirección
            String direccion = Calle.getText().trim() + " " +
                    numCalle.getText().trim() + ", " +
                    Colonia.getText().trim();
            clienteEdicion.setDireccion(direccion);

            // Informar éxito
            MensajeAvisoRegistro.setText("Cliente guardado correctamente");
            MensajeAvisoRegistro.setStyle("-fx-text-fill: green;");

            // Cerrar ventana después de un breve delay
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Stage stage = (Stage) GuardarRegistro.getScene().getWindow();
                stage.close();
            });
        }
    }

    public Cliente getClienteResultado() {
        return clienteEdicion;
    }

    private boolean validarFormulario() {
        if (ApellidoPaterno.getText().trim().isEmpty()) {
            mostrarError("El apellido paterno es obligatorio");
            return false;
        }

        if (NombreCliente.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }

        if (NumeroTel.getText().trim().isEmpty()) {
            mostrarError("El teléfono es obligatorio");
            return false;
        }

        // Validar email solo si no está vacío
        if (!eMail.getText().trim().isEmpty() && !validarEmail(eMail.getText().trim())) {
            mostrarError("El formato del email no es válido");
            return false;
        }

        return true;
    }

    private boolean validarEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void mostrarError(String mensaje) {
        MensajeAvisoRegistro.setText(mensaje);
        MensajeAvisoRegistro.setStyle("-fx-text-fill: red;");
    }
}