package az.xazar.msminio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

//@RestController
//@RequestMapping("/int/api/v1/")
@Slf4j
public class FileControllerTEST {

    @Value("${minio.image-folder}")
    private String imageFolder;


//    public FileControllerTEST(FileServiceInt fileService) {
//        this.fileService = fileService;
//    }
//
//
//    @PostMapping("/image/{id}")
//    @ApiOperation(value = "Internal: Add User image")
//    public ResponseEntity<String> createImageInt(@PathVariable("id") Long id,
//                                                 @Valid @RequestParam MultipartFile file,
//                                                 @RequestParam String type) {
//        return ResponseEntity.status(200).body(fileService.uploadImageForUser(file, id, type));
//    }
//
//    @PostMapping("/file/{id}")
//    @ApiOperation(value = "Internal: Add User File")
//    public ResponseEntity<String> createFileInt(@PathVariable("id") Long id,
//                                                @Valid @RequestParam MultipartFile file,
//                                                @RequestParam String type) {
//        return ResponseEntity.status(200).body(fileService.uploadFileForUser(file, id, type));
//    }
//
//    @GetMapping("/image/{id}")
//    @ApiOperation(value = "Internal: Get User photo")
//    public byte[] getImage(@PathVariable Long id, @RequestParam String fileName) {
//
//        return fileService.getImage(id, fileName, imageFolder);
//    }
//
//    @DeleteMapping("/image/{id}")
//    @ApiOperation(value = "Internal: Delete User photo")
//
//    public void deleteUserFile(@PathVariable Long id,
//                               @RequestParam String fileName) {
//        //TODO edit it. separate for file and image
//        fileService.deleteUserImage(id, fileName);
//    }


//    @PostMapping(path = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
//    public String uploadFile(@RequestParam("image") MultipartFile file) {
//
//        try {
//
//
//            byte[] bytes = file.getBytes();
//            Path path = Paths.get("C:\\Users\\yunus\\Downloads\\testtt\\uploadedImages\\" + file.getOriginalFilename());
//            Files.write(path, bytes);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error";
//        }
//
//        return "File Uploaded";
//    }


}
