package pl.szmidla.chatappbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesConfig {

    @Value("${chat-app.security.jwt.secret}")
    public String JWT_SECRET;

    @Value("${chat-app.security.jwt.expiration-hours}")
    public int JWT_EXPIRATION_HOURS;
}
