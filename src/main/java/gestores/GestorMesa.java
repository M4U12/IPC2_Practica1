package gestores;

import dbconection.DBConnection;
import excepciones.BDException;
import modelos.Mesa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestorMesa {

    private DBConnection conexionDB;

    public GestorMesa() {
        this.conexionDB = new DBConnection();
    }

    public boolean registrarMesa(Mesa mesa) throws BDException {
        String query = "INSERT INTO MESA (Numero_mesa, Capacidad, Estado_actual) VALUES (?, ?, ?)";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, mesa.getNumeroMesa());
            ps.setInt(2, mesa.getCapacidad());
            ps.setString(3, mesa.getEstadoActual());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new BDException("Error al registrar la mesa: " + e.getMessage(), e);
        }
    }

    public List<Mesa> listarMesas() throws BDException {
        List<Mesa> listaMesas = new ArrayList<>();
        String query = "SELECT Numero_mesa, Capacidad, Estado_actual FROM MESA";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Mesa mesaDB = new Mesa(
                        rs.getInt("Numero_mesa"),
                        rs.getInt("Capacidad"),
                        rs.getString("Estado_actual")
                );
                listaMesas.add(mesaDB);
            }

        } catch (SQLException e) {
            throw new BDException("Error al consultar el estado de las mesas: " + e.getMessage(), e);
        }
        return listaMesas;
    }

    public boolean cambiarEstadoMesa(int numeroMesa, String nuevoEstado) throws BDException {
        String query = "UPDATE MESA SET Estado_actual = ? WHERE Numero_mesa = ?";

        try (Connection connection = conexionDB.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, numeroMesa);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new BDException("Error al actualizar el estado de la mesa " + numeroMesa + ": " + e.getMessage(), e);
        }
    }

    public List<Mesa> listarMesasDisponibles() throws BDException {
        List<Mesa> mesasLibres = new ArrayList<>();
        String query = "SELECT Numero_mesa, Capacidad, Estado_actual FROM MESA WHERE Estado_actual = 'DISPONIBLE' OR Estado_actual = 'LIBRE'";

        try (Connection connection = conexionDB.getConnection(); 
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Mesa mesa = new Mesa(
                        rs.getInt("Numero_mesa"),
                        rs.getInt("Capacidad"),
                        rs.getString("Estado_actual")
                );
                mesasLibres.add(mesa);
            }
        } catch (SQLException e) {
            throw new BDException("Error al consultar las mesas disponibles: " + e.getMessage(), e);
        }
        return mesasLibres;
    }
}
