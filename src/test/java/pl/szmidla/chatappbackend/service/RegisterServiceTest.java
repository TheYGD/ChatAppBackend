package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.szmidla.chatappbackend.data.dto.UserRequest;
import pl.szmidla.chatappbackend.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {
    @Spy
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    RegisterService registerService;

    private UserRequest createUserRequest(String username, String email, String password) {
        UserRequest user = new UserRequest();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void registerUserSuccess() {
        UserRequest user = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = RegisterService.REGISTER_SUCCESS;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( false );

        String actualResponse = registerService.registerUser(user);

        assertEquals(expectedResponse, actualResponse);
        verify( passwordEncoder ).encode(any());
    }

    @Test
    void registerUserEmailTaken() {
        UserRequest user = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = RegisterService.REGISTER_EMAIL_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( true );

        Throwable response = assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(user));

        assertEquals( expectedResponse, response.getMessage() );
        verify( passwordEncoder, times(0) ).encode(any());
    }

    @Test
    void registerUserUsernameTaken() {
        UserRequest user = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = RegisterService.REGISTER_USERNAME_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( true );

        Throwable response = assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(user));

        assertEquals( expectedResponse, response.getMessage() );
        verify( passwordEncoder, times(0) ).encode(any());
    }
    @Test
    void usernameExistsNo() {
        String username = "user123";
        when( userRepository.existsByUsername(username) ).thenReturn( false );

        boolean response = registerService.usernameExists(username);

        assertFalse(response);
    }

    @Test
    void usernameExistsYes() {
        String username = "user123";
        when( userRepository.existsByUsername(username) ).thenReturn( true );

        boolean response = registerService.usernameExists(username);

        assertTrue(response);
    }

    @Test
    void emailExistsNo() {
        String email = "email@email.com";
        when( userRepository.existsByEmail(email) ).thenReturn( false );

        boolean response = registerService.emailExists(email);

        assertFalse(response);
    }

    @Test
    void emailExistsYes() {
        String email = "email@email.com";
        when( userRepository.existsByEmail(email) ).thenReturn( true );

        boolean response = registerService.emailExists(email);

        assertTrue(response);
    }
}