package az.xazar.msminio.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface FileService {


    ResponseEntity<Object> getFile(HttpServletRequest request);

    String getFileUrl(HttpServletRequest request);

    String deleteFileById(Long id);

}
