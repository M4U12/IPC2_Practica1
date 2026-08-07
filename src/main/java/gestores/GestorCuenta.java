package gestores;
import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Cuenta;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import modelos.DetalleCuenta;

public class GestorCuenta {
    private DBConnection conexionDB;
    
    public GestorCuenta(){
        this.conexionDB = new DBConnection();
    }
    
    public boolean abrirCuenta(Cuenta cuenta) throws BDException {
        String query = "INSERT INTO CUENTA (Codigo_cuenta, Fecha, Hora_ocupacion, Hora_liberacion, Estado_cuenta, Total_de_cuenta, Propina, Numero_mesa, DPI_mesero) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, cuenta.getCodigoCuenta());
            ps.setDate(2, Date.valueOf(cuenta.getFecha())); 
            ps.setTime(3, Time.valueOf(cuenta.getHoraOcupacion())); 
            
            if (cuenta.getHoraLiberacion() != null) { // si la hora de liberación es nula (porque acaba de entrar) guardo un NULL en la bd
                ps.setTime(4, Time.valueOf(cuenta.getHoraLiberacion()));
            } else {
                ps.setNull(4, java.sql.Types.TIME);
            }
            
            ps.setString(5, cuenta.getEstadoCuenta());
            ps.setDouble(6, cuenta.getTotalCuenta());
            ps.setDouble(7, cuenta.getPropina());
            ps.setInt(8, cuenta.getNumeroMesa());
            ps.setString(9, cuenta.getDpiMesero());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new BDException("Error al abrir la cuenta: " + e.getMessage(), e);
        }
    }
    
    public boolean agregarDetalle(DetalleCuenta detalle) throws BDException {
        String queryInsert = "INSERT INTO DETALLE_CUENTA (Codigo_cuenta, Cantidad, Codigo_producto, Subtotal) VALUES (?, ?, ?, ?)";
        String queryUpdateTotal = "UPDATE CUENTA SET Total_de_cuenta = Total_de_cuenta + ? WHERE Codigo_cuenta = ?";
        
        try (Connection connection = conexionDB.getConnection()) {
            
            // guarda el platillo en el detalle
            try (PreparedStatement psInsert = connection.prepareStatement(queryInsert)) {
                psInsert.setString(1, detalle.getCodigoCuenta());
                psInsert.setInt(2, detalle.getCantidad());
                psInsert.setString(3, detalle.getCodigoProducto());
                psInsert.setDouble(4, detalle.getSubtotal());
                psInsert.executeUpdate();
            }
            
            // actualiza el total de la cuenta
            try (PreparedStatement psUpdate = connection.prepareStatement(queryUpdateTotal)) {
                psUpdate.setDouble(1, detalle.getSubtotal());
                psUpdate.setString(2, detalle.getCodigoCuenta());
                
                int filasAfectadas = psUpdate.executeUpdate();
                return filasAfectadas > 0;
            }
            
        } catch (SQLException e) {
            throw new BDException("Error al agregar detalle y actualizar total: " + e.getMessage(), e);
        }
    }
    
    public List<DetalleCuenta> obtenerDetalles(String codigoCuenta) throws BDException {
        List<DetalleCuenta> listaDetalles = new ArrayList<>();
        String query = "SELECT Codigo_cuenta, Cantidad, Codigo_producto, Subtotal FROM DETALLE_CUENTA WHERE Codigo_cuenta = ?";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, codigoCuenta);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCuenta dc = new DetalleCuenta(
                        rs.getString("Codigo_cuenta"),
                        rs.getInt("Cantidad"),
                        rs.getString("Codigo_producto"),
                        rs.getDouble("Subtotal")
                    );
                    listaDetalles.add(dc);
                }
            }
        } catch (SQLException e) {
            throw new BDException("Error al obtener los detalles: " + e.getMessage(), e);
        }
        return listaDetalles;
    }
    
    public boolean cobrarCuenta(String codigoCuenta, double totalFinal, double propina) throws BDException {
        String query = "UPDATE CUENTA SET Estado_cuenta = 'PAGADA', Hora_liberacion = ?, Total_de_cuenta = ?, Propina = ? WHERE Codigo_cuenta = ?";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setTime(1, Time.valueOf(LocalTime.now())); //hora de salida
            ps.setDouble(2, totalFinal);
            ps.setDouble(3, propina);
            ps.setString(4, codigoCuenta);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new BDException("Error al cobrar la cuenta " + codigoCuenta + ": " + e.getMessage(), e);
        }
    }
    
    public List<Cuenta> listarCuentasAbiertas(String dpiMesero) throws BDException {
        List<Cuenta> cuentas = new ArrayList<>();
        String query = "SELECT * FROM CUENTA WHERE DPI_mesero = ? AND Estado_cuenta = 'ABIERTA'";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, dpiMesero);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    
                    Time horaLibSQL = rs.getTime("Hora_liberacion");
                    LocalTime horaLibLocal = (horaLibSQL != null) ? horaLibSQL.toLocalTime() : null;

                    Cuenta c = new Cuenta(
                        rs.getString("Codigo_cuenta"),
                        rs.getDate("Fecha").toLocalDate(),
                        rs.getTime("Hora_ocupacion").toLocalTime(),
                        horaLibLocal,
                        rs.getString("Estado_cuenta"),
                        rs.getDouble("Total_de_cuenta"),
                        rs.getDouble("Propina"),
                        rs.getInt("Numero_mesa"),
                        rs.getString("DPI_mesero")
                    );
                    cuentas.add(c);
                }
            }
        } catch (SQLException e) {
            throw new BDException("Error al listar cuentas del mesero: " + e.getMessage(), e);
        }
        return cuentas;
    }
}
