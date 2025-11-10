package com.example.loginapp;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private int id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String email;
    private String direccion;
    private List<Mascota> mascotas;

    public Cliente() {
        this.mascotas = new ArrayList<>();
    }

    public Cliente(String nombre, String apellidoPaterno, String apellidoMaterno,
                   String telefono, String email, String direccion) {
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.mascotas = new ArrayList<>();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getDireccionCompleta() { return direccion != null ? direccion : ""; }

    public List<Mascota> getMascotas() {
        if (mascotas == null) mascotas = new ArrayList<>();
        return mascotas;
    }

    public void setMascotas(List<Mascota> mascotas) { this.mascotas = mascotas; }

    public void agregarMascota(Mascota mascota) {
        if (mascotas == null) mascotas = new ArrayList<>();
        mascotas.add(mascota);
        mascota.setDueno(this);
    }

    public void eliminarMascota(Mascota mascota) {
        if (mascotas != null) mascotas.remove(mascota);
    }

    public int getCantidadMascotas() {
        return mascotas != null ? mascotas.size() : 0;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidoPaterno +
                (apellidoMaterno != null && !apellidoMaterno.isEmpty() ? " " + apellidoMaterno : "");
    }

    @Override
    public String toString() { return getNombreCompleto(); }
}