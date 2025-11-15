package com.example.loginapp;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private int stock;
    private int stockMinimo;
    private double precioCompra;
    private double precioVenta;
    private String proveedor;
    private String estado;
    private java.time.LocalDateTime fechaCreacion;

    // Constructores
    public Producto() {}

    public Producto(String codigo, String nombre, String categoria, int stock,
                    int stockMinimo, double precioCompra, double precioVenta,
                    String proveedor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.proveedor = proveedor;
        this.estado = "Activo";
        this.fechaCreacion = java.time.LocalDateTime.now();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getEstado() {
        // Calcular estado basado en stock
        if (stock <= 0) return "Agotado";
        if (stock <= stockMinimo) return "Stock Bajo";
        return "Activo";
    }

    public void setEstado(String estado) { this.estado = estado; }

    public java.time.LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(java.time.LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}