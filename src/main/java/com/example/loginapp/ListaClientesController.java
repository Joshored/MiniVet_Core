// ListaClientesController.java
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ListaClientesController {
    // -------------------- Campos enlazados con FXML --------------------
    // La anotación @FXML indica que estos campos serán inyectados desde el archivo FXML
    @FXML private TableView<Cliente> tablaClientes; // Vista de tabla que mostrará objetos Cliente
    @FXML private TableColumn<Cliente, String> ColumnaApellidoP; // Columna para apellido paterno
    @FXML private TableColumn<Cliente, String> ColumnaApellidoM; // Columna para apellido materno
    @FXML private TableColumn<Cliente, String> ColumnaTelefono; // Columna para teléfono
    @FXML private TableColumn<Cliente, String> ColumnaEmail; // Columna para correo electrónico
    @FXML private TableColumn<Cliente, String> ColumnaDireccion; // Columna para dirección completa
    @FXML private TextField busquedaClientes; // Campo de búsqueda (declarado pero no usado aquí)
    @FXML private Button nuevoCliente; // Botón para crear nuevo cliente
    @FXML private Button editarCliente; // Botón para editar cliente seleccionado
    @FXML private Button eliminarCliente; // Botón para eliminar cliente seleccionado

    // Lista observable que alimenta la TableView. Si se modifica, la vista se actualiza automáticamente.
    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    // Este método se ejecuta automáticamente después de que el FXML ha sido cargado.
    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla indicando la propiedad del modelo Cliente
        // PropertyValueFactory espera el nombre de la propiedad (ej. "apellidoPaterno") y usará
        // el getter correspondiente en la clase Cliente (getApellidoPaterno()).
        ColumnaApellidoP.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        ColumnaApellidoM.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        ColumnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        ColumnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        ColumnaDireccion.setCellValueFactory(new PropertyValueFactory<>("direccionCompleta"));

        // Cargar datos de ejemplo en la lista (esto es solo para pruebas).
        // En una versión final, este método deberá consultar la base de datos y reemplazar el contenido.
        cargarDatosEjemplo();

        // Configurar búsqueda: usar FilteredList para filtrar según texto de busquedaClientes
        FilteredList<Cliente> filteredData = new FilteredList<>(listaClientes, p -> true);
        // Escuchar cambios en el TextField de búsqueda y actualizar el predicate
        if (busquedaClientes != null) {
            busquedaClientes.textProperty().addListener((obs, oldVal, newVal) -> {
                String filtro = (newVal == null) ? "" : newVal.trim().toLowerCase();
                filteredData.setPredicate(cliente -> {
                    if (filtro.isEmpty()) return true; // no filtrar
                    // comprobar varias propiedades (apellido paterno, materno, teléfono, email, dirección)
                    if (cliente.getApellidoPaterno() != null && cliente.getApellidoPaterno().toLowerCase().contains(filtro)) return true;
                    if (cliente.getApellidoMaterno() != null && cliente.getApellidoMaterno().toLowerCase().contains(filtro)) return true;
                    if (cliente.getTelefono() != null && cliente.getTelefono().toLowerCase().contains(filtro)) return true;
                    if (cliente.getEmail() != null && cliente.getEmail().toLowerCase().contains(filtro)) return true;
                    if (cliente.getDireccionCompleta() != null && cliente.getDireccionCompleta().toLowerCase().contains(filtro)) return true;
                    if (cliente.getNombre() != null && cliente.getNombre().toLowerCase().contains(filtro)) return true;
                    return false;
                });
            });
        }

        // SortedList para que la tabla pueda seguir ordenando por columnas
        SortedList<Cliente> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaClientes.comparatorProperty());

        // Asignar la lista ordenada/filtrada a la tabla
        tablaClientes.setItems(sortedData);

        // Configurar manejadores de eventos para los botones.
        // Nuevo cliente -> abrir formulario en modo creación (cliente == null)
        nuevoCliente.setOnAction(e -> abrirFormularioRegistro(null));
        // Editar -> abrir formulario con el cliente seleccionado
        editarCliente.setOnAction(e -> editarClienteSeleccionado());
        // Eliminar -> confirmar y eliminar el cliente seleccionado
        eliminarCliente.setOnAction(e -> eliminarClienteSeleccionado());
    }

    // Añade datos de ejemplo a la lista para poblar la tabla durante desarrollo
    private void cargarDatosEjemplo() {
        // Nota: este método usa datos estáticos. Limpiamos la lista primero para evitar duplicados
        // si se vuelve a llamar tras cerrar formularios.
        listaClientes.clear();
        listaClientes.add(new Cliente("García", "López", "555-1234", "garcia@email.com", "Calle Primavera 123"));
        listaClientes.add(new Cliente("Martínez", "Rodríguez", "555-5678", "martinez@email.com", "Av. Central 456"));
    }

    /*
     * Abre el formulario de registro/edición.
     * Si se pasa un objeto Cliente (no nulo), el formulario se inicializa en modo edición.
     * Se usa FXMLLoader para cargar el FXML del formulario y obtener su controlador.
     */
    private void abrirFormularioRegistro(Cliente cliente) {
        try {
            // Cargar la vista FXML del formulario. Se utiliza HelloApplication como clase de referencia
            // para localizar el recurso "registro-view.fxml".
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("registro-view.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario para pasar el cliente a editar (si aplica).
            RegistroController controller = loader.getController();
            if (cliente != null) {
                controller.setClienteParaEditar(cliente); // pasar datos para edición
            }

            // Crear una nueva ventana (Stage) y mostrarla de forma modal (showAndWait bloquea hasta cerrar).
            Stage stage = new Stage();
            stage.setTitle(cliente == null ? "Nuevo Cliente" : "Editar Cliente");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Obtener el resultado del formulario: si el usuario guardó, controller.getClienteResultado()
            // devolverá el cliente creado/actualizado; si cerró sin guardar devolverá null.
            Cliente resultado = controller.getClienteResultado();
            if (resultado != null) {
                if (cliente == null) {
                    // Nuevo cliente: añadir a la lista observable (la tabla mostrará el cambio)
                    listaClientes.add(resultado);
                } else {
                    // Edición: el objeto pasado fue modificado en clienteEdicion, por lo que la tabla
                    // debería reflejar los cambios automáticamente; forzar refresco por si acaso.
                    tablaClientes.refresh();
                }
            }

        } catch (IOException e) {
            // Mostrar una alerta al usuario si falla la carga del FXML y registrar el error.
            mostrarAlerta("Error", "No se pudo abrir el formulario de registro");
            e.printStackTrace();
        }
    }

    // Maneja la acción de editar el cliente seleccionado en la tabla
    private void editarClienteSeleccionado() {
        // Obtener el elemento seleccionado de la tabla (puede ser null si no hay selección)
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            // Abrir el formulario en modo edición pasando el cliente seleccionado
            abrirFormularioRegistro(clienteSeleccionado);
        } else {
            // Informar al usuario que debe seleccionar un cliente antes de editar
            mostrarAlerta("Advertencia", "Por favor seleccione un cliente para editar");
        }
    }

    // Maneja la eliminación del cliente seleccionado
    private void eliminarClienteSeleccionado() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            // Pedir confirmación al usuario antes de borrar
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar al cliente?");
            alert.setContentText("Esta acción no se puede deshacer");

            // showAndWait devuelve un Optional<ButtonType>. Es más seguro comprobar isPresent() antes de get().
            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                // Eliminar de la lista observable actual; la tabla se actualizará automáticamente.
                listaClientes.remove(clienteSeleccionado);
                // En producción también debe eliminarse de la base de datos aquí.
                mostrarAlerta("Éxito", "Cliente eliminado correctamente");
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor seleccione un cliente para eliminar");
        }
    }

    // Método utilitario para mostrar mensajes al usuario
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null); // sin cabecera
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}