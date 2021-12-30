package az.xazar.msminio.controller;

import az.xazar.msminio.service.ProfilePictureService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/")
@Slf4j
public class ProfilePictureController {

    private final ProfilePictureService profilePictureService;

    @Value("${minio.image-folder}")
    private String imageFolder;


    public ProfilePictureController(ProfilePictureService profilePictureService) {
        this.profilePictureService = profilePictureService;
    }


    @PostMapping("/image/{id}")
    @ApiOperation(value = "Add User Image")
    public ResponseEntity<String> createImage(@PathVariable("id") Long id,
                                              @Valid @RequestParam MultipartFile file,
                                              String requestType) {
        return ResponseEntity.status(200).body(profilePictureService.uploadImageForProfile(file, id, requestType));
    }

    @PutMapping("/image/{id}")
    @ApiOperation(value = "Update User Image")
    public ResponseEntity<String> updateImage(@PathVariable("id") Long userId,
                                              @RequestParam Long id,
                                              @Valid @RequestParam MultipartFile multipartFile,
                                              @RequestParam String type) {
        return ResponseEntity.status(200).body(
                profilePictureService.updateUserImage(id,userId,multipartFile,type));
    }

    @GetMapping("/image/{fileName}")
    @ApiOperation(value = "Get User photo")
    public byte[] getImage(@PathVariable("fileName") String fileName) {
        return profilePictureService.getFile(fileName, imageFolder);
    }

    @DeleteMapping("/image/{id}")
    public void deleteUserFile(@PathVariable Long id,
                               @RequestParam String fileName) {
        profilePictureService.deleteUserImage(id, fileName);
    }

}
