package pl.szmidla.chatappbackend.api;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ProfileInfo;
import pl.szmidla.chatappbackend.service.ProfileService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/profile")
public class ProfileApi {

    private ProfileService profileService;


    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileInfo getProfileInfo() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ProfileInfo.fromUser(user);
    }

    @PostMapping(
            path= "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void uploadUserImage(@RequestParam(value="file", required=false) MultipartFile file) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        profileService.uploadUserImage(user, file);
    }
}
