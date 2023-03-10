package pl.szmidla.chatappbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/register-templates.properties")
public class PropertiesConfig {

    @Value("${chat-app.security.jwt.secret}")
    public String JWT_SECRET;

    @Value("${chat-app.security.jwt.expiration-hours}")
    public int JWT_EXPIRATION_HOURS;

    @Value("${aws.frontend.origin}")
    public String FRONTEND_ORIGIN;

    @Value("${chat-app.register.templates.register-confirmation}")
    public String REGISTER_CONFIRMATION_EMAIL;

    @Value("${chat-app.register.templates.account-activated}")
    public String ACCOUNT_ACTIVATED_EMAIL;
}
