package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DetalleFacturaDAO {
    private static final Logger logger = LoggerFactory.getLogger(DetalleFacturaDAO.class);

    public void guardar(DetalleFactura detalle) {
        String sql = """
            INSERT INTO detalles_factura (factura_id, producto_id, cantidad, precio_unitario, subtotal)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, detalle.getFacturaId());
            pstmt.setInt(2, detalle.getProductoId());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setDouble(4, detalle.getPrecioUnitario());
            pstmt.setDouble(5, detalle.getSubtotal());

            pstmt.executeUpdate();
            logger.info("Detalle de factura guardado para factura: {}", detalle.getFacturaId());

        } catch (SQLException e) {
            logger.error("Error guardando detalle de factura", e);
            throw new RuntimeException("Error guardando detalle de factura", e);
        }
    }

    public void eliminarPorFactura(int facturaId) {
        String sql = "DELETE FROM detalles_factura WHERE factura_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facturaId);
            pstmt.executeUpdate();
            logger.info("Detalles eliminados para factura: {}", facturaId);

        } catch (SQLException e) {
            logger.error("Error eliminando detalles de factura: {}", facturaId, e);
            throw new RuntimeException("Error eliminando detalles de factura", e);
        }
    }

    public void actualizarDetallesFactura(Factura factura) {
        // Primero eliminar los detalles existentes
        eliminarPorFactura(factura.getId());

        // Luego guardar los nuevos detalles
        for (DetalleFactura detalle : factura.getDetalles()) {
            guardar(detalle);
        }
        logger.info("Detalles actualizados para factura: {}", factura.getId());
    }
}