package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeterinarioDAO {
    private static final Logger logger = LoggerFactory.getLogger(VeterinarioDAO.class);

    public void guardar(Veterinario veterinario) {
        String sql = """
            INSERT INTO veterinarios (nombre, apellidos, cedula_profesional, especialidad, telefono, usuario_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, veterinario.getNombre());
            pstmt.setString(2, veterinario.getApellidos());
            pstmt.setString(3, veterinario.getCedula());
            pstmt.setString(4, veterinario.getEspecialidad());
            pstmt.setString(5, veterinario.getTelefono());
            pstmt.setInt(6, veterinario.getUsuarioId());

            pstmt.executeUpdate();
            logger.info("Perfil de veterinario guardado para usuario ID: {}", veterinario.getUsuarioId());

        } catch (SQLException e) {
            logger.error("Error guardando perfil de veterinario", e);
            throw new RuntimeException("Error guardando perfil de veterinario", e);
        }
    }

    public List<Veterinario> obtenerTodos() {
        List<Veterinario> lista = new ArrayList<>();
        String sql = "SELECT * FROM veterinarios ORDER BY apellidos, nombre";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Veterinario vet = new Veterinario();
                vet.setId(rs.getInt("id"));
                vet.setNombre(rs.getString("nombre"));
                vet.setApellidos(rs.getString("apellidos"));
                vet.setCedula(rs.getString("cedula_profesional"));
                vet.setEspecialidad(rs.getString("especialidad"));
                vet.setTelefono(rs.getString("telefono"));
                vet.setUsuarioId(rs.getInt("usuario_id"));
                lista.add(vet);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo veterinarios", e);
        }
        return lista;
    }
}