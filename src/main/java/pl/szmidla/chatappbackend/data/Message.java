package pl.szmidla.chatappbackend.data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Message extends BaseEntity {

    private LocalDateTime date;
    private String content;
    private boolean byUser1;

    @ManyToOne
    @JoinColumn(name="chat_id")
    private Chat chat;
}
