package pl.szmidla.chatappbackend.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class MockEmailService implements EmailService {

    @Override
    public void sendEmail(String subject, String receiver, String body) {
        // do nothing
    }
}
