package com.example.loginapp;

public class SesionUsuario {
    private static SesionUsuario instance;

    private String username;
    private String role;

    private SesionUsuario() {}

    public static SesionUsuario get() {
        if (instance == null) {
            instance = new SesionUsuario();
        }
        return instance;
    }

    public void iniciarSesion(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void cerrarSesion() {
        this.username = null;
        this.role = null;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }
}