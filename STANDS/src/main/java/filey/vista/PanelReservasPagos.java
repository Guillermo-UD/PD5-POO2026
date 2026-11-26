package filey.vista;

import filey.controlador.ControladorFiley;
import filey.modelo.datos.Reserva;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public final class PanelReservasPagos extends JPanel {
    private final ControladorFiley controlador;
    private final JTextField editorial = new JTextField(8);
    private final JTextField stand = new JTextField(8);
    private final JTextField fecha = new JTextField(12);
    private final JTextField reserva = new JTextField(8);
    private final JTextField importe = new JTextField(10);
    private final JTextField fechaPago = new JTextField(12);
    private final JLabel estado = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[] {"Reserva", "Editorial", "Stand", "Fecha", "Vencimiento",
                    "Total", "Abonado", "Pendiente", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };

    public PanelReservasPagos(ControladorFiley controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        agregar(formulario, "ID editorial:", editorial);
        agregar(formulario, "ID stand:", stand);
        agregar(formulario, "Fecha reserva:", fecha);
        JButton crearReserva = new JButton("Crear reserva");
        formulario.add(crearReserva);

        agregar(formulario, "ID reserva:", reserva);
        agregar(formulario, "Importe:", importe);
        agregar(formulario, "Fecha pago:", fechaPago);
        JButton registrarPago = new JButton("Registrar pago");
        JButton comprobar = new JButton("Comprobar fechas");
        JButton guardar = new JButton("Guardar BD");
        formulario.add(registrarPago);
        formulario.add(comprobar);
        formulario.add(guardar);

        crearReserva.addActionListener(e -> crearReserva());
        registrarPago.addActionListener(e -> registrarPago());
        comprobar.addActionListener(e -> comprobarFechas());
        guardar.addActionListener(e -> mostrar(controlador.guardarEnBaseDatos()));

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        refrescar();
    }

    private void agregar(JPanel panel, String etiqueta, JTextField campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void crearReserva() {
        try {
            int idEditorial = Integer.parseInt(editorial.getText().strip());
            int idStand = Integer.parseInt(stand.getText().strip());
            LocalDate dia = LocalDate.parse(fecha.getText().strip());
            mostrar(controlador.crearReserva(idEditorial, idStand, dia));
            refrescar();
        } catch (NumberFormatException | DateTimeParseException e) {
            mostrar("No se pudo crear la reserva: revise los números y use fecha AAAA-MM-DD.");
        }
    }

    private void registrarPago() {
        try {
            int idReserva = Integer.parseInt(reserva.getText().strip());
            BigDecimal valor = new BigDecimal(importe.getText().strip());
            LocalDate dia = LocalDate.parse(fechaPago.getText().strip());
            mostrar(controlador.registrarPago(idReserva, valor, dia));
            refrescar();
        } catch (NumberFormatException | DateTimeParseException e) {
            mostrar("No se pudo registrar el pago: revise el importe y use fecha AAAA-MM-DD.");
        }
    }

    private void comprobarFechas() {
        try {
            controlador.comprobarFechas(LocalDate.parse(fecha.getText().strip()));
            mostrar("Se comprobaron las fechas y vencimientos.");
            refrescar();
        } catch (DateTimeParseException e) {
            mostrar("No se pudo comprobar: use fecha AAAA-MM-DD.");
        }
    }

    private void refrescar() {
        modelo.setRowCount(0);
        for (Reserva r : controlador.consultarReservas()) {
            modelo.addRow(new Object[] {r.getId(), r.getEditorial().getNombre(),
                    r.getStand().getId(), r.getFechaCreacion(), r.getVencimiento(),
                    r.getPrecioTotal(), r.getAbonado(), r.getPendiente(),
                    r.isVencida() ? "Vencida" : r.getStand().getEstado()});
        }
    }

    private void mostrar(String mensaje) {
        estado.setForeground(mensaje.startsWith("No se pudo") ? Color.RED : new Color(0, 120, 0));
        estado.setText(mensaje);
    }
}
