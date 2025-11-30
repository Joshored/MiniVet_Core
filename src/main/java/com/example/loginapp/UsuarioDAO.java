package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UsuarioDAO {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAO.class);

    // Valida el usuario y retorna su rol si es exitoso, o null si falla
    public String validarUsuarioYObtenerRol(String username, String password) {
        String sql = "SELECT role FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role"); // Retorna "Administrador", "Veterinario", etc.
            }

        } catch (SQLException e) {
            logger.error("Error validando usuario", e);
            // Fallback temporal para tu admin hardcodeado si la BD falla
            if ("admin".equals(username) && "admin123".equals(password)) {
                return "Administrador";
            }
        }
        return null; // Login fallido
    }

    // Método actualizado para recibir 'role'
    public void crearUsuario(String username, String password, String email, String role) {
        // Asegúrate de que tu tabla 'usuarios' tenga la columna 'role' (ver paso anterior de DatabaseConfig)
        String sql = "INSERT INTO usuarios (username, password, email, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, role); // Insertar el rol

            pstmt.executeUpdate();
            logger.info("Usuario creado: {} [{}]", username, role);

        } catch (SQLException e) {
            logger.error("Error creando usuario: {}", username, e);
            throw new RuntimeException("Error creando usuario", e);
        }
    }

    public boolean existeUsuario(String username) {
        String sql = "SELECT COUNT(*) as count FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;

        } catch (SQLException e) {
            logger.error("Error verificando usuario: {}", username, e);
            return false;
        }
    }

    public boolean verificarConexionBD() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error verificando conexión a BD", e);
            return false;
        }
    }
}