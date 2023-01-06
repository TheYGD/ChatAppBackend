package pl.szmidla.chatappbackend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.service.UserService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserApiTest {

    @Mock
    UserService userService;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserApi userApi = new UserApi(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userApi).build();
    }

    @Test
    void getNUsersByPhrase() throws Exception {
        String path = "/api/users-by-phrase";
        String phrase = "phrase";
        Page page = Page.empty();
        String expectedResponseString = objectMapper.writeValueAsString(page);
        when( userService.getNUsersByPhrase( anyString(), anyInt(), anyInt()) ).thenReturn( page );

        String actualResponseString = mockMvc.perform( get(path)
                        .param("phrase", phrase)
                        .param("pageNr", "1"))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedResponseString, actualResponseString );
    }


    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void getUsernameFromJWT() throws Exception {
        String path = "/api/get-username";
        User user = createUser("username", "email@email.com", "password");
        addUserToSecurityContext(user);
        String expectedResponse = user.getUsername();

        String actualResponse = mockMvc.perform( get(path) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedResponse, actualResponse );
    }

    private void addUserToSecurityContext(User user) {
        Authentication auth = Mockito.spy(Authentication.class);
        when( auth.getPrincipal() ).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getUsersActiveStatuses() throws Exception {
        String path = "/api/users-active-status";
        long userId = 1L;
        Map<Long, String> expectedResponse = Map.of(userId, "2023-01-06T20:39:00");
        String responseJson = objectMapper.writeValueAsString(expectedResponse);
        when( userService.getUsersActiveStatuses(any()) ).thenReturn( expectedResponse );

        String actualResponse = mockMvc.perform( get(path)
                        .param("usersIds", String.valueOf(userId)))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( responseJson, actualResponse );
    }
}