package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.UserRepository;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    public static String REGISTER_SUCCESS = "Successfully registered.";
    public static String REGISTER_EMAIL_TAKEN = "This email is already registered.";
    public static String REGISTER_USERNAME_TAKEN = "This username is already taken.";
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User id={} not found", id);
                    return new ItemNotFoundException("user");
                });
    }

    public String registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(REGISTER_EMAIL_TAKEN);
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException(REGISTER_USERNAME_TAKEN);
        }

        user.setPassword( passwordEncoder.encode(user.getPassword()) );
        userRepository.save(user);

        return REGISTER_SUCCESS;
    }

    public Page<User> getNUsersByPhrase(String phrase, int pageNr, int pageSize) {
        if (pageNr < 0) {
            throw new IllegalArgumentException("pageNr < 0");
        }
        Pageable usersPage = PageRequest.of(pageNr, pageSize);
        return userRepository.findAllByUsernameContainingIgnoreCase(phrase, usersPage);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
