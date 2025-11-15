package com.example.loginapp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private int id;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private int clienteId;
    private Cliente cliente;
    private String metodoPago;
    private String estado;
    private double subtotal;
    private double iva;
    private double total;
    private List<DetalleFactura> detalles;
    private LocalDateTime createdAt;

    // Constructores
    public Factura() {
        this.detalles = new ArrayList<>();
        this.fechaEmision = LocalDateTime.now();
        this.estado = "Pendiente";
        this.createdAt = LocalDateTime.now();
    }

    public Factura(String numeroFactura, int clienteId, String metodoPago) {
        this();
        this.numeroFactura = numeroFactura;
        this.clienteId = clienteId;
        this.metodoPago = metodoPago;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Metodo para agregar detalle
    public void agregarDetalle(DetalleFactura detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
        detalle.setFacturaId(this.id);
    }

    // Metodo para calcular totales
    public void calcularTotales() {
        this.subtotal = 0;
        if (this.detalles != null) {
            for (DetalleFactura detalle : this.detalles) {
                this.subtotal += detalle.getSubtotal();
            }
        }
        this.iva = this.subtotal * 0.16; // 16% de IVA
        this.total = this.subtotal + this.iva;
    }

    @Override
    public String toString() {
        return "Factura #" + numeroFactura + " - " + (cliente != null ? cliente.getNombreCompleto() : "Cliente");
    }
}