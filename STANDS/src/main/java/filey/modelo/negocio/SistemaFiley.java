package filey.modelo.negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import filey.modelo.datos.Configuracion;
import filey.modelo.datos.Editorial;
import filey.modelo.datos.EstadoStand;
import filey.modelo.datos.Reserva;
import filey.modelo.datos.Stand;
import filey.modelo.excepciones.DatosInvalidosException;
import filey.modelo.excepciones.OperacionNoPermitidaException;
import filey.modelo.excepciones.RegistroDuplicadoException;
import filey.modelo.excepciones.RegistroNoEncontradoException;
import filey.modelo.notificaciones.Sujeto;

public final class SistemaFiley {
    private final Configuracion configuracion;
    private final Sujeto notificador;
    private final Map<Integer, Stand> stands = new LinkedHashMap<>();
    private final Map<Integer, Editorial> editoriales = new LinkedHashMap<>();
    private final Map<Integer, Reserva> reservas = new LinkedHashMap<>();
    private final Set<String> avisosEmitidos = new HashSet<>();
    private int siguienteReserva = 1;

    public SistemaFiley(Configuracion configuracion, Sujeto notificador) {
        this.configuracion = Objects.requireNonNull(configuracion);
        this.notificador = Objects.requireNonNull(notificador);
    }

    public void registrarStand(Stand stand) {
        Objects.requireNonNull(stand);
        if (stand.getEstado() != EstadoStand.DISPONIBLE) {
            throw new OperacionNoPermitidaException("El stand debe registrarse disponible.");
        }
        if (stands.putIfAbsent(stand.getId(), stand) != null) {
            throw new RegistroDuplicadoException("Ya existe un stand con el ID " + stand.getId() + ".");
        }
    }

    public void editarStand(int id, BigDecimal ancho, BigDecimal largo, BigDecimal precioPorMetroCuadrado) {
        buscarStand(id).editar(ancho, largo, precioPorMetroCuadrado);
    }

    public void eliminarStand(int id) {
        Stand stand = buscarStand(id);
        if (stand.getEstado() != EstadoStand.DISPONIBLE) {
            throw new OperacionNoPermitidaException("Solo se puede eliminar un stand disponible.");
        }
        if (reservas.values().stream().anyMatch(r -> r.getStand().getId() == id)) {
            throw new OperacionNoPermitidaException("El stand tiene reservas asociadas.");
        }
        stands.remove(id);
    }

    public Stand buscarStand(int id) {
        Stand stand = stands.get(id);
        if (stand == null) { throw new RegistroNoEncontradoException("No existe un stand con el ID " + id + "."); }
        return stand;
    }

    public Reserva buscarReserva(int id) {
        Reserva reserva = reservas.get(id);
        if (reserva == null) { throw new RegistroNoEncontradoException("No existe una reserva con el ID " + id + "."); }
        return reserva;
    }

    // ---------- CRUD de editoriales ----------

    public void registrarEditorial(Editorial editorial) {
        Objects.requireNonNull(editorial);
        if (editoriales.containsKey(editorial.getId())) {
            throw new RegistroDuplicadoException("Ya existe una editorial con el ID " + editorial.getId() + ".");
        }
       
        editoriales.put(editorial.getId(), editorial);
        notificador.suscribir(editorial.getId(), editorial);
    }

    public Editorial buscarEditorial(int id) {
        Editorial editorial = editoriales.get(id);
        if (editorial == null) {
            throw new RegistroNoEncontradoException("No existe una editorial con el ID " + id + ".");
        }
        return editorial;
    }

