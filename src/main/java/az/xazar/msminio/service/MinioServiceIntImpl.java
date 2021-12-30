package az.xazar.msminio.service;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.entity.UsersFileEntity;
import az.xazar.msminio.model.MinioFileDto;
import az.xazar.msminio.model.error.*;
import az.xazar.msminio.repository.UserFileRepository;
import az.xazar.msminio.util.IntFileUtil;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
public class MinioServiceIntImpl implements MinioServiceInt {
    private final UserClientRest userClient;
    private final UserFileRepository userRepository;

    private final MinioService minioService;
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


    public MinioServiceIntImpl(UserClientRest userClient,
                               UserFileRepository userRepository,
                               MinioService minioService,
                               MinioClient minioClient,
                               IntFileUtil intFileUtil) {
        this.userClient = userClient;
        this.userRepository = userRepository;
        this.minioService = minioService;
        this.minioClient = minioClient;
        this.intFileUtil = intFileUtil;
    }


    @Override
    @Transactional
    public String uploadFileForUser(MultipartFile file, Long userId, String type) {
        log.info("uploadFile to User started with, {}",
                kv("partnerId", userId));

        userClient.getById(userId);

        MinioFileDto minioFileDto = minioService.uploadFile(MinioFileDto.builder()
                .file(file)
                .build(), userId, fileFolder);

        String fileName = minioFileDto.getFilename();
        String fileUrl = minioFileDto.getUrl();

        try {
            userRepository.save(UsersFileEntity.builder()
                    .userId(userId)
                    .fileUrl(fileUrl)
                    .requestTypeName(type)
                    .originalName(file.getOriginalFilename())
                    .fileName(fileName)
                    .isDeleted(false)
                    .build());

            return fileName;
        } catch (FileCantUploadException e) {
            throw new FileCantUploadException(file.getOriginalFilename());
        }
        //TODO Burda gelen exception u tut. USer not found
        // MAPPER ELAVE ET!!!

    }

    @Transactional
    public String updateFileForUser(Long id, Long userId, MultipartFile file, String type) {
        log.info("updateFile to User started with, {}",
                kv("partnerId", userId));

        userClient.getById(userId);
        UsersFileEntity entity = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity Not Found"));

        if (!entity.isDeleted()) {

            deleteFileById(id);

            MinioFileDto minioFileDto = minioService.uploadFile(MinioFileDto.builder()
                    .file(file)
                    .build(), userId, fileFolder);

            String fileName = minioFileDto.getFilename();
            String fileUrl = minioFileDto.getUrl();

            try {

                userRepository.save(UsersFileEntity.builder()
                        .id(entity.getId())
                        .userId(userId)
                        .fileUrl(fileUrl)
                        .originalName(file.getOriginalFilename())
                        .requestTypeName(type)
                        .fileName(fileName)
                        //  .isDeleted(false)
                        .build());

                return fileName;
            } catch (FileCantUploadException e) {
                throw new FileCantUpdateException(file.getOriginalFilename());
            }
        }
        throw new FileCantUpdateException(file.getOriginalFilename());
    }

    @Transactional
    public ResponseEntity<Object> getFile(HttpServletRequest request) throws IOException {
        log.info("getFile started with {}", kv("request", request));

        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        String fileName = new AntPathMatcher().extractPathWithinPattern(pattern,
                request.getServletPath());
        Long id = Long.valueOf(fileName.split("[/]")[1].split("[i][i]")[0]);

        log.info("getFile started with {}", kv("fileName", fileName + ",userId: " + id));
        userRepository.findAllByUserIdAndFileName(id, fileName).
                filter(entity -> !entity.isDeleted())
                .orElseThrow(() -> new FileNotFoundException(ErrorCodes.NOT_FOUND));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(IOUtils.toByteArray(minioService.getObject(fileName)));

    }


    @Transactional
    public String deleteFileById(Long id) {

        log.info("deleteFile started from User with {}", kv("id", id));

        UsersFileEntity usersFileEntity = userRepository.findById(id)
              //  .filter(e -> !e.isDeleted())
                .orElseThrow(() ->
                        new EntityNotFoundException(UsersFileEntity.class, id));

        String fileName = usersFileEntity.getFileName();

        Long userId = Long.valueOf(fileName.split("[/]")[1].split("[i][i]")[0]);

        log.info("deleteFile started from User with {}", kv("userId", userId));

        userClient.getById(userId);

        if (!usersFileEntity.isDeleted()) {
            minioService.deleteFile(usersFileEntity.getFileName());
            usersFileEntity.setDeleted(true);
            userRepository.save(usersFileEntity);
        } else {
            log.info("deleteFile already completed. {}", kv("Deleted Time", usersFileEntity.getUpdatedAt()));
            return "File Already Deleted. Delete Time: " + usersFileEntity.getUpdatedAt();
        }

        log.info("deleteFile completed successfully from User with {} ",
                kv("userId", userId));
        return "File Deleted";
    }


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

