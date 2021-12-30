package az.xazar.msminio.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface FileService {

    String uploadFileForUser(MultipartFile file, Long id, String type);

    String updateFileForUser(Long id, Long userId, MultipartFile file, String type);

    ResponseEntity<Object> getFile(HttpServletRequest request);

    String deleteFileById(Long id);

}
