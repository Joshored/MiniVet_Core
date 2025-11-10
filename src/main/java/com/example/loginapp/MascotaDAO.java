package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MascotaDAO {
    private static final Logger logger = LoggerFactory.getLogger(MascotaDAO.class);

    public List<Mascota> obtenerTodas() {
        List<Mascota> mascotas = new ArrayList<>();
        String sql = "SELECT * FROM mascotas ORDER BY nombre";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Mascota mascota = resultSetToMascota(rs);
                // NO cargar el dueño completo para evitar recursión
                // Solo establecer un cliente básico con ID
                Cliente clienteBasico = new Cliente();
                clienteBasico.setId(rs.getInt("cliente_id"));
                mascota.setDueno(clienteBasico);

                mascotas.add(mascota);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo mascotas", e);
            throw new RuntimeException("Error obteniendo mascotas", e);
        }
        return mascotas;
    }

    public List<Mascota> obtenerPorCliente(int clienteId) {
        List<Mascota> mascotas = new ArrayList<>();
        String sql = "SELECT * FROM mascotas WHERE cliente_id = ? ORDER BY nombre";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Mascota mascota = resultSetToMascota(rs);
                // NO cargar el dueño completo para evitar recursión
                Cliente clienteBasico = new Cliente();
                clienteBasico.setId(clienteId);
                mascota.setDueno(clienteBasico);

                mascotas.add(mascota);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo mascotas del cliente: {}", clienteId, e);
            throw new RuntimeException("Error obteniendo mascotas del cliente", e);
        }
        return mascotas;
    }

    public Mascota obtenerPorId(int id) {
        String sql = "SELECT * FROM mascotas WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Mascota mascota = resultSetToMascota(rs);
                // NO cargar el dueño completo para evitar recursión
                Cliente clienteBasico = new Cliente();
                clienteBasico.setId(rs.getInt("cliente_id"));
                mascota.setDueno(clienteBasico);

                return mascota;
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo mascota por ID: {}", id, e);
            throw new RuntimeException("Error obteniendo mascota por ID", e);
        }
        return null;
    }

    public int guardar(Mascota mascota) {
        String sql = """
            INSERT INTO mascotas (nombre, especie, edad, raza, sexo, color, numero_chip, esterilizado, sintomas, cliente_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, mascota.getNombre());
            pstmt.setString(2, mascota.getEspecie());
            pstmt.setInt(3, mascota.getEdad());
            pstmt.setString(4, mascota.getRaza());
            pstmt.setString(5, mascota.getSexo());
            pstmt.setString(6, mascota.getColor());
            pstmt.setString(7, mascota.getNumeroChip());
            pstmt.setBoolean(8, mascota.isEsterilizado());
            pstmt.setString(9, mascota.getSintomas());
            pstmt.setInt(10, mascota.getDueno().getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error guardando mascota, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int nuevoId = generatedKeys.getInt(1);
                    logger.info("Mascota guardada con ID: {}", nuevoId);
                    return nuevoId;
                } else {
                    throw new SQLException("Error guardando mascota, no se obtuvo ID");
                }
            }
        } catch (SQLException e) {
            logger.error("Error guardando mascota", e);
            throw new RuntimeException("Error guardando mascota", e);
        }
    }

    public void actualizar(Mascota mascota) {
        String sql = """
            UPDATE mascotas 
            SET nombre = ?, especie = ?, edad = ?, raza = ?, sexo = ?, color = ?, 
                numero_chip = ?, esterilizado = ?, sintomas = ?, cliente_id = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mascota.getNombre());
            pstmt.setString(2, mascota.getEspecie());
            pstmt.setInt(3, mascota.getEdad());
            pstmt.setString(4, mascota.getRaza());
            pstmt.setString(5, mascota.getSexo());
            pstmt.setString(6, mascota.getColor());
            pstmt.setString(7, mascota.getNumeroChip());
            pstmt.setBoolean(8, mascota.isEsterilizado());
            pstmt.setString(9, mascota.getSintomas());
            pstmt.setInt(10, mascota.getDueno().getId());
            pstmt.setInt(11, mascota.getId());

            pstmt.executeUpdate();
            logger.info("Mascota actualizada: {}", mascota.getId());

        } catch (SQLException e) {
            logger.error("Error actualizando mascota: {}", mascota.getId(), e);
            throw new RuntimeException("Error actualizando mascota", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM mascotas WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Mascota eliminada: {}", id);

        } catch (SQLException e) {
            logger.error("Error eliminando mascota: {}", id, e);
            throw new RuntimeException("Error eliminando mascota", e);
        }
    }

    public List<Mascota> buscar(String criterio) {
        List<Mascota> mascotas = new ArrayList<>();
        String sql = """
            SELECT m.* FROM mascotas m
            WHERE m.nombre LIKE ? OR m.especie LIKE ? OR m.raza LIKE ?
            ORDER BY m.nombre
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            pstmt.setString(1, likeCriterio);
            pstmt.setString(2, likeCriterio);
            pstmt.setString(3, likeCriterio);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Mascota mascota = resultSetToMascota(rs);
                // NO cargar el dueño completo para evitar recursión
                Cliente clienteBasico = new Cliente();
                clienteBasico.setId(rs.getInt("cliente_id"));
                mascota.setDueno(clienteBasico);

                mascotas.add(mascota);
            }
        } catch (SQLException e) {
            logger.error("Error buscando mascotas: {}", criterio, e);
            throw new RuntimeException("Error buscando mascotas", e);
        }
        return mascotas;
    }

    private Mascota resultSetToMascota(ResultSet rs) throws SQLException {
        Mascota mascota = new Mascota();
        mascota.setId(rs.getInt("id"));
        mascota.setNombre(rs.getString("nombre"));
        mascota.setEspecie(rs.getString("especie"));
        mascota.setEdad(rs.getInt("edad"));
        mascota.setRaza(rs.getString("raza"));
        mascota.setSexo(rs.getString("sexo"));
        mascota.setColor(rs.getString("color"));
        mascota.setNumeroChip(rs.getString("numero_chip"));
        mascota.setEsterilizado(rs.getBoolean("esterilizado"));
        mascota.setSintomas(rs.getString("sintomas"));
        return mascota;
    }
}