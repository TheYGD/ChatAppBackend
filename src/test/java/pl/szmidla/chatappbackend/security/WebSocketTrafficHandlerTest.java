package pl.szmidla.chatappbackend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.szmidla.chatappbackend.service.UserService;

import java.security.Principal;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketTrafficHandlerTest {

    @Mock
    UserService userService;
    @Mock
    private SimpUserRegistry simpUserRegistry;
    @InjectMocks
    WebSocketTrafficHandler webSocketTrafficHandler;

    @Test
    void websocketConnected() {
        Principal user = () -> "name";
        SessionConnectEvent event = Mockito.spy( createSillyConnectEvent() );
        when( event.getUser() ).thenReturn( user );

        webSocketTrafficHandler.websocketConnected(event);

        verify( userService ).setUsersActiveStatus(user.getName(), true);
    }

    private static SessionConnectEvent createSillyConnectEvent() {
        return new SessionConnectEvent(new Object(), new GenericMessage(""));
    }

    @Test
    void websocketDisconnectedLastSession() {
        Principal user = () -> "name";
        SessionDisconnectEvent event = Mockito.spy(createSillyDisconnectEvent());
        when( event.getUser() ).thenReturn( user );
        when( simpUserRegistry.getUsers() ).thenReturn( Set.of() );

        webSocketTrafficHandler.websocketDisconnected(event);

        verify( userService ).setUsersActiveStatus( user.getName(), false );
    }

    @Test
    void websocketDisconnectedNotLastSession() {
        SimpUser user = createSimpUser("name");
        SessionDisconnectEvent event = Mockito.spy(createSillyDisconnectEvent());
        when( event.getUser() ).thenReturn( user.getPrincipal() );
        Set<SimpUser> users = Set.of( user );
        when( simpUserRegistry.getUsers() ).thenReturn( users );

        webSocketTrafficHandler.websocketDisconnected(event);

        verify( userService, times(0) ).setUsersActiveStatus( anyString(), anyBoolean() );
    }

    private static SessionDisconnectEvent createSillyDisconnectEvent() {
        return new SessionDisconnectEvent(new Object(), new GenericMessage(""), "someId", null);
    }

    private SimpUser createSimpUser(String name) {
        return new SimpUser() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Principal getPrincipal() {
                return () -> name;
            }

            @Override
            public boolean hasSessions() {
                return false;
            }

            @Override
            public SimpSession getSession(String s) {
                return null;
            }

            @Override
            public Set<SimpSession> getSessions() {
                return null;
            }
        };
    }
}