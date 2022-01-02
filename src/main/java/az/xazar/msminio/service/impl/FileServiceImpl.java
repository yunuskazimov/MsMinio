package az.xazar.msminio.service.impl;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.entity.UsersFileEntity;
import az.xazar.msminio.model.MinioFileDto;
import az.xazar.msminio.model.clinet.FileDto;
import az.xazar.msminio.model.error.*;
import az.xazar.msminio.repository.UserFileRepository;
import az.xazar.msminio.service.FileService;
import az.xazar.msminio.util.IntFileUtil;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final UserClientRest userClient;
    private final UserFileRepository userRepository;

    private final MinioServiceImpl minioServiceImpl;
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


    public FileServiceImpl(UserClientRest userClient,
                           UserFileRepository userRepository,
                           MinioServiceImpl minioServiceImpl,
                           MinioClient minioClient,
                           IntFileUtil intFileUtil) {
        this.userClient = userClient;
        this.userRepository = userRepository;
        this.minioServiceImpl = minioServiceImpl;
        this.minioClient = minioClient;
        this.intFileUtil = intFileUtil;
    }

    @Transactional
    public FileDto uploadFileWithFileDto(MultipartFile file, Long userId, String type) {
        log.info("uploadFile to User started with, {}",
                kv("partnerId", userId));

        userClient.getById(userId);

        MinioFileDto minioFileDto = uploadMinioFileDto(file, userId);

        String fileName = minioFileDto.getFilename();
        String fileUrl = minioFileDto.getUrl();

        try {
            UsersFileEntity fileEntity =
                    saveAndGetUserFileEntity(file, userId, type, fileName, fileUrl);

            return FileDto.builder()
                    .fileName(fileEntity.getFileName())
                    .fileId(fileEntity.getId())
                    .type(fileEntity.getRequestTypeName())
                    .userId(fileEntity.getUserId())
                    .isDeleted(fileEntity.isDeleted())
                    .build();

        } catch (FileCantUploadException e) {
            throw new FileCantUploadException(file.getOriginalFilename());
        }
        //TODO Burda gelen exception u tut. USer not found
        // MAPPER ELAVE ET!!!

    }

    @Transactional
    public FileDto updateFileWithFileDto(Long id, Long userId, MultipartFile file, String type) {
        log.info("updateFile to User started with, {}",
                kv("partnerId", userId));

        userClient.getById(userId);
        UsersFileEntity entity = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity Not Found"));

        if (!entity.isDeleted()) {

            intFileUtil.getFileExtensionIfAcceptable(file, FILE_MEDIA_TYPE);
            deleteFileById(id);

            MinioFileDto minioFileDto = uploadMinioFileDto(file, userId);

            String fileName = minioFileDto.getFilename();
            String fileUrl = minioFileDto.getUrl();

            try {
                UsersFileEntity fileEntity = saveAndGetUserFileEntity(entity, file, userId, type, fileName, fileUrl);

                return FileDto.builder()
                        .fileName(fileEntity.getFileName())
                        .fileId(fileEntity.getId())
                        .type(fileEntity.getRequestTypeName())
                        .userId(fileEntity.getUserId())
                        .isDeleted(fileEntity.isDeleted())
                        .build();
            } catch (FileCantUploadException e) {
                throw new FileCantUpdateException(file.getOriginalFilename());
            }
        }
        throw new FileCantUpdateException(file.getOriginalFilename());
    }

    @Transactional
    public ResponseEntity<Object> getFile(HttpServletRequest request) {
        log.info("getFile started with {}", kv("request", request));

        String fileName = getFileName(request);
        Long userId = getUserId(fileName);

        log.info("getFile started with {}", kv("fileName", fileName + ",userId: " + userId));

        checkFileIsDeletedAndGetEntity(fileName, userId);

        try {
            log.info("getFile completed with {}", kv("fileName", fileName + ",userId: " + userId));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(IOUtils.toByteArray(minioServiceImpl.getObject(fileName)));
        } catch (IOException e) {
            log.info("getFile exception with {}", kv("fileName", fileName + ",userId: " + userId));
            throw new FileNotFoundException(e.getMessage());
        }

    }

    @Transactional
    public String getFileUrl(HttpServletRequest request) {
        log.info("getFileUrl started with {}", kv("request", request));

        String fileName = getFileName(request);
        Long userId = getUserId(fileName);

        log.info("getFileUrl started with {}", kv("fileName", fileName + ",userId: " + userId));
        UsersFileEntity fileEntity = checkFileIsDeletedAndGetEntity(fileName, userId);
        try {
            minioServiceImpl.getObject(fileName);
            log.info("getFileUrl completed with {}", kv("fileName", fileName + ",userId: " + userId));
            return fileEntity.getFileUrl();
        } catch (FileNotFoundException e) {
            log.info("getFileUrl exception with {}", kv("fileName", fileName + ",userId: " + userId));
            throw new FileNotFoundException(e.getMessage());
        }

    }

    @Transactional
    public String deleteFileById(Long id) {

        log.info("deleteFileById started from User with {}", kv("id", id));

        UsersFileEntity usersFileEntity = userRepository.findById(id)
                //  .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException(UsersFileEntity.class, id));

        String fileName = usersFileEntity.getFileName();
        Long userId = getUserId(fileName);

        log.info("deleteFileById started from User with {}", kv("userId", userId));

        userClient.getById(userId);

        if (!usersFileEntity.isDeleted()) {
            minioServiceImpl.deleteFile(usersFileEntity.getFileName());
            usersFileEntity.setDeleted(true);
            userRepository.save(usersFileEntity);
        } else {
            log.info("deleteFileById already completed. {}",
                    kv("Deleted Time", usersFileEntity.getUpdatedAt()));
            return "File Already Deleted. Delete Time: " + usersFileEntity.getUpdatedAt();
        }

        log.info("deleteFileById completed successfully from User with {} ",
                kv("userId", userId));
        return "File Deleted";
    }

    private MinioFileDto uploadMinioFileDto(MultipartFile file, Long userId) {
        log.info("uploadMinioFileDto continuous with, {}", kv("partnerId", userId));
        return minioServiceImpl.uploadFile(MinioFileDto.builder()
                .file(file)
                .build(), userId, fileFolder);
    }

    @NotNull
    private UsersFileEntity saveAndGetUserFileEntity(MultipartFile file,
                                                     Long userId,
                                                     String type,
                                                     String fileName,
                                                     String fileUrl) {
        return userRepository.save(UsersFileEntity.builder()
                .userId(userId)
                .fileUrl(fileUrl)
                .requestTypeName(type)
                .originalName(file.getOriginalFilename())
                .fileName(fileName)
                .isDeleted(false)
                .build());
    }

    @NotNull
    private UsersFileEntity saveAndGetUserFileEntity(UsersFileEntity entity,
                                                     MultipartFile file,
                                                     Long userId,
                                                     String type,
                                                     String fileName,
                                                     String fileUrl) {
        UsersFileEntity fileEntity = userRepository.save(UsersFileEntity.builder()
                .id(entity.getId())
                .userId(userId)
                .fileUrl(fileUrl)
                .originalName(file.getOriginalFilename())
                .requestTypeName(type)
                .fileName(fileName)
                .isDeleted(false)
                .build());
        return fileEntity;
    }

    private UsersFileEntity checkFileIsDeletedAndGetEntity(String fileName, Long userId) {
        return userRepository.findAllByUserIdAndFileName(userId, fileName).
                filter(entity -> !entity.isDeleted())
                .orElseThrow(() -> new FileNotFoundException(ErrorCodes.NOT_FOUND));
    }

    @NotNull
    private String getFileName(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(pattern, request.getServletPath());
    }

    @NotNull

    private Long getUserId(String fileName) {
        return Long.valueOf(fileName.split("[/]")[1].split("[i][i]")[0]);
    }


//    @Override
//    @Transactional
//    public String uploadFileForUser(MultipartFile file, Long userId, String type) {
//        log.info("uploadFileForUser started with, {}", kv("partnerId", userId));
//
//        userClient.getById(userId);
//
//        MinioFileDto minioFileDto = uploadMinioFileDto(file, userId);
//
//        String fileName = minioFileDto.getFilename();
//        String fileUrl = minioFileDto.getUrl();
//
//        try {
//            saveAndGetUserFileEntity(file, userId, type, fileName, fileUrl);
//            log.info("uploadFileForUser completed with, {}", kv("partnerId", userId));
//            return fileName;
//        } catch (FileCantUploadException e) {
//            throw new FileCantUploadException(file.getOriginalFilename());
//        }
//    }

//    @Transactional
//    public String updateFileForUser(Long id, Long userId, MultipartFile file, String type) {
//        log.info("updateFile to User started with, {}",
//                kv("partnerId", userId));
//
//        userClient.getById(userId);
//        UsersFileEntity entity = userRepository.findById(id)
//                .orElseThrow(() ->
//                        new EntityNotFoundException("Entity Not Found"));
//
//        if (!entity.isDeleted()) {
//
//            intFileUtil.getFileExtensionIfAcceptable(file, FILE_MEDIA_TYPE);
//            deleteFileById(id);
//
//            MinioFileDto minioFileDto = uploadMinioFileDto(file, userId);
//
//            String fileName = minioFileDto.getFilename();
//            String fileUrl = minioFileDto.getUrl();
//
//            try {
//                userRepository.save(UsersFileEntity.builder()
//                        .id(entity.getId())
//                        .userId(userId)
//                        .fileUrl(fileUrl)
//                        .originalName(file.getOriginalFilename())
//                        .requestTypeName(type)
//                        .fileName(fileName)
//                        .isDeleted(false)
//                        .build());
//
//                return fileName;
//            } catch (FileCantUploadException e) {
//                throw new FileCantUpdateException(file.getOriginalFilename());
//            }
//        }
//        throw new FileCantUpdateException(file.getOriginalFilename());
//    }

//    private String getFileUrl(String path) {
//        try {
//            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
//                    .bucket(bucketName)
//                    .object(path).
//                            method(Method.GET)
//                    .expiry(7, TimeUnit.DAYS).build());
//            return url;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
}

