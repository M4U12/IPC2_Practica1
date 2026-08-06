package excepciones;

public class BDException extends Exception{
    
    public BDException(String mensaje){
        super(mensaje);
    }
    
    public BDException(String mensaje, Throwable causa){
        super (mensaje, causa);
    }
    
}
