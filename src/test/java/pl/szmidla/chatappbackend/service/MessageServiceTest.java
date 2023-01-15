package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.MessageRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    MessageRepository messageRepository;
    @InjectMocks
    MessageService messageService;

    User user1;
    User user2;

    @BeforeEach
    void setUp() {
        user1 = createUser(1L, "user1", "em1@o2.com", "password");
        user2 = createUser(2L, "user2", "some@em.com", "passw");
    }


    private User createUser(long id, String username, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void getMessageById() {
        Chat chat = createChat(1L, user1, user2, null);
        Message expectedMessage = createMessageNow(1L, "content", true, chat);
        when( messageRepository.findById(anyLong()) ).thenReturn( Optional.of(expectedMessage) );

        Message actualMessage = messageService.getMessageById(expectedMessage.getId());

        assertEquals( expectedMessage, actualMessage );
    }

    Chat createChat(long id, User thisUser, User otherUser, Message lastMessage) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUser1(thisUser);
        chat.setUser2(otherUser);

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
        chat.setLastMessage(message);
        chat.setLastDate(message.getDate());
        return message;
    }

    @Test
    void getMessageByIdException() {
        when( messageRepository.findById(anyLong()) ).thenReturn( Optional.empty() );

        Throwable exception = assertThrows(ItemNotFoundException.class, () ->
                messageService.getMessageById(anyLong()));

        assertEquals( ItemNotFoundException.MESSAGE_TEMPLATE.formatted("Message"), exception.getMessage() );
    }

    @Test
    void getMessageByIdWithinChat() {
        Chat chat = createChat(1L, user1, user2, null);
        Message expectedMessage = createMessageNow(1L, "content", true, chat);
        when( messageRepository.findById(anyLong()) ).thenReturn( Optional.of(expectedMessage) );

        Message actualMessage = messageService.getMessageByIdWithinChat(expectedMessage.getId(), chat);

        assertEquals( expectedMessage, actualMessage );
    }

    @Test
    void getMessageByIdWithinChatException() {
        Chat chat1 = createChat(1L, user1, user2, null);
        Chat chat2 = createChat(2L, user1, user2, null);
        Message message = createMessageNow(1L, "content", true, chat1);
        when( messageRepository.findById(anyLong()) ).thenReturn( Optional.of(message) );

        assertThrows( IllegalArgumentException.class, () ->
                messageService.getMessageByIdWithinChat(message.getId(), chat2));
    }

    @Test
    void getLastNMessagesFromChat() {
        Chat chat = createChat(1L, user1, user2, null);
        List<Message> expectedMessages = List.of( createMessageNow(1L, "hello", true, chat) );
        PageRequest pageable = PageRequest.of(0, 1);
        when( messageRepository.findLastNMessagesFromChat(anyLong(), any()) ).thenReturn( expectedMessages );

        List<Message> actualMessages = messageService.getLastNMessagesFromChat(chat.getId(), pageable);

        assertEquals( expectedMessages, actualMessages );
    }

    @Test
    void getLastNMessagesFromChatAfterId() {
        Chat chat = createChat(1L, user1, user2, null);
        long paramId = 1L;
        List<Message> expectedMessages = List.of( createMessageNow(2L, "hello", true, chat) );
        PageRequest pageable = PageRequest.of(0, 1);
        when( messageRepository.findLastNMessagesFromChatAfterId(anyLong(), anyLong(),any()) ).thenReturn( expectedMessages );

        List<Message> actualMessages = messageService.getLastNMessagesFromChatAfterId(chat.getId(), paramId, pageable);

        assertEquals( expectedMessages, actualMessages );
    }

    @Test
    void saveMessage() {
        Chat chat = createChat(1L, user1, user2, null);
        Message message = createMessageNow(2L, "hello", true, chat);

        messageService.saveMessage(message);
    }
}