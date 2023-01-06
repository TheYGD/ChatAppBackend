package pl.szmidla.chatappbackend.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.User;

/**  This class represents the chat previews on Chat page */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatPreview {

    private Long id;
    private Long usersId;
    private String usersName;
    private String usersImageUrl;
    private Long firstMessageId;
    private String message; // last message
    private String lastInteractionDate; // date of last message or creation time    LocalDateTime.toString()
    private String lastActiveDate; // date of last activity / null if active now

    public static ChatPreview fromChat(Chat chat, User receiver) {
        User userToBeInChat = chat.getUser1().equals(receiver) ? chat.getUser2() : chat.getUser1();
        String messageContent = chat.getLastMessage();
        String lastActiveDateString = userToBeInChat.getLastActive() != null ?
                userToBeInChat.getLastActive().toString() : null;
        return new ChatPreview(
                chat.getId(),
                userToBeInChat.getId(),
                userToBeInChat.getUsername(),
                userToBeInChat.getImageUrl(),
                chat.getFirstMessageId(),
                messageContent,
                chat.getLastDate().toString(),
                lastActiveDateString
        );
    }
}
