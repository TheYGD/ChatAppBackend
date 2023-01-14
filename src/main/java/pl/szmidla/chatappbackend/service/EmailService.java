package pl.szmidla.chatappbackend.service;

public interface EmailService {
    void sendEmail(String subject, String receiver, String body);
}
