package pl.szmidla.chatappbackend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameSuccess() {
        String usernameOrEmail = "asdjasd";
        User expectedUser = new User();
        when( userRepository.findByUsernameOrEmail(anyString()) ).thenReturn( Optional.of(expectedUser) );

        User actualUser = userDetailsService.loadUserByUsername(usernameOrEmail);

        assertEquals( expectedUser, actualUser );
    }

    @Test
    void loadUserByUsernameFail() {
        String usernameOrEmail = "asdjasd";
        when( userRepository.findByUsernameOrEmail(anyString()) ).thenReturn( Optional.empty() );

        assertThrows( ItemNotFoundException.class, () -> userDetailsService.loadUserByUsername(usernameOrEmail));
    }
}