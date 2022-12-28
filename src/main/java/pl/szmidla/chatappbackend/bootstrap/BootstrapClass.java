package pl.szmidla.chatappbackend.bootstrap;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.service.UserService;

@Component
@AllArgsConstructor
public class BootstrapClass implements CommandLineRunner {

    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        registerUsers();
    }

    private void registerUsers() {
        UserRequest user = createUser("login123", "email@email.com", "Haslo123");
        userService.registerUser(user);
    }

    private UserRequest createUser(String username, String email, String password) {
        UserRequest user = new UserRequest();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
