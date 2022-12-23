package pl.szmidla.chatappbackend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterApiTest {

    @Mock
    UserService userService;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RegisterApi registerApi = new RegisterApi(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(registerApi).build();
    }

    @Test
    void registerUser() throws Exception {
        String path = "/api/register";
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
        String path = "/api/register";
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
        String path = "/api/register";
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
        String path = "/api/register";
        UserRequest userRequest = createUserRequest("username", "ememail.com",
                "passasdasdasdasdasdddasdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddrd");
        String expectedResponseString = UserService.REGISTER_SUCCESS;

        mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(userRequest) ))
                .andExpect( status().isBadRequest() );
    }

    private UserRequest createUserRequest(String username, String email, String password) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        return userRequest;
    }

    @Test
    void isUsernameTakenNo() throws Exception {
        String path = "/api/register/username-exists";
        String username = "username123";
        boolean expectedResponse = false;
        when( userService.usernameExists(username) ).thenReturn( expectedResponse );

        MvcResult result = mockMvc.perform(get(path)
                        .param("username", username))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals( new ObjectMapper().writeValueAsString(expectedResponse), result.getResponse().getContentAsString() );
    }

    @Test
    void isUsernameTakenYes() throws Exception {
        String path = "/api/register/username-exists";
        String username = "username123";
        boolean expectedResponse = false;
        when(userService.usernameExists(username)).thenReturn( expectedResponse );

        MvcResult result = mockMvc.perform(get(path)
                        .param("username", username))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals( new ObjectMapper().writeValueAsString(expectedResponse), result.getResponse().getContentAsString() );
    }

    @Test
    void isEmailTakenNo() throws Exception {
        String path = "/api/register/email-exists";
        String email = "email@email.com";
        boolean expectedResponse = true;
        when( userService.emailExists(email) ).thenReturn( expectedResponse );

        MvcResult result = mockMvc.perform(get(path)
                        .param("email", email))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals( new ObjectMapper().writeValueAsString(expectedResponse), result.getResponse().getContentAsString() );
    }

    @Test
    void isEmailTakenYes() throws Exception {
        String path = "/api/register/email-exists";
        String email = "email@email.com";
        boolean expectedResponse = false;
        when( userService.emailExists(email) ).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(get(path)
                        .param("email", email))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals( new ObjectMapper().writeValueAsString(expectedResponse), result.getResponse().getContentAsString() );
    }
}