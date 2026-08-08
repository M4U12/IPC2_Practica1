package modelos;

public class Producto {
    private String codigoProducto;
    private String nombreProducto;
    private String categoria;
    private double precioVenta;
    private byte[] fotografia;

    public Producto(String codigoProducto, String nombreProducto, String categoria, double precioVenta, byte[] fotografia) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.precioVenta = precioVenta;
        this.fotografia = fotografia;
    }

    public byte[] getFotografia() {
        return fotografia;
    }

    public void setFotografia(byte[] fotografia) {
        this.fotografia = fotografia;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setCodigoProducto(String codigoPoducto) {
        this.codigoProducto = codigoPoducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }
    
    
    
}
