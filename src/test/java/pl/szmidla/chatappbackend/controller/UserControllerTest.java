package pl.szmidla.chatappbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration( classes = { ValidatorContext.class })
class UserControllerTest {

    @Mock
    UserService userService;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup( userController ).build();
    }

    @Test
    void registerUser() throws Exception {
        String path = "/register";
        UserRequest userRequest = createUserRequest("username", "em@email.com", "password");
        String expectedResponseString = UserService.REGISTER_SUCCESS;
        when( userService.registerUser(any()) ).thenReturn( expectedResponseString );

        String actualResponseString = mockMvc.perform( post(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content( objectMapper.writeValueAsString(userRequest) ))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedResponseString, actualResponseString );
    }

    /** username is invalid - too short */
    @Test
    void registerUserInvalidBodyUsername() throws Exception {
        String path = "/register";
        UserRequest userRequest = createUserRequest("usern", "em@email.com", "password");
        String expectedResponseString = UserService.REGISTER_SUCCESS;

        mockMvc.perform( post(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content( objectMapper.writeValueAsString(userRequest) ))
                .andExpect( status().isBadRequest() );
    }

    /** email is invalid */
    @Test
    void registerUserInvalidBodyEmail() throws Exception {
        String path = "/register";
        UserRequest userRequest = createUserRequest("username", "ememail.com", "password");
        String expectedResponseString = UserService.REGISTER_SUCCESS;

        mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(userRequest) ))
                .andExpect( status().isBadRequest() );
    }

    /** password is invalid - too long */
    @Test
    void registerUserInvalidBodyPassword() throws Exception {
        String path = "/register";
        UserRequest userRequest = createUserRequest("username", "ememail.com",
                "passasdasdasdasdasdddasdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddrd");
        String expectedResponseString = UserService.REGISTER_SUCCESS;

        mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(userRequest) ))
                .andExpect( status().isBadRequest() );
    }

    @Test
    void getNUsersByPhrase() throws Exception {
        String path = "/users-by-phrase";UserRequest userRequest = createUserRequest("username", "em@email.com", "password");
        String phrase = "phrase";
        Page page = Page.empty();
        String expectedResponseString = objectMapper.writeValueAsString(page);
        when( userService.getNUsersByPhrase( anyString(), anyInt(), anyInt()) ).thenReturn( page );

        String actualResponseString = mockMvc.perform( get(path)
                        .param("phrase", phrase))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedResponseString, actualResponseString );
    }


    private UserRequest createUserRequest(String username, String email, String password) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        return userRequest;
    }
}