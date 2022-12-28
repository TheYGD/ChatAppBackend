package pl.szmidla.chatappbackend.api;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.service.UserService;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api/register")
public class RegisterApi {

    private UserService userService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerUser(@RequestBody @Valid UserRequest user) {
        return userService.registerUser(user);
    }

    @GetMapping(value = "/username-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean usernameExists(@RequestParam String username) {
        return userService.usernameExists(username);
    }

    @GetMapping(value = "/email-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean emailExists(@RequestParam String email) {
        return userService.emailExists(email);
    }

}
