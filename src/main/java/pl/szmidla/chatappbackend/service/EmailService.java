package pl.szmidla.chatappbackend.service;

public interface EmailService {
    boolean sendEmail(String subject, String receiver, String body);
}
