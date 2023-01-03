package pl.szmidla.chatappbackend.data.dto;

import lombok.Getter;
import lombok.Setter;
import pl.szmidla.chatappbackend.data.User;

@Getter
@Setter
public class UserResponse {

    private long id;
    private String username;
    private String imageUrl;

    public static UserResponse fromUser(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setImageUrl(user.getImageUrl());
        return userResponse;
    }
}
