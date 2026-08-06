package modelos;


public class Receta {
    private String codigoProducto;
    private String codigoInsumo;
    private double cantidad;

    public Receta(String codigoProducto, String codigoInsumo, double cantidad) {
        this.codigoProducto = codigoProducto;
        this.codigoInsumo = codigoInsumo;
        this.cantidad = cantidad;
    }
    
    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getCodigoInsumo() {
        return codigoInsumo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public void setCodigoInsumo(String codigoInsumo) {
        this.codigoInsumo = codigoInsumo;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
    
    
}
