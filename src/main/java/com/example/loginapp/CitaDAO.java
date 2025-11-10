package com.example.loginapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    public List<Cita> obtenerTodas() {
        List<Cita> citas = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   cli.nombre as cliente_nombre, cli.apellido_paterno as cliente_apellido,
                   m.nombre as mascota_nombre, m.especie as mascota_especie
            FROM citas c
            LEFT JOIN clientes cli ON c.cliente_id = cli.id
            LEFT JOIN mascotas m ON c.mascota_id = m.id
            ORDER BY c.fecha DESC, c.hora_inicio DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cita cita = resultSetToCita(rs);
                citas.add(cita);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo citas", e);
        }
        return citas;
    }

    public Cita obtenerPorId(int id) {
        String sql = """
            SELECT c.*, 
                   cli.nombre as cliente_nombre, cli.apellido_paterno as cliente_apellido,
                   m.nombre as mascota_nombre, m.especie as mascota_especie
            FROM citas c
            LEFT JOIN clientes cli ON c.cliente_id = cli.id
            LEFT JOIN mascotas m ON c.mascota_id = m.id
            WHERE c.id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultSetToCita(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo cita por ID", e);
        }
        return null;
    }

    public int guardar(Cita cita) {
        String sql = """
            INSERT INTO citas (cliente_id, mascota_id, tipo_servicio, fecha, hora_inicio, 
                              hora_fin, sintomas, descripcion, veterinario, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, cita.getCliente().getId());
            pstmt.setInt(2, cita.getMascota().getId());
            pstmt.setString(3, cita.getTipoServicio());
            pstmt.setDate(4, Date.valueOf(cita.getFecha()));
            pstmt.setString(5, cita.getHoraInicio());
            pstmt.setString(6, cita.getHoraFin());
            pstmt.setString(7, cita.getSintomas());
            pstmt.setString(8, cita.getDescripcion());
            pstmt.setString(9, cita.getVeterinario());
            pstmt.setString(10, cita.getEstado());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error guardando cita, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Error guardando cita, no se obtuvo ID");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando cita", e);
        }
    }

    public void actualizar(Cita cita) {
        String sql = """
            UPDATE citas 
            SET cliente_id = ?, mascota_id = ?, tipo_servicio = ?, fecha = ?, hora_inicio = ?, 
                hora_fin = ?, sintomas = ?, descripcion = ?, veterinario = ?, estado = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cita.getCliente().getId());
            pstmt.setInt(2, cita.getMascota().getId());
            pstmt.setString(3, cita.getTipoServicio());
            pstmt.setDate(4, Date.valueOf(cita.getFecha()));
            pstmt.setString(5, cita.getHoraInicio());
            pstmt.setString(6, cita.getHoraFin());
            pstmt.setString(7, cita.getSintomas());
            pstmt.setString(8, cita.getDescripcion());
            pstmt.setString(9, cita.getVeterinario());
            pstmt.setString(10, cita.getEstado());
            pstmt.setInt(11, cita.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando cita", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM citas WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando cita", e);
        }
    }

    public List<Cita> buscar(String criterio) {
        List<Cita> citas = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   cli.nombre as cliente_nombre, cli.apellido_paterno as cliente_apellido,
                   m.nombre as mascota_nombre, m.especie as mascota_especie
            FROM citas c
            LEFT JOIN clientes cli ON c.cliente_id = cli.id
            LEFT JOIN mascotas m ON c.mascota_id = m.id
            WHERE cli.nombre LIKE ? OR cli.apellido_paterno LIKE ? OR m.nombre LIKE ? 
               OR c.tipo_servicio LIKE ? OR c.veterinario LIKE ? OR c.estado LIKE ?
            ORDER BY c.fecha DESC, c.hora_inicio DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, likeCriterio);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita cita = resultSetToCita(rs);
                citas.add(cita);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando citas", e);
        }
        return citas;
    }

    private Cita resultSetToCita(ResultSet rs) throws SQLException {
        Cita cita = new Cita();
        cita.setId(rs.getInt("id"));
        cita.setTipoServicio(rs.getString("tipo_servicio"));
        cita.setFecha(rs.getDate("fecha").toLocalDate());
        cita.setHoraInicio(rs.getString("hora_inicio"));
        cita.setHoraFin(rs.getString("hora_fin"));
        cita.setSintomas(rs.getString("sintomas"));
        cita.setDescripcion(rs.getString("descripcion"));
        cita.setVeterinario(rs.getString("veterinario"));
        cita.setEstado(rs.getString("estado"));

        // Crear cliente básico
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("cliente_id"));
        cliente.setNombre(rs.getString("cliente_nombre"));
        cliente.setApellidoPaterno(rs.getString("cliente_apellido"));
        cita.setCliente(cliente);

        // Crear mascota básica
        Mascota mascota = new Mascota();
        mascota.setId(rs.getInt("mascota_id"));
        mascota.setNombre(rs.getString("mascota_nombre"));
        mascota.setEspecie(rs.getString("mascota_especie"));
        cita.setMascota(mascota);

        return cita;
    }
}