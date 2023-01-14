package pl.szmidla.chatappbackend.data.dto;

import lombok.Getter;
import lombok.Setter;
import pl.szmidla.chatappbackend.data.NotActivatedUser;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
public class RegisterRequest {
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

    public NotActivatedUser toNotActivatedUser() {
        NotActivatedUser user = NotActivatedUser.builder()
            .username(username)
            .email(email)
            .password(password)
            .creationDate(LocalDateTime.now(ZoneOffset.UTC))
            .build();
        return user;
    }
}
