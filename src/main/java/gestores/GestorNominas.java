package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Nomina;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelos.Empleado;

public class GestorNominas {
    private DBConnection conexionDB;
    
    public GestorNominas(){
        conexionDB = new DBConnection();
    }
    
    public double calcularPropinasAcumuladas(String dpiMesero, LocalDate fechaInicio, LocalDate fechaFin) throws BDException{
        String query = "SELECT SUM(Propina) AS TotalPropinas FROM CUENTA WHERE DPI_mesero = ? AND Estado_cuenta = 'Pagada' AND Fecha BETWEEN ? AND ?";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, dpiMesero);;
            ps.setDate(2, Date.valueOf(fechaInicio));
            ps.setDate(3, Date.valueOf(fechaFin));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TotalPropinas");
                }
                return 0.0;
            }
            
        } catch (SQLException e) {
            throw new BDException("Error al calcular propinas: " + e.getMessage(), e);
        }
    }
    
    public boolean registrarPago(Nomina nomina) throws BDException {
        String query = "INSERT INTO nomina (Codigo_nomina, DPI, Estado_pago, Fecha_emision_pago, Tipo_pago, Monto_pago) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, nomina.getCodigoNomina());
            ps.setString(2, nomina.getEmpleado().getDpi()); 
            ps.setString(3, nomina.getEstadoPago());
            ps.setDate(4, Date.valueOf(nomina.getFechaEmisionPago()));
            ps.setString(5, nomina.getTipoPago());
            ps.setDouble(6, nomina.getMontoPago());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new BDException("Error al registrar el pago de nómina: " + e.getMessage(), e);
        }
    }
    
    public boolean procesarPago(String codigoNomina, Empleado empleado, double sueldo, String rol, LocalDate inicioQuincena, LocalDate finQuincena) throws BDException{
            double pagoTotal = sueldo;
            
            if (empleado.getRol().equalsIgnoreCase("Mesero")) {
                double propinas = calcularPropinasAcumuladas(empleado.getDpi(), inicioQuincena, finQuincena);
                pagoTotal += propinas;
            }
            
            Nomina nuevaNomina = new Nomina(
            codigoNomina, LocalDate.now(), "Quincenal", pagoTotal, "Efectuado", empleado); 
            
            return registrarPago(nuevaNomina);    
        
    }
    
    
    public List<Nomina> listarPagos() throws BDException {
        List<Nomina> historial = new ArrayList<>();
        
        String query = "SELECT n.Codigo_nomina, n.DPI, n.Estado_pago, n.Fecha_emision_pago, n.Tipo_pago, n.Monto_pago, " +
                       "e.Nombre, e.Rol " + 
                       "FROM nomina n " +
                       "INNER JOIN empleado e ON n.DPI = e.DPI";
        
        try (Connection connection = conexionDB.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                
                Empleado empReal = new Empleado(
                    rs.getString("DPI"),
                    rs.getString("Nombre"),
                    rs.getString("Rol")
                );

                Nomina n = new Nomina(
                    rs.getString("Codigo_nomina"),
                    rs.getDate("Fecha_emision_pago").toLocalDate(),
                    rs.getString("Tipo_pago"),
                    rs.getDouble("Monto_pago"),
                    rs.getString("Estado_pago"),
                    empReal
                );
                historial.add(n);
            }
        } catch (SQLException e) {
            throw new BDException("Error al listar nóminas: " + e.getMessage(), e);
        }
        return historial;
    }
}
