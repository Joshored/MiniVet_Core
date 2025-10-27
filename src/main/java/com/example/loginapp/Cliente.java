// Cliente.java
package com.example.loginapp;

/**
 * Modelo simple que representa un cliente.
 *
 * Campos:
 * - nombre: nombre(s) del cliente (no se inicializa en el constructor actual)
 * - apellidoPaterno, apellidoMaterno: apellidos utilizados por la vista para mostrar
 * - telefono, email, direccion: datos de contacto
 *
 * Observaciones:
 * - El constructor actual inicializa solo apellidos y datos de contacto; si necesitas
 *   incluir el nombre, añade un parámetro al constructor o usa setNombre().
 * - Este POJO (plain old Java object) se usa en la TableView del controlador para
 *   mostrar propiedades mediante PropertyValueFactory.
 */
public class Cliente {
    // Nombre del cliente (puede estar vacío/null si no se usa)
    private String nombre;

    // Apellidos (usados para mostrar en la tabla)
    private String apellidoPaterno;
    private String apellidoMaterno;

    // Datos de contacto
    private String telefono;
    private String email;

    // Dirección completa (puede ser una cadena con calle, número, ciudad, etc.)
    private String direccion;

    /**
     * Constructor sin argumentos necesario para crear instancias vacías
     * (por ejemplo cuando el formulario crea un nuevo cliente).
     */
    public Cliente() {
    }

    /**
     * Constructor principal usado en el prototipo.
     * Actualmente toma: apellido paterno, apellido materno, teléfono, email y dirección.
     * Nota: no inicializa el campo "nombre".
     */
    public Cliente(String apellidoPaterno, String apellidoMaterno, String telefono, String email, String direccion) {
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    // -------------------- Getters y Setters --------------------
    // Se exponen para que PropertyValueFactory y otras partes de la UI puedan acceder a los valores.

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Método auxiliar que devuelve la dirección completa.
     * Actualmente retorna exactamente el valor de "direccion"; si quieres
     * formatearla (por ejemplo incluir ciudad, código postal) puedes cambiar
     * esta implementación aquí sin afectar a las vistas que la consumen.
     */
    public String getDireccionCompleta() {
        return direccion; // Se puede formatear o construir a partir de varios campos
    }
}