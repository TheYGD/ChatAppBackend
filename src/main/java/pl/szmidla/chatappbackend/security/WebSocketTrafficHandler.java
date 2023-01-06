package pl.szmidla.chatappbackend.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import pl.szmidla.chatappbackend.service.UserService;

@Component
@AllArgsConstructor
@Slf4j
public class WebSocketTrafficHandler {

    private UserService userService;
    private SimpUserRegistry simpUserRegistry;


    @EventListener(SessionConnectEvent.class)
    public void websocketConnected(SessionConnectEvent event) {
        String username = event.getUser().getName();
        log.info("Established WebSocket connection, username={}", username);
        userService.setUsersActiveStatus(username, true);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void websocketDisconnected(SessionDisconnectEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : "{{anonymous}}";
        log.info("Closed WebSocket connection, username={}", username);

        // make sure it was the only active session
        if (event.getUser() != null && noActiveSessionsForUser(username)) {
            log.info("Set users status to inactive, username={}", username);
            userService.setUsersActiveStatus(username, false);
        }
    }

    private boolean noActiveSessionsForUser(String username) {
        return simpUserRegistry.getUsers().stream().noneMatch(user -> user.getName().equals(username));
    }
}

