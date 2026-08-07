package modelos;
import java.time.LocalDate;
import java.time.LocalTime;

public class Cuenta {
    private String codigoCuenta;
    private LocalDate fecha;
    private LocalTime horaOcupacion;
    private LocalTime horaLiberacion;
    private String estadoCuenta;
    private double totalCuenta;
    private double propina;
    private int numeroMesa;
    private String dpiMesero;
    
    public Cuenta(String codigoCuenta, LocalDate fecha, LocalTime horaOcupacion, LocalTime horaLiberacion, 
                  String estadoCuenta, double totalCuenta, double propina, int numeroMesa, String dpiMesero){
        this.codigoCuenta = codigoCuenta;
        this.fecha = fecha;
        this.horaOcupacion = horaOcupacion;
        this.horaLiberacion = horaLiberacion;
        this.estadoCuenta = estadoCuenta;
        this.totalCuenta = totalCuenta;
        this.propina = propina;
        this.numeroMesa = numeroMesa;
        this.dpiMesero = dpiMesero;
    }

    public String getCodigoCuenta() {
        return codigoCuenta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraOcupacion() {
        return horaOcupacion;
    }

    public LocalTime getHoraLiberacion() {
        return horaLiberacion;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public double getTotalCuenta() {
        return totalCuenta;
    }

    public double getPropina() {
        return propina;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public String getDpiMesero() {
        return dpiMesero;
    }

    public void setCodigoCuenta(String codigoCuenta) {
        this.codigoCuenta = codigoCuenta;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHoraOcupacion(LocalTime horaOcupacion) {
        this.horaOcupacion = horaOcupacion;
    }

    public void setHoraLiberacion(LocalTime horaLiberacion) {
        this.horaLiberacion = horaLiberacion;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public void setTotalCuenta(double totalCuenta) {
        this.totalCuenta = totalCuenta;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public void setDpiMesero(String dpiMesero) {
        this.dpiMesero = dpiMesero;
    }

    
    
}
