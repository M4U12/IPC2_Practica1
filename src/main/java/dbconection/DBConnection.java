package dbconection;

import excepciones.BDException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    private static final  String IP = "localhost";
    private static final int PUERTO = 3306;
    private static final String SCHEMA = "restaurante";
    public static final String USER_NAME = "mau";
    public static final String PASSWORD = "IPC2026";
    public static final String URL = "jdbc:mysql://" + IP + ":" + PUERTO + "/" + SCHEMA;
    
    
    public Connection getConnection() throws BDException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new BDException("Error crítico: No se pudo conectar a la base de datos del restaurante.", e);
        }
        return connection;
    }
}
