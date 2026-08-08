package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Insumo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestorInsumo {
    private DBConnection conexionDB;
    
    public GestorInsumo(){
        this.conexionDB = new DBConnection();
    }
    
    public boolean registrarInsumo(Insumo insumo) throws BDException{
        String query = "INSERT INTO INSUMO (Codigo_insumo, Nombre_insumo, Unidad_medida, Cantidad_actual, Stock_minimo, Costo_insumo) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)){
            
            ps.setString(1, insumo.getCodigoInsumo());
            ps.setString(2, insumo.getNombreInsumo());
            ps.setString(3, insumo.getUnidadMedida());
            ps.setDouble(4, insumo.getCantidadActual());
            ps.setDouble(5, insumo.getStockMinimo());
            ps.setDouble(6, insumo.getCostoInsumo());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new BDException("Error al registrar el insumo en bodega: " + e.getMessage(), e);
        }
    }
    
    public List<Insumo> listarInventario() throws BDException{
        List<Insumo> inventario = new ArrayList<>();
        String query = "SELECT Codigo_insumo, Nombre_insumo, Unidad_medida, Cantidad_actual, Stock_minimo, Costo_insumo FROM INSUMO";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()){
            
            while (rs.next()) {
                String codigo = rs.getString("Codigo_insumo");
                String nombre = rs.getString("Nombre_insumo");
                String unidad = rs.getString("Unidad_medida");
                double cantidad = rs.getDouble("Cantidad_actual");
                double minimo = rs.getDouble("Stock_minimo");
                double costo = rs.getDouble("Costo_insumo");
                
                Insumo insumoDB = new Insumo(codigo, nombre, unidad, cantidad, minimo, costo);
                inventario.add(insumoDB);
            }
        } catch (SQLException e) {
            throw new BDException("Error al consultar el inventario: " + e.getMessage(), e);
        }
        return inventario;
    }
    
    public boolean reabastecerStockActual(String codigoInsumo, double cantidad) throws BDException{
        double stockAntiguo = obtenerStockActual(codigoInsumo);
        if (stockAntiguo == -1.0) {
            System.out.println("El insumo no existe en la base de datos.");
            return false;
        }
        double nuevoStock = stockAntiguo + cantidad;
        
        String query = "UPDATE INSUMO SET Cantidad_actual = Cantidad_actual + ? WHERE Codigo_insumo = ?";
          
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            

            ps.setDouble(1, cantidad);
            ps.setString(2, codigoInsumo);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new BDException("Error al reabastecer el insumo: " + e.getMessage(), e);
        }
    }
    
    public double obtenerStockActual(String codigoInsumo) throws BDException {
        String query = "SELECT Cantidad_actual FROM INSUMO WHERE Codigo_insumo = ?";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, codigoInsumo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Cantidad_actual");
                } else {
                    // -1 es que no existe
                    return -1.0;
                }
            }
            
        } catch (SQLException e) {
            throw new BDException("Error al consultar el stock actual: " + e.getMessage(), e);
        }
    }
    public boolean descontarStockActual(String codigoInsumo, double cantidadUsada)throws BDException{
        double stockActual = obtenerStockActual(codigoInsumo);
        
        if (stockActual == -1.0){
            System.out.println("Error: el insumo no existe en la base de datos");
            return false;
        }
        
        if (stockActual < cantidadUsada) {
            System.out.println("Stock insuficiente. Tienes " + stockActual + 
                               " y se intentó descontar " + cantidadUsada);
            return false; 
        }
        
        double nuevoStock = stockActual - cantidadUsada;
        String query = "UPDATE INSUMO SET Cantidad_actual = ? WHERE Codigo_insumo = ?";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setDouble(1, nuevoStock);
            ps.setString(2, codigoInsumo);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new BDException("Error al descontar el stock del insumo: " + e.getMessage(), e);
        }
    }
    
    public List<Insumo> obtenerInsumosBajoStock() throws BDException {
        List<Insumo> listaAlertas = new ArrayList<>();
        
        String query = "SELECT Codigo_insumo, Nombre_insumo, Unidad_medida, Cantidad_actual, Stock_minimo, Costo_insumo " +
                       "FROM insumo WHERE Cantidad_actual <= Stock_minimo";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                
                Insumo insumo = new Insumo(
                    rs.getString("Codigo_insumo"),
                    rs.getString("Nombre_insumo"),
                    rs.getString("Unidad_medida"),
                    rs.getDouble("Cantidad_actual"),
                    rs.getDouble("Stock_minimo"),
                    rs.getDouble("Costo_insumo")
                );
                
                listaAlertas.add(insumo);
            }
        } catch (SQLException e) {
            throw new BDException("Error al consultar alertas de stock: " + e.getMessage(), e);
        }
        
        return listaAlertas;
    }
}
