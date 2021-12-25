package az.xazar.msminio.clinet;


import az.xazar.msminio.model.clinet.UserDto;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


//@EnableFeignClients
//@FeignClient(value = "msUser", url = "http://localhost:8051")
public interface UserClientF {
    @RequestMapping(method = RequestMethod.GET,
            value = "/int/users/{id}"
            , produces = "application/json")
    UserDto getyId(@PathVariable("id") Long id);
}
