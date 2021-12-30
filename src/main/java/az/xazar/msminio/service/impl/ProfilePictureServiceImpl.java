package az.xazar.msminio.service.impl;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.entity.ProfilePictureEntity;
import az.xazar.msminio.model.error.EntityNotFoundException;
import az.xazar.msminio.model.error.FileCantUpdateException;
import az.xazar.msminio.model.error.FileCantUploadException;
import az.xazar.msminio.repository.ProfilePictureRepository;
import az.xazar.msminio.service.ProfilePictureService;
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
import java.io.InputStream;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
public class ProfilePictureServiceImpl implements ProfilePictureService {

    private final UserClientRest userClient;
    private final ProfilePictureRepository pictureRepo;
    private final MinioClient minioClient;
    private final IntFileUtil intFileUtil;

    private final String IMAGE_MEDIA_TYPE = "image";
    @Value("${server.address}")
    String msAddress;
    @Value("${server.port}")
    String msPort;
    @Value("${server.secure}")
    String msSecure;
    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.bucket}")
    private String bucketName;

    public ProfilePictureServiceImpl(UserClientRest userClient,
                                     ProfilePictureRepository pictureRepo,
                                     MinioClient minioClient,
                                     IntFileUtil intFileUtil) {
        this.userClient = userClient;
        this.pictureRepo = pictureRepo;
        this.minioClient = minioClient;
        this.intFileUtil = intFileUtil;
    }

    @Override
    @Transactional
    public String uploadImageForProfile(MultipartFile file, Long userId, String type) {
        log.info("uploadImage to Profile started with, {}", kv("partnerId", userId));

        var ref = new Object() {
            String img = null;
        };

        pictureRepo.findByUserId(userId).ifPresentOrElse(entity -> {

            log.info("uploadImage to Profile is Present and forwarded to Update Image " +
                            "with, {}",
                    kv("partnerId", userId));

            ref.img = updateImageForProfile(entity.getId(), userId, file, type);

            log.info("uploadImage to Profile was Presented and completed with Update Image " +
                    "with, {}", kv("partnerId", userId));

        }, () -> {

            log.info("uploadImage to continuous with, {}", kv("partnerId", userId));

            String imageName = uploadImage(userId, file, imageFolder);
            String imageUrl = getPreSignedUrl(imageName);
            ref.img = imageName;
            userClient.getById(userId);

            try {
                pictureRepo.save(ProfilePictureEntity.builder()
                        .userId(userId)
                        .imageName(imageName)
                        .requestTypeName(type)
                        .imageUrl(imageUrl)
                        .originalName(file.getOriginalFilename())
                        .isDeleted(false)
                        .build());

                log.info("uploadImage to Profile completed with, {}, {}",
                        kv("partnerId", userId), kv("ImageN Name: ", imageName));

            } catch (FileCantUploadException e) {
                throw new FileCantUploadException(file.getOriginalFilename());
            }

        });

        return ref.img;

        //TODO Burda gelen exception u tut. USer not found
        // MAPPER ELAVE ET!!!
    }

    @Override
    @Transactional
    public String updateImageForProfile(Long id, Long userId, MultipartFile file, String type) {
        log.info("updateImage to Profile started with, {}", kv("partnerId", userId));

        userClient.getById(userId);

        pictureRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ProfilePictureEntity.class, id));


            intFileUtil.getFileExtensionIfAcceptable(file, IMAGE_MEDIA_TYPE);
            deleteUserImage(id);

            String newImageName = uploadImage(userId, file, imageFolder);
            String imageUrl = getPreSignedUrl(newImageName);

            try {
                pictureRepo.save(ProfilePictureEntity.builder()
                        .id(id)
                        .userId(userId)
                        .imageName(newImageName)
                        .requestTypeName(type)
                        .imageUrl(imageUrl)
                        .originalName(file.getOriginalFilename())
                        .isDeleted(false)
                        .build());

                log.info("updateImage to Profile completed with, {}, {}",
                        kv("partnerId", userId), kv("fileName", newImageName));

                return newImageName;
            } catch (FileCantUploadException e) {
                throw new FileCantUploadException(file.getOriginalFilename());
            }
    }

    @Override
    @Transactional
    public String deleteUserImage(Long id) {

        log.info("deleteUserImage started from User with {}", kv("id", id));

        ProfilePictureEntity entity = pictureRepo.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(ProfilePictureEntity.class, id));

        userClient.getById(entity.getUserId());

        if (!entity.isDeleted()) {
            deleteFile(entity.getImageName(), imageFolder);
            entity.setDeleted(true);
            pictureRepo.save(entity);
        } else {
            log.info("deleteImage already completed. {}",
                    kv("Deleted Time", entity.getUpdatedAt()));
            return "Profile Picture Already Deleted. Delete Time: " + entity.getUpdatedAt();
        }

        log.info("deleteUserImage completed successfully from User with {} ", kv("id", id));

        return "Profile Picture Deleted";

    }

    @SneakyThrows
    public void deleteFile(String fileName, String folder) {
        log.info("delete Profile Image started from User with {}", kv("fileName", fileName));
        String objectName = folder + fileName;
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        log.info("delete Profile Image completed successfully from User " +
                "with {} ", kv("fileName", fileName));
    }

    @Transactional
    @Override
    public byte[] getFile(String fileName, String folder) {
        log.info("get Profile Image started with {}", kv("fileName", fileName));
        return getFileS(fileName, folder);
    }

    @SneakyThrows
    private byte[] getFileS(String fileName, String folder) {
        log.info("get Profile Image continuous with {}", kv("fileName", fileName));
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
                    kv("code", response.code()), kv("message", response.message()),
                    kv("objectName", response.objectName()));
        }
        log.info("get Profile Image completed with {}", kv("fileName", fileName));

        return bytes;
    }

    @SneakyThrows
    private String uploadImage(Long userid, MultipartFile file, String folder) {
        String fileExtension = intFileUtil.getFileExtensionIfAcceptable(file, IMAGE_MEDIA_TYPE);
        String imageName = intFileUtil.generateUniqueNameForImage(userid, fileExtension);
        String objectName = folder + imageName;

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
        return imageName;
    }

    private BufferedImage resizeImage(BufferedImage originalImage,
                                      int targetWidth,
                                      int targetHeight) {

        Image resultingImage = originalImage.getScaledInstance(targetWidth,
                targetHeight,
                Image.SCALE_DEFAULT);

        BufferedImage outputImage = new BufferedImage(targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_RGB);

        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    private String getPreSignedUrl(String filename) {
        return msSecure + "://" + msAddress + ":" + msPort + "/file/".concat(filename);
    }

}