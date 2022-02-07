package az.xazar.msminio.model.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private Long id;
    private Long fileId;
    private String fileName;
    private Long userId;
    private String type;
    private boolean isDeleted;

    @SuppressWarnings("java:S1948")
    private MultipartFile file;

}
