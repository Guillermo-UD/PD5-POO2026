package filey.modelo.persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {
    private static final String URL =
            "jdbc:mysql://localhost:3306/filey?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Merida";
    private static final String ARCHIVO = "db.properties";

    private ConexionBD() { }

    /** Usuario de solo lectura (Observador). */
    public static Connection obtener() throws SQLException {
        return conectar("FILEY_DB_USER", "FILEY_DB_PASS");
    }

    /** Usuario con permiso de escritura (Gestor). */
    public static Connection obtenerEscritura() throws SQLException {
        return conectar("FILEY_DB_USER_W", "FILEY_DB_PASS_W");
    }

    private static Connection conectar(String varUsuario, String varClave) throws SQLException {
        String usuario = leer(varUsuario);
        String clave = leer(varClave);
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            throw new IllegalStateException("Configura " + varUsuario + " y " + varClave
                    + " con las credenciales de MySQL, ya sea como variables de entorno o en "
                    + "src/main/resources/db.properties. El valor de la contraseña no puede estar vacío.");
        }
        return DriverManager.getConnection(URL, usuario, clave);
    }

    /** Busca en: variables de entorno, luego db.properties del classpath, luego de la carpeta actual. */
    private static String leer(String nombre) {
        String valor = System.getenv(nombre);
        if (valor != null && !valor.isBlank()) {
            return valor;
        }
        return cargarPropiedades().getProperty(nombre);
    }

    private static Properties cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getResourceAsStream("/" + ARCHIVO)) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (IOException e) {
            // se intenta con la carpeta actual
        }
        try (FileInputStream in = new FileInputStream(ARCHIVO)) {
            props.load(in);
        } catch (IOException e) {
            // sin archivo: quedan las propiedades vacías
        }
        return props;
    }
}
