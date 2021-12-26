package az.xazar.msminio.service;

import az.xazar.msminio.model.clinet.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileServiceInt {

    UserDto findById(Long id);

    String uploadImageForUser(MultipartFile file, Long id, String type);

    String uploadFileForUser(MultipartFile file, Long id, String type);

    void deleteUserImage(Long id,String fileName);

    byte[] getImage(Long id, String fileName, String folder);

}
