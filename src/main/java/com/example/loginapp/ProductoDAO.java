package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductoDAO.class);

    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos ORDER BY nombre";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productos.add(resultSetToProducto(rs));
            }
            logger.info("Obtenidos {} productos", productos.size());
        } catch (SQLException e) {
            logger.error("Error obteniendo productos", e);
            throw new RuntimeException("Error obteniendo productos", e);
        }
        return productos;
    }

    public Producto obtenerPorId(int id) {
        String sql = "SELECT * FROM productos WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultSetToProducto(rs);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo producto por ID: {}", id, e);
            throw new RuntimeException("Error obteniendo producto por ID", e);
        }
        return null;
    }

    public Producto obtenerPorCodigo(String codigo) {
        String sql = "SELECT * FROM productos WHERE codigo = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultSetToProducto(rs);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo producto por código: {}", codigo, e);
            throw new RuntimeException("Error obteniendo producto por código", e);
        }
        return null;
    }

    public int guardar(Producto producto) {
        String sql = """
            INSERT INTO productos (codigo, nombre, descripcion, categoria, stock, 
                                 stock_minimo, precio_compra, precio_venta, proveedor)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setString(4, producto.getCategoria());
            pstmt.setInt(5, producto.getStock());
            pstmt.setInt(6, producto.getStockMinimo());
            pstmt.setDouble(7, producto.getPrecioCompra());
            pstmt.setDouble(8, producto.getPrecioVenta());
            pstmt.setString(9, producto.getProveedor());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error guardando producto, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int nuevoId = generatedKeys.getInt(1);
                    logger.info("Producto guardado con ID: {}", nuevoId);
                    return nuevoId;
                } else {
                    throw new SQLException("Error guardando producto, no se obtuvo ID");
                }
            }
        } catch (SQLException e) {
            logger.error("Error guardando producto", e);
            throw new RuntimeException("Error guardando producto", e);
        }
    }

    public void actualizar(Producto producto) {
        String sql = """
            UPDATE productos 
            SET codigo = ?, nombre = ?, descripcion = ?, categoria = ?, stock = ?,
                stock_minimo = ?, precio_compra = ?, precio_venta = ?, proveedor = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setString(4, producto.getCategoria());
            pstmt.setInt(5, producto.getStock());
            pstmt.setInt(6, producto.getStockMinimo());
            pstmt.setDouble(7, producto.getPrecioCompra());
            pstmt.setDouble(8, producto.getPrecioVenta());
            pstmt.setString(9, producto.getProveedor());
            pstmt.setInt(10, producto.getId());

            pstmt.executeUpdate();
            logger.info("Producto actualizado: {}", producto.getId());

        } catch (SQLException e) {
            logger.error("Error actualizando producto: {}", producto.getId(), e);
            throw new RuntimeException("Error actualizando producto", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Producto eliminado: {}", id);

        } catch (SQLException e) {
            logger.error("Error eliminando producto: {}", id, e);
            throw new RuntimeException("Error eliminando producto", e);
        }
    }

    public void actualizarStock(int productoId, int nuevoStock) {
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, productoId);
            pstmt.executeUpdate();
            logger.info("Stock actualizado para producto {}: {}", productoId, nuevoStock);

        } catch (SQLException e) {
            logger.error("Error actualizando stock para producto: {}", productoId, e);
            throw new RuntimeException("Error actualizando stock", e);
        }
    }

    public List<Producto> buscar(String criterio) {
        List<Producto> productos = new ArrayList<>();
        String sql = """
            SELECT * FROM productos 
            WHERE codigo LIKE ? OR nombre LIKE ? OR categoria LIKE ? OR proveedor LIKE ?
            ORDER BY nombre
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            pstmt.setString(1, likeCriterio);
            pstmt.setString(2, likeCriterio);
            pstmt.setString(3, likeCriterio);
            pstmt.setString(4, likeCriterio);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                productos.add(resultSetToProducto(rs));
            }
            logger.info("Búsqueda '{}' encontró {} productos", criterio, productos.size());

        } catch (SQLException e) {
            logger.error("Error buscando productos: {}", criterio, e);
            throw new RuntimeException("Error buscando productos", e);
        }
        return productos;
    }

    public List<Producto> obtenerConStockBajo() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE stock <= stock_minimo ORDER BY stock ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productos.add(resultSetToProducto(rs));
            }
            logger.info("Encontrados {} productos con stock bajo", productos.size());

        } catch (SQLException e) {
            logger.error("Error obteniendo productos con stock bajo", e);
            throw new RuntimeException("Error obteniendo productos con stock bajo", e);
        }
        return productos;
    }

    public int contarProductosConStockBajo() {
        String sql = "SELECT COUNT(*) FROM productos WHERE stock <= stock_minimo";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando productos con stock bajo", e);
        }
        return 0;
    }

    private Producto resultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setStock(rs.getInt("stock"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setPrecioCompra(rs.getDouble("precio_compra"));
        producto.setPrecioVenta(rs.getDouble("precio_venta"));
        producto.setProveedor(rs.getString("proveedor"));

        Timestamp timestamp = rs.getTimestamp("fecha_creacion");
        if (timestamp != null) {
            producto.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return producto;
    }
}