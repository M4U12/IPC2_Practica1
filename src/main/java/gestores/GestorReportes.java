package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GestorReportes {
    private DBConnection conexionDB;

    public GestorReportes() {
        this.conexionDB = new DBConnection();
    }
    
    public double[] obtenerFlujoCaja(LocalDate inicio, LocalDate fin) throws BDException {
        // guarda: [0]ingresos, [1]egresos nomina, [2]egresos insumos
        double[] totales = new double[3];

        // filtros de fecha 
        String filtroFechaCuenta = (inicio != null && fin != null) ? " AND Fecha BETWEEN ? AND ?" : "";
        String filtroFechaNomina = (inicio != null && fin != null) ? " AND Fecha_emision_pago BETWEEN ? AND ?" : "";
        String filtroFechaCompra = (inicio != null && fin != null) ? " WHERE Fecha_compra BETWEEN ? AND ?" : "";

        String queryIngresos = "SELECT SUM(Total_de_cuenta) AS Total FROM cuenta WHERE Estado_cuenta = 'PAGADA'" + filtroFechaCuenta;
        String queryNominas = "SELECT SUM(Monto_pago) AS Total FROM nomina WHERE (Estado_pago = 'PAGADO' OR Estado_pago = 'PAGADO')" + filtroFechaNomina; 
        String queryCompras = "SELECT SUM(Total_gastado) AS Total FROM compra_insumo" + filtroFechaCompra;

        try (Connection conn = conexionDB.getConnection()) {
            
            // ingresos
            try (PreparedStatement ps = conn.prepareStatement(queryIngresos)) {
                if (inicio != null && fin != null) {
                    ps.setDate(1, Date.valueOf(inicio));
                    ps.setDate(2, Date.valueOf(fin));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totales[0] = rs.getDouble("Total");
                }
            }

            // egresos por nómina
            try (PreparedStatement ps = conn.prepareStatement(queryNominas)) {
                if (inicio != null && fin != null) {
                    ps.setDate(1, Date.valueOf(inicio));
                    ps.setDate(2, Date.valueOf(fin));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totales[1] = rs.getDouble("Total");
                }
            }

            // calcular egresos por insumos
            try (PreparedStatement ps = conn.prepareStatement(queryCompras)) {
                if (inicio != null && fin != null) {
                    ps.setDate(1, Date.valueOf(inicio));
                    ps.setDate(2, Date.valueOf(fin));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totales[2] = rs.getDouble("Total");
                }
            }

        } catch (SQLException e) {
            throw new BDException("Error al calcular el flujo de caja: " + e.getMessage(), e);
        }

        return totales;
    }
    
    public List<Object[]> obtenerProductosMasVendidos(LocalDate inicio, LocalDate fin) throws BDException {
        List<Object[]> ranking = new ArrayList<>();
        String filtroFecha = (inicio != null && fin != null) ? " AND c.Fecha BETWEEN ? AND ?" : "";
        
        String query = "SELECT p.Codigo_producto, p.Nombre_producto, SUM(d.Cantidad) AS Total_Vendidos, SUM(d.Subtotal) AS Ingresos " +
                       "FROM detalle_cuenta d " +
                       "INNER JOIN cuenta c ON d.Codigo_cuenta = c.Codigo_cuenta " +
                       "INNER JOIN producto p ON d.Codigo_producto = p.Codigo_producto " +
                       "WHERE c.Estado_cuenta = 'PAGADA'" + filtroFecha + " " +
                       "GROUP BY p.Codigo_producto, p.Nombre_producto " +
                       "ORDER BY Total_Vendidos DESC";
                       
        try (Connection conn = conexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            if (inicio != null && fin != null) {
                ps.setDate(1, java.sql.Date.valueOf(inicio));
                ps.setDate(2, java.sql.Date.valueOf(fin));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ranking.add(new Object[]{
                        rs.getString("Codigo_producto"),
                        rs.getString("Nombre_producto"),
                        rs.getInt("Total_Vendidos"),
                        rs.getDouble("Ingresos")
                    });
                }
            }
        } catch (SQLException e) {
            throw new BDException("Error al generar ranking de productos: " + e.getMessage(), e);
        }
        
        return ranking;
    }
    
    
    public void exportarFlujoCajaHTML(String rutaArchivo, String periodo, String ingresos, String egresosNomina, String egresosInsumos, String balance) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang='es'>\n<head>\n<meta charset='UTF-8'>\n<title>Reporte de Flujo de Caja</title>\n<style>\n")
            .append("body { font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; }\n")
            .append("h2 { color: #333; }\n.contenedor { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n")
            .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
            .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }\nth { background-color: #0056b3; color: white; }\n")
            .append(".balance { font-weight: bold; font-size: 1.1em; }\n</style>\n</head>\n<body>\n<div class='contenedor'>\n")
            .append("<h2>Reporte de Flujo de Caja</h2>\n<p><strong>Período evaluado:</strong> ").append(periodo).append("</p>\n<table>\n")
            .append("<tr><th>Ingresos Totales</th><th>Egresos Nóminas</th><th>Egresos Insumos</th><th>Balance Final</th></tr>\n<tr>")
            .append("<td>").append(ingresos).append("</td><td>").append(egresosNomina).append("</td><td>").append(egresosInsumos).append("</td>");

        if (balance.contains("GANANCIA")) {
            html.append("<td class='balance' style='color: green;'>").append(balance).append("</td>");
        } else {
            html.append("<td class='balance' style='color: red;'>").append(balance).append("</td>");
        }
        html.append("</tr>\n</table>\n</div>\n</body>\n</html>");

        try (FileWriter fw = new FileWriter(rutaArchivo); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(html.toString());
        }
    }

    public void exportarBajoStockHTML(String rutaArchivo, List<Object[]> datosTabla) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang='es'>\n<head>\n<meta charset='UTF-8'>\n<title>Insumos con Bajo Stock</title>\n<style>\n")
            .append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n")
            .append(".contenedor { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n")
            .append("h2 { color: #d9534f; }\ntable { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
            .append("th, td { border: 1px solid #ddd; padding: 10px; text-align: center; }\n")
            .append("th { background-color: #d9534f; color: white; }\ntr:nth-child(even) { background-color: #f9f9f9; }\n")
            .append("</style>\n</head>\n<body>\n<div class='contenedor'>\n<h2>Reporte: Insumos con Bajo Stock</h2>\n")
            .append("<p>Los siguientes insumos han alcanzado o descendido por debajo de su límite de stock mínimo y requieren reabastecimiento urgente.</p>\n")
            .append("<table>\n<tr><th>Código</th><th>Nombre del Insumo</th><th>Unidad</th><th>Cantidad Actual</th><th>Stock Mínimo</th><th>Costo (Q)</th></tr>\n");

        for (Object[] fila : datosTabla) {
            html.append("<tr>");
            for (Object celda : fila) {
                html.append("<td>").append(celda.toString()).append("</td>");
            }
            html.append("</tr>\n");
        }
        html.append("</table>\n</div>\n</body>\n</html>");

        try (FileWriter fw = new FileWriter(rutaArchivo); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(html.toString());
        }
    }

    public void exportarMasVendidosHTML(String rutaArchivo, String periodo, List<Object[]> datosTabla) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang='es'>\n<head>\n<meta charset='UTF-8'>\n<title>Ranking de Productos Más Vendidos</title>\n<style>\n")
            .append("body { font-family: Arial, sans-serif; background-color: #f4f7f6; padding: 20px; }\n")
            .append(".contenedor { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n")
            .append("h2 { color: #2c3e50; }\ntable { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
            .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }\n")
            .append("th { background-color: #2980b9; color: white; }\ntr:nth-child(even) { background-color: #f9f9f9; }\n")
            .append("tr:first-child td { font-weight: bold; background-color: #fff3cd; color: #856404; }\n")
            .append("</style>\n</head>\n<body>\n<div class='contenedor'>\n<h2>Ranking de Productos Más Vendidos</h2>\n")
            .append("<p><strong>Período evaluado:</strong> ").append(periodo).append("</p>\n<table>\n")
            .append("<tr><th>Código</th><th>Nombre del Producto</th><th>Cantidad Vendida</th><th>Ingresos Generados (Q)</th></tr>\n");

        for (Object[] fila : datosTabla) {
            html.append("<tr>");
            for (Object celda : fila) {
                html.append("<td>").append(celda.toString()).append("</td>");
            }
            html.append("</tr>\n");
        }
        html.append("</table>\n</div>\n</body>\n</html>");

        try (FileWriter fw = new FileWriter(rutaArchivo); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(html.toString());
        }
    }
}
