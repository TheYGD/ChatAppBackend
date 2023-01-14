package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.szmidla.chatappbackend.data.NotActivatedUser;
import pl.szmidla.chatappbackend.repository.NotActivatedUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterScheduledServiceTest {

    @Mock
    NotActivatedUserRepository notActivatedUserRepository;
    @InjectMocks
    RegisterScheduledService registerScheduledService;

    @Test
    void removeNotActivatedAccountsSchedule() {
        NotActivatedUser notActivatedUser = createNotActivatedUser(1L, "token", "user",
                "email@asd.com", "pass");
        List<NotActivatedUser> notActivatedUserList = List.of( notActivatedUser );
        when( notActivatedUserRepository.findAllBefore(any()) ).thenReturn( notActivatedUserList );

        registerScheduledService.removeNotActivatedAccountsSchedule();

        verify( notActivatedUserRepository ).deleteAll( notActivatedUserList );
    }

    private NotActivatedUser createNotActivatedUser(long id, String token, String username, String email,
                                                    String password) {
        NotActivatedUser user = NotActivatedUser.builder()
                .activationToken(token)
                .creationDate(LocalDateTime.now(ZoneOffset.UTC))
                .username(username)
                .email(email)
                .password(password)
                .build();
        user.setId(id);
        return user;
    }
}