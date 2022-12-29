package pl.szmidla.chatappbackend.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.utils.DateConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**  This class represents the chat previews on Chat page */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatPreview {

    private Long id;
    private Long usersId;
    private String usersName;
    // private String usersImageUrl
    private String message; // last message
    private String date; // date of last message, date + time without nanoseconds

    public static ChatPreview fromChat(Chat chat, User receiver) {
        User toBeInChatUser = chat.getUser1().equals(receiver) ? chat.getUser2() : chat.getUser1();
        String messageContent = chat.getLastMessage() != null ? chat.getLastMessage().getContent() : null;
        String shortDateString = chat.getLastMessage() != null ?
                DateConverter.LocalDateTimeToShortString( chat.getLastMessage().getDate() ) : null;
        return new ChatPreview(
                chat.getId(),
                toBeInChatUser.getId(),
                toBeInChatUser.getUsername(),
                messageContent,
                shortDateString
        );
    }
}
