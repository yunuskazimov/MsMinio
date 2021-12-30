package az.xazar.msminio.service;

import az.xazar.msminio.model.clinet.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {

    String uploadImageForProfile(MultipartFile file, Long id, String type);

    String updateUserImage(Long id, Long userId, MultipartFile file, String type);

    void deleteUserImage(Long id,String fileName);

    byte[] getFile(String fileName, String folder);

}
