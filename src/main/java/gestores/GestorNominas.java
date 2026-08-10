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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import modelos.Empleado;

public class GestorNominas {

    private DBConnection conexionDB;

    public GestorNominas() {
        conexionDB = new DBConnection();
    }

    public double calcularPropinasAcumuladas(String dpiMesero, LocalDate fechaInicio, LocalDate fechaFin) throws BDException {
        String query = "SELECT SUM(Propina) AS TotalPropinas FROM CUENTA WHERE DPI_mesero = ? AND Estado_cuenta = 'Pagada' AND Fecha BETWEEN ? AND ?";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, dpiMesero);
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

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

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

    public List<Nomina> listarPagos() throws BDException {
        List<Nomina> historial = new ArrayList<>();

        String query = "SELECT n.Codigo_nomina, n.DPI, n.Estado_pago, n.Fecha_emision_pago, n.Tipo_pago, n.Monto_pago, "
                + "e.Nombre, e.Rol "
                + "FROM nomina n "
                + "INNER JOIN empleado e ON n.DPI = e.DPI";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

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

    public boolean planillaPendiente(LocalDate fechaCorte, boolean esPrimeraQuincena) throws BDException {
        String query = "SELECT DPI, Nombre, Rol, Salario FROM empleado WHERE Estado = 1";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement psEmpleados = connection.prepareStatement(query); ResultSet rs = psEmpleados.executeQuery()) {

            boolean huboPagos = false;
            int correlativo = 1;

            while (rs.next()) {
                String dpi = rs.getString("DPI");
                if (empleadoYaTieneBoleta(dpi, fechaCorte)) {
                    continue;
                }

                String rol = rs.getString("Rol");
                double sueldoBase = rs.getDouble("Salario");

                double montoAPagar = esPrimeraQuincena ? (sueldoBase * 0.30) : (sueldoBase * 0.70);

                if (rol.equalsIgnoreCase("Mesero")) {
                    LocalDate inicioPeriodo;
                    LocalDate finPeriodo = fechaCorte;

                    if (esPrimeraQuincena) {
                        inicioPeriodo = fechaCorte.withDayOfMonth(1);
                    } else {
                        inicioPeriodo = fechaCorte.withDayOfMonth(16);
                    }

                    double propinas = calcularPropinasAcumuladas(dpi, inicioPeriodo, finPeriodo);
                    montoAPagar += propinas;
                }

                String codigoNomina = "NOM-" + fechaCorte.toString().replace("-", "") + "-" + (System.currentTimeMillis() % 100000) + "-" + correlativo;

                Empleado empleadoReferencia = new Empleado(dpi, rs.getString("Nombre"), rol);

                Nomina nominaPendiente = new Nomina(
                        codigoNomina,
                        LocalDate.now(),
                        esPrimeraQuincena ? "QUINCENAL" : "FIN_DE_MES",
                        montoAPagar,
                        "PENDIENTE",
                        empleadoReferencia
                );

                registrarPago(nominaPendiente);
                huboPagos = true;
                correlativo++;
            }

            return huboPagos;

        } catch (SQLException e) {
            throw new BDException("Error al generar planilla pendiente: " + e.getMessage(), e);
        }
    }

    public boolean efectuarPagosPendientes(LocalDate fechaCorte) throws BDException {
        String query = "UPDATE nomina SET Estado_pago = 'PAGADO' WHERE Fecha_emision_pago = ? AND Estado_pago = 'PENDIENTE'";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(fechaCorte));
            int filasAfectadas = ps.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new BDException("Error al efectuar los pagos pendientes: " + e.getMessage(), e);
        }
    }

    public String verificarYGenerarPlanillaAutomatica() throws BDException {
        LocalDate hoy = LocalDate.now();
        int dia = hoy.getDayOfMonth();

        YearMonth mesActual = YearMonth.from(hoy);
        int ultimoDiaDelMes = mesActual.lengthOfMonth();

        int diaAlertaFinDeMes = ultimoDiaDelMes - 5;

        if (dia == 10 || dia == diaAlertaFinDeMes) {
            boolean esPrimeraQuincena = (dia == 10);
            LocalDate fechaCorte = esPrimeraQuincena ? hoy.withDayOfMonth(15) : hoy.withDayOfMonth(ultimoDiaDelMes);

            boolean generado = planillaPendiente(fechaCorte, esPrimeraQuincena);
            if (generado) {
                return "Sistema Automático: Se han generado las boletas PENDIENTES para el corte del " + fechaCorte.toString();
            }
        }
        return null;
    }

    private boolean empleadoYaTieneBoleta(String dpi, LocalDate fechaCorte) throws BDException {
        String prefijo = "NOM-" + fechaCorte.toString().replace("-", "") + "-%";
        String query = "SELECT COUNT(*) AS total FROM nomina WHERE DPI = ? AND Codigo_nomina LIKE ?";

        
        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, dpi);
            ps.setString(2, prefijo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }

        } catch (Exception e) {
            throw new BDException("Error al verificar la boleta individual del empleado: " + e.getMessage(), e);
        }

        return false;
    }

}
