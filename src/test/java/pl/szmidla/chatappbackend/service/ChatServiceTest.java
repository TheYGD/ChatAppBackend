package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ChatPreview;
import pl.szmidla.chatappbackend.data.dto.MessageResponse;
import pl.szmidla.chatappbackend.exception.ItemAlreadyExistsException;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.ChatRepository;
import pl.szmidla.chatappbackend.repository.MessageRepository;
import pl.szmidla.chatappbackend.websocket.ChatWebSocketController;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
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
    @Mock
    ChatWebSocketController chatWebSocketController;
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
        chat.setLastMessage( lastMessage == null ? null : lastMessage.getContent());
        chat.setLastDate( lastMessage == null ? LocalDateTime.now() : lastMessage.getDate());
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
    void getUsersNChatPreviewsWithParams() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        List<Chat> chats = List.of( createChatObj(1L, loggedUser, user1, null),
                createChatObj(2L, loggedUser, user1, null));
        when( chatRepository.findAllWithUserBeforeGivenDateAndExceptId(any(), any(), anyLong(), any()) )
                .thenReturn( chats );

        List<ChatPreview> chatPreviews = chatService.getUsersNChatPreviews(loggedUser, 1L,
                "2022-12-07T12:10:20", 10);

        assertEquals( chats.size(), chatPreviews.size() );
        verify( chatRepository ).findAllWithUserBeforeGivenDateAndExceptId(any(), any(), anyLong(), any());
        verify( chatRepository, times(0) ).findAllWithUser(any(), any());
    }

    @Test
    void getUsersNChatPreviewsWithoutParams() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        List<Chat> chats = List.of( createChatObj(1L, loggedUser, user1, null),
                createChatObj(2L, loggedUser, user1, null));
        when( chatRepository.findAllWithUser(any(), any()) ).thenReturn( chats );

        List<ChatPreview> chatPreviews = chatService.getUsersNChatPreviews(loggedUser, -1L,
                null, 10);

        assertEquals( chats.size(), chatPreviews.size() );
        verify( chatRepository, times(0) ).findAllWithUserBeforeGivenDateAndExceptId(any(),
                any(), anyLong(), any());
        verify( chatRepository ).findAllWithUser(any(), any());
    }

    @Test
    void getUsersNChatPreviewsBadDate() {
        assertThrows( DateTimeParseException.class, () -> chatService.getUsersNChatPreviews(loggedUser, 1L,
                "2022-12-07", 10));
    }

    @Test
    void createChat() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        when( userService.getUserById(user1.getId()) ).thenReturn( user1 );
        when( chatRepository.existsByUser1IdAndUser2Id(anyLong(), anyLong()) ).thenReturn( false );

        Chat chat = chatService.createChat(loggedUser, user1.getId());

        assertEquals( loggedUser, chat.getUser1() );
        assertEquals( user1, chat.getUser2() );
        assertEquals( null, chat.getLastMessage() );
        assertEquals( false, chat.isClosed() );
        verify( chatRepository ).save( any() );
        verify( chatWebSocketController, times(2) ).sendMessage((Chat) any(), any());
    }

    @Test
    void createChatWithThemselves() {
        assertThrows( IllegalArgumentException.class, () -> chatService.createChat(loggedUser, loggedUser.getId()));
        verify( chatRepository, times(0) ).save( any() );
        verify( chatWebSocketController, times(0) ).sendMessage((Chat) any(), any());
    }

    @Test
    void createChatAgain() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        when( userService.getUserById(user1.getId()) ).thenReturn( user1 );
        when( chatRepository.existsByUser1IdAndUser2Id(anyLong(), anyLong()) ).thenReturn( true );

        assertThrows( ItemAlreadyExistsException.class, () -> chatService.createChat(loggedUser, user1.getId()));
        verify( chatRepository, times(0) ).save( any() );
        verify( chatWebSocketController, times(0) ).sendMessage((Chat) any(), any());
    }

    @Test
    void getNMessagesFromChatWithoutLastMessageId() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        Chat chat = createChatObj(1L, loggedUser, user1, null);
        List<Message> messages = new LinkedList<>(List.of(createMessageNow(1L, "mess1", true, chat),
                createMessageNow(2L, "mess2", false, chat),
                createMessageNow(3L, "mess3", true, chat)));
        when( chatRepository.findById(chat.getId()) ).thenReturn(Optional.of(chat));
        when( messageRepository.findLastNMessagesFromChat(any(), any()) ).thenReturn( messages );

        List<MessageResponse> messageResponses = chatService.getNMessagesFromChat(loggedUser, chat.getId(), -1,
                10);

        assertEquals( messages.size(), messageResponses.size() );
        for (int i = 0; i < messages.size(); i++) {
            assertEquals( messages.get(i).getContent(), messageResponses.get(i).getContent() );
        }
    }

    Message createMessageNow(long id, String content, boolean byUser1, Chat chat) {
        Message message = Message.builder()
                .content(content)
                .byUser1(byUser1)
                .date(LocalDateTime.now())
                .chat(chat).build();
        message.setId(id);
        chat.setLastMessage(message.getContent());
        chat.setLastDate(message.getDate());
        return message;
    }

    @Test
    void getNMessagesFromChatWithLastMessageId() {
        User user1 = createUser(2L, "user1", "email1@o2.pl", "password");
        Chat chat = createChatObj(1L, loggedUser, user1, null);
        List<Message> messages = new LinkedList<>(List.of(createMessageNow(1L, "mess1", true, chat),
                createMessageNow(2L, "mess2", false, chat),
                createMessageNow(3L, "mess3", true, chat)));
        when( chatRepository.findById(chat.getId()) ).thenReturn(Optional.of(chat));
        when( messageRepository.findLastNMessagesFromChatAfterId(anyLong(), anyLong(), any()) ).thenReturn( messages );

        List<MessageResponse> messageResponses = chatService.getNMessagesFromChat(loggedUser, chat.getId(), 1,
                10);

        assertEquals( messages.size(), messageResponses.size() );
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
        verify( chatWebSocketController, times(2) ).sendMessage((Message) any(), any());
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
        verify( chatWebSocketController, times(0) ).sendMessage((Message) any(), any());
    }
}