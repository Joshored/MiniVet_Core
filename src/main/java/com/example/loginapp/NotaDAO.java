package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class NotaDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotaDAO.class);

    public String obtenerNota(String tipo) {
        String sql = "SELECT contenido FROM notas WHERE tipo = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String contenido = rs.getString("contenido");
                logger.info("Nota cargada para tipo '{}': {} caracteres", tipo, contenido != null ? contenido.length() : 0);
                return contenido != null ? contenido : "";
            } else {
                logger.warn("No se encontró nota para tipo: {}", tipo);
                return "";
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo nota para tipo: {}", tipo, e);
            return "";
        }
    }

    public boolean guardarNota(String tipo, String contenido) {
        String sql = """
            INSERT OR REPLACE INTO notas (tipo, contenido, fecha_actualizacion)
            VALUES (?, ?, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);
            pstmt.setString(2, contenido);

            int filasAfectadas = pstmt.executeUpdate();
            boolean exito = filasAfectadas > 0;

            if (exito) {
                logger.info("Nota guardada exitosamente para tipo: {} ({} caracteres)", tipo, contenido.length());
            } else {
                logger.error("No se pudo guardar la nota para tipo: {}", tipo);
            }

            return exito;

        } catch (SQLException e) {
            logger.error("Error guardando nota para tipo: {}", tipo, e);
            return false;
        }
    }

    // Método para verificar si la tabla de notas existe
    public boolean tablaNotasExiste() {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='notas'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            return rs.next();

        } catch (SQLException e) {
            logger.error("Error verificando existencia de tabla notas", e);
            return false;
        }
    }
}