package pl.szmidla.chatappbackend.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import pl.szmidla.chatappbackend.security.jwt.JWTExtractor;
import pl.szmidla.chatappbackend.security.UserDetailsServiceImpl;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketBrokerConfig implements
        WebSocketMessageBrokerConfigurer {

    private JWTExtractor jwtExtractor;
    private UserDetailsServiceImpl userDetailsService;
    private final String FRONTEND_ORIGIN;

    public WebSocketBrokerConfig(JWTExtractor jwtExtractor, UserDetailsServiceImpl userDetailsService,
                                 PropertiesConfig propertiesConfig) {
        this.jwtExtractor = jwtExtractor;
        this.userDetailsService = userDetailsService;
        this.FRONTEND_ORIGIN = propertiesConfig.FRONTEND_ORIGIN;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chats").setAllowedOrigins(FRONTEND_ORIGIN)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null &&
                        (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                                StompCommand.SEND.equals(accessor.getCommand()))) {
                    String jwtHeaderValue = accessor.getFirstNativeHeader("jwt");
                    if (jwtHeaderValue == null) {
                        log.error("WebSocket inbound: no jwt header");
                        throw new IllegalArgumentException("no jwt header");
                    }
                    String username = jwtExtractor.getSubject(jwtHeaderValue);
                    userDetailsService.loadUserByUsername(username); // makes sure user exists
                    accessor.setUser( () -> username ); // anonymous Principal
                }
                else if (accessor == null) {
                    throw new IllegalArgumentException();
                }
                return message;
            }
        });
    }
}