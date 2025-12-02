package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {
    private static final Logger logger = LoggerFactory.getLogger(FacturaDAO.class);

    public List<Factura> obtenerTodas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = """
            SELECT f.*, c.nombre as cliente_nombre, c.apellido_paterno as cliente_apellido 
            FROM facturas f
            LEFT JOIN clientes c ON f.cliente_id = c.id
            ORDER BY f.fecha_emision DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Factura factura = resultSetToFactura(rs);
                factura.setDetalles(obtenerDetallesPorFactura(factura.getId()));
                facturas.add(factura);
            }
            logger.info("Obtenidas {} facturas", facturas.size());
        } catch (SQLException e) {
            logger.error("Error obteniendo facturas", e);
            throw new RuntimeException("Error obteniendo facturas", e);
        }
        return facturas;
    }

    public int guardar(Factura factura) {
        String sql = """
            INSERT INTO facturas (numero_factura, cliente_id, metodo_pago, estado, subtotal, iva, total)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, factura.getNumeroFactura());
            pstmt.setInt(2, factura.getClienteId());
            pstmt.setString(3, factura.getMetodoPago());
            pstmt.setString(4, factura.getEstado());
            pstmt.setDouble(5, factura.getSubtotal());
            pstmt.setDouble(6, factura.getIva());
            pstmt.setDouble(7, factura.getTotal());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error guardando factura, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int nuevoId = generatedKeys.getInt(1);
                    logger.info("Factura guardada con ID: {}", nuevoId);
                    return nuevoId;
                } else {
                    throw new SQLException("Error guardando factura, no se obtuvo ID");
                }
            }
        } catch (SQLException e) {
            logger.error("Error guardando factura", e);
            throw new RuntimeException("Error guardando factura", e);
        }
    }

    public void actualizar(Factura factura) {
        String sql = """
            UPDATE facturas 
            SET numero_factura = ?, cliente_id = ?, metodo_pago = ?, estado = ?, 
                subtotal = ?, iva = ?, total = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, factura.getNumeroFactura());
            pstmt.setInt(2, factura.getClienteId());
            pstmt.setString(3, factura.getMetodoPago());
            pstmt.setString(4, factura.getEstado());
            pstmt.setDouble(5, factura.getSubtotal());
            pstmt.setDouble(6, factura.getIva());
            pstmt.setDouble(7, factura.getTotal());
            pstmt.setInt(8, factura.getId());

            pstmt.executeUpdate();
            logger.info("Factura actualizada: {}", factura.getId());

        } catch (SQLException e) {
            logger.error("Error actualizando factura: {}", factura.getId(), e);
            throw new RuntimeException("Error actualizando factura", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM facturas WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Factura eliminada: {}", id);

        } catch (SQLException e) {
            logger.error("Error eliminando factura: {}", id, e);
            throw new RuntimeException("Error eliminando factura", e);
        }
    }

    public List<Factura> buscar(String criterio) {
        List<Factura> facturas = new ArrayList<>();
        String sql = """
            SELECT f.*, c.nombre as cliente_nombre, c.apellido_paterno as cliente_apellido 
            FROM facturas f
            LEFT JOIN clientes c ON f.cliente_id = c.id
            WHERE f.numero_factura LIKE ? OR c.nombre LIKE ? OR c.apellido_paterno LIKE ?
            ORDER BY f.fecha_emision DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            pstmt.setString(1, likeCriterio);
            pstmt.setString(2, likeCriterio);
            pstmt.setString(3, likeCriterio);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Factura factura = resultSetToFactura(rs);
                factura.setDetalles(obtenerDetallesPorFactura(factura.getId()));
                facturas.add(factura);
            }
            logger.info("Búsqueda '{}' encontró {} facturas", criterio, facturas.size());

        } catch (SQLException e) {
            logger.error("Error buscando facturas: {}", criterio, e);
            throw new RuntimeException("Error buscando facturas", e);
        }
        return facturas;
    }

    private List<DetalleFactura> obtenerDetallesPorFactura(int facturaId) {
        List<DetalleFactura> detalles = new ArrayList<>();
        String sql = """
            SELECT df.*, p.nombre as producto_nombre, p.codigo as producto_codigo
            FROM detalles_factura df
            LEFT JOIN productos p ON df.producto_id = p.id
            WHERE df.factura_id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facturaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DetalleFactura detalle = new DetalleFactura();
                detalle.setId(rs.getInt("id"));
                detalle.setFacturaId(rs.getInt("factura_id"));
                detalle.setProductoId(rs.getInt("producto_id"));
                detalle.setCantidad(rs.getInt("cantidad"));
                detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));
                detalle.setSubtotal(rs.getDouble("subtotal"));

                // Crear producto básico
                Producto producto = new Producto();
                producto.setId(rs.getInt("producto_id"));
                producto.setNombre(rs.getString("producto_nombre"));
                producto.setCodigo(rs.getString("producto_codigo"));
                detalle.setProducto(producto);

                detalles.add(detalle);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo detalles de factura: {}", facturaId, e);
            throw new RuntimeException("Error obteniendo detalles de factura", e);
        }
        return detalles;
    }

    private Factura resultSetToFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        factura.setId(rs.getInt("id"));
        factura.setNumeroFactura(rs.getString("numero_factura"));

        Timestamp timestamp = rs.getTimestamp("fecha_emision");
        if (timestamp != null) {
            factura.setFechaEmision(timestamp.toLocalDateTime());
        }

        factura.setClienteId(rs.getInt("cliente_id"));
        factura.setMetodoPago(rs.getString("metodo_pago"));
        factura.setEstado(rs.getString("estado"));
        factura.setSubtotal(rs.getDouble("subtotal"));
        factura.setIva(rs.getDouble("iva"));
        factura.setTotal(rs.getDouble("total"));

        // Crear cliente básico
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("cliente_id"));
        cliente.setNombre(rs.getString("cliente_nombre"));
        cliente.setApellidoPaterno(rs.getString("cliente_apellido"));
        factura.setCliente(cliente);

        return factura;
    }

    public double obtenerTotalVentasHoy() {
        String sql = "SELECT SUM(total) FROM facturas WHERE DATE(fecha_emision) = DATE('now')";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo total de ventas de hoy", e);
        }
        return 0;
    }

    public double obtenerTotalVentasMes() {
        String sql = "SELECT SUM(total) FROM facturas WHERE strftime('%Y-%m', fecha_emision) = strftime('%Y-%m', 'now')";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo total de ventas del mes", e);
        }
        return 0;
    }
}