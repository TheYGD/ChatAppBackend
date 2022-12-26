package pl.szmidla.chatappbackend.bootstrap;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.szmidla.chatappbackend.data.User;
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
        User user = createUser("login123", "email@email.com", "Haslo123");
        userService.registerUser(user);
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
