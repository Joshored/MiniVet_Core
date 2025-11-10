package com.example.loginapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClienteDAO {
    private static final Logger logger = LoggerFactory.getLogger(ClienteDAO.class);

    public List<Cliente> obtenerTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre, apellido_paterno";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Primero cargar todos los clientes sin mascotas
            while (rs.next()) {
                Cliente cliente = resultSetToCliente(rs);
                clientes.add(cliente);
            }

            // Luego cargar todas las mascotas y asignarlas a los clientes
            cargarMascotasParaClientes(clientes, conn);

        } catch (SQLException e) {
            logger.error("Error obteniendo clientes", e);
            throw new RuntimeException("Error obteniendo clientes", e);
        }
        return clientes;
    }

    public Cliente obtenerPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Cliente cliente = resultSetToCliente(rs);

                // Cargar mascotas sin crear recursión
                List<Mascota> mascotas = obtenerMascotasPorClienteId(id, conn);
                cliente.setMascotas(mascotas);

                return cliente;
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo cliente por ID: {}", id, e);
            throw new RuntimeException("Error obteniendo cliente por ID", e);
        }
        return null;
    }

    private List<Mascota> obtenerMascotasPorClienteId(int clienteId, Connection conn) {
        List<Mascota> mascotas = new ArrayList<>();
        String sql = "SELECT * FROM mascotas WHERE cliente_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Mascota mascota = resultSetToMascota(rs);
                // NO establecer el dueño aquí para evitar recursión
                mascotas.add(mascota);
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo mascotas del cliente: {}", clienteId, e);
            throw new RuntimeException("Error obteniendo mascotas del cliente", e);
        }
        return mascotas;
    }

    private void cargarMascotasParaClientes(List<Cliente> clientes, Connection conn) {
        if (clientes.isEmpty()) return;

        // Crear mapa de clientes por ID para acceso rápido
        Map<Integer, Cliente> clienteMap = new HashMap<>();
        for (Cliente cliente : clientes) {
            clienteMap.put(cliente.getId(), cliente);
        }

        // Obtener todas las mascotas de estos clientes
        String sql = "SELECT * FROM mascotas WHERE cliente_id IN (" +
                crearPlaceholders(clientes.size()) + ")";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Establecer los parámetros
            for (int i = 0; i < clientes.size(); i++) {
                pstmt.setInt(i + 1, clientes.get(i).getId());
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Mascota mascota = resultSetToMascota(rs);
                int clienteId = rs.getInt("cliente_id");

                Cliente cliente = clienteMap.get(clienteId);
                if (cliente != null) {
                    mascota.setDueno(cliente); // Establecer la referencia al dueño
                    cliente.getMascotas().add(mascota);
                }
            }
        } catch (SQLException e) {
            logger.error("Error cargando mascotas para clientes", e);
            throw new RuntimeException("Error cargando mascotas para clientes", e);
        }
    }

    private String crearPlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    public int guardar(Cliente cliente) {
        String sql = """
            INSERT INTO clientes (nombre, apellido_paterno, apellido_materno, telefono, email, direccion)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellidoPaterno());
            pstmt.setString(3, cliente.getApellidoMaterno());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getEmail());
            pstmt.setString(6, cliente.getDireccion());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error guardando cliente, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int nuevoId = generatedKeys.getInt(1);
                    logger.info("Cliente guardado con ID: {}", nuevoId);
                    return nuevoId;
                } else {
                    throw new SQLException("Error guardando cliente, no se obtuvo ID");
                }
            }
        } catch (SQLException e) {
            logger.error("Error guardando cliente", e);
            throw new RuntimeException("Error guardando cliente", e);
        }
    }

    public void actualizar(Cliente cliente) {
        String sql = """
            UPDATE clientes 
            SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, 
                telefono = ?, email = ?, direccion = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellidoPaterno());
            pstmt.setString(3, cliente.getApellidoMaterno());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getEmail());
            pstmt.setString(6, cliente.getDireccion());
            pstmt.setInt(7, cliente.getId());

            pstmt.executeUpdate();
            logger.info("Cliente actualizado: {}", cliente.getId());

        } catch (SQLException e) {
            logger.error("Error actualizando cliente: {}", cliente.getId(), e);
            throw new RuntimeException("Error actualizando cliente", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Cliente eliminado: {}", id);

        } catch (SQLException e) {
            logger.error("Error eliminando cliente: {}", id, e);
            throw new RuntimeException("Error eliminando cliente", e);
        }
    }

    public List<Cliente> buscar(String criterio) {
        List<Cliente> clientes = new ArrayList<>();
        String sql = """
            SELECT * FROM clientes 
            WHERE nombre LIKE ? OR apellido_paterno LIKE ? OR apellido_materno LIKE ? 
               OR telefono LIKE ? OR email LIKE ? OR direccion LIKE ?
            ORDER BY nombre, apellido_paterno
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, likeCriterio);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cliente cliente = resultSetToCliente(rs);
                clientes.add(cliente);
            }

            // Cargar mascotas para los clientes encontrados
            cargarMascotasParaClientes(clientes, conn);

        } catch (SQLException e) {
            logger.error("Error buscando clientes: {}", criterio, e);
            throw new RuntimeException("Error buscando clientes", e);
        }
        return clientes;
    }

    private Cliente resultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellidoPaterno(rs.getString("apellido_paterno"));
        cliente.setApellidoMaterno(rs.getString("apellido_materno"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setMascotas(new ArrayList<>()); // Inicializar lista vacía
        return cliente;
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