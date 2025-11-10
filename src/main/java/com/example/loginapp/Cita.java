package com.example.loginapp;

import java.time.LocalDate;

public class Cita {
    private int id;
    private Cliente cliente;
    private Mascota mascota;
    private String tipoServicio;
    private LocalDate fecha;
    private String horaInicio;
    private String horaFin;
    private String sintomas;
    private String descripcion;
    private String veterinario;
    private String estado;

    public Cita() {}

    public Cita(Cliente cliente, Mascota mascota, String tipoServicio, LocalDate fecha,
                String horaInicio, String veterinario) {
        this.cliente = cliente;
        this.mascota = mascota;
        this.tipoServicio = tipoServicio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.veterinario = veterinario;
        this.estado = "Programada";
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Mascota getMascota() { return mascota; }
    public void setMascota(Mascota mascota) { this.mascota = mascota; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getVeterinario() { return veterinario; }
    public void setVeterinario(String veterinario) { this.veterinario = veterinario; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return String.format("Cita %s - %s (%s)", fecha, horaInicio, mascota.getNombre());
    }
}