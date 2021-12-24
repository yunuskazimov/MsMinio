package az.xazar.msminio.clinet;

import az.xazar.msminio.model.clinet.UserDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@EnableFeignClients
//@Configuration
//@FeignClient(value = "msUser", url = "http://localhost:8051")
public interface UserClientF {
    @RequestMapping(method = RequestMethod.GET, value = "/int/users/{id}"
            , produces = "application/json")
    UserDto getPostById(@PathVariable("id") Long id);
}
