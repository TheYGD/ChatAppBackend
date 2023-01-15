package pl.szmidla.chatappbackend.api;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ChatPreview;
import pl.szmidla.chatappbackend.data.dto.MessageResponse;
import pl.szmidla.chatappbackend.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@AllArgsConstructor
public class ChatApi {

    public static int CHATS_PAGE_SIZE = 10;
    public static int MESSAGES_PAGE_SIZE = 15;

    private ChatService chatService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ChatPreview> getUsersNChatPreviews(@RequestParam(value="lastId", defaultValue="-1") long lastChatId,
                                                   @RequestParam(value = "lastDate", required = false) String lastChatDateString) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getUsersNChatPreviews(user, lastChatId, lastChatDateString, CHATS_PAGE_SIZE);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createChat(@RequestParam("id") long userId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Chat createdChat = chatService.createChat(user, userId);
        return createdChat.getId();
    }

    @GetMapping(value = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MessageResponse> getNMessagesFromChat(@PathVariable("id") long chatId,
                                                      @RequestParam(defaultValue = "-1") long lastMessageId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MessageResponse> messages = chatService.getNMessagesFromChat(user, chatId, lastMessageId, MESSAGES_PAGE_SIZE);
        return messages;
    }

    @PostMapping(value = "/{id}/messages")
    public void sendMessage(@PathVariable("id") long chatId,
                               @RequestParam String content) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        chatService.sendTextMessage(user, chatId, content);
    }

    @PostMapping(value = "/{id}/files", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendFiles(@PathVariable("id") long chatId,
                               @RequestParam("file") List<MultipartFile> files) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        chatService.sendFiles(user, chatId, files);
    }

    @PostMapping(value = "/{id}/message-read")
    public void messageRead(@PathVariable("id") long chatId,
                               @RequestParam long messageId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        chatService.messageRead(user, chatId, messageId);
    }
}
