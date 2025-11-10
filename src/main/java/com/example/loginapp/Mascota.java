package com.example.loginapp;

public class Mascota {
    private int id;
    private String nombre;
    private String especie;
    private int edad;
    private String raza;
    private String sexo;
    private String color;
    private String numeroChip;
    private boolean esterilizado;
    private String sintomas;
    private Cliente dueno;

    public Mascota() {}

    public Mascota(String nombre, String especie, int edad, String raza,
                   String sexo, String color, String numeroChip,
                   boolean esterilizado, String sintomas, Cliente dueno) {
        this.nombre = nombre;
        this.especie = especie;
        this.edad = edad;
        this.raza = raza;
        this.sexo = sexo;
        this.color = color;
        this.numeroChip = numeroChip;
        this.esterilizado = esterilizado;
        this.sintomas = sintomas;
        this.dueno = dueno;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getNumeroChip() { return numeroChip; }
    public void setNumeroChip(String numeroChip) { this.numeroChip = numeroChip; }

    public boolean isEsterilizado() { return esterilizado; }
    public void setEsterilizado(boolean esterilizado) { this.esterilizado = esterilizado; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public Cliente getDueno() { return dueno; }
    public void setDueno(Cliente dueno) { this.dueno = dueno; }

    public String getNombreDueno() {
        if (dueno != null) {
            if (dueno.getNombre() != null && dueno.getApellidoPaterno() != null) {
                return dueno.getNombre() + " " + dueno.getApellidoPaterno();
            } else if (dueno.getId() > 0) {
                return "Cliente #" + dueno.getId();
            }
        }
        return "Sin dueño";
    }

    public String getEsterilizadoTexto() { return esterilizado ? "Sí" : "No"; }

    @Override
    public String toString() { return nombre + " (" + especie + ")"; }
}