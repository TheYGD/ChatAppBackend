package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ChatPreview;
import pl.szmidla.chatappbackend.data.dto.MessageResponse;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.ChatRepository;
import pl.szmidla.chatappbackend.repository.MessageRepository;

import javax.print.attribute.standard.PageRanges;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ChatService {

    private UserService userService;
    private ChatRepository chatRepository;
    private MessageRepository messageRepository;

    public Chat getChatById(long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Chat id={} not found", id);
                    return new ItemNotFoundException("chat");
                });
    }

    public Chat getChatByIdForUser(long id, User user) {
        Chat chat = getChatById(id);
        if (!chat.getUser1().equals(user) && !chat.getUser2().equals(user)) {
            log.error("Forbidden access to chat. ChatService.getChatByIdForUser({}, {})", id, user.getId());
            throw new IllegalArgumentException("Forbidden");
        }
        return chat;
    }

    public List<ChatPreview> getUsersNChatPreviews(User user, long lastChatId, String lastChatDateString, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize);
        List<Chat> chatPage;

        // is this 1 page without previous data?
        if (lastChatId == -1) {
            chatPage = chatRepository.findAllWithUser(user, pageable);
        }
        else {
            LocalDateTime lastChatDate = LocalDateTime.parse(lastChatDateString); // usunac Z z konca stringa w js
            chatPage = chatRepository.findAllWithUserBeforeGivenDateAndExceptId(user, lastChatDate, lastChatId, pageable);
        }
        return chatPage.stream().map(chat -> ChatPreview.fromChat(chat, user)).toList();
    }

    @Transactional
    public Chat createChat(User thisUser, long otherUserId) {
        long thisUserId = thisUser.getId();
        if (thisUserId == otherUserId) {
            log.error("Invalid arguments in ChatService.createChat({}, {}), u1==u2", thisUser.getId(), otherUserId);
            throw new IllegalArgumentException("Invalid operation");
        }

        User otherUser = userService.getUserById(otherUserId);
        boolean chatAlreadyExists = chatRepository.existsByUser1IdAndUser2Id(thisUserId, otherUserId) ||
                chatRepository.existsByUser1IdAndUser2Id(otherUserId, thisUserId);

        if (chatAlreadyExists) {
            log.error("Invalid arguments in ChatService.createChat({}, {}), alreadyExists", thisUser.getId(), otherUserId);
            throw new IllegalArgumentException("Invalid operation");
        }
        Chat chat = Chat.builder()
                .user1(thisUser)
                .user2(otherUser)
                .lastMessage(null)
                .firstMessageId(null)
                .lastDate(LocalDateTime.now())
                .closed(false).build();
        chatRepository.save(chat);

        // todo WEBSOCKET ********************

        return chat;
    }

    /** @param lastMessageId use negative number to have it calculated */
    public List<MessageResponse> getNMessagesFromChat(User user, long chatId, long lastMessageId, int pageSize) {
        Chat chat = getChatByIdForUser(chatId, user);
        List<Message> lastNMessages;
        Pageable pageable = PageRequest.of(0, pageSize);

        // we have to return most recent ones
        if (lastMessageId < 0) {
            lastNMessages = messageRepository.findLastNMessagesFromChat(chat.getId(), pageable);
        }
        else {
            lastNMessages = messageRepository.findLastNMessagesFromChatAfterId(chat.getId(), lastMessageId, pageable);
        }
        Collections.reverse(lastNMessages);
        return lastNMessages.stream().map( message -> MessageResponse.fromMessage(message, user) ).toList();
    }

    @Transactional
    public void sendMessage(User sender, long chatId, String content) {
        Chat chat = getChatByIdForUser(chatId, sender);
        Message message = createMessage(chat, sender, content);
        messageRepository.save(message);

        if (chat.getFirstMessageId() == null) {
            chat.setFirstMessageId(message.getId());
        }

        chat.setLastMessage(message.getContent());
        chat.setLastDate(message.getDate());
        chatRepository.save(chat);

        // todo WEBSOCKET ********************
    }

    private Message createMessage(Chat chat, User sender, String content) {
        return Message.builder()
                .date(LocalDateTime.now())
                .content(content)
                .byUser1( chat.getUser1().equals(sender) )
                .chat(chat).build();
    }
}
