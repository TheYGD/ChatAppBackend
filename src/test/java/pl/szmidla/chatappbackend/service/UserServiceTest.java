package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Spy
    PasswordEncoder passwordEncoder;
    @InjectMocks
    UserService userService;

    @Test
    void registerUserSuccess() {
        UserRequest userRequest = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = UserService.REGISTER_EMAIL_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( true );

        String actualResponse = userService.registerUser(userRequest);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void registerUserEmailTaken() {
        UserRequest userRequest = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = UserService.REGISTER_USERNAME_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( true );

        String actualResponse = userService.registerUser(userRequest);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void registerUserUsernameTaken() {
        UserRequest userRequest = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = UserService.REGISTER_SUCCESS;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( false );

        String actualResponse = userService.registerUser(userRequest);

        assertEquals(expectedResponse, actualResponse);
        verify( passwordEncoder ).encode(anyString());
    }

    @Test
    void getNUsersByPhraseSuccess() {
        String phrase = "ASJD";
        int pageNr = 2;
        int pageSize = 1;
        Page expectedPage = Page.empty();
        when( userRepository.findAllContainingUsernameIgnoreCase(anyString(), any()) ).thenReturn( expectedPage );

        Page actualPage = userService.getNUsersByPhrase(phrase, pageNr, pageSize);

        assertEquals( expectedPage, actualPage );
    }

    @Test
    void getNUsersByPhrasePageNrLessThan0() {
        String phrase = "ASJD";
        int pageNr = -2;
        int pageSize = 1;
        assertThrows( IllegalArgumentException.class, () -> userService.getNUsersByPhrase(phrase, pageNr, pageSize) );
    }


    private UserRequest createUserRequest(String username, String email, String password) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        return userRequest;
    }
}