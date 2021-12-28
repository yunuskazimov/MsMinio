package az.xazar.msminio.model.error;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class entity) {
        super(entity.getSimpleName() + " ");
    }

    public EntityNotFoundException(String msg) {
        super(msg);
    }

    public EntityNotFoundException(Class entity, Object id) {
        super(entity.getSimpleName() + "with ID:" + id + " ");
    }
}
