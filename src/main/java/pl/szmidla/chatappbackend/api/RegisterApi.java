package pl.szmidla.chatappbackend.api;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.service.RegisterService;
import pl.szmidla.chatappbackend.service.UserService;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api/register")
public class RegisterApi {

    private RegisterService registerService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerUser(@RequestBody @Valid UserRequest user) {
        return registerService.registerUser(user);
    }

    @GetMapping(value = "/username-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean usernameExists(@RequestParam String username) {
        return registerService.usernameExists(username);
    }

    @GetMapping(value = "/email-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean emailExists(@RequestParam String email) {
        return registerService.emailExists(email);
    }

}
