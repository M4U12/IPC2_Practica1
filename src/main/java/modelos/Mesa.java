package modelos;

public class Mesa {
    private int mesa;
    private int capacidad;
    private String estadoActual;
    
    public Mesa(int mesa, int capacidad, String estadoActual){
        this.mesa = mesa;
        this.capacidad = capacidad;
        this.estadoActual = estadoActual;
    }

    public int getNumeroMesa() {
        return mesa;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setNumeroMesa(int mesa) {
        this.mesa = mesa;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }
    
    
    
}
