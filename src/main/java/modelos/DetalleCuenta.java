package modelos;

public class DetalleCuenta {
    private String codigoCuenta;
    private int cantidad;
    private String codigoProducto;
    private double subtotal;
    
    public DetalleCuenta(String codigoCuenta, int cantidad, String codigoProducto, double subtotal) {
        this.codigoCuenta = codigoCuenta;
        this.cantidad = cantidad;
        this.codigoProducto = codigoProducto;
        this.subtotal = subtotal;
    }

    public String getCodigoCuenta() {
        return codigoCuenta;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setCodigoCuenta(String codigoCuenta) {
        this.codigoCuenta = codigoCuenta;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    
}
