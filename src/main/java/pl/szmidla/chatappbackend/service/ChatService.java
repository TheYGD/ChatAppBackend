package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ChatPreview;
import pl.szmidla.chatappbackend.data.dto.MessageResponse;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.ChatRepository;
import pl.szmidla.chatappbackend.repository.MessageRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

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

    public Page<ChatPreview> getUsersNChatPreviews(User user, long lastChatId, String lastChatDateString, int pageSize) {
        Sort sortBy = Sort.by("lastMessage.date").descending();
        Pageable pageable = PageRequest.of(0, pageSize, sortBy);
        Page<Chat> chatPage;

        // is this 1 page without previous data?
        if (lastChatId == -1) {
            chatPage = chatRepository.findAllWithUser(user, pageable);
        }
        else {
            LocalDateTime lastChatDate = LocalDateTime.parse(lastChatDateString); // usunac Z z konca stringa w js
            chatPage = chatRepository.findAllWithUserBeforeGivenDateAndExceptId(user, lastChatDate, lastChatId, pageable);
        }
        return chatPage.map(chat -> ChatPreview.fromChat(chat, user));
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
                .closed(false).build();
        chatRepository.save(chat);

        // todo WEBSOCKET ********************

        return chat;
    }

    /** @param pageNr use negative number to have it calculated */
    public Page<MessageResponse> getNMessagesFromChat(User user, long chatId, int pageNr, int pageSize) {
        Chat chat = getChatByIdForUser(chatId, user);
        // we have to calculate the right pageNr
        if (pageNr < 0) {
            long pageCount = messageRepository.countByChat(chat);
            pageNr = (int) Math.ceil((float) pageCount / pageSize) - 1;
        }

        Pageable pageable = PageRequest.of(pageNr, pageSize);
        Page<Message> messages = messageRepository.findAllByChatOrderByIdAsc(chat, pageable);

        return messages.map( message -> MessageResponse.fromMessage(message, user) );
    }

    @Transactional
    public void sendMessage(User sender, long chatId, String content) {
        Chat chat = getChatByIdForUser(chatId, sender);
        Message message = createMessage(chat, sender, content);
        messageRepository.save(message);

        chat.setLastMessage(message);
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
