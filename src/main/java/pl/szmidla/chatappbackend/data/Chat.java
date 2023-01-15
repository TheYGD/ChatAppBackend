package pl.szmidla.chatappbackend.data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name="user2_id")
    private User user2;

    private Long firstMessageId;

    @OneToOne
    @JoinColumn(name="last_message_id")
    private Message lastMessage;
    /** last message's date or date of creation the chat */
    private LocalDateTime lastDate;

    @OneToOne
    @JoinColumn(name = "last_message_read_by_user1_id")
    private Message lastReadByUser1;

    @OneToOne
    @JoinColumn(name = "last_message_read_by_user2_id")
    private Message lastReadByUser2;

    private boolean closed;
}
