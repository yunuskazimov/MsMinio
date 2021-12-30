package az.xazar.msminio.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {

    String uploadImageForProfile(MultipartFile file, Long id, String type);

    String updateImageForProfile(Long id, Long userId, MultipartFile file, String type);

    String deleteUserImage(Long id);

    byte[] getFile(String fileName, String folder);

}
