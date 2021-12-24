package az.xazar.msminio.service;

import az.xazar.msminio.model.clinet.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    UserDto findById(Long id);

    String uploadUserImage(MultipartFile file, Long id);

    String updateUserImage(MultipartFile file, Long id);

    void deleteUserImage(Long id);

    String uploadUserFile(MultipartFile file, Long id);

    String updateUserFile(MultipartFile file, Long id);

    void deleteUserFile(Long id);

    byte[] getFile(String fileName, String folder);

    void deleteFile(String fileName, String folder);
}
