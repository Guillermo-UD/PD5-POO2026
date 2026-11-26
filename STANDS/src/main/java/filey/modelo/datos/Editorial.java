package filey.modelo.datos;

import filey.modelo.excepciones.DatosInvalidosException;
import filey.modelo.notificaciones.Observador;

public final class Editorial implements Observador {
    private final int id;
    private String nombre;

    public Editorial(int id, String nombre) {
        if (id <= 0) {
            throw new DatosInvalidosException("El ID de la editorial debe ser positivo.");
        }
        this.id = id;
        this.nombre = validarNombre(nombre);
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    /** El ID nunca cambia; solo se puede editar el nombre. */
    public void renombrar(String nuevoNombre) {
        this.nombre = validarNombre(nuevoNombre);
    }

    private static String validarNombre(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DatosInvalidosException("El nombre de la editorial no puede estar vacío.");
        }
        if (texto.strip().length() > 100) {
            throw new DatosInvalidosException("El nombre de la editorial admite máximo 100 caracteres.");
        }
        return texto.strip();
    }

    @Override
    public void actualizar(String mensaje) {
        System.out.println("[Aviso a " + nombre + "] " + mensaje);
    }
}
