package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.szmidla.chatappbackend.aws.AWSFileService;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Spy
    PasswordEncoder passwordEncoder;
    @Mock
    AWSFileService fileService;
    @InjectMocks
    UserService userService;

    @Test
    void registerUserSuccess() {
        UserRequest user = createUser("username1", "email@em.pl", "passW0rd");
        String expectedResponse = UserService.REGISTER_SUCCESS;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( false );

        String actualResponse = userService.registerUser(user);

        assertEquals(expectedResponse, actualResponse);
        verify( passwordEncoder ).encode(any());
    }

    @Test
    void registerUserEmailTaken() {
        UserRequest user = createUser("username1", "email@em.pl", "passW0rd");
        String expectedResponse = UserService.REGISTER_EMAIL_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( true );

        Throwable response = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        assertEquals( expectedResponse, response.getMessage() );
        verify( passwordEncoder, times(0) ).encode(any());
    }

    @Test
    void registerUserUsernameTaken() {
        UserRequest user = createUser("username1", "email@em.pl", "passW0rd");
        String expectedResponse = UserService.REGISTER_USERNAME_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( true );

        Throwable response = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        assertEquals( expectedResponse, response.getMessage() );
        verify( passwordEncoder, times(0) ).encode(any());
    }

    @Test
    void getNUsersByPhraseSuccess() {
        String phrase = "ASJD";
        int pageNr = 2;
        int pageSize = 1;
        Page expectedPage = Page.empty();
        when( userRepository.findAllByUsernameContainingIgnoreCase(anyString(), any()) ).thenReturn( expectedPage );

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

    private UserRequest createUser(String username, String email, String password) {
        UserRequest user = new UserRequest();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void usernameExistsNo() {
        String username = "user123";
        when( userRepository.existsByUsername(username) ).thenReturn( false );

        boolean response = userService.usernameExists(username);

        assertFalse(response);
    }

    @Test
    void usernameExistsYes() {
        String username = "user123";
        when( userRepository.existsByUsername(username) ).thenReturn( true );

        boolean response = userService.usernameExists(username);

        assertTrue(response);
    }

    @Test
    void emailExistsNo() {
        String email = "email@email.com";
        when( userRepository.existsByEmail(email) ).thenReturn( false );

        boolean response = userService.emailExists(email);

        assertFalse(response);
    }

    @Test
    void emailExistsYes() {
        String email = "email@email.com";
        when( userRepository.existsByEmail(email) ).thenReturn( true );

        boolean response = userService.emailExists(email);

        assertTrue(response);
    }

    @Test
    void getUserById() {
        User user = createUser("user", "sth@email.com", "password").toUser();
        user.setId(1L);
        when( userRepository.findById(user.getId()) ).thenReturn( Optional.of(user) );

        User actualUser = userService.getUserById(user.getId());

        assertEquals( user, actualUser );
    }

    @Test
    void getUserByIdException() {
        User user = createUser("user", "sth@email.com", "password").toUser();
        user.setId(1L);
        when( userRepository.findById(user.getId()) ).thenThrow( new ItemNotFoundException() );

        assertThrows(ItemNotFoundException.class, () -> userService.getUserById(user.getId()));
    }

    @Test
    void getUserByUsername() {
        User user = createUser("user", "sth@email.com", "password").toUser();
        when( userRepository.findByUsername(user.getUsername()) ).thenReturn( Optional.of(user) );

        User actualUser = userService.getUserByUsername(user.getUsername());

        assertEquals( user, actualUser );
    }

    @Test
    void getUserByUsernameException() {
        User user = createUser("user", "sth@email.com", "password").toUser();
        user.setId(1L);
        when(userRepository.findByUsername(user.getUsername())).thenThrow(new ItemNotFoundException());

        assertThrows(ItemNotFoundException.class, () -> userService.getUserByUsername(user.getUsername()));
    }
}