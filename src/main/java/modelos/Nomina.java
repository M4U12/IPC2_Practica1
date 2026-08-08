package modelos;

import java.time.LocalDate;

public class Nomina {
    private String codigoNomina;
    private LocalDate fechaEmisionPago;
    private String tipoPago;
    private double montoPago;
    private String estadoPago;
    private Empleado empleado;
    
    public Nomina(String codigoNomina, LocalDate fechaEmisionPago, String tipoPago, 
                  double montoPago, String estadoPago, Empleado empleado) {
        this.codigoNomina = codigoNomina;
        this.fechaEmisionPago = fechaEmisionPago;
        this.tipoPago = tipoPago;
        this.montoPago = montoPago;
        this.estadoPago = estadoPago;
        this.empleado = empleado;
    }

    public String getCodigoNomina() {
        return codigoNomina;
    }

    public LocalDate getFechaEmisionPago() {
        return fechaEmisionPago;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public double getMontoPago() {
        return montoPago;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setCodigoNomina(String codigoNomina) {
        this.codigoNomina = codigoNomina;
    }

    public void setFechaEmisionPago(LocalDate fechaEmisionPago) {
        this.fechaEmisionPago = fechaEmisionPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public void setMontoPago(double montoPago) {
        this.montoPago = montoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
    
    
}
