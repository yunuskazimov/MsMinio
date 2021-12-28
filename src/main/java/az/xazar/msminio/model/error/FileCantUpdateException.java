package az.xazar.msminio.model.error;

public class FileCantUpdateException extends RuntimeException{
    public FileCantUpdateException(String fileName){
        super(fileName + ", this file cant update!");
    }
}
