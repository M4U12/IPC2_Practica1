package modelos;

public class Insumo {
    private String codigoInsumo;
    private String nombreInsumo;
    private String unidadMedida;
    private double cantidadActual;
    private double stockMinimo;
    private double costoInsumo;
    
    public Insumo(String codigoInsumo, String nombreInsumo, String unidadMedida, double cantidadActual, double stockMinimo, double costoInsumo){
        this.codigoInsumo = codigoInsumo;
        this.nombreInsumo = nombreInsumo;
        this.unidadMedida = unidadMedida;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
        this.costoInsumo = costoInsumo;
    }

    public String getCodigoInsumo() {
        return codigoInsumo;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public double getCantidadActual() {
        return cantidadActual;
    }

    public double getStockMinimo() {
        return stockMinimo;
    }

    public double getCostoInsumo() {
        return costoInsumo;
    }

    public void setCodigoInsumo(String codigoInsumo) {
        this.codigoInsumo = codigoInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public void setCantidadActual(double cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public void setStockMinimo(double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public void setCostoInsumo(double costoInsumo) {
        this.costoInsumo = costoInsumo;
    }
    
    
}
