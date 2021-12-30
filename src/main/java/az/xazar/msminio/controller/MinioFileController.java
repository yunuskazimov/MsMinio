package az.xazar.msminio.controller;

import az.xazar.msminio.service.impl.MinioService;
import az.xazar.msminio.service.impl.FileServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = "/int/api/")
public class MinioFileController {

    private final MinioService minioService;
    private final FileServiceImpl fileService;


    public MinioFileController(MinioService minioService, FileServiceImpl fileService) {
        this.minioService = minioService;
        this.fileService = fileService;
    }


    @PostMapping("/file/{id}")
    @ApiOperation(value = "Internal: Add User File")
    public ResponseEntity<String> createFileInt(@PathVariable("id") Long userId,
                                                @Valid @RequestParam MultipartFile file,
                                                @RequestParam String type) {
        return ResponseEntity.status(200).body(fileService.uploadFileForUser(file, userId, type));
    }

    @PutMapping("/file/{id}")
    @ApiOperation(value = "Internal: Edit User File")
    public ResponseEntity<String> editFileInt(@PathVariable("id") Long userId,
                                              @RequestParam Long id,
                                              @Valid @RequestParam MultipartFile file,
                                              @RequestParam String type) {
        return ResponseEntity.status(200).body(fileService.updateFileForUser(id, userId, file, type));
    }


    @GetMapping(value = "/file/**")
    @ApiOperation(value = "Internal: Get User File by File Name")
    private ResponseEntity<Object> getFile(HttpServletRequest request) throws IOException {

        return fileService.getFile(request);
    }

//    @GetMapping("/file")
//    @ApiOperation(value = "Internal: Get User Files")
//    public ResponseEntity<Object> getFiles() {
//        return ResponseEntity.ok(minioService.getListObjects());
//    }

    @DeleteMapping("/file/{id}")
    @ApiOperation(value = "Internal: Delete User Files")
    public ResponseEntity<Object> DeleteFile(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.deleteFileById(id));
    }


}
