package filey.modelo.persistencia;

import filey.modelo.datos.Editorial;
import filey.modelo.datos.Pago;
import filey.modelo.datos.Reserva;
import filey.modelo.datos.Stand;
import filey.modelo.excepciones.OperacionNoPermitidaException;
import filey.modelo.excepciones.RegistroDuplicadoException;
import filey.modelo.excepciones.RegistroNoEncontradoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Date;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Todo el SQL del sistema vive aquí; la conexión la da ConexionBD. */
public final class RepositorioFiley {

    // =============== CRUD de editoriales ===============

    /** READ: todas las editoriales (usuario de solo lectura). */
    public List<Editorial> cargarEditoriales() throws SQLException {
        String sql = "SELECT id, nombre FROM editorial ORDER BY id";
        List<Editorial> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Editorial(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    /** CREATE. */
    public void insertarEditorial(Editorial editorial) throws SQLException {
        String sql = "INSERT INTO editorial (id, nombre) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, editorial.getId());
            ps.setString(2, editorial.getNombre());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RegistroDuplicadoException(
                    "La editorial " + editorial.getId() + " ya existe en la base de datos.");
        }
    }

    /** UPDATE. */
    public void actualizarEditorial(Editorial editorial) throws SQLException {
        String sql = "UPDATE editorial SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, editorial.getNombre());
            ps.setInt(2, editorial.getId());
            if (ps.executeUpdate() == 0) {
                throw new RegistroNoEncontradoException(
                        "La editorial " + editorial.getId() + " no está en la base de datos.");
            }
        }
    }

