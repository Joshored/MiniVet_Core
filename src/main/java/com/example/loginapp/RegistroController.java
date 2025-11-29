package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistroController {
    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    // Controles de selección
    @FXML private ComboBox<String> tipoRegistroComboBox;
    @FXML private VBox formularioUsuario;
    @FXML private VBox formularioCliente;

    // Campos de USUARIO
    @FXML private TextField username;
    @FXML private PasswordField contrasena;
    @FXML private PasswordField contrasenaConfirmacion;

    // Campos de CLIENTE
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

    @FXML private Button GuardarRegistro;
    @FXML private Label MensajeAvisoRegistro;

    private Stage stageLogin;
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private Cliente clienteEdicion;
    private Cliente clienteResultado; // Para devolver el resultado

    public void setStageLogin(Stage stageLogin) {
        this.stageLogin = stageLogin;
    }

    // MÉTODO PARA EDITAR CLIENTE EXISTENTE (requerido por ListaClientesController)
    public void setClienteParaEditar(Cliente cliente) {
        this.clienteEdicion = cliente;
        if (cliente != null) {
            // Configurar para modo cliente y cargar datos
            tipoRegistroComboBox.setValue("Cliente");
            cambiarFormulario();

            // Cargar datos del cliente en el formulario
            if (cliente.getApellidoPaterno() != null) ApellidoPaterno.setText(cliente.getApellidoPaterno());
            if (cliente.getApellidoMaterno() != null) ApellidoMaterno.setText(cliente.getApellidoMaterno());
            if (cliente.getNombre() != null) NombreCliente.setText(cliente.getNombre());
            if (cliente.getTelefono() != null) NumeroTel.setText(cliente.getTelefono());
            if (cliente.getEmail() != null) eMail.setText(cliente.getEmail());
            if (cliente.getDireccion() != null) {
                String dir = cliente.getDireccion();
                String[] partes = dir.split(",");
                if (partes.length > 0) {
                    String[] calleNum = partes[0].trim().split(" ");
                    if (calleNum.length > 1) {
                        Calle.setText(calleNum[0]);
                        numCalle.setText(calleNum[calleNum.length - 1]);
                    } else Calle.setText(partes[0].trim());
                }
                if (partes.length > 1) Colonia.setText(partes[1].trim());
            }

            // Cambiar texto del botón para indicar edición
            GuardarRegistro.setText("Actualizar Cliente");
        }
    }

    // MÉTODO REQUERIDO POR ListaClientesController
    public Cliente getClienteResultado() {
        return clienteResultado;
    }

    @FXML
    public void initialize() {
        // Configurar ComboBox de tipo de registro
        tipoRegistroComboBox.getItems().addAll("Usuario", "Cliente");
        tipoRegistroComboBox.setOnAction(e -> cambiarFormulario());

        // Configurar ComboBox de meses
        Mes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        if (GuardarRegistro != null) GuardarRegistro.setOnAction(e -> guardarRegistro());

        // Inicialmente ocultar ambos formularios
        formularioUsuario.setVisible(false);
        formularioCliente.setVisible(false);
    }

    private void cambiarFormulario() {
        String seleccion = tipoRegistroComboBox.getValue();

        if (seleccion == null) {
            formularioUsuario.setVisible(false);
            formularioCliente.setVisible(false);
            return;
        }

        switch (seleccion) {
            case "Usuario":
                formularioUsuario.setVisible(true);
                formularioCliente.setVisible(false);
                GuardarRegistro.setText("Crear Usuario");
                break;
            case "Cliente":
                formularioUsuario.setVisible(false);
                formularioCliente.setVisible(true);
                GuardarRegistro.setText(clienteEdicion != null ? "Actualizar Cliente" : "Crear Cliente");
                break;
        }

        // Limpiar mensajes al cambiar formulario
        MensajeAvisoRegistro.setText("");
    }

    @FXML
    public void guardarRegistroOnAction() {
        guardarRegistro();
    }

    private void guardarRegistro() {
        String tipoRegistro = tipoRegistroComboBox.getValue();

        if (tipoRegistro == null) {
            mostrarError("Por favor seleccione el tipo de registro");
            return;
        }

        switch (tipoRegistro) {
            case "Usuario":
                guardarUsuario();
                break;
            case "Cliente":
                guardarCliente();
                break;
        }
    }

    private void guardarUsuario() {
        if (validarFormularioUsuario()) {
            try {
                String usuario = username.getText().trim();
                String password = contrasena.getText();

                // Verificar si el usuario ya existe
                if (usuarioDAO.existeUsuario(usuario)) {
                    mostrarError("El nombre de usuario ya existe. Por favor elige otro.");
                    return;
                }

                // Crear el usuario
                usuarioDAO.crearUsuario(usuario, password, "");
                logger.info("Usuario creado exitosamente: {}", usuario);

                // Limpiar cliente resultado cuando se crea usuario
                clienteResultado = null;
                mostrarExito("Usuario creado correctamente");

            } catch (Exception e) {
                logger.error("Error al crear usuario", e);
                mostrarError("Error al crear usuario: " + e.getMessage());
            }
        }
    }

    private void guardarCliente() {
        if (validarFormularioCliente()) {
            try {
                Cliente cliente;
                boolean esEdicion = (clienteEdicion != null);

                if (esEdicion) {
                    cliente = clienteEdicion;
                } else {
                    cliente = new Cliente();
                }

                cliente.setApellidoPaterno(ApellidoPaterno.getText().trim());
                cliente.setApellidoMaterno(ApellidoMaterno.getText().trim());
                cliente.setNombre(NombreCliente.getText().trim());
                cliente.setTelefono(NumeroTel.getText().trim());
                cliente.setEmail(eMail.getText().trim());

                String direccion = Calle.getText().trim() + " " + numCalle.getText().trim() + ", " + Colonia.getText().trim();
                cliente.setDireccion(direccion);

                // Guardar o actualizar cliente en base de datos
                if (esEdicion) {
                    clienteDAO.actualizar(cliente);
                    logger.info("Cliente actualizado con ID: {}", cliente.getId());
                    clienteResultado = cliente; // Asignar el resultado
                    mostrarExito("Cliente actualizado correctamente");
                } else {
                    int clienteId = clienteDAO.guardar(cliente);
                    cliente.setId(clienteId);
                    clienteResultado = cliente; // Asignar el resultado
                    logger.info("Cliente guardado con ID: {}", clienteId);
                    mostrarExito("Cliente guardado correctamente");
                }

            } catch (Exception e) {
                logger.error("Error al guardar cliente", e);
                mostrarError("Error al guardar cliente: " + e.getMessage());
            }
        }
    }

    private boolean validarFormularioUsuario() {
        // Validaciones del usuario
        if (username.getText().trim().isEmpty()) {
            mostrarError("El nombre de usuario es obligatorio");
            return false;
        }
        if (contrasena.getText().isEmpty()) {
            mostrarError("La contraseña es obligatoria");
            return false;
        }
        if (contrasenaConfirmacion.getText().isEmpty()) {
            mostrarError("Debe confirmar la contraseña");
            return false;
        }
        if (!contrasena.getText().equals(contrasenaConfirmacion.getText())) {
            mostrarError("Las contraseñas no coinciden");
            return false;
        }
        if (contrasena.getText().length() < 4) {
            mostrarError("La contraseña debe tener al menos 4 caracteres");
            return false;
        }

        return true;
    }

    private boolean validarFormularioCliente() {
        // Validaciones del cliente
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
        if (Calle.getText().trim().isEmpty()) {
            mostrarError("La calle es obligatoria");
            return false;
        }
        if (numCalle.getText().trim().isEmpty()) {
            mostrarError("El número de calle es obligatorio");
            return false;
        }
        if (Colonia.getText().trim().isEmpty()) {
            mostrarError("La colonia es obligatoria");
            return false;
        }
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

    private void mostrarExito(String mensaje) {
        MensajeAvisoRegistro.setText(mensaje);
        MensajeAvisoRegistro.setStyle("-fx-text-fill: green;");

        // Cerrar ventana de registro después de un breve delay
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // Cerrar ventana de registro
            Stage stageRegistro = (Stage) GuardarRegistro.getScene().getWindow();
            stageRegistro.close();

            // Traer el login al frente (si existe)
            if (stageLogin != null) {
                stageLogin.toFront();
                stageLogin.requestFocus();
            }
        });
    }
}