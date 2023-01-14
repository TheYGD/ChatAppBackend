package pl.szmidla.chatappbackend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ChatPreview;
import pl.szmidla.chatappbackend.data.dto.MessageResponse;
import pl.szmidla.chatappbackend.service.ChatService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatApiTest {

    @Mock
    ChatService chatService;
    MockMvc mockMvc;
    User loggedUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ChatApi chatApi = new ChatApi(chatService);
        mockMvc = MockMvcBuilders.standaloneSetup(chatApi).build();

        loggedUser = createUser(1L, "username", "em@ai.l", "password");
        Authentication auth = new UsernamePasswordAuthenticationToken(loggedUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
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
    void getUsersNChatPreviews() throws Exception {
        User user2 = createUser(2L, "us2", "em", "pas");
        List<ChatPreview> chatPreviews = List.of(
                createChatPreview(user2, createChatObj(1L, loggedUser, user2, createMessageNow(1L, "m1", true))),
                createChatPreview(user2, createChatObj(2L, loggedUser, user2, createMessageNow(2L, "m2", true))));
        String expectedJson = new ObjectMapper().writeValueAsString(chatPreviews);
        long lastChatId = 1L;
        String lastChatDateString = "mockDate";
        when( chatService.getUsersNChatPreviews(loggedUser, lastChatId, lastChatDateString, ChatApi.CHATS_PAGE_SIZE) )
                .thenReturn( chatPreviews );

        String responseJson = mockMvc.perform( get("/api/chats")
                    .param("lastId", String.valueOf(lastChatId))
                    .param("lastDate", lastChatDateString) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }

    Message createMessageNow(long id, String content, boolean byUser1) {
        Message message = Message.builder()
                .content(content)
                .byUser1(byUser1)
                .date(LocalDateTime.now(ZoneOffset.UTC)).build();
        message.setId(id);
        return message;
    }

    Chat createChatObj(long id, User thisUser, User otherUser, Message lastMessage) {
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

    ChatPreview createChatPreview(User thisUser, Chat chat) {
        return ChatPreview.fromChat(chat, thisUser);
    }

    @Test
    void createChat() throws Exception {
        User user2 = createUser(2L, "us2", "em2", "pass");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        String expectedChatIdJson = chat.getId().toString();
        when( chatService.createChat(loggedUser, user2.getId()) ).thenReturn( chat );

        String responseJson = mockMvc.perform( post("/api/chats")
                        .param("id", String.valueOf(user2.getId())))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedChatIdJson, responseJson );
    }

    @Test
    void getNMessagesFromChatWithoutLastMessageId() throws Exception {
        User user2 = createUser(2L, "us2", "em2", "pass");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        List<MessageResponse> messages = List.of(
            createMessageResponseNowWithinChat(1L, "message1", loggedUser, chat),
            createMessageResponseNowWithinChat(2L, "message2", user2, chat),
            createMessageResponseNowWithinChat(3L, "message3", loggedUser, chat));
        String expectedJson = new ObjectMapper().writeValueAsString(messages);
        when( chatService.getNMessagesFromChat(loggedUser, chat.getId(), -1, ChatApi.MESSAGES_PAGE_SIZE) )
                .thenReturn( messages );

        String responseJson = mockMvc.perform( get("/api/chats/{id}/messages", chat.getId()) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }

    @Test
    void getNMessagesFromChatWithLastMessageId() throws Exception {
        User user2 = createUser(2L, "us2", "em2", "pass");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        List<MessageResponse> messages = List.of(
                createMessageResponseNowWithinChat(1L, "message1", loggedUser, chat),
                createMessageResponseNowWithinChat(2L, "message2", user2, chat),
                createMessageResponseNowWithinChat(3L, "message3", loggedUser, chat));
        String expectedJson = new ObjectMapper().writeValueAsString(messages);
        long lastMessageId = 2L;
        when( chatService.getNMessagesFromChat(loggedUser, chat.getId(), lastMessageId, ChatApi.MESSAGES_PAGE_SIZE) )
                .thenReturn( messages );

        String responseJson = mockMvc.perform( get("/api/chats/{id}/messages", chat.getId())
                        .param("lastMessageId", String.valueOf(lastMessageId) ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }


    MessageResponse createMessageResponseNowWithinChat(long id, String content, User sender, Chat chat) {
        Message message = createMessageNow(id, content, chat.getUser1().equals(sender));
        message.setChat(chat);
        return MessageResponse.fromMessage( message, sender );
    }

    @Test
    void sendMessage() throws Exception {
        User user2 = createUser(2L, "us2", "em2", "pass");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        String content = "message :)";
        String expectedJson  = "true";

        String responseJson = mockMvc.perform( post("/api/chats/{id}/messages", chat.getId())
                        .param("content", content))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }

    @Test
    void messageRead() throws Exception {
        long chatId = 1L;
        long messageId = 1L;

        mockMvc.perform( post("/api/chats/{id}/message-read", chatId)
                        .param("messageId", String.valueOf(messageId) ) )
                .andExpect( status().isOk() );
    }
}