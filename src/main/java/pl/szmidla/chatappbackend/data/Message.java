package pl.szmidla.chatappbackend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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
