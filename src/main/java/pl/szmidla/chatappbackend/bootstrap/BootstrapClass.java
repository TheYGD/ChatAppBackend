package pl.szmidla.chatappbackend.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.repository.UserRepository;
import pl.szmidla.chatappbackend.service.ChatService;
import pl.szmidla.chatappbackend.service.RegisterService;

@Component
@RequiredArgsConstructor
public class BootstrapClass implements CommandLineRunner {

    private final RegisterService registerService;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private User user1;
    private User user2;
    private User user3;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            registerUsers();
            createChat(user1, user2);
            Chat chat = createChat(user1, user3);
            sendSomeMessages(chat);

            createALotOfUsersWithChats();
        }
    }

    private void createALotOfUsersWithChats() {
        for (int i = 0; i < 5; i++) {
            String username = randomString(8,15);
            String email = "email@das.email" + i;
            UserRequest userReq1 = createUser(username, email, "Haslo123");
            registerService.registerUser(userReq1);
            User registered = userRepository.findByUsernameOrEmail(username, "").orElseThrow();
            createChat(user1, registered);
        }
    }

    String randomString(int min, int max) {
        StringBuilder sb = new StringBuilder();
        int n = (int)Math.round(Math.random() * (float)(max-min)) + min;
        for (int i = 0; i < n; i++) {
            char x = (char)((int)Math.round(Math.random() * (float)('z' - 'a')) + 'a');
            sb.append( x );
        }
        return sb.toString();
    }

    private void sendSomeMessages(Chat chat) {
        User user1 = chat.getUser1();
        User user2 = chat.getUser2();
        chatService.sendMessage(user1, chat.getId(), "Henlo");
        chatService.sendMessage(user2, chat.getId(), "Hello, what's up?");
        chatService.sendMessage(user1, chat.getId(), "s'all good man, u?");
        chatService.sendMessage(user1, chat.getId(), "asdasd");
        chatService.sendMessage(user1, chat.getId(), "something");
    }

    private Chat createChat(User user1, User user2) {
        return chatService.createChat(user1, user2.getId());
    }

    private void registerUsers() {
        UserRequest userReq1 = createUser("login123", "email@email.com", "Haslo123");
        registerService.registerUser(userReq1);
        user1 = userRepository.findByUsernameOrEmail("login123", "").orElseThrow();
        UserRequest userReq2 = createUser("login222", "email2@email.com", "Haslo123");
        registerService.registerUser(userReq2);
        user2 = userRepository.findByUsernameOrEmail("login222", "").orElseThrow();
        UserRequest userReq3 = createUser("thirdUser", "email3@email.com", "Haslo123");
        registerService.registerUser(userReq3);
        user3 = userRepository.findByUsernameOrEmail("thirdUser", "").orElseThrow();
    }

    private UserRequest createUser(String username, String email, String password) {
        UserRequest user = new UserRequest();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
