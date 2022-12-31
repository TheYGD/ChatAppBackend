package pl.szmidla.chatappbackend.websocket;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.MessageWS;

@Controller
@AllArgsConstructor
public class ChatWebSocketController {

    public static String TOPIC_DESTINATION = "/topic/users/";

    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendMessage(Message message, User user) {
        MessageWS response = MessageWS.fromMessage(message, user);
        simpMessagingTemplate.convertAndSend(TOPIC_DESTINATION + user.getUsername(), response);
    }

    public void sendMessage(Chat chat, User user) {
        MessageWS response = MessageWS.fromChat(chat, user);
        simpMessagingTemplate.convertAndSend(TOPIC_DESTINATION + user.getUsername(), response);
    }
}
