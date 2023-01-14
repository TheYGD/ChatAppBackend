package pl.szmidla.chatappbackend.data;

import lombok.*;

import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotActivatedUser extends BaseEntity {

    String username;
    String email;
    String password;
    String activationToken;

    public User toUser() {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .lastActive(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return user;
    }
}
