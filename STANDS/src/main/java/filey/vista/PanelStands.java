package filey.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import filey.controlador.ControladorFiley;
import filey.modelo.datos.Stand;

public final class PanelStands extends JPanel {
    private final ControladorFiley controlador;
    private final JTextField campoId = new JTextField(6);
    private final JTextField campoAncho = new JTextField(8);
    private final JTextField campoLargo = new JTextField(8);
    private final JTextField campoPrecio = new JTextField(10);
    private final JLabel estado = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[] {"ID", "Ancho", "Largo", "Precio/m2", "Precio total", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public PanelStands(ControladorFiley controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        agregar(formulario, "ID:", campoId);
        agregar(formulario, "Ancho:", campoAncho);
        agregar(formulario, "Largo:", campoLargo);
        agregar(formulario, "Precio/m2:", campoPrecio);

        JButton crear = new JButton("Crear");
        JButton editar = new JButton("Editar");
        JButton eliminar = new JButton("Eliminar");
        JButton bloquear = new JButton("Bloquear");
        JButton desbloquear = new JButton("Desbloquear");
        JButton limpiar = new JButton("Limpiar");
        for (JButton boton : new JButton[] {crear, editar, eliminar, bloquear, desbloquear, limpiar}) {
            formulario.add(boton);
        }
        crear.addActionListener(e -> crear());
        editar.addActionListener(e -> editar());
        eliminar.addActionListener(e -> eliminar());
        bloquear.addActionListener(e -> {
            try {
                ejecutar(controlador.bloquearStand(leerId()));
            } catch (RuntimeException ex) {
                ejecutar("No se pudo bloquear el stand: " + ex.getMessage());
            }
        });
        desbloquear.addActionListener(e -> {
            try {
                ejecutar(controlador.desbloquearStand(leerId()));
            } catch (RuntimeException ex) {
                ejecutar("No se pudo desbloquear el stand: " + ex.getMessage());
            }
        });
        limpiar.addActionListener(e -> limpiar());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                campoId.setText(String.valueOf(modelo.getValueAt(fila, 0)));
                campoAncho.setText(String.valueOf(modelo.getValueAt(fila, 1)));
                campoLargo.setText(String.valueOf(modelo.getValueAt(fila, 2)));
                campoPrecio.setText(String.valueOf(modelo.getValueAt(fila, 3)));
            }
        });
        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        refrescar();
    }

    private void agregar(JPanel panel, String etiqueta, JTextField campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void crear() {
        try {
            int id = leerId();
            Stand stand = new Stand(id, leerDecimal(campoAncho), leerDecimal(campoLargo),
                    leerDecimal(campoPrecio));
            ejecutar(controlador.registrarStand(stand));
            limpiar();
        } catch (RuntimeException e) {
            ejecutar("No se pudo crear el stand: " + e.getMessage());
        }
    }

    private void editar() {
        try {
            int id = leerId();
            ejecutar(controlador.editarStand(id, leerDecimal(campoAncho),
                    leerDecimal(campoLargo), leerDecimal(campoPrecio)));
        } catch (RuntimeException e) {
            ejecutar("No se pudo editar el stand: " + e.getMessage());
        }
    }

    private void eliminar() {
        int id = leerId();
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el stand " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            ejecutar(controlador.eliminarStand(id));
        }
    }

    private int leerId() {
        try {
            return Integer.parseInt(campoId.getText().strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID debe ser un número entero.");
        }
    }

    private BigDecimal leerDecimal(JTextField campo) {
        try {
            return new BigDecimal(campo.getText().strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los valores numéricos no son válidos.");
        }
    }

    public void refrescar() {
        modelo.setRowCount(0);
        for (Stand stand : controlador.consultarStands()) {
            modelo.addRow(new Object[] {stand.getId(), stand.getAncho(), stand.getLargo(),
                    stand.getPrecioPorMetroCuadrado(), stand.getPrecioTotal(), stand.getEstado()});
        }
    }

    private void limpiar() {
        campoId.setText("");
        campoAncho.setText("");
        campoLargo.setText("");
        campoPrecio.setText("");
        tabla.clearSelection();
        refrescar();
    }

    private void ejecutar(String mensaje) {
        estado.setForeground(mensaje.startsWith("No se pudo") ? Color.RED : new Color(0, 120, 0));
        estado.setText(mensaje);
        refrescar();
    }
}
