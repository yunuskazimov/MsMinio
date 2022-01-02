package az.xazar.msminio.util;


import az.xazar.msminio.model.error.ExtensionNotAcceptableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class IntFileUtil {


    @Value("${file.upload.acceptableFileExtensions}")
    private String[] acceptableFileExtensions;
    @Value("${file.upload.acceptableImageExtensions}")
    private String[] acceptableImageExtensions;

    private boolean isExtensionAcceptable(String extension, String mediaType) {
        if (mediaType.equals("image")) {
            for (String s : acceptableImageExtensions) {
                if (s.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        } else if (mediaType.equals("file")) {
            for (String s : acceptableFileExtensions) {
                if (s.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getFileExtensionIfAcceptable(@NotNull MultipartFile file, String mediaType) {
        String extension = file.getContentType().split("[/]")[1];
        if (isExtensionAcceptable(extension, mediaType)) {
            return extension;
        } else {
            throw new ExtensionNotAcceptableException(extension);
        }
    }

    public String generateUniqueNameForFile(Long userId) {
        return userId + "ii" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-";
    }

    public String generateUniqueName(Long userId, String extension) {
        return userId + "ii" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "." + extension;
    }

    public String generateUniqueNameForImage(Long userId, String extension) {
        return userId + "ii" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "." + extension;
    }
}
