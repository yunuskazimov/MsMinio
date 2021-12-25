package az.xazar.msminio.controller;

import az.xazar.msminio.clinet.UserClientRest;
import az.xazar.msminio.model.clinet.UserDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/test")
public class TestController {
    private final UserClientRest client;

    public TestController(UserClientRest client) {
        this.client = client;
    }


//    @GetMapping("/id/{id}")
//    public UserDto getById(@PathVariable Long id){
//        return client.getById(id);
//    }
}
