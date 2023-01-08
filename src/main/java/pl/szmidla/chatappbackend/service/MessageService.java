package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.MessageRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MessageService {

    private MessageRepository messageRepository;

    public Message getMessageById(long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Message with id={} not found", id);
                    return new ItemNotFoundException("message");
                });
    }

    public Message getMessageByIdWithinChat(long messageId, Chat chat) {
        Message message = getMessageById(messageId);
        if (!message.getChat().equals(chat)) {
            log.error("Message with id={} is not in chat with id={}", messageId, chat.getId());
            throw new IllegalArgumentException("Message now within a chat");
        }
        return message;
    }

    public List<Message> getLastNMessagesFromChat(long chatId, Pageable pageable) {
        return messageRepository.findLastNMessagesFromChat(chatId, pageable);
    }

    public List<Message> getLastNMessagesFromChatAfterId(long chatId, long lastMessageId, Pageable pageable) {
        return messageRepository.findLastNMessagesFromChatAfterId(chatId, lastMessageId, pageable);
    }

    public void saveMessage(Message message) {
        messageRepository.save(message);
    }
}
