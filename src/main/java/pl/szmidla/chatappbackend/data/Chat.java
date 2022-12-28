package pl.szmidla.chatappbackend.data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.*;

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

    @OneToOne
    private Message lastMessage;

    private boolean closed;
}
