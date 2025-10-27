// RegistroController.java
package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroController {
    // -------------------- Campos enlazados con FXML --------------------
    // Los nombres de las variables deben coincidir con los fx:id del FXML.
    @FXML private TextField ApellidoPaterno; // campo para apellido paterno
    @FXML private TextField ApellidoMaterno; // campo para apellido materno
    @FXML private TextField NombreCliente; // campo para nombre(s)
    @FXML private TextField Dia; // día de nacimiento (como texto)
    @FXML private ComboBox<String> Mes; // mes de nacimiento (ComboBox)
    @FXML private TextField Ano; // año de nacimiento
    @FXML private TextField NumeroTel; // número telefónico
    @FXML private TextField eMail; // correo electrónico
    @FXML private TextField Calle; // nombre de la calle
    @FXML private TextField numCalle; // número de la calle
    @FXML private TextField Colonia; // colonia o barrio
    @FXML private TextField username; // nombre de usuario (si aplica)
    @FXML private PasswordField contrasena; // contraseña
    @FXML private PasswordField contrasenaConfirmacion; // confirmación de contraseña
    @FXML private Button GuardarRegistro; // botón para guardar los datos
    @FXML private Label MensajeAvisoRegistro; // etiqueta para mostrar mensajes/errores

    // Si se abre el formulario para editar, aquí se guarda la referencia al cliente a editar
    private Cliente clienteEdicion;

    // Este método se ejecuta al inicializar el controlador (después de cargar el FXML)
    @FXML
    public void initialize() {
        // Configurar combo box de meses con los nombres en español
        Mes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        // Configurar el manejador del botón Guardar: llama a guardarCliente() cuando se pulsa
        GuardarRegistro.setOnAction(e -> guardarCliente());
    }

    /**
     * Método llamado por `ListaClientesController` cuando se desea editar un cliente.
     * Recibe el cliente y rellena los campos del formulario con sus datos.
     */
    public void setClienteParaEditar(Cliente cliente) {
        this.clienteEdicion = cliente;
        // Llenar los campos con los datos del cliente (si no es null)
        if (cliente != null) {
            ApellidoPaterno.setText(cliente.getApellidoPaterno());
            ApellidoMaterno.setText(cliente.getApellidoMaterno());
            // Nota: aquí debes completar el resto de campos según los getters del modelo Cliente
            // Por ejemplo: NombreCliente.setText(cliente.getNombre());
            // Fecha de nacimiento, dirección, teléfono, email, etc.
        }
    }

    /**
     * Recolecta datos del formulario, valida y crea/actualiza el objeto Cliente.
     * Actualmente no implementa persistencia: se debe guardar en BD donde aparece el comentario.
     */
    private void guardarCliente() {
        if (validarFormulario()) {
            // Crear o actualizar cliente: si clienteEdicion existe se actualiza, si no, crear y
            // asignarlo a clienteEdicion para que pueda recuperarse desde el controlador llamador.
            if (clienteEdicion == null) {
                clienteEdicion = new Cliente();
            }

            // Asignar valores desde los campos del formulario al modelo (clienteEdicion)
            clienteEdicion.setApellidoPaterno(ApellidoPaterno.getText());
            clienteEdicion.setApellidoMaterno(ApellidoMaterno.getText());
            clienteEdicion.setNombre(NombreCliente.getText());
            clienteEdicion.setTelefono(NumeroTel.getText());
            clienteEdicion.setEmail(eMail.getText());

            // Construir la dirección completa a partir de varios campos
            String direccion = Calle.getText() + " " + numCalle.getText() + ", " + Colonia.getText();
            clienteEdicion.setDireccion(direccion);

            // TODO: Aquí se debería invocar el servicio/DAO para persistir el cliente en la base de datos

            // Informar al usuario que se guardó correctamente
            MensajeAvisoRegistro.setText("Cliente guardado correctamente");
            MensajeAvisoRegistro.setStyle("-fx-text-fill: green;");

            // Cerrar la ventana actual (obtener el Stage del botón Guardar)
            Stage stage = (Stage) GuardarRegistro.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Devuelve el cliente creado o modificado por este formulario.
     * Si el usuario cerró el formulario sin guardar, devolverá null.
     */
    public Cliente getClienteResultado() {
        return clienteEdicion;
    }

    /**
     * Valida los campos mínimos del formulario. Devuelve true si todo es válido.
     */
    private boolean validarFormulario() {
        // Validaciones básicas: comprobar que campos obligatorios no estén vacíos
        if (ApellidoPaterno.getText().isEmpty()) {
            mostrarError("El apellido paterno es obligatorio");
            return false;
        }

        if (NombreCliente.getText().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }

        if (NumeroTel.getText().isEmpty()) {
            mostrarError("El teléfono es obligatorio");
            return false;
        }

        // Validar formato de email si se proporcionó uno
        if (!eMail.getText().isEmpty() && !validarEmail(eMail.getText())) {
            mostrarError("El formato del email no es válido");
            return false;
        }

        // Si todas las validaciones pasan, devolver true
        return true;
    }

    // Validación simple del formato de email usando una expresión regular básica
    private boolean validarEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    // Muestra un mensaje de error en la etiqueta correspondiente y lo pinta en rojo
    private void mostrarError(String mensaje) {
        MensajeAvisoRegistro.setText(mensaje);
        MensajeAvisoRegistro.setStyle("-fx-text-fill: red;");
    }
}