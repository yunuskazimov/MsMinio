package az.xazar.msminio.service;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.entity.UsersFileEntity;
import az.xazar.msminio.model.clinet.UserDto;
import az.xazar.msminio.model.error.EntityNotFoundException;
import az.xazar.msminio.model.error.FileCantUploadException;
import az.xazar.msminio.repository.UserFileRepository;
import az.xazar.msminio.util.IntFileUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final UserClientRest userClient;
    private final UserFileRepository userRepository;
    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.video-folder}")
    private String fileFolder;


    private final MinioClient minioClient;
    private final IntFileUtil intFileUtil;
    @Value("${minio.bucket}")
    private String bucketName;
    private final String VIDEO_MEDIA_TYPE = "video";
    private final String IMAGE_MEDIA_TYPE = "image";


    public FileServiceImpl(UserClientRest userClient,
                           UserFileRepository userRepository,
                           MinioClient minioClient,
                           IntFileUtil intFileUtil) {
        this.userClient = userClient;
        this.userRepository = userRepository;
        this.minioClient = minioClient;
        this.intFileUtil = intFileUtil;
    }


    @Override
    public UserDto findById(Long id) {
        log.info("findById User:   " + userClient.getById(id).toString());
        return userClient.getById(id);
    }

    @Override
    @Transactional
    public String uploadUserImage(MultipartFile file, Long id) {
        log.info("uploadImage to User started with, {}",
                kv("partnerId", id));

        //TODO Burda gelen exception u tut. USer not found

        // MAPPER ELAVE ET!!!

        UserDto clientUser = userClient.getById(id);

        UsersFileEntity usersFileEntity = new UsersFileEntity();
        usersFileEntity.setUserId(clientUser.getId());

        if (usersFileEntity.getFileName() == null) {
            String fileName = uploadImage(file, imageFolder);
            usersFileEntity.setFileName(fileName);
            userRepository.save(usersFileEntity);

            return fileName;
        }
        throw new FileCantUploadException(file.getOriginalFilename());
    }

    @Override
    public String updateUserImage(MultipartFile file, Long id) {
        log.info("updateImage to User started with, {}",
                kv("partnerId", id));

        userClient.getById(id);
        UsersFileEntity usersFileEntity = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(UsersFileEntity.class, id));

        deleteFile(usersFileEntity.getFileName(), imageFolder);
        String fileName = uploadImage(file, imageFolder);
        usersFileEntity.setFileName(fileName);
        userRepository.save(usersFileEntity);
        return fileName;
    }

    @Override
    public void deleteUserImage(Long id) {
        log.info("deleteUserImage started from User with {}",
                kv("id", id));

        userClient.getById(id);
        UsersFileEntity usersFileEntity = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(UsersFileEntity.class, id));

        if (usersFileEntity.getFileName() != null) {
            deleteFile(usersFileEntity.getFileName(), imageFolder);
            usersFileEntity.setFileName(null);
            userRepository.save(usersFileEntity);
        }

        log.info("deleteUserImage completed successfully from User with {} ", kv("id", id));

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

    @Transactional
    @Override
    public void deleteFile(String fileName, String folder) {
        log.info("deleteFile started from User with {}", kv("fileName", fileName));
        deleteFileS(fileName, folder);
        log.info("deleteFile completed successfully from User with {} ", kv("fileName", fileName));
    }

    @Transactional
    @Override
    public byte[] getFile(String fileName, String folder) {
        log.info("getFile started with {}", kv("fileName", fileName));
        return getFileS(fileName, folder);
    }

    @SneakyThrows
    public byte[] getFileS(String fileName, String folder) {
        log.info("getFile started with {}", kv("fileName", fileName));
        String objectName = folder + fileName;
        GetObjectArgs minioRequest = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        byte[] bytes = null;
        try {
            bytes = minioClient.getObject(minioRequest).readAllBytes();
        } catch (ErrorResponseException e) {
            ErrorResponse response = e.errorResponse();
            log.error("Minio error occurred with: {}, {}, {}",
                    kv("code", response.code()), kv("message",
                            response.message()),
                    kv("objectName", response.objectName()));
        }
        return bytes;
    }


    @SneakyThrows
    public void deleteFileS(String fileName, String folder) {
        log.info("deleteFile started from User with {}", kv("fileName", fileName));
        String objectName = folder + fileName;
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        log.info("deleteFile completed successfully from User with {} ", kv("fileName", fileName));
    }

    @SneakyThrows
    public String uploadImage(MultipartFile file, String folder) {
        String fileExtension = intFileUtil.getFileExtensionIfAcceptable(file, IMAGE_MEDIA_TYPE);
        String fileName = intFileUtil.generateUniqueName(fileExtension);
        String objectName = folder + fileName;

        BufferedImage image = ImageIO.read(file.getInputStream());
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > 2560 && height > 1080) {
            width = width / 3;
            height = height / 3;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(resizeImage(image, width, height),
                fileExtension,
                byteArrayOutputStream);
        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream,
                        inputStream.available(), -1)
                .contentType(file.getContentType())
                .build());
        return fileName;
    }

    private BufferedImage resizeImage(BufferedImage originalImage,
                                      int targetWidth,
                                      int targetHeight) throws IOException {

        Image resultingImage = originalImage.getScaledInstance(targetWidth,
                targetHeight,
                Image.SCALE_DEFAULT);

        BufferedImage outputImage = new BufferedImage(targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_RGB);

        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    @SneakyThrows
    public String uploadVideo(MultipartFile file, String folder) {
        String fileExtension = intFileUtil.getFileExtensionIfAcceptable(file,
                VIDEO_MEDIA_TYPE);
        String fileName = intFileUtil.generateUniqueName(fileExtension);

        String objectName = folder + fileName;

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(),
                        file.getInputStream().available(), -1)
                .contentType(file.getContentType())
                .build());
        return fileName;
    }
}
