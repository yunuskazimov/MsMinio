package az.xazar.msminio.service;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.entity.UsersFileEntity;
import az.xazar.msminio.model.clinet.UserDto;
import az.xazar.msminio.model.error.EntityNotFoundException;
import az.xazar.msminio.model.error.FileCantUploadException;
import az.xazar.msminio.repository.UserFileRepository;
import az.xazar.msminio.util.IntFileUtil;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
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
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
public class FileServiceIntImpl implements FileServiceInt {
    private final UserClientRest userClient;
    private final UserFileRepository userRepository;
    private final MinioClient minioClient;
    private final IntFileUtil intFileUtil;
    private final String FILE_MEDIA_TYPE = "file";
    private final String IMAGE_MEDIA_TYPE = "image";
    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.file-folder}")
    private String fileFolder;
    @Value("${minio.bucket}")
    private String bucketName;


    public FileServiceIntImpl(UserClientRest userClient,
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
    public String uploadImageForUser(MultipartFile file, Long id, String type) {
        log.info("uploadImage to User started with, {}",
                kv("partnerId", id));

        String fileName = uploadImage(file, imageFolder, id);
        userClient.getById(id);

        try {
            userRepository.save(UsersFileEntity.builder()
                    .userId(id)
                    .requestTypeName(type)
                    .fileName(fileName)
                    .build());

            return fileName;
        } catch (FileCantUploadException e) {
            throw new FileCantUploadException(file.getOriginalFilename());
        }
        //TODO Burda gelen exception u tut. USer not found
        // MAPPER ELAVE ET!!!

    }

    @Override
    @Transactional
    public String uploadFileForUser(MultipartFile file, Long id, String type) {
        log.info("uploadImage to User started with, {}",
                kv("partnerId", id));

        String fileName = uploadFile(file, fileFolder, id);
        userClient.getById(id);

        try {
            userRepository.save(UsersFileEntity.builder()
                    .userId(id)
                    .requestTypeName(type)
                    .fileName(fileName)
                    .build());

            return fileName;
        } catch (FileCantUploadException e) {
            throw new FileCantUploadException(file.getOriginalFilename());
        }
        //TODO Burda gelen exception u tut. USer not found
        // MAPPER ELAVE ET!!!

    }

    @Transactional
    @Override
    public void deleteUserImage(Long id, String fileName) {
        log.info("deleteUserImage started from User with {}",
                kv("id", id));

        userClient.getById(id);
        UsersFileEntity usersFileEntity = userRepository.findAllByUserIdAndFileName(id, fileName)
                .orElseThrow(() ->
                        new EntityNotFoundException(UsersFileEntity.class, id));

        if (usersFileEntity.getFileName() != null) {
            deleteImage(usersFileEntity.getFileName(), imageFolder);
            usersFileEntity.setFileName(null);
            userRepository.save(usersFileEntity);
        }

        log.info("deleteUserImage completed successfully from User with {} ", kv("id", id));

    }

    private void deleteImage(String fileName, String folder) {

        log.info("deleteFile started from User with {}", kv("fileName", fileName));
        deleteFileByFolder(fileName, folder);
        log.info("deleteFile completed successfully from User with {} ", kv("fileName", fileName));

    }

    @SneakyThrows
    private void deleteFileByFolder(String fileName, String folder) {

        log.info("deleteFile started from User with {}", kv("fileName", fileName));
        String objectName = folder + fileName;
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        log.info("deleteFile completed successfully from User with {} ", kv("fileName", fileName));

    }

    @Transactional
    @Override
    public byte[] getImage(Long id, String fileName, String folder) {

        log.info("getFile started with {}", kv("fileName", fileName + ",userId: " + id));
        return getImageFiles(id, fileName, folder);

    }

    @SneakyThrows
    private byte[] getImageFiles(Long id, String fileName, String folder) {

        userRepository.findAllByUserIdAndFileName(id, fileName);

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
        log.info("getFile completed successfully with {} ", kv("fileName", fileName));
        return bytes;

    }


    @SneakyThrows
    private String uploadImage(MultipartFile file, String folder, Long id) {
        String fileExtension = intFileUtil.getFileExtensionIfAcceptable(file, IMAGE_MEDIA_TYPE);
        String fileName = intFileUtil.generateUniqueName(id, fileExtension);
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
    private String uploadFile(MultipartFile multipartFile, String folder, Long id) {
        String fileExtension = intFileUtil.getFileExtensionIfAcceptable(multipartFile, FILE_MEDIA_TYPE);
        String fileName = intFileUtil.generateUniqueName(id, fileExtension);
        String objectName = folder + fileName;

        try {
//            int idx = Objects.requireNonNull(multipartFile.getOriginalFilename()).lastIndexOf(".");
//            String suffix = multipartFile.getOriginalFilename().substring(idx + 1);
//            String fileNameUUID = UUID.randomUUID() + "." + suffix;

            // Save file
            minioClient.putObject(PutObjectArgs.builder()
                    .stream(multipartFile.getInputStream(), multipartFile.getSize(), PutObjectArgs.MIN_MULTIPART_SIZE)
                    .object(objectName)
                    .contentType(multipartFile.getContentType())
                    .bucket(bucketName)
                    .build());
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }


    private String getFileUrl(String path) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(path).
                            method(Method.GET)
                    .expiry(7, TimeUnit.DAYS).build());
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

