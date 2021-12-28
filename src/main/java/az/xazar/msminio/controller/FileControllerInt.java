package az.xazar.msminio.controller;

import az.xazar.msminio.service.FileServiceInt;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/int/api/v1/")
@Slf4j
public class FileControllerInt {

    private final FileServiceInt fileService;

    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.file-folder}")
    private String fileFolder;

    public FileControllerInt(FileServiceInt fileService) {
        this.fileService = fileService;
    }


    @PostMapping("/image/{id}")
    @ApiOperation(value = "Internal: Add User image")
    public ResponseEntity<String> createImageInt(@PathVariable("id") Long id,
                                                 @Valid @RequestParam MultipartFile file,
                                                 @RequestParam String type) {
        return ResponseEntity.status(200).body(fileService.uploadImageForUser(file, id,type));
    }

    @PostMapping("/file/{id}")
    @ApiOperation(value = "Internal: Add User File")
    public ResponseEntity<String> createFileInt(@PathVariable("id") Long id,
                                                 @Valid @RequestParam MultipartFile file,
                                                 @RequestParam String type) {
        return ResponseEntity.status(200).body(fileService.uploadFileForUser(file, id,type));
    }

    @GetMapping("/image/{id}")
    @ApiOperation(value = "Internal: Get User photo")
    public byte[] getImage(@PathVariable Long id, @RequestParam String fileName) {

        return fileService.getImage(id,fileName, imageFolder);
    }

    @DeleteMapping("/image/{id}")
    @ApiOperation(value = "Internal: Delete User photo")

    public void deleteUserFile(@PathVariable Long id,
                               @RequestParam String fileName) {
        //TODO edit it. separate for file and image
        fileService.deleteUserImage(id, fileName);
    }


}
