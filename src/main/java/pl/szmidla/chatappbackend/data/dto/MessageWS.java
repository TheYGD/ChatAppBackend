package pl.szmidla.chatappbackend.data.dto;

import lombok.*;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.utils.DateConverter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageWS {

    private ChatPreview chat;
    private MessageResponse message;

    public static MessageWS fromMessage(Message message, User user) {
        return new MessageWS( ChatPreview.fromChat(message.getChat(), user), MessageResponse.fromMessage(message, user) );
    }

    public static MessageWS fromChat(Chat chat, User user) {
        return new MessageWS( ChatPreview.fromChat(chat, user), null );
    }
}
