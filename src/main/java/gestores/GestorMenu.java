package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Receta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import modelos.Producto;

public class GestorMenu {

    private DBConnection conexionDB;

    public GestorMenu() {
        this.conexionDB = new DBConnection();
    }

    public boolean agregarInsumoAReceta(Receta receta) throws BDException {
        String query = "INSERT INTO RECETA (Codigo_producto, Codigo_insumo, Cantidad) VALUES (?, ?, ?)";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, receta.getCodigoProducto());
            ps.setString(2, receta.getCodigoInsumo());
            ps.setDouble(3, receta.getCantidad());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new BDException("Error al vincular insumo a la receta: " + e.getMessage(), e);
        }
    }

    public List<Producto> listaProductos() throws BDException {
        List<Producto> menu = new ArrayList<>();
        String query = "SELECT Codigo_producto, Nombre_producto, Categoria, Precio_venta, Fotografia FROM PRODUCTO";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto prod = new Producto(
                        rs.getString("Codigo_producto"),
                        rs.getString("Nombre_producto"),
                        rs.getString("Categoria"),
                        rs.getDouble("Precio_venta"),
                        rs.getBytes("Fotografia")
                );
                menu.add(prod);
            }
        } catch (SQLException e) {
            throw new BDException("Error al consultar el menú: " + e.getMessage(), e);
        }
        return menu;
    }

    public List<Receta> obtenerRecetaProducto(String codigoProducto) throws BDException {
        List<Receta> ingredientes = new ArrayList<>();
        String query = "SELECT Codigo_producto, Codigo_insumo, Cantidad FROM RECETA WHERE Codigo_producto = ?";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, codigoProducto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Receta rec = new Receta(
                            rs.getString("Codigo_producto"),
                            rs.getString("Codigo_insumo"),
                            rs.getDouble("Cantidad")
                    );
                    ingredientes.add(rec);
                }
            }
        } catch (SQLException e) {
            throw new BDException("Error al extraer la receta del producto: " + e.getMessage(), e);
        }
        return ingredientes;
    }

    public boolean registrarProductoCompleto(Producto producto, List<Receta> ingredientes) throws BDException {
        String queryProducto = "INSERT INTO PRODUCTO (Codigo_producto, Nombre_producto, Categoria, Precio_venta, Fotografia) VALUES (?, ?, ?, ?, ?)";
        String queryReceta = "INSERT INTO RECETA (Codigo_producto, Codigo_insumo, Cantidad) VALUES (?, ?, ?)";

        Connection connection = null;

        try {
            connection = conexionDB.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement psProd = connection.prepareStatement(queryProducto)) {
                psProd.setString(1, producto.getCodigoProducto());
                psProd.setString(2, producto.getNombreProducto());
                psProd.setString(3, producto.getCategoria());
                psProd.setDouble(4, producto.getPrecioVenta());
                psProd.setBytes(5, producto.getFotografia());
                psProd.executeUpdate();
            }

            try (PreparedStatement psReceta = connection.prepareStatement(queryReceta)) {
                for (Receta r : ingredientes) {
                    psReceta.setString(1, r.getCodigoProducto());
                    psReceta.setString(2, r.getCodigoInsumo());
                    psReceta.setDouble(3, r.getCantidad());
                    psReceta.executeUpdate();
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new BDException("Error fatal al intentar deshacer los cambios: " + ex.getMessage(), ex);
                }
            }
            throw new BDException("Error al registrar. Se canceló todo para evitar datos incompletos: " + e.getMessage(), e);

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    System.err.println("Error al cerrar la conexión: " + ex.getMessage());
                }
            }
        }
    }

    public boolean descontarInsumoPorVentas(String codigoProducto) throws BDException {
        List<Receta> ingredientes = obtenerRecetaProducto(codigoProducto); 
        GestorInsumo gestorInsumos = new GestorInsumo();
        for (Receta ingrediente : ingredientes) {
            boolean exito = gestorInsumos.descontarStockActual(
                    ingrediente.getCodigoInsumo(),
                    ingrediente.getCantidad()
            );
            if (!exito) {
                return false;
            }
        }
        return true;
    }
    
    public void exportarCatalogoHTML(String rutaArchivo, List<Producto> lista) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n<meta charset=\"UTF-8\">\n");
        html.append("<title>JavaBeans Café</title>\n");

        
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; text-align: center; color: #333; margin: 0; padding: 20px; }\n");
        html.append("h1 { color: #2c3e50; font-size: 2.5em; margin-bottom: 30px; }\n");
        html.append(".contenedor-menu { display: flex; flex-wrap: wrap; justify-content: center; gap: 25px; max-width: 1200px; margin: 0 auto; }\n");
        html.append(".tarjeta { background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); width: 260px; padding: 20px; transition: transform 0.3s; }\n");
        html.append(".tarjeta:hover { transform: translateY(-5px); }\n");
        html.append(".imagen-producto { width: 100%; height: 200px; object-fit: cover; border-radius: 8px; }\n");
        html.append(".nombre { font-size: 1.4em; font-weight: bold; margin: 15px 0 10px 0; color: #34495e; }\n");
        html.append(".precio { font-size: 1.3em; font-weight: bold; color: #27ae60; margin: 0; }\n");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<h1>Nuestro Menú</h1>\n");
        html.append("<div class=\"contenedor-menu\">\n");
        
        // lista de productos y se haga tarjeta html por cada uno
        for (Producto p : lista) {
            html.append("<div class=\"tarjeta\">\n");

            if (p.getFotografia() != null) {
                String imagenBase64 = Base64.getEncoder().encodeToString(p.getFotografia());
                html.append("<img class=\"imagen-producto\" src=\"data:image/jpeg;base64,").append(imagenBase64).append("\" alt=\"").append(p.getNombreProducto()).append("\">\n");
            } else {//si no tiene foto
                html.append("<div style=\"height: 200px; display: flex; align-items: center; justify-content: center; background-color: #eee; border-radius: 8px;\">Sin Imagen</div>\n");
            }

            html.append("<div class=\"nombre\">").append(p.getNombreProducto()).append("</div>\n");
            html.append("<div class=\"precio\">Q ").append(p.getPrecioVenta()).append("</div>\n");

            html.append("</div>\n");
        }

        html.append("</div>\n</body>\n</html>");

        try (FileWriter escritor = new FileWriter(rutaArchivo)) {
            escritor.write(html.toString());
        }
    }
}
