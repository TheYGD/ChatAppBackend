package pl.szmidla.chatappbackend.data.dto;

import lombok.*;
import pl.szmidla.chatappbackend.data.Message;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.utils.DateConverter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MessageResponse {
    private String content;
    private String date;
    private boolean sent;

    public static MessageResponse fromMessage(Message message, User user) {
        boolean sentByUser = message.isByUser1() && message.getChat().getUser1().equals(user) ||
                !message.isByUser1() && message.getChat().getUser2().equals(user);
        String shortDateString = DateConverter.LocalDateTimeToShortString( message.getDate() );
        return MessageResponse.builder()
                .content(message.getContent())
                .date(shortDateString)
                .sent( sentByUser )
                .build();
    }
}