package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.szmidla.chatappbackend.config.PropertiesConfig;
import pl.szmidla.chatappbackend.data.dto.RegisterRequest;
import pl.szmidla.chatappbackend.data.NotActivatedUser;
import pl.szmidla.chatappbackend.exception.InvalidArgumentException;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.NotActivatedUserRepository;
import pl.szmidla.chatappbackend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {
    @Mock
    EmailService emailService;
    @Mock
    UserRepository userRepository;
    @Mock
    NotActivatedUserRepository notActivatedUserRepository;
    @Spy
    PasswordEncoder passwordEncoder;
    RegisterService registerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PropertiesConfig propertiesConfig = Mockito.mock( PropertiesConfig.class );
        propertiesConfig.ACCOUNT_ACTIVATED_EMAIL = "ACCOUNT_ACTIVATED_EMAIL";
        propertiesConfig.REGISTER_CONFIRMATION_EMAIL = "REGISTER_CONFIRMATION_EMAIL";
        registerService = new RegisterService(propertiesConfig, emailService, userRepository, notActivatedUserRepository,
                passwordEncoder);
    }


    private RegisterRequest createUserRequest(String username, String email, String password) {
        RegisterRequest user = new RegisterRequest();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void registerUserSuccess() {
        RegisterRequest user = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = RegisterService.REGISTER_SUCCESS;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( false );

        String actualResponse = registerService.handleRegisterRequest(user);

        assertEquals(expectedResponse, actualResponse);
        verify( passwordEncoder ).encode( any() );
        verify( notActivatedUserRepository ).save( any() );
        verify( emailService ).sendEmail( anyString(), anyString(), anyString() );
    }

    @Test
    void registerUserEmailTaken() {
        RegisterRequest user = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = RegisterService.REGISTER_EMAIL_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( true );

        Throwable response = assertThrows(IllegalArgumentException.class, () -> registerService.handleRegisterRequest(user));

        assertEquals( expectedResponse, response.getMessage() );
        verify( passwordEncoder, times(0) ).encode( any() );
        verify( notActivatedUserRepository, times(0) ).save( any() );
        verify( emailService, times(0) ).sendEmail( anyString(), anyString(), anyString() );
    }

    @Test
    void registerUserUsernameTaken() {
        RegisterRequest user = createUserRequest("username1", "email@em.pl", "passW0rd");
        String expectedResponse = RegisterService.REGISTER_USERNAME_TAKEN;
        when( userRepository.existsByEmail(anyString()) ).thenReturn( false );
        when( userRepository.existsByUsername(anyString()) ).thenReturn( true );

        Throwable response = assertThrows(IllegalArgumentException.class, () -> registerService.handleRegisterRequest(user));

        assertEquals( expectedResponse, response.getMessage() );
        verify( passwordEncoder, times(0) ).encode( any() );
        verify( notActivatedUserRepository, times(0) ).save( any() );
        verify( emailService, times(0) ).sendEmail( anyString(), anyString(), anyString() );
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

    @Test
    void activateUserAccountSuccess() {
        long id = 12L;
        String token = "asdsa-123fdads-123fda";
        String idAndToken = id + "_" + token;
        NotActivatedUser notActivatedUser = createNotActivatedUser(id, token, "username", "em@ai.l",
                "password");
        when( notActivatedUserRepository.findById(id) ).thenReturn( Optional.of(notActivatedUser) );

        String response = registerService.activateUserAccount(idAndToken);

        assertEquals( RegisterService.ACCOUNT_ACTIVATED_RESPONSE, response );
        verify( notActivatedUserRepository ).delete( any() );
        verify( userRepository ).save( any() );
        verify( emailService ).sendEmail( anyString(), anyString(), anyString() );
    }

    @Test
    void activateUserAccountWrongToken() {
        long id = 12L;
        String methodToken = "diff-token";
        String usersToken = "asdsa-123fdads-123fda";
        String idAndToken = id + "_" + methodToken;
        NotActivatedUser notActivatedUser = createNotActivatedUser(id, usersToken, "username", "em@ai.l",
                "password");
        when( notActivatedUserRepository.findById(id) ).thenReturn( Optional.of(notActivatedUser) );

        assertThrows( InvalidArgumentException.class, () -> registerService.activateUserAccount(idAndToken) );

        verify( notActivatedUserRepository, times(0) ).delete( any() );
        verify( userRepository, times(0) ).save( any() );
        verify( emailService, times(0) ).sendEmail( anyString(), anyString(), anyString() );
    }

    @Test
    void activateUserAccountLinkExpired() {
        long id = 12L;
        String token = "asdsa-123fdads-123fda";
        String idAndToken = id + "_" + token;
        when( notActivatedUserRepository.findById(id) ).thenReturn( Optional.empty() );

        assertThrows( ItemNotFoundException.class, () -> registerService.activateUserAccount(idAndToken) );

        verify( notActivatedUserRepository, times(0) ).delete( any() );
        verify( userRepository, times(0) ).save( any() );
        verify( emailService, times(0) ).sendEmail( anyString(), anyString(), anyString() );
    }

    private NotActivatedUser createNotActivatedUser(long id, String token, String username, String email,
                                                    String password) {
        NotActivatedUser user = NotActivatedUser.builder()
                .activationToken(token)
                .username(username)
                .email(email)
                .password(password)
                .build();
        user.setId(id);
        return user;
    }
}