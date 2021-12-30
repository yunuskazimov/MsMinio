package az.xazar.msminio.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface ProfilePictureService {

    String uploadImageForProfile(MultipartFile file, Long id, String type);

    String updateImageForProfile(Long id, Long userId, MultipartFile file, String type);

    String deleteUserImage(Long id);

    ResponseEntity<Object> getFile(HttpServletRequest request);

}
