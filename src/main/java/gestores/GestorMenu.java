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
    
    public GestorMenu(){
        this.conexionDB = new DBConnection();
    }
    
    public boolean registrarProducto(Producto producto) throws BDException {
        String query = "INSERT INTO PRODUCTO (Codigo_producto, Nombre_producto, Categoria, Precio_venta, Fotografia) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)){
            ps.setString(1, producto.getCodigoProducto());
            ps.setString(2, producto.getNombreProducto());
            ps.setString(3, producto.getCategoria());
            ps.setDouble(4, producto.getPrecioVenta());
            ps.setString(5, producto.getFotografia());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new BDException("Error al registrar el producto en el menu: " + e.getMessage(), e);
        }
    }
    
    public boolean agregarInsumoAReceta(Receta receta) throws BDException {
        String query = "INSERT INTO RECETA (Codigo_producto, Codigo_insumo, Cantidad) VALUES (?, ?, ?)";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
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
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Producto prod = new Producto(
                    rs.getString("Codigo_producto"),
                    rs.getString("Nombre_producto"),
                    rs.getString("Categoria"),
                    rs.getDouble("Precio_venta"),
                    rs.getString("Fotografia")
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
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
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
}
