package az.xazar.msminio.service;

import az.xazar.msminio.model.MinioFileDto;
import az.xazar.msminio.util.IntFileUtil;
import io.minio.*;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class MinioService {
    private final IntFileUtil intFileUtil;
    private final MinioClient minioClient;

    private final String FILE_MEDIA_TYPE = "file";

    @Value("${server.address}")
    String msAdress;
    @Value("${server.port}")
    String msPort;
    @Value("${server.secure}")
    String msSecure;
    @Value("${minio.bucket}")
    private String bucketName;


    public MinioService(IntFileUtil intFileUtil, MinioClient minioClient) {
        this.intFileUtil = intFileUtil;
        this.minioClient = minioClient;
    }

    public MinioFileDto uploadFile(MinioFileDto request, Long userId, String folder) {
        intFileUtil.getFileExtensionIfAcceptable(request.getFile(), FILE_MEDIA_TYPE);
        String fileName = intFileUtil.generateUniqueNameForFile(userId);
        String objectName = folder + fileName + request.getFile().getOriginalFilename();
        try {

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(request.getFile().getInputStream(), request.getFile().getSize(), -1)
                    .build());

        } catch (Exception e) {
            log.error("Happened error when upload file: ", e);
        }
        return MinioFileDto.builder()
                //   .title(request.getTitle())
                //  .description(request.getDescription())
                .size(request.getFile().getSize())
                .url(getPreSignedUrl(objectName))
                .filename(objectName)
                .build();
    }

    public InputStream getObject(String filename) {
        InputStream stream;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
            return null;
        }

        return stream;
    }

    public List<MinioFileDto> getListObjects() {
        List<MinioFileDto> objects = new ArrayList<>();
        try {
            Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
            for (Result<Item> item : result) {
                objects.add(MinioFileDto.builder()
                        .filename(item.get().objectName())
                        .size(item.get().size())
                        .url(getPreSignedUrl(item.get().objectName()))
                        .build());
            }
            return objects;
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
        }

        return objects;
    }

    private String getPreSignedUrl(String filename) {
        return msSecure + "://" + msAdress + ":" + msPort + "/file/".concat(filename);
    }

    @SneakyThrows
    public void deleteFile(String fileName) {
        log.info("deleteFile started from User with {}", kv("fileName", fileName));
        String objectName = fileName;
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        log.info("deleteFile completed successfully from User with {} ", kv("fileName", fileName));
    }
}
