package az.xazar.msminio.controller;

import az.xazar.msminio.service.ProfilePictureService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/")
@Slf4j
public class ProfilePictureController {

    private final ProfilePictureService pictureService;

    @Value("${minio.image-folder}")
    private String imageFolder;

    public ProfilePictureController(ProfilePictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/image/{id}")
    @ApiOperation(value = "Add User Profile Picture")
    public ResponseEntity<String> createImage(@PathVariable("id") Long userId,
                                              @Valid @RequestParam MultipartFile file,
                                              String type) {
        return ResponseEntity.status(200).body(
                pictureService.uploadImageForProfile(file, userId, type));
    }

    @PutMapping("/image/{id}")
    @ApiOperation(value = "Update User Profile Picture")
    public ResponseEntity<String> updateImage(@PathVariable("id") Long userId,
                                              @RequestParam Long id,
                                              @Valid @RequestParam MultipartFile file,
                                              @RequestParam String type) {
        return ResponseEntity.status(200).body(
                pictureService.updateImageForProfile(id, userId, file, type));
    }

    @GetMapping(value = "/image/**")
    @ApiOperation(value = "Get Profile Picture User by File Name")
    private ResponseEntity<Object> getImage(HttpServletRequest request) {
        return pictureService.getFile(request);
    }

//    @GetMapping("/image/{fileName}")
//    @ApiOperation(value = "Get User Profile Picture")
//    public byte[] getImage(@PathVariable("fileName") String fileName) {
//        return profilePictureService.getFile(fileName, imageFolder);
//    }

    @DeleteMapping("/image/{id}")
    @ApiOperation(value = "Delete User Profile Picture By Image Id")
    public void deleteUserFile(@PathVariable Long id) {
        pictureService.deleteUserImage(id);
    }

}