    /** DELETE. Si la editorial tiene reservas, MySQL lo rechaza por la clave foránea. */
    public void eliminarEditorial(int id) throws SQLException {
        String sql = "DELETE FROM editorial WHERE id = ?";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new RegistroNoEncontradoException(
                        "La editorial " + id + " no está en la base de datos.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar la editorial " + id + ": tiene reservas asociadas.");
        }
    }

    // =============== CRUD de stands ===============

    public List<Stand> cargarStands() throws SQLException {
        String sql = "SELECT id, ancho, largo, precio_m2, estado FROM stand ORDER BY id";
        List<Stand> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Stand stand = new Stand(rs.getInt("id"),
                        rs.getBigDecimal("ancho"),
                        rs.getBigDecimal("largo"),
                        rs.getBigDecimal("precio_m2"));
                stand.restaurarEstado(
                        filey.modelo.datos.EstadoStand.valueOf(rs.getString("estado")));
                lista.add(stand);
            }
        }
        return lista;
    }

    public void insertarStand(Stand stand) throws SQLException {
        String sql = "INSERT INTO stand (id, ancho, largo, precio_m2, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stand.getId());
            ps.setBigDecimal(2, stand.getAncho());
            ps.setBigDecimal(3, stand.getLargo());
            ps.setBigDecimal(4, stand.getPrecioPorMetroCuadrado());
            ps.setString(5, stand.getEstado().name());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RegistroDuplicadoException(
                    "El stand " + stand.getId() + " ya existe en la base de datos.");
        }
    }

    public void actualizarStand(Stand stand) throws SQLException {
        String sql = "UPDATE stand SET ancho = ?, largo = ?, precio_m2 = ?, estado = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, stand.getAncho());
            ps.setBigDecimal(2, stand.getLargo());
            ps.setBigDecimal(3, stand.getPrecioPorMetroCuadrado());
            ps.setString(4, stand.getEstado().name());
            ps.setInt(5, stand.getId());
            if (ps.executeUpdate() == 0) {
                throw new RegistroNoEncontradoException(
                        "El stand " + stand.getId() + " no está en la base de datos.");
            }
        }
    }

    public void eliminarStand(int id) throws SQLException {
        String sql = "DELETE FROM stand WHERE id = ?";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new RegistroNoEncontradoException(
                        "El stand " + id + " no está en la base de datos.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el stand " + id + ": tiene reservas asociadas.");
        }
    }

    // =============== Reservas y pagos ===============

    public List<Reserva> cargarReservas(List<Editorial> editoriales, List<Stand> stands) throws SQLException {
        Map<Integer, Editorial> porEditorial = new HashMap<>();
        for (Editorial editorial : editoriales) {
            porEditorial.put(editorial.getId(), editorial);
        }
        Map<Integer, Stand> porStand = new HashMap<>();
        for (Stand stand : stands) {
            porStand.put(stand.getId(), stand);
        }

        String sql = "SELECT id, id_editorial, id_stand, fecha_creacion, vencimiento, "
                + "limite_liquidacion, vencida FROM reserva ORDER BY id";
        List<Reserva> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Editorial editorial = porEditorial.get(rs.getInt("id_editorial"));
                Stand stand = porStand.get(rs.getInt("id_stand"));
                if (editorial == null || stand == null) {
                    throw new SQLException("La reserva referencia una entidad inexistente.");
                }
                Reserva reserva = new Reserva(rs.getInt("id"),
                        editorial, stand,
                        rs.getObject("fecha_creacion", LocalDate.class),
                        rs.getObject("vencimiento", LocalDate.class),
                        rs.getObject("limite_liquidacion", LocalDate.class));
                reserva.restaurarVencida(rs.getBoolean("vencida"));
                lista.add(reserva);
            }
        }
        for (Reserva reserva : lista) {
            cargarPagos(reserva);
            reserva.getStand().restaurarReserva(reserva,
                    reserva.isVencida() ? filey.modelo.datos.EstadoStand.DISPONIBLE
                            : reserva.alcanzaMinimo() ? filey.modelo.datos.EstadoStand.OCUPADO
                            : filey.modelo.datos.EstadoStand.RESERVADO);
        }
        return lista;
    }

    private void cargarPagos(Reserva reserva) throws SQLException {
        String sql = "SELECT importe, fecha FROM pago WHERE id_reserva = ? ORDER BY fecha";
        try (Connection con = ConexionBD.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reserva.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reserva.restaurarPago(new Pago(rs.getBigDecimal("importe"),
                            rs.getObject("fecha", LocalDate.class)));
                }
            }
        }
    }

    public void insertarReserva(Reserva reserva) throws SQLException {
        try (Connection con = ConexionBD.obtenerEscritura()) {
            con.setAutoCommit(false);
            try {
                String sqlReserva = "INSERT INTO reserva (id, id_editorial, id_stand, fecha_creacion, vencimiento, "
                        + "limite_liquidacion, vencida) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlReserva)) {
                    ps.setInt(1, reserva.getId());
                    ps.setInt(2, reserva.getEditorial().getId());
                    ps.setInt(3, reserva.getStand().getId());
                    ps.setObject(4, reserva.getFechaCreacion());
                    ps.setObject(5, reserva.getVencimiento());
                    ps.setObject(6, reserva.getLimiteLiquidacion());
                    ps.setBoolean(7, reserva.isVencida());
                    ps.executeUpdate();
                }
                actualizarStandEnTransaccion(con, reserva.getStand());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void actualizarStandEnTransaccion(Connection con, Stand stand) throws SQLException {
        String sql = "UPDATE stand SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stand.getEstado().name());
            ps.setInt(2, stand.getId());
            if (ps.executeUpdate() == 0) {
                throw new RegistroNoEncontradoException(
                        "El stand " + stand.getId() + " no está en la base de datos.");
            }
        }
    }

    public void insertarPago(int idReserva, Pago pago) throws SQLException {
        String sql = "INSERT INTO pago (id_reserva, importe, fecha) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtenerEscritura();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.setBigDecimal(2, pago.getImporte());
            ps.setObject(3, pago.getFecha());
            ps.executeUpdate();
        }
    }

    // =============== Guardado completo (el que ya tenías) ===============

    public void guardar(List<Editorial> editoriales, List<Stand> stands, List<Reserva> reservas)
            throws SQLException {
        try (Connection con = ConexionBD.obtenerEscritura()) {
            con.setAutoCommit(false);
            try {
                guardarEditoriales(con, editoriales);
                guardarStands(con, stands);
                guardarReservas(con, reservas);
                guardarPagos(con, reservas);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void guardarEditoriales(Connection con, List<Editorial> editoriales) throws SQLException {
        String sql = "INSERT INTO editorial (id, nombre) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Editorial e : editoriales) {
                ps.setInt(1, e.getId());
                ps.setString(2, e.getNombre());
                ps.executeUpdate();
            }
        }
    }

    private void guardarStands(Connection con, List<Stand> stands) throws SQLException {
        String sql = "INSERT INTO stand (id, ancho, largo, precio_m2, estado) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE ancho = VALUES(ancho), largo = VALUES(largo), "
                + "precio_m2 = VALUES(precio_m2), estado = VALUES(estado)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Stand s : stands) {
                ps.setInt(1, s.getId());
                ps.setBigDecimal(2, s.getAncho());
                ps.setBigDecimal(3, s.getLargo());
                ps.setBigDecimal(4, s.getPrecioPorMetroCuadrado());
                ps.setString(5, s.getEstado().name());
                ps.executeUpdate();
            }
        }
    }

    private void guardarReservas(Connection con, List<Reserva> reservas) throws SQLException {
        String sql = "INSERT INTO reserva (id, id_editorial, id_stand, fecha_creacion, vencimiento, "
                + "limite_liquidacion, vencida) VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE id_editorial = VALUES(id_editorial), "
                + "id_stand = VALUES(id_stand), fecha_creacion = VALUES(fecha_creacion), "
                + "vencimiento = VALUES(vencimiento), limite_liquidacion = VALUES(limite_liquidacion), "
                + "vencida = VALUES(vencida)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Reserva r : reservas) {
                ps.setInt(1, r.getId());
                ps.setInt(2, r.getEditorial().getId());
                ps.setInt(3, r.getStand().getId());
                ps.setObject(4, r.getFechaCreacion());
                ps.setObject(5, r.getVencimiento());
                ps.setObject(6, r.getLimiteLiquidacion());
                ps.setBoolean(7, r.isVencida());
                ps.executeUpdate();
            }
        }
    }

    private void guardarPagos(Connection con, List<Reserva> reservas) throws SQLException {
        for (Reserva r : reservas) {
            try (PreparedStatement eliminar = con.prepareStatement(
                    "DELETE FROM pago WHERE id_reserva = ?")) {
                eliminar.setInt(1, r.getId());
                eliminar.executeUpdate();
            }
            String sql = "INSERT INTO pago (id_reserva, importe, fecha) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Pago p : r.getPagos()) {
                    ps.setInt(1, r.getId());
                    ps.setBigDecimal(2, p.getImporte());
                    ps.setObject(3, p.getFecha());
                    ps.executeUpdate();
                }
            }
        }
    }
}
