package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.data.dto.UserResponse;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User username={} not found", username);
                    return new ItemNotFoundException("user");
                });
    }

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

    public Page<UserResponse> getNUsersByPhrase(String phrase, int pageNr, int pageSize) {
        if (pageNr < 0) {
            throw new IllegalArgumentException("pageNr < 0");
        }
        Pageable usersPage = PageRequest.of(pageNr, pageSize);
        return userRepository.findAllByUsernameContainingIgnoreCase(phrase, usersPage)
                .map(UserResponse::fromUser);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void setUsersActiveStatus(String username, boolean active) {
        User user = getUserByUsername(username);

        if (active) {
            user.setLastActive(null);
        }
        else {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            user.setLastActive(now);
        }

        userRepository.save(user);
    }

    public Map<Long, String> getUsersActiveStatuses(List<Long> usersIds) {
        Map<Long, String> statusMap = new HashMap<>();
        for (long userId : usersIds) {
            User user = getUserById(userId);
            String statusString = user.getLastActive() != null ? user.getLastActive().toString() : null;
            statusMap.put(userId, statusString);
        }
        return statusMap;
    }
}
