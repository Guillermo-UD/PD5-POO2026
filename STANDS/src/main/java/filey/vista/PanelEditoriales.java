package filey.vista;

import filey.controlador.ControladorFiley;
import filey.modelo.datos.Editorial;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Pantalla del CRUD de editoriales. Solo muestra datos; las reglas las pone el controlador. */
public final class PanelEditoriales extends JPanel {
    private final ControladorFiley controlador;
    private final JTextField campoId = new JTextField(6);
    private final JTextField campoNombre = new JTextField(22);
    private final DefaultTableModel modelo = new DefaultTableModel(new String[] {"ID", "Nombre"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel estado = new JLabel(" ");

    public PanelEditoriales(ControladorFiley controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        formulario.add(new JLabel("ID:"));
        formulario.add(campoId);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        JButton botonCrear = new JButton("Crear");
        JButton botonEditar = new JButton("Editar nombre");
        JButton botonEliminar = new JButton("Eliminar");
        JButton botonLimpiar = new JButton("Limpiar");
        formulario.add(botonCrear);
        formulario.add(botonEditar);
        formulario.add(botonEliminar);
        formulario.add(botonLimpiar);

        botonCrear.addActionListener(e -> crear());
        botonEditar.addActionListener(e -> editar());
        botonEliminar.addActionListener(e -> eliminar());
        botonLimpiar.addActionListener(e -> limpiar());

        // Al elegir una fila de la tabla, sus datos pasan a los campos.
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                campoId.setText(String.valueOf(modelo.getValueAt(fila, 0)));
                campoNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
            }
        });

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        refrescar();
    }

    private void crear() {
        Integer id = leerId();
        if (id == null) { return; }
        String mensaje = controlador.crearEditorial(id, campoNombre.getText());
        mostrar(mensaje);
        refrescar();
        if (!esError(mensaje)) { limpiar(); }
    }

    private void editar() {
        Integer id = leerId();
        if (id == null) { return; }
        String mensaje = controlador.editarEditorial(id, campoNombre.getText());
        mostrar(mensaje);
        refrescar();
    }

    private void eliminar() {
        Integer id = leerId();
        if (id == null) { return; }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la editorial " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) { return; }
        String mensaje = controlador.eliminarEditorial(id);
        mostrar(mensaje);
        refrescar();
        if (!esError(mensaje)) { limpiar(); }
    }

    private void limpiar() {
        campoId.setText("");
        campoNombre.setText("");
        tabla.clearSelection();
    }

    public void refrescar() {
        modelo.setRowCount(0);
        for (Editorial e : controlador.consultarEditoriales()) {
            modelo.addRow(new Object[] {e.getId(), e.getNombre()});
        }
    }

    private Integer leerId() {
        try {
            return Integer.parseInt(campoId.getText().strip());
        } catch (NumberFormatException e) {
            mostrar("No se pudo continuar: el ID debe ser un número entero.");
            return null;
        }
    }

    private boolean esError(String mensaje) {
        return mensaje.startsWith("No se pudo");
    }

    private void mostrar(String mensaje) {
        estado.setForeground(esError(mensaje) ? Color.RED : new Color(0, 120, 0));
        estado.setText(mensaje);
    }
}
