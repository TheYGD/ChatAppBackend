package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@AllArgsConstructor
public class RegisterService {
    public static String REGISTER_SUCCESS = "Successfully registered.";
    public static String REGISTER_EMAIL_TAKEN = "This email is already registered.";
    public static String REGISTER_USERNAME_TAKEN = "This username is already taken.";
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public String registerUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException(REGISTER_EMAIL_TAKEN);
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new IllegalArgumentException(REGISTER_USERNAME_TAKEN);
        }

        userRequest.setPassword( passwordEncoder.encode(userRequest.getPassword()) );
        User user = userRequest.toUser();
        user.setLastActive(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        return REGISTER_SUCCESS;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
