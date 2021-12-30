package az.xazar.msminio.model.error;


import az.xazar.msminio.model.ErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static az.xazar.msminio.model.error.ErrorCodes.*;


@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    Logger logger = LoggerFactory.getLogger(ErrorHandler.class.getName());

    @ExceptionHandler(ExtensionNotAcceptableException.class)
    public ResponseEntity<Object> handleAdNotFoundException(ExtensionNotAcceptableException ex,
                                                            WebRequest webRequest) {
        logger.info(ex.toString());

        return handleExceptionInternal(ex, ErrorDto.builder()
                        .code(EXTENSION_NOT_ACCEPTABLE)
                        .message(ex.getMessage())
                        .build(),
                new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE, webRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleAdNotFoundException(EntityNotFoundException ex,
                                                            WebRequest webRequest) {
        logger.info(ex.toString());

        return handleExceptionInternal(ex, ErrorDto.builder()
                        .code(ENTITY_NOT_FOUND)
                        .message(ex.getMessage())
                        .build(),
                new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);
    }

    @ExceptionHandler(FileCantUpdateException.class)
    public ResponseEntity<Object> handleAdNotFoundException(FileCantUpdateException ex,
                                                            WebRequest webRequest) {
        logger.info(ex.toString());

        return handleExceptionInternal(ex, ErrorDto.builder()
                        .code(FILE_NOT_UPDATE)
                        .message(ex.getMessage())
                        .build(),
                new HttpHeaders(), HttpStatus.NOT_MODIFIED, webRequest);
    }

    @ExceptionHandler(FileCantUploadException.class)
    public ResponseEntity<Object> handleAdNotFoundException(FileCantUploadException ex,
                                                            WebRequest webRequest) {
        logger.info(ex.toString());

        return handleExceptionInternal(ex, ErrorDto.builder()
                        .code(FILE_NOT_UPLOAD)
                        .message(ex.getMessage())
                        .build(),
                new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE, webRequest);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Object> handleAdNotFoundException(FileNotFoundException ex,
                                                            WebRequest webRequest) {
        logger.info(ex.toString());

        return handleExceptionInternal(ex, ErrorDto.builder()
                        .code(NOT_FOUND)
                        .message(ex.getMessage())
                        .build(),
                new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllException(Exception ex,
                                                     WebRequest webRequest) {
        logger.info(ex.getMessage());

        return handleExceptionInternal(ex, ErrorDto.builder()
                        .code(UNEXPECTED_EXCEPTION)
                        .message(ex.getMessage())
                        .build(),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }
}