    public void editarEditorial(int id, String nuevoNombre) {
        Editorial editorial = buscarEditorial(id);
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new DatosInvalidosException("El nombre de la editorial no puede estar vacío.");
        }
        editorial.renombrar(nuevoNombre);
    }

    public void eliminarEditorial(int id) {
        Editorial editorial = buscarEditorial(id);
        if (reservas.values().stream().anyMatch(r -> r.getEditorial().getId() == id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar la editorial " + id + ": tiene reservas asociadas.");
        }
        editoriales.remove(id);
        notificador.retirar(id, editorial);
    }


    public List<Stand> consultarDisponibles() {
        return stands.values().stream().filter(s -> s.getEstado() == EstadoStand.DISPONIBLE).toList();
    }
    public List<Reserva> consultarReservas() { return List.copyOf(reservas.values()); }
    public List<Stand> consultarStands() { return List.copyOf(stands.values()); }
    public List<Editorial> consultarEditoriales() { return List.copyOf(editoriales.values()); }

    public Reserva crearReserva(int idEditorial, int idStand, LocalDate fecha) {
        Objects.requireNonNull(fecha);
        Editorial editorial = buscarEditorial(idEditorial);
        Stand stand = buscarStand(idStand);
        if (fecha.isAfter(configuracion.getLimiteReserva())) {
            throw new OperacionNoPermitidaException("Se ha superado la fecha límite de reserva.");
        }
        LocalDate vencimiento = fecha.plusDays(configuracion.getDiasParaPagarReserva());
        if (vencimiento.isAfter(configuracion.getLimiteReserva())) {
            vencimiento = configuracion.getLimiteReserva();
        }
        Reserva reserva = stand.reservar(siguienteReserva, editorial, fecha, vencimiento,
                configuracion.getLimiteLiquidacion());
        reservas.put(siguienteReserva++, reserva);
        return reserva;
    }

    public void registrarPago(int idReserva, BigDecimal importe, LocalDate fecha) {
        buscarReserva(idReserva).registrarPago(importe, fecha);
    }

    public void cargarStand(Stand stand) {
        Objects.requireNonNull(stand);
        if (stands.putIfAbsent(stand.getId(), stand) != null) {
            throw new RegistroDuplicadoException("Ya existe un stand con el ID " + stand.getId() + ".");
        }
    }

    public void cargarReserva(Reserva reserva) {
        Objects.requireNonNull(reserva);
        if (reservas.containsKey(reserva.getId())) {
            throw new RegistroDuplicadoException("Ya existe una reserva con el ID " + reserva.getId() + ".");
        }
        reservas.put(reserva.getId(), reserva);
        siguienteReserva = Math.max(siguienteReserva, reserva.getId() + 1);
    }
    public void cancelarReservaCreada(Reserva reserva) {
        Objects.requireNonNull(reserva);
        reservas.remove(reserva.getId());
        reserva.getStand().restaurarEstado(EstadoStand.DISPONIBLE);
        siguienteReserva = Math.max(1, reserva.getId());
    }
    public void bloquearStand(int idStand) { buscarStand(idStand).bloquear(); }
    public void desbloquearStand(int idStand) { buscarStand(idStand).desbloquear(); }

    public void comprobarFechas(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        for (Reserva reserva : reservas.values()) {
            if (fecha.isBefore(reserva.getFechaCreacion())) { continue; }
            reserva.vencerSiCorresponde(fecha);
            if (reserva.isVencida() || reserva.getPendiente().signum() == 0) { continue; }
            if (!reserva.alcanzaMinimo()) {
                avisar(reserva, "RESERVA", reserva.getVencimiento(), fecha,
                        "Vence el plazo para cubrir al menos el 15% de la reserva");
            }
            avisar(reserva, "DESCUENTO", configuracion.getLimiteDescuento(), fecha,
                    "Vence el plazo de descuento por pronto pago");
            avisar(reserva, "LIQUIDACION", reserva.getLimiteLiquidacion(), fecha,
                    "Vence el plazo para liquidar el saldo");
        }
    }

    private void avisar(Reserva reserva, String tipo, LocalDate limite, LocalDate fecha, String texto) {
        if (fecha.isBefore(limite.minusDays(configuracion.getDiasAnticipacionAviso()))
                || fecha.isAfter(limite)) { return; }
        String clave = reserva.getId() + ":" + tipo + ":" + limite;
        if (!avisosEmitidos.contains(clave)) {
            notificador.notificar(reserva.getEditorial().getId(), texto + " el " + limite
                    + " (reserva " + reserva.getId() + ", stand " + reserva.getStand().getId() + ").");
            avisosEmitidos.add(clave);
        }
    }

}
