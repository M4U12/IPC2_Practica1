package modelos;
import java.time.LocalDate;

public class Empleado {
    private String dpi;
    private String nombre;
    private String jornadaLaboral;
    private double salario;
    private LocalDate fechaContratacion;
    private boolean estado;
    private String rol;
    
    
    public Empleado(String dpi, String nombre, String jornadaLaboral, double salario, LocalDate fechaContratacion, boolean estado, String rol){
        this.dpi = dpi;
        this.nombre = nombre;
        this.jornadaLaboral = jornadaLaboral;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.estado = estado;
        this.rol = rol;
    }
    
    public Empleado(String dpi, String nombre, String rol) {
        this.dpi = dpi;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getDpi() {
        return dpi;
    }

    public String getNombre() {
        return nombre;
    }

    public String getJornadaLaboral() {
        return jornadaLaboral;
    }

    public double getSalario() {
        return salario;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public boolean getEstado() {
        return estado;
    }

    public String getRol() {
        return rol;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setJornadaLaboral(String jornadaLaboral) {
        this.jornadaLaboral = jornadaLaboral;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
    
    
    
    
}

    

