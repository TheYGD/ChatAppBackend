package pl.szmidla.chatappbackend.data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Chat extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name="user2_id")
    private User user2;

    private boolean closed;
}
