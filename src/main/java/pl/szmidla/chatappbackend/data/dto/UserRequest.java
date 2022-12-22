package pl.szmidla.chatappbackend.data.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pl.szmidla.chatappbackend.data.User;

@Getter
@Setter
public class UserRequest {

    @NotNull
    @Size(min=6, max=30)
    @Pattern(regexp = "\\w+")
    private String username;
    @NotNull
    @Email
    private String email;
    @NotNull
    @Size(min=8, max=30)
    @Pattern(regexp = "\\w+")
    private String password;

    public User toUser() {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
