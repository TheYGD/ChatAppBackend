package pl.szmidla.chatappbackend.data;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    private LocalDateTime date;
    private String content;
    private boolean byUser1;
    private boolean isText;

    @ManyToOne
    @JoinColumn(name="chat_id")
    private Chat chat;
}
