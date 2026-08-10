package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.time.LocalDate;

public class GestorPersonal {

    private DBConnection conexionDB;

    public GestorPersonal() {
        this.conexionDB = new DBConnection();
    }

    public boolean registrarEmpleado(Empleado empleado) throws BDException {
        String query = "INSERT INTO EMPLEADO (DPI, Nombre, Jornada_LABORAL, Salario, Fecha_contratacion, Estado, Rol) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, empleado.getDpi());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getJornadaLaboral());
            ps.setDouble(4, empleado.getSalario());
            ps.setDate(5, Date.valueOf(empleado.getFechaContratacion()));
            ps.setBoolean(6, empleado.getEstado());
            ps.setString(7, empleado.getRol());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new BDException("Error al registrar el empleado en la base de datos");
        }
    }

    public boolean actualizarEmpleado(String dpiAntiguo, Empleado empleado) throws BDException {
        String query = "UPDATE EMPLEADO SET DPI = ?, Nombre = ?, Jornada_LABORAL = ?, Salario = ?, Fecha_contratacion = ?, Estado = ?, Rol = ? WHERE DPI = ?";
        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, empleado.getDpi()); // es el dpi nuevo que viene de la tabla
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getJornadaLaboral());
            ps.setDouble(4, empleado.getSalario());
            ps.setDate(5, Date.valueOf(empleado.getFechaContratacion()));
            ps.setBoolean(6, empleado.getEstado());
            ps.setString(7, empleado.getRol());
            ps.setString(8, dpiAntiguo); // este es el dpi que tenía para saber a quién modificar
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new BDException("Error al actualizar los datos del empleado: " + e.getMessage(), e);
        }
    }

    public List<Empleado> listarEmpleados(String filtro) throws BDException {
        List<Empleado> listaEmpleados = new ArrayList<>();
        String query = "SELECT DPI, Nombre, Jornada_LABORAL, Salario, Fecha_contratacion, Estado, Rol FROM EMPLEADO";

        if (filtro.equals("Activos")) {
            query += " WHERE Estado = 1";
        } else if (filtro.equals("Inactivos")) {
            query += " WHERE Estado = 0";
        }

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                //para extraer de las columnas
                String dpi = rs.getString("DPI");
                String nombre = rs.getString("Nombre");
                String jornada = rs.getString("Jornada_LABORAL");
                double salario = rs.getDouble("Salario");
                LocalDate fecha = rs.getDate("Fecha_contratacion").toLocalDate();
                boolean estado = rs.getBoolean("Estado");
                String rol = rs.getString("Rol");

                //lo guardo en mi clase modelo
                Empleado empleadoDB = new Empleado(dpi, nombre, jornada, salario, fecha, estado, rol);
                listaEmpleados.add(empleadoDB);
            }

        } catch (SQLException e) {
            throw new BDException("Error al extraer la lista de empleados: " + e.getMessage(), e);
        }
        return listaEmpleados;
    }

    public boolean desactivarEmpleado(String dpi) throws BDException {
        String query = "UPDATE EMPLEADO SET Estado = false WHERE DPI = ?";
        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, dpi);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new BDException("Error al desactivar al empleado: " + e.getMessage(), e);
        }
    }

    public Empleado buscarEmpleadoPorDPI(String dpi) throws BDException {
        String query = "SELECT * FROM EMPLEADO WHERE DPI = ?";
        try (Connection conn = conexionDB.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, dpi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Empleado(
                            rs.getString("DPI"),
                            rs.getString("Nombre"),
                            rs.getString("Jornada_LABORAL"),
                            rs.getDouble("Salario"),
                            rs.getDate("Fecha_contratacion").toLocalDate(),
                            rs.getBoolean("Estado"),
                            rs.getString("Rol")
                    );
                }
            }
        } catch (SQLException e) {
            throw new BDException("Error al buscar el empleado: " + e.getMessage(), e);
        }
        return null;
    }

}
