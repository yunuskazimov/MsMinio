package az.xazar.msminio.controller;

import az.xazar.msminio.model.client.FileDto;
import az.xazar.msminio.service.impl.FileServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = "/int/api/minio")
public class MinioFileController {

    private final FileServiceImpl fileService;

    public MinioFileController(FileServiceImpl fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/file", consumes = {"multipart/form-data"})
    @ApiOperation(value = "Internal: Add User File form Business Trip with FileDto")
    public ResponseEntity<FileDto> createFileWithFileDto(@ModelAttribute FileDto file) {

        log.info("create FILE DTO" + file);

        return ResponseEntity.status(200).body(
                fileService.uploadFileWithFileDto
                        (file.getFile(), file.getUserId(), file.getType()));
    }

    @PutMapping(value = "/file", consumes = {"multipart/form-data"})
    @ApiOperation(value = "Internal: Edit User File form Business Trip with FileDto")
    public ResponseEntity<FileDto> editFileWithFileDto(@ModelAttribute FileDto file) {

        log.info("edit FILE DTO" + file);

        return ResponseEntity.status(200).body(
                fileService.updateFileWithFileDto
                        (file.getFileId(), file.getUserId(), file.getFile(), file.getType()));
    }

    @GetMapping(value = "/file/url/**")
    @ApiOperation(value = "Internal: Get User File Url by File Name")
    public String getFileUrl(HttpServletRequest request) {
        return fileService.getFileUrl(request);
    }

    @GetMapping(value = "/file/**")
    @ApiOperation(value = "Internal: Get User File by File Name")
    public ResponseEntity<Object> getFile(HttpServletRequest request) {

        return fileService.getFile(request);
    }


    @DeleteMapping("/file/{id}")
    @ApiOperation(value = "Internal: Delete User Files")
    public ResponseEntity<Object> DeleteFile(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.deleteFileById(id));
    }

    //    @PostMapping("/file/{id}")
//    @ApiOperation(value = "Internal: Add User File")
//    public ResponseEntity<String> createFileInt(@PathVariable("id") Long userId,
//                                                @Valid @RequestParam MultipartFile file,
//                                                @RequestParam String type) {
//        return ResponseEntity.status(200).body(fileService.uploadFileForUser(file, userId, type));
//    }

    //    @GetMapping("/file")
//    @ApiOperation(value = "Internal: Get User Files")
//    public ResponseEntity<Object> getAllFilesAndImagesAsList() {
//        return ResponseEntity.ok(minioService.getListObjects());
//    }

//    @PutMapping("/file/{id}")
//    @ApiOperation(value = "Internal: Edit User File")
//    public ResponseEntity<String> editFileInt(@PathVariable("id") Long userId,
//                                              @RequestParam Long id,
//                                              @Valid @RequestParam MultipartFile file,
//                                              @RequestParam String type) {
//        return ResponseEntity.status(200).body(
//                fileService.updateFileForUser(id, userId, file, type));
//    }


}
