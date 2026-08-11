package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Producto;
import modelos.Receta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

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
}
