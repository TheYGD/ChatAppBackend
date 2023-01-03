package pl.szmidla.chatappbackend.data.dto;

import lombok.Getter;
import lombok.Setter;
import pl.szmidla.chatappbackend.data.User;

@Getter
@Setter
public class ProfileInfo {

    private String username;
    private String email;
    private String imageUrl;


    public static ProfileInfo fromUser(User user) {
        ProfileInfo profileInfo = new ProfileInfo();
        profileInfo.setUsername(user.getUsername());
        profileInfo.setEmail(user.getEmail());
        profileInfo.setImageUrl(user.getImageUrl());
        return profileInfo;
    }
}
