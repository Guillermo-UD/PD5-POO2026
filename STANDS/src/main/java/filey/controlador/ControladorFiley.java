package filey.controlador;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import filey.modelo.datos.Editorial;
import filey.modelo.datos.Pago;
import filey.modelo.datos.Reserva;
import filey.modelo.datos.Stand;
import filey.modelo.excepciones.FileyException;
import filey.modelo.negocio.SistemaFiley;
import filey.modelo.persistencia.RepositorioFiley;

public final class ControladorFiley {
    private final SistemaFiley sistema;
    private final RepositorioFiley repositorio;

    public ControladorFiley(SistemaFiley sistema, RepositorioFiley repositorio) {
        this.sistema = sistema;
        this.repositorio = repositorio;
    }

    // ================= Arranque =================

    /** Lee de MySQL lo que ya existe y lo carga en el sistema. */
    public String cargarDesdeBaseDeDatos() {
        try {
            int total = 0;
            for (Editorial editorial : repositorio.cargarEditoriales()) {
                sistema.registrarEditorial(editorial);
                total++;
            }
            for (Stand stand : repositorio.cargarStands()) {
                sistema.cargarStand(stand);
            }
            for (Reserva reserva : repositorio.cargarReservas(
                    sistema.consultarEditoriales(), sistema.consultarStands())) {
                sistema.cargarReserva(reserva);
            }
            return total + " editoriales, " + sistema.consultarStands().size()
                    + " stands y " + sistema.consultarReservas().size()
                    + " reservas cargadas de la base de datos.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("cargar los datos", e);
        }
    }



    public String crearEditorial(int id, String nombre) {
        try {
            Editorial editorial = new Editorial(id, nombre);
            sistema.registrarEditorial(editorial);
            try {
                repositorio.insertarEditorial(editorial);
            } catch (SQLException | RuntimeException e) {
                sistema.eliminarEditorial(id); // si MySQL falla, se deshace en memoria
                throw e;
            }
            return "Editorial " + id + " creada.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("crear la editorial", e);
        }
    }

    /** Para que una ventana pueda llenar su tabla con las editoriales. */
    public List<Editorial> consultarEditoriales() {
        return sistema.consultarEditoriales();
    }

    public String listarEditoriales() {
        List<Editorial> lista = sistema.consultarEditoriales();
        if (lista.isEmpty()) {
            return "No hay editoriales registradas.";
        }
        StringBuilder texto = new StringBuilder();
        for (Editorial e : lista) {
            texto.append(String.format("  %-4d %s%n", e.getId(), e.getNombre()));
        }
        return texto.toString().stripTrailing();
    }

    public String buscarEditorial(int id) {
        try {
            Editorial e = sistema.buscarEditorial(id);
            return "Editorial " + e.getId() + ": " + e.getNombre();
        } catch (FileyException e) {
            return fallo("buscar la editorial", e);
        }
    }

    public String editarEditorial(int id, String nuevoNombre) {
        try {
            Editorial editorial = sistema.buscarEditorial(id);
            String anterior = editorial.getNombre();
            sistema.editarEditorial(id, nuevoNombre);
            try {
                repositorio.actualizarEditorial(editorial);
            } catch (SQLException | RuntimeException e) {
                sistema.editarEditorial(id, anterior); // se restaura el nombre anterior
                throw e;
            }
            return "Editorial " + id + " actualizada: " + editorial.getNombre();
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("editar la editorial", e);
        }
    }

    public String eliminarEditorial(int id) {
        try {
            Editorial editorial = sistema.buscarEditorial(id);
            sistema.eliminarEditorial(id);
            try {
                repositorio.eliminarEditorial(id);
            } catch (SQLException | RuntimeException e) {
                sistema.registrarEditorial(editorial); // se vuelve a registrar si MySQL rechaza
                throw e;
            }
            return "Editorial " + id + " eliminada.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("eliminar la editorial", e);
        }
    }

    // ================= Lo que ya tenías (stands, reservas, pagos) =================

    public String registrarStand(Stand stand) {
        try {
            sistema.registrarStand(stand);
            try {
                repositorio.insertarStand(stand);
            } catch (SQLException | RuntimeException e) {
                sistema.eliminarStand(stand.getId());
                throw e;
            }
            return "Stand " + stand.getId() + " registrado.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("registrar el stand", e);
        }
    }

    public String editarStand(int id, BigDecimal ancho, BigDecimal largo,
            BigDecimal precioPorMetroCuadrado) {
        try {
            Stand stand = sistema.buscarStand(id);
            BigDecimal anchoAnterior = stand.getAncho();
            BigDecimal largoAnterior = stand.getLargo();
            BigDecimal precioAnterior = stand.getPrecioPorMetroCuadrado();
            sistema.editarStand(id, ancho, largo, precioPorMetroCuadrado);
            try {
                repositorio.actualizarStand(stand);
            } catch (SQLException | RuntimeException e) {
                sistema.editarStand(id, anchoAnterior, largoAnterior, precioAnterior);
                throw e;
            }
            return "Stand " + id + " actualizado.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("editar el stand", e);
        }
    }

    public String eliminarStand(int id) {
        try {
            Stand stand = sistema.buscarStand(id);
            sistema.eliminarStand(id);
            try {
                repositorio.eliminarStand(id);
            } catch (SQLException | RuntimeException e) {
                sistema.cargarStand(stand);
                throw e;
            }
            return "Stand " + id + " eliminado.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("eliminar el stand", e);
        }
    }

    public String crearReserva(int idEditorial, int idStand, LocalDate fecha) {
        try {
            Reserva r = sistema.crearReserva(idEditorial, idStand, fecha);
            try {
                repositorio.insertarReserva(r);
            } catch (SQLException | RuntimeException e) {
                sistema.cancelarReservaCreada(r);
                throw e;
            }
            return "Reserva " + r.getId() + " creada. Total: $" + r.getPrecioTotal()
                    + ", vence el " + r.getVencimiento() + ".";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("crear la reserva", e);
        }
    }

    public String registrarPago(int idReserva, BigDecimal importe, LocalDate fecha) {
        try {
            Reserva reserva = sistema.buscarReserva(idReserva);
            boolean estabaOcupado = reserva.getStand().getEstado()
                    == filey.modelo.datos.EstadoStand.OCUPADO;
            sistema.registrarPago(idReserva, importe, fecha);
            Pago pago = reserva.getPagos().get(reserva.getPagos().size() - 1);
            try {
                repositorio.insertarPago(idReserva, pago);
                repositorio.actualizarStand(reserva.getStand());
            } catch (SQLException | RuntimeException e) {
                reserva.retirarUltimoPago();
                if (!estabaOcupado
                        && reserva.getStand().getEstado()
                                == filey.modelo.datos.EstadoStand.OCUPADO) {
                    reserva.getStand().restaurarEstado(
                            filey.modelo.datos.EstadoStand.RESERVADO);
                }
                throw e;
            }
            return "Pago de $" + importe + " registrado.";
        } catch (FileyException | SQLException | IllegalArgumentException | IllegalStateException e) {
            return fallo("registrar el pago", e);
        }
    }

    public String bloquearStand(int idStand) {
        try {
            sistema.bloquearStand(idStand);
            return "Stand " + idStand + " bloqueado.";
        } catch (FileyException | IllegalArgumentException | IllegalStateException e) {
            return fallo("bloquear el stand", e);
        }
    }

    public String desbloquearStand(int idStand) {
        try {
            sistema.desbloquearStand(idStand);
            return "Stand " + idStand + " desbloqueado.";
        } catch (FileyException | IllegalArgumentException | IllegalStateException e) {
            return fallo("desbloquear el stand", e);
        }
    }

    public void comprobarFechas(LocalDate fecha) {
        sistema.comprobarFechas(fecha);
    }

    /** Devuelve todos los stands registrados para tablas y otras vistas. */
    public List<Stand> consultarStands() {
        return sistema.consultarStands();
    }

    /** Devuelve los stands disponibles para iniciar reservas. */
    public List<Stand> consultarDisponibles() {
        return sistema.consultarDisponibles();
    }

    public String buscarStand(int id) {
        try {
            Stand stand = sistema.buscarStand(id);
            return String.format("Stand %d: %s x %s m, $%s/m2, total $%s, estado %s",
                    stand.getId(), stand.getAncho(), stand.getLargo(),
                    stand.getPrecioPorMetroCuadrado(), stand.getPrecioTotal(), stand.getEstado());
        } catch (FileyException e) {
            return fallo("buscar el stand", e);
        }
    }

    public String listarStands() {
        List<Stand> lista = sistema.consultarStands();
        if (lista.isEmpty()) {
            return "No hay stands registrados.";
        }
        StringBuilder texto = new StringBuilder();
        for (Stand stand : lista) {
            texto.append(String.format("  %-4d %s x %s m  $%s/m2  total $%s  %s%n",
                    stand.getId(), stand.getAncho(), stand.getLargo(),
                    stand.getPrecioPorMetroCuadrado(), stand.getPrecioTotal(), stand.getEstado()));
        }
        return texto.toString().stripTrailing();
    }

    public List<Reserva> consultarReservas() {
        return sistema.consultarReservas();
    }

    public String guardarEnBaseDatos() {
        try {
            repositorio.guardar(sistema.consultarEditoriales(),
                    sistema.consultarStands(), sistema.consultarReservas());
            return "Datos guardados en la base de datos.";
        } catch (FileyException | SQLException | IllegalStateException e) {
            return fallo("guardar en la base de datos", e);
        }
    }

    // ================= Mensajes de error =================

    private String fallo(String accion, Exception e) {
        if (e instanceof SQLException) {
            return "No se pudo " + accion + " (error de base de datos): " + e.getMessage();
        }
        return "No se pudo " + accion + ": " + e.getMessage();
    }
}
