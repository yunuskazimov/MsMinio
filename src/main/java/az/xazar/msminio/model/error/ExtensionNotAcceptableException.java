package az.xazar.msminio.model.error;

public class ExtensionNotAcceptableException extends RuntimeException {
    public ExtensionNotAcceptableException(String extension){
        super("." +extension + " ");
    }
}
