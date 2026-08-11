package utilidades;

import gestores.GestorInsumo;
import modelos.Insumo;
import excepciones.BDException;
import java.awt.Component;
import java.util.List;
import javax.swing.JOptionPane;

public class AlertaStock {
    public void revisarInventario(Component ventana) {
        try {
            GestorInsumo gestor = new GestorInsumo();
            List<Insumo> listaAlertas = gestor.obtenerInsumosBajoStock();

            if (!listaAlertas.isEmpty()) {
                String mensaje = "Los siguientes insumos están en stock crítico o agotados:\n\n";

                // Concatenamos cada insumo con +=
                for (Insumo i : listaAlertas) {
                    mensaje += "- " + i.getNombreInsumo() +
                               " (Quedan: " + i.getCantidadActual() +
                               " " + i.getUnidadMedida() + ")\n";
                }

                // Agregamos la línea final
                mensaje += "\nConsidere reabastecerlos lo antes posible.";

                JOptionPane.showMessageDialog(ventana, mensaje, "Alerta de Stock Crítico", JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (BDException ex) {
            System.out.println("Error al cargar alertas: " + ex.getMessage());
        }
    }
}

