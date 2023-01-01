package pl.szmidla.chatappbackend.api;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserResponse;
import pl.szmidla.chatappbackend.service.UserService;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserApi {

    public static int USERS_PAGE_SIZE = 10;
    private UserService userService;

    @GetMapping(value = "/users-by-phrase", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<UserResponse> getNUsersByPhrase(@RequestParam String phrase,
                                                @RequestParam int pageNr) {
        return userService.getNUsersByPhrase(phrase, pageNr, USERS_PAGE_SIZE);
    }

    @GetMapping(value = "/get-username", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUsernameFromJWT() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUsername();
    }
}