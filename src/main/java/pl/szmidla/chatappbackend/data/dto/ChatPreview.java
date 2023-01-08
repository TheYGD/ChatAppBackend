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
    private long lastReadMessageIdByThis;
    private long lastReadMessageIdByOther;
    private String lastInteractionDate; // date of last message or creation time    LocalDateTime.toString()
    private String lastActiveDate; // date of last activity / null if active now

    public static ChatPreview fromChat(Chat chat, User logged) {
        User userToBeInChat = chat.getUser1().equals(logged) ? chat.getUser2() : chat.getUser1();
        String messageContent = chat.getLastMessage();
        String lastActiveDateString = userToBeInChat.getLastActive() != null ?
                userToBeInChat.getLastActive().toString() : null;
        long lastReadMessageIdByThis;
        long lastReadMessageIdByOther;
        if (logged.equals(chat.getUser1())) {
            lastReadMessageIdByThis = chat.getLastReadByUser1() == null ? 0 : chat.getLastReadByUser1().getId();
            lastReadMessageIdByOther = chat.getLastReadByUser2() == null ? 0 : chat.getLastReadByUser2().getId();
        }
        else {
            lastReadMessageIdByThis = chat.getLastReadByUser2() == null ? 0 : chat.getLastReadByUser2().getId();
            lastReadMessageIdByOther = chat.getLastReadByUser1() == null ? 0 : chat.getLastReadByUser1().getId();
        }

        return new ChatPreview(
                chat.getId(),
                userToBeInChat.getId(),
                userToBeInChat.getUsername(),
                userToBeInChat.getImageUrl(),
                chat.getFirstMessageId(),
                messageContent,
                lastReadMessageIdByThis,
                lastReadMessageIdByOther,
                chat.getLastDate().toString(),
                lastActiveDateString
        );
    }
}
