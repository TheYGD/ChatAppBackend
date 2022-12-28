package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ChatPreview;
import pl.szmidla.chatappbackend.data.dto.MessageResponse;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.ChatRepository;
import pl.szmidla.chatappbackend.repository.MessageRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    ChatRepository chatRepository;
    @Mock
    MessageRepository messageRepository;
    @Mock
    UserService userService;
    @InjectMocks
    ChatService chatService;
    User loggedUser = createUser(1L, "username", "em@ai.l", "password");

    private User createUser(long id, String username, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void getChatById() {
        User user2 = createUser(2L, "user2", "em@aillll.l", "password");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        when( chatRepository.findById(chat.getId()) ).thenReturn( Optional.of(chat) );

        Chat actualChat = chatService.getChatById(chat.getId());

        assertEquals( chat, actualChat );
    }

    Chat createChatObj(long id, User thisUser, User otherUser, Message lastMessage) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUser1(thisUser);
        chat.setUser2(otherUser);
        chat.setLastMessage(lastMessage);
        chat.setClosed(false);

        if (lastMessage != null) {
            lastMessage.setChat(chat);
        }
        return chat;
    }

    @Test
    void getChatByIdException() {
        long id = 1L;
        when( chatRepository.findById(id) ).thenReturn( Optional.empty() );

        assertThrows( ItemNotFoundException.class, () -> chatService.getChatById(id) );
    }

    @Test
    void getChatByIdForUser() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        Chat chat = createChatObj(1L, loggedUser, user1, null);
        when( chatRepository.findById(chat.getId()) ).thenReturn( Optional.of(chat) );

        Chat actualChat = chatService.getChatByIdForUser(chat.getId(), loggedUser);

        assertEquals( chat, actualChat );
    }

    @Test
    void getChatByIdForUserException() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        User user2 = createUser(3L, "user2", "email2@o2.pl", "password");
        Chat chat = createChatObj(1L, user1, user2, null);
        when( chatRepository.findById(chat.getId()) ).thenReturn( Optional.of(chat) );

        assertThrows( IllegalArgumentException.class, () -> chatService.getChatByIdForUser(chat.getId(), loggedUser));
    }

    @Test
    void getUsersNChatPreviews() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        List<Chat> chats = List.of( createChatObj(1L, loggedUser, user1, null),
                createChatObj(2L, loggedUser, user1, null));
        when( chatRepository.findAllByIdNotAndLastMessageDateBefore(any(), anyLong(), any()) )
                .thenReturn( new PageImpl<>(chats) );

        List<ChatPreview> chatPreviews = chatService.getUsersNChatPreviews(loggedUser, 1L,
                "2022-12-07T12:10:20", 10).getContent();

        assertEquals( chats.size(), chatPreviews.size() );
    }


    @Test
    void getUsersNChatPreviewsBadDate() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        List<Chat> chats = List.of( createChatObj(1L, loggedUser, user1, null),
                createChatObj(2L, loggedUser, user1, null));

        assertThrows( DateTimeParseException.class, () -> chatService.getUsersNChatPreviews(loggedUser, 1L,
                "2022-12-07", 10).getContent());
    }

    @Test
    void createChat() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        when( userService.getUserById(user1.getId()) ).thenReturn( user1 );
        when( chatRepository.existsByUser1IdOrUser2Id(anyLong(), anyLong()) ).thenReturn( false );

        Chat chat = chatService.createChat(loggedUser, user1.getId());

        assertEquals( loggedUser, chat.getUser1() );
        assertEquals( user1, chat.getUser2() );
        assertEquals( null, chat.getLastMessage() );
        assertEquals( false, chat.isClosed() );
        verify( chatRepository ).save( any() );
    }

    @Test
    void createChatWithThemselves() {
        assertThrows( IllegalArgumentException.class, () -> chatService.createChat(loggedUser, loggedUser.getId()));
    }

    @Test
    void createChatAgain() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        when( userService.getUserById(user1.getId()) ).thenReturn( user1 );
        when( chatRepository.existsByUser1IdOrUser2Id(anyLong(), anyLong()) ).thenReturn( true );

        assertThrows( IllegalArgumentException.class, () -> chatService.createChat(loggedUser, user1.getId()));
    }

    @Test
    void getNMessagesFromChatWithoutPageNr() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        Chat chat = createChatObj(1L, loggedUser, user1, null);
        List<Message> messages = List.of( createMessageNow("mess1", true, chat),
                createMessageNow("mess2", false, chat),
                createMessageNow("mess3", true, chat));
        when( chatRepository.findById(chat.getId()) ).thenReturn(Optional.of(chat));
        when(  messageRepository.countByChat(chat) ).thenReturn( 2L );
        when( messageRepository.findAllByChatOrderByIdAsc(any(), any()) ).thenReturn( new PageImpl<>(messages) );

        List<MessageResponse> messageResponses = chatService.getNMessagesFromChat(loggedUser, chat.getId(), -1,
                10).getContent();

        assertEquals( messages.size(), messageResponses.size() );
        verify( messageRepository ).countByChat( chat );
        for (int i = 0; i < messages.size(); i++) {
            assertEquals( messages.get(i).getContent(), messageResponses.get(i).getContent() );
        }
    }

    Message createMessageNow(String content, boolean byUser1, Chat chat) {
        Message message = Message.builder()
                .content(content)
                .byUser1(byUser1)
                .date(LocalDateTime.now())
                .chat(chat).build();
        chat.setLastMessage(message);
        return message;
    }

    @Test
    void getNMessagesFromChatWithPageNr() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        Chat chat = createChatObj(1L, loggedUser, user1, null);
        List<Message> messages = List.of( createMessageNow("mess1", true, chat),
                createMessageNow("mess2", false, chat),
                createMessageNow("mess3", true, chat));
        when( chatRepository.findById(chat.getId()) ).thenReturn(Optional.of(chat));
        when( messageRepository.findAllByChatOrderByIdAsc(any(), any()) ).thenReturn( new PageImpl<>(messages) );

        List<MessageResponse> messageResponses = chatService.getNMessagesFromChat(loggedUser, chat.getId(), 1,
                10).getContent();

        assertEquals( messages.size(), messageResponses.size() );
        verify( messageRepository, times(0) ).countByChat( chat );
        for (int i = 0; i < messages.size(); i++) {
            assertEquals( messages.get(i).getContent(), messageResponses.get(i).getContent() );
        }
    }

    @Test
    void sendMessage() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        Chat chat = createChatObj(1L, loggedUser, user1, null);
        when( chatRepository.findById(chat.getId()) ).thenReturn( Optional.of(chat) );

        chatService.sendMessage(loggedUser, chat.getId(), "message");

        verify( messageRepository ).save( any() );
        verify( chatRepository ).save( any() );
    }

    @Test
    void sendMessageToNotOwnChat() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        User user2 = createUser(3L, "user2", "email2@o2.pl", "password");
        Chat chat = createChatObj(1L, user1, user2, null);
        when( chatRepository.findById(chat.getId()) ).thenReturn( Optional.of(chat) );

        assertThrows( IllegalArgumentException.class, () ->
                chatService.sendMessage(loggedUser, chat.getId(), "message"));

        verify( messageRepository, times(0) ).save( any() );
        verify( chatRepository , times(0)).save( any() );
    }
}