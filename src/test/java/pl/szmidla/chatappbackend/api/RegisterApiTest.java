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
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.exception.InvalidArgumentException;
import pl.szmidla.chatappbackend.service.RegisterService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterApiTest {

    @Mock
    RegisterService registerService;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RegisterApi registerApi = new RegisterApi(registerService);
        mockMvc = MockMvcBuilders.standaloneSetup(registerApi).build();
    }

    @Test
    void registerRequest() throws Exception {
        String path = "/api/register";
        User user = createUser("username", "em@email.com", "password");
        String expectedResponseString = RegisterService.REGISTER_SUCCESS;
        when( registerService.handleRegisterRequest(any()) ).thenReturn( expectedResponseString );

        String actualResponseString = mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(user) ))
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedResponseString, actualResponseString );
    }

    /** username is invalid - too short */
    @Test
    void registerRequestInvalidBodyUsername() throws Exception {
        String path = "/api/register";
        User user = createUser("usern", "em@email.com", "password");

        mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(user) ))
                .andExpect( status().isBadRequest() );
    }

    /** email is invalid */
    @Test
    void registerRequestInvalidBodyEmail() throws Exception {
        String path = "/api/register";
        User user = createUser("username", "ememail.com", "password");

        mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(user) ))
                .andExpect( status().isBadRequest() );
    }

    /** password is invalid - too long */
    @Test
    void registerRequestInvalidBodyPassword() throws Exception {
        String path = "/api/register";
        User user = createUser("username", "ememail.com",
                "passasdasdasdasdasdddasdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddrd");
        String expectedResponseString = RegisterService.REGISTER_SUCCESS;

        mockMvc.perform( post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( objectMapper.writeValueAsString(user) ))
                .andExpect( status().isBadRequest() );
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void isUsernameTakenNo() throws Exception {
        String path = "/api/register/username-exists";
        String username = "username123";
        boolean expectedResponse = false;
        when( registerService.usernameExists(username) ).thenReturn( expectedResponse );

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
        when( registerService.usernameExists(username) ).thenReturn( expectedResponse );

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
        when( registerService.emailExists(email) ).thenReturn( expectedResponse );

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
        when( registerService.emailExists(email) ).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(get(path)
                        .param("email", email))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals( new ObjectMapper().writeValueAsString(expectedResponse), result.getResponse().getContentAsString() );
    }

    @Test
    void confirmRegistrationSuccess() throws Exception {
        String path = "/api/register/confirm";
        String token = "12_hdia-sd76gb-asdasd";
        String expectedResponse = RegisterService.ACCOUNT_ACTIVATED_RESPONSE;
        when( registerService.activateUserAccount(token) ).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(post(path)
                        .param("token", token))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals( expectedResponse, result.getResponse().getContentAsString() );
    }

    @Test
    void confirmRegistrationFail() throws Exception {
        String path = "/api/register/confirm";
        String token = "12_hdia-sd76gb-asdasd";
        when( registerService.activateUserAccount(token) ).thenThrow( new InvalidArgumentException("Wrong token") );

        mockMvc.perform(post(path)
                        .param("token", token))
                .andExpect(status().isBadRequest());
    }
}