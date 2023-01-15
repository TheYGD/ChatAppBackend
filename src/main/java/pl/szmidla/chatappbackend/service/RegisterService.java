package pl.szmidla.chatappbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.config.PropertiesConfig;
import pl.szmidla.chatappbackend.data.NotActivatedUser;
import pl.szmidla.chatappbackend.data.dto.RegisterRequest;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.other.UserActivationToken;
import pl.szmidla.chatappbackend.exception.InvalidArgumentException;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.NotActivatedUserRepository;
import pl.szmidla.chatappbackend.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class RegisterService {
    public static final String REGISTER_SUCCESS = "Successfully registered.";
    public static final String REGISTER_EMAIL_TAKEN = "This email is already registered.";
    public static final String REGISTER_USERNAME_TAKEN = "This username is already taken.";
    public static final String ACCOUNT_ACTIVATED_RESPONSE = "Account is now active.";
    public static final String REGISTER_CONFIRMATION_EMAIL_SUBJECT = "ChatApp - Confirm your registration";
    /** formatted with username and activationToken */
    public final String REGISTER_CONFIRMATION_EMAIL_TEMPLATE;
    public static final String ACCOUNT_ACTIVATED_EMAIL_SUBJECT = "ChatApp - Account activated";
    /** formatted with username */
    public final String ACCOUNT_ACTIVATED_EMAIL_TEMPLATE;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NotActivatedUserRepository notActivatedUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(PropertiesConfig propertiesConfig, EmailService emailService, UserRepository userRepository,
                           NotActivatedUserRepository notActivatedUserRepository,  PasswordEncoder passwordEncoder) {
        this.REGISTER_CONFIRMATION_EMAIL_TEMPLATE = propertiesConfig.REGISTER_CONFIRMATION_EMAIL;
        this.ACCOUNT_ACTIVATED_EMAIL_TEMPLATE = propertiesConfig.ACCOUNT_ACTIVATED_EMAIL;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.notActivatedUserRepository = notActivatedUserRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public String handleRegisterRequest(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail()) ||
                notActivatedUserRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException(REGISTER_EMAIL_TAKEN);
        }
        if (userRepository.existsByUsername(registerRequest.getUsername()) ||
                notActivatedUserRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException(REGISTER_USERNAME_TAKEN);
        }

        registerRequest.setPassword( passwordEncoder.encode(registerRequest.getPassword()) );
        NotActivatedUser user = registerRequest.toNotActivatedUser();
        notActivatedUserRepository.save(user);

        user.setActivationToken(UUID.randomUUID().toString());
        String idAndToken = user.getId().toString() + UserActivationToken.ID_TOKEN_DIVIDER_CHAR + user.getActivationToken();

        sendRegisterConfirmationEmail(user.getEmail(), user.getUsername(), idAndToken);

        return idAndToken;
    }

    private void sendRegisterConfirmationEmail(String email, String username, String idAndToken) {
        String content = REGISTER_CONFIRMATION_EMAIL_TEMPLATE.formatted(username, idAndToken);

        emailService.sendEmail(REGISTER_CONFIRMATION_EMAIL_SUBJECT, email, content);
    }

    @Transactional
    public String activateUserAccount(String idAndToken) {
        UserActivationToken activationToken = UserActivationToken.create(idAndToken);
        NotActivatedUser notActivatedUser = getNotActivatedUserById(activationToken.getId());

        if (!notActivatedUser.getActivationToken().equals(activationToken.getToken())) {
            throw new InvalidArgumentException("Wrong token");
        }

        User user = notActivatedUser.toUser();
        notActivatedUserRepository.delete(notActivatedUser);
        userRepository.save(user);

        sendAccountActivatedEmail(user);
        return ACCOUNT_ACTIVATED_RESPONSE;
    }

    private void sendAccountActivatedEmail(User user) {
        String email = user.getEmail();
        String username = user.getUsername();
        String content = ACCOUNT_ACTIVATED_EMAIL_TEMPLATE.formatted(username);

        emailService.sendEmail(ACCOUNT_ACTIVATED_EMAIL_SUBJECT, email, content);
    }

    private NotActivatedUser getNotActivatedUserById(long id) {
        return notActivatedUserRepository.findById(id)
                .orElseThrow( () -> new ItemNotFoundException("not activated user") );
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username) || notActivatedUserRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email) || notActivatedUserRepository.existsByEmail(email);
    }

}

