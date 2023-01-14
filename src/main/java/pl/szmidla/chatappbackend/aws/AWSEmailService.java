package pl.szmidla.chatappbackend.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.service.EmailService;

@Profile("aws")
@Service
@Slf4j
public class AWSEmailService implements EmailService {

    @Override
    public boolean sendEmail(String subject, String receiver, String body) {
        return false;
    }
}
