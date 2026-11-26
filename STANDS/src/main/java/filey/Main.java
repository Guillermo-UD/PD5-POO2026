package filey;

import java.time.LocalDate;

import javax.swing.SwingUtilities;

import filey.controlador.ControladorFiley;
import filey.modelo.datos.Configuracion;
import filey.modelo.negocio.SistemaFiley;
import filey.modelo.notificaciones.Notificador;
import filey.modelo.persistencia.RepositorioFiley;
import filey.vista.VentanaFiley;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
      
        LocalDate hoy = LocalDate.now();
        Configuracion configuracion = new Configuracion(hoy.plusDays(4), hoy.plusDays(10),
                hoy.plusDays(6), 3, 2);
        SistemaFiley sistema = new SistemaFiley(configuracion, new Notificador());
        ControladorFiley controlador = new ControladorFiley(sistema, new RepositorioFiley());

        System.out.println(controlador.cargarDesdeBaseDeDatos());

   
       
            SwingUtilities.invokeLater(() -> new VentanaFiley(controlador).setVisible(true));
        }
    }

