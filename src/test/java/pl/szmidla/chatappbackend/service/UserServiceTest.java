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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserService userService;

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


    private User createUser(long id, String username, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    void getUserById() {
        User user = createUser(1L, "user", "sth@email.com", "password");
        when( userRepository.findById(user.getId()) ).thenReturn( Optional.of(user) );

        User actualUser = userService.getUserById(user.getId());

        assertEquals( user, actualUser );
    }

    @Test
    void getUserByIdException() {
        User user = createUser(1L, "user", "sth@email.com", "password");
        when( userRepository.findById(user.getId()) ).thenReturn( Optional.empty() );

        Throwable exception = assertThrows(ItemNotFoundException.class, () -> userService.getUserById(user.getId()));

        assertEquals( ItemNotFoundException.MESSAGE_TEMPLATE.formatted("User"), exception.getMessage() );
    }

    @Test
    void getUserByUsername() {
        User user = createUser(1L, "user", "sth@email.com", "password");
        when( userRepository.findByUsername(user.getUsername()) ).thenReturn( Optional.of(user) );

        User actualUser = userService.getUserByUsername(user.getUsername());

        assertEquals( user, actualUser );
    }

    @Test
    void getUserByUsernameException() {
        User user = createUser(1L, "user", "sth@email.com", "password");
        when(userRepository.findByUsername(user.getUsername())).thenReturn( Optional.empty() );

        Throwable exception = assertThrows(ItemNotFoundException.class, () -> userService.getUserByUsername(user.getUsername()));

        assertEquals( ItemNotFoundException.MESSAGE_TEMPLATE.formatted("User"), exception.getMessage() );
    }

    @Test
    void setUsersActiveStatusToActive() {
        User user = createUser(1L, "user", "sth@email.com", "password");
        user.setLastActive(LocalDateTime.now(ZoneOffset.UTC));
        when( userRepository.findByUsername( user.getUsername()) ).thenReturn( Optional.of(user) );

        userService.setUsersActiveStatus(user.getUsername(), true);

        assertNull(user.getLastActive());
        verify( userRepository ).save( user );
    }

    @Test
    void setUsersActiveStatusToInactive() {
        User user = createUser(1L, "user", "sth@email.com", "password");
        user.setLastActive(null);
        long deltaSeconds = 30;
        long epochSecondsNow = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneId.systemDefault()).toEpochSecond();
        when( userRepository.findByUsername( user.getUsername()) ).thenReturn( Optional.of(user) );

        userService.setUsersActiveStatus(user.getUsername(), false);
        long epochSecondsInMethod = user.getLastActive().atZone(ZoneId.systemDefault()).toEpochSecond();

        assertEquals( epochSecondsNow, epochSecondsInMethod, deltaSeconds );
        verify( userRepository ).save( user );
    }

    @Test
    void getUsersActiveStatuses() {
        List<User> userList = List.of(
                createUser(1L, "user1", "sth@email.com", "password"),
                createUser(2L, "user2", "sth@email2.com", "password")
        );
        userList.get(1).setLastActive(LocalDateTime.now(ZoneOffset.UTC));
        when( userRepository.findById(1L) ).thenReturn( Optional.of(userList.get(0) ));
        when( userRepository.findById(2L) ).thenReturn( Optional.of(userList.get(1) ));

        Map<Long, String> responseMap = userService.getUsersActiveStatuses(userList
                .stream().map(user -> user.getId()).toList());

        for (User user : userList) {
            String activeStatus = responseMap.get( user.getId() );
            assertEquals( user.getLastActive() != null ? user.getLastActive().toString() : null, activeStatus );
        }
    }
}