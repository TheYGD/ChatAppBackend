package pl.szmidla.chatappbackend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
                createChatPreview(user2, createChatObj(1L, loggedUser, user2, createMessageNow("m1", true))),
                createChatPreview(user2, createChatObj(2L, loggedUser, user2, createMessageNow("m2", true))));
        Page<ChatPreview> expectedPage = new PageImpl<>(chatPreviews);
        String expectedJson = new ObjectMapper().writeValueAsString(expectedPage);
        long lastChatId = 1L;
        String lastChatDateString = "mockDate";
        when( chatService.getUsersNChatPreviews(loggedUser, lastChatId, lastChatDateString, ChatApi.CHATS_PAGE_SIZE) )
                .thenReturn( expectedPage );

        String responseJson = mockMvc.perform( get("/api/chats")
                    .param("lastId", String.valueOf(lastChatId))
                    .param("lastDate", lastChatDateString) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }

    Message createMessageNow(String message, boolean byUser1) {
        return Message.builder()
                .content(message)
                .byUser1(byUser1)
                .date(LocalDateTime.now()).build();
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
                        .param("userId", String.valueOf(user2.getId())))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedChatIdJson, responseJson );
    }

    @Test
    void getNMessagesFromChatWithoutPageNr() throws Exception {
        User user2 = createUser(2L, "us2", "em2", "pass");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        List<MessageResponse> messages = List.of(
            createMessageResponseNowWithinChat("message1", loggedUser, chat),
            createMessageResponseNowWithinChat("message2", user2, chat),
            createMessageResponseNowWithinChat("message3", loggedUser, chat));
        Page<MessageResponse> expectedPage = new PageImpl<>(messages);
        String expectedJson = new ObjectMapper().writeValueAsString(expectedPage);
        when( chatService.getNMessagesFromChat(loggedUser, chat.getId(), -1, ChatApi.MESSAGES_PAGE_SIZE) )
                .thenReturn( expectedPage );

        String responseJson = mockMvc.perform( get("/api/chats/{id}/messages", chat.getId()) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }

    @Test
    void getNMessagesFromChatWithPageNr() throws Exception {
        User user2 = createUser(2L, "us2", "em2", "pass");
        Chat chat = createChatObj(1L, loggedUser, user2, null);
        List<MessageResponse> messages = List.of(
                createMessageResponseNowWithinChat("message1", loggedUser, chat),
                createMessageResponseNowWithinChat("message2", user2, chat),
                createMessageResponseNowWithinChat("message3", loggedUser, chat));
        Page<MessageResponse> expectedPage = new PageImpl<>(messages);
        int pageNr = 2;
        String expectedJson = new ObjectMapper().writeValueAsString(expectedPage);
        when( chatService.getNMessagesFromChat(loggedUser, chat.getId(), pageNr, ChatApi.MESSAGES_PAGE_SIZE) )
                .thenReturn( expectedPage );

        String responseJson = mockMvc.perform( get("/api/chats/{id}/messages", chat.getId())
                        .param("pageNr", String.valueOf(pageNr) ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, responseJson );
    }


    MessageResponse createMessageResponseNowWithinChat(String content, User sender, Chat chat) {
        Message message = createMessageNow(content, chat.getUser1().equals(sender));
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
}