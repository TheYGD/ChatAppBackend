package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

    public static String REGISTER_SUCCESS = "Successfully registered.";
    public static String REGISTER_EMAIL_TAKEN = "This email is already registered.";
    public static String REGISTER_USERNAME_TAKEN = "This username is already taken.";
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public String registerUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return REGISTER_EMAIL_TAKEN;
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            return REGISTER_USERNAME_TAKEN;
        }

        User user = userRequest.toUser();
        user.setPassword( passwordEncoder.encode(user.getPassword()) );
        userRepository.save(user);

        return REGISTER_SUCCESS;
    }

    public Page<User> getNUsersByPhrase(String phrase, int pageNr, int pageSize) {
        if (pageNr < 0) {
            throw new IllegalArgumentException("pageNr < 0");
        }
        Pageable usersPage = PageRequest.of(pageNr, pageSize);
        return userRepository.findAllContainingUsernameIgnoreCase(phrase, usersPage);
    }
}
