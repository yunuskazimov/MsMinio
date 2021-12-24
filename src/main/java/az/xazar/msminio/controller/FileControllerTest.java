package az.xazar.msminio.controller;

import az.xazar.msminio.model.clinet.UserDto;
import az.xazar.msminio.service.FileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/filetest")
public class FileControllerTest {
    private final FileService service;

    public FileControllerTest(FileService service) {
        this.service = service;
    }


    @GetMapping("/id/{id}")
    @ApiOperation(value = "Get User by Id")
    public UserDto getUser(@PathVariable Long id){
        return service.findById(id);
    }
}
