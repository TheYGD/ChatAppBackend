package pl.szmidla.chatappbackend.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.service.UserService;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserController {

    public static int USERS_PAGE_SIZE = 10;
    private UserService userService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerUser(@RequestBody @Valid UserRequest userRequest) {
        return userService.registerUser(userRequest);
    }

    @GetMapping(value = "/users-by-phrase", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<User> getNUsersByPhrase(@RequestParam String phrase,
                                        @RequestParam(defaultValue = "0") int pageNr) {
        return userService.getNUsersByPhrase(phrase, pageNr, USERS_PAGE_SIZE);
    }

    @GetMapping(value = "/register/username-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean usernameExists(@RequestParam String username) {
        return userService.usernameExists(username);
    }

    @GetMapping(value = "/register/email-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean emailExists(@RequestParam String email) {
        return userService.emailExists(email);
    }
}