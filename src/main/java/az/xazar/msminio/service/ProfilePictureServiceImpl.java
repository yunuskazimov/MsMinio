package az.xazar.msminio.service;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.entity.ProfilePictureEntity;
import az.xazar.msminio.entity.UsersFileEntity;
import az.xazar.msminio.model.error.EntityNotFoundException;
import az.xazar.msminio.model.error.FileCantUpdateException;
import az.xazar.msminio.model.error.FileCantUploadException;
import az.xazar.msminio.repository.ProfilePictureRepository;
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
public class ProfilePictureServiceImpl implements ProfilePictureService {
    private final UserClientRest userClient;
    private final ProfilePictureRepository pictureRepo;

    private final MinioClient minioClient;
    private final IntFileUtil intFileUtil;

    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.bucket}")
    private String bucketName;

    private final String IMAGE_MEDIA_TYPE = "image";

    @Value("${server.address}")
    String msAdress;
    @Value("${server.port}")
    String msPort;
    @Value("${server.secure}")
    String msSecure;


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
        log.info("uploadImage to Profile started with, {}",
                kv("partnerId", userId));

        String imageName = uploadImage(file, imageFolder);
        String imageUrl = getPreSignedUrl(imageName);

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

            return imageName;
        } catch (FileCantUploadException e) {
            throw new FileCantUploadException(file.getOriginalFilename());
        }
        //TODO Burda gelen exception u tut. USer not found
        // MAPPER ELAVE ET!!!

    }

    @Override
    public String updateUserImage(Long id, Long userId, MultipartFile file, String type) {
        log.info("updateImage to Profile started with, {}",
                kv("partnerId", userId));

        userClient.getById(userId);

        ProfilePictureEntity entity = pictureRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ProfilePictureEntity.class, id));

        if (!entity.isDeleted()) {
            String imageName = entity.getImageName();

            deleteFile(imageName, imageFolder);

            String newImageName = uploadImage(file, imageFolder);
            String imageUrl = getPreSignedUrl(newImageName);

            try {
                pictureRepo.save(ProfilePictureEntity.builder()
                        .userId(userId)
                        .imageName(newImageName)
                        .requestTypeName(type)
                        .imageUrl(imageUrl)
                        .originalName(file.getOriginalFilename())
                       // .isDeleted(false)
                        .build());

                return newImageName;
            } catch (FileCantUploadException e) {
                throw new FileCantUploadException(file.getOriginalFilename());
            }
        }
        throw new FileCantUpdateException(file.getOriginalFilename());
    }

    @Override
    public void deleteUserImage(Long id,String fileName) {
        log.info("deleteUserImage started from User with {}",
                kv("id", id));

//        userClient.getById(id);
//        UsersFileEntity usersFileEntity = userRepository.findAllByUserIdAndFileName(id,fileName)
//                .orElseThrow(() ->
//                        new EntityNotFoundException(UsersFileEntity.class, id));
//
//        if (usersFileEntity.getFileName() != null) {
//            deleteFile(usersFileEntity.getFileName(), imageFolder);
//            usersFileEntity.setFileName(null);
//            userRepository.save(usersFileEntity);
//        }

        log.info("deleteUserImage completed successfully from User with {} ", kv("id", id));

    }

    @Transactional
    public void deleteFile(String fileName, String folder) {
        log.info("delete Profile Image started from User with {}", kv("fileName", fileName));
        deleteFileS(fileName, folder);
        log.info("delete Profile Image completed successfully from User with {} ", kv("fileName", fileName));
    }

    @Transactional
    @Override
    public byte[] getFile(String fileName, String folder) {
        log.info("get Profile Image started with {}", kv("fileName", fileName));
        return getFileS(fileName, folder);
    }

    @SneakyThrows
    public byte[] getFileS(String fileName, String folder) {
        log.info("get Profile Image started with {}", kv("fileName", fileName));
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
        log.info("delete Profile Image started from User with {}", kv("fileName", fileName));
        String objectName = folder + fileName;
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        log.info("delete Profile Image completed successfully from User with {} ", kv("fileName", fileName));
    }

    @SneakyThrows
    public String uploadImage(MultipartFile file, String folder) {
        String fileExtension = intFileUtil.getFileExtensionIfAcceptable(file, IMAGE_MEDIA_TYPE);
        String imageName = intFileUtil.generateUniqueNameForImage(fileExtension);
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

    private String getPreSignedUrl(String filename) {
        return msSecure + "://" + msAdress + ":" + msPort + "/file/".concat(filename);
    }

}
