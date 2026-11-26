package filey.vista;

import filey.controlador.ControladorFiley;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/** Ventana principal: una pestaña por entidad. Las demás se agregan igual que Editoriales. */
public final class VentanaFiley extends JFrame {
    public VentanaFiley(ControladorFiley controlador) {
        super("Sistema FILEY");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Editoriales", new PanelEditoriales(controlador));
        pestanas.addTab("Stands", new PanelStands(controlador));
        pestanas.addTab("Reservas y pagos", new PanelReservasPagos(controlador));
        add(pestanas);
        setSize(720, 420);
        setLocationRelativeTo(null);
    }
}
