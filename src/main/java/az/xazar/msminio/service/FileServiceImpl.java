package az.xazar.msminio.service;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.model.clinet.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final UserClientRest userClient;

    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.video-folder}")
    private String fileFolder;

    public FileServiceImpl(UserClientRest userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserDto findById(Long id) {
        log.info("findById User:   " + userClient.getById(id).toString());
        return userClient.getById(id);
    }

    @Override
    public String uploadUserImage(MultipartFile file, Long id) {
        return null;
    }

    @Override
    public String updateUserImage(MultipartFile file, Long id) {
        return null;
    }

    @Override
    public void deleteUserImage(Long id) {

    }

    @Override
    public String uploadUserFile(MultipartFile file, Long id) {
        return null;
    }

    @Override
    public String updateUserFile(MultipartFile file, Long id) {
        return null;
    }

    @Override
    public void deleteUserFile(Long id) {

    }

    @Override
    public byte[] getFile(String fileName, String folder) {
        return new byte[0];
    }

    @Override
    public void deleteFile(String fileName, String folder) {

    }
}
