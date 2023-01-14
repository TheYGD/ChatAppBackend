package pl.szmidla.chatappbackend.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.MessageWS;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;
    @InjectMocks
    ChatWebSocketController chatWebSocketController;

    User user1;
    User user2;

    @BeforeEach
    void setUp() {
        user1 = createUser(1L, "user1");
        user2 = createUser(2L, "user2");
    }

    @Test
    void sendMessageWithMessage() {
        Chat chat = createChat(1L, user1, user2, null);
        Message message = createMessageNow(1L, "message", false, chat);
        String destination = ChatWebSocketController.TOPIC_DESTINATION + user1.getUsername();
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessageWS> messageCaptor = ArgumentCaptor.forClass(MessageWS.class);
        doNothing().when( simpMessagingTemplate ).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        chatWebSocketController.sendMessage(message, user1);

        assertEquals(destination, destinationCaptor.getValue());
        assertEquals(message.getContent(), messageCaptor.getValue().getMessage().getContent());
    }

    private User createUser(long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    Chat createChat(long id, User thisUser, User otherUser, Message lastMessage) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUser1(thisUser);
        chat.setUser2(otherUser);
        chat.setLastMessage( lastMessage == null ? null : lastMessage.getContent());
        chat.setLastDate( lastMessage == null ? LocalDateTime.now(ZoneOffset.UTC) : lastMessage.getDate());
        chat.setClosed(false);

        if (lastMessage != null) {
            lastMessage.setChat(chat);
        }
        return chat;
    }

    Message createMessageNow(long id, String content, boolean byUser1, Chat chat) {
        Message message = Message.builder()
                .content(content)
                .byUser1(byUser1)
                .date(LocalDateTime.now(ZoneOffset.UTC))
                .chat(chat).build();
        message.setId(id);
        chat.setLastMessage(message.getContent());
        chat.setLastDate(message.getDate());
        return message;
    }

    @Test
    void sendMessageWithChat() {
        Chat chat = createChat(1L, user1, user2, null);
        String destination = ChatWebSocketController.TOPIC_DESTINATION + user1.getUsername();
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessageWS> messageCaptor = ArgumentCaptor.forClass(MessageWS.class);
        doNothing().when( simpMessagingTemplate ).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        chatWebSocketController.sendMessage(chat, user1);

        assertEquals(destination, destinationCaptor.getValue());
        assertNull(messageCaptor.getValue().getMessage());
    }
}