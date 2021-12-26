package az.xazar.msminio.controller;

import az.xazar.msminio.model.clinet.UserDto;
import az.xazar.msminio.service.FileService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/file")
@Slf4j
public class FileController {

    private final FileService fileService;

    @Value("${minio.image-folder}")
    private String imageFolder;
    @Value("${minio.file-folder}")
    private String fileFolder;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @GetMapping("/{id}")
    @ApiOperation(value = "Get User by Id")
    public ResponseEntity<UserDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.status(200).body(fileService.findById(id));
    }

//    @PostMapping
//    @ApiOperation("Add User")
//    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userRequestDto){
//        return ResponseEntity.status(201).body(fileService.create(userRequestDto));
//    }

//    @PutMapping("/{id}")
//    @ApiOperation("Update User")
//    public ResponseEntity<UserDto> update( @PathVariable("id") Long id,
//                                                   @Valid @RequestBody UserDto userRequestDto){
//        return ResponseEntity.status(200).body(fileService.up(userRequestDto, id));
//    }

//    @DeleteMapping("/{id}")
//    @ApiOperation(value = "Delete User")
//    public ResponseEntity<UserResponseDto> delete(@PathVariable("id") Long id){
//        return ResponseEntity.status(200).body(fileService.delete(id));
//    }

    @PostMapping("/image/{id}")
    @ApiOperation(value = "Add User File")
    public ResponseEntity<String> createImage(@PathVariable("id") Long id,
                                              @Valid @RequestParam MultipartFile file,
                                              String requestType) {
        return ResponseEntity.status(200).body(fileService.uploadImageForUser(file, id, requestType));
    }

    @PutMapping("/image/{id}")
    @ApiOperation(value = "Update User File")
    public ResponseEntity<String> updateImage(@PathVariable("id") Long id,
                                              @Valid @RequestParam MultipartFile multipartFile) {
        return ResponseEntity.status(200).body(fileService.updateUserImage(multipartFile, id));
    }

    @GetMapping("/image/{fileName}")
    @ApiOperation(value = "Get User photo")
    public byte[] getImage(@PathVariable("fileName") String fileName) {
        return fileService.getFile(fileName, imageFolder);
    }

    @DeleteMapping("/image/{id}")
    public void deleteUserFile(@PathVariable Long id,
                               @RequestParam String fileName) {
        fileService.deleteUserImage(id, fileName);
    }

}
