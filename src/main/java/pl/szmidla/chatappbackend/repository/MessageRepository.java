package pl.szmidla.chatappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        LEFT JOIN m.chat as c
        WHERE c.id = :id AND 
              m.id < :lastMessageId
        ORDER BY m.date DESC
    """)
    List<Message> findLastNMessagesFromChatAfterId(@Param("id") long chatId, @Param("lastMessageId") long lastMessageId, Pageable pageable);

    @Query("""
        SELECT m FROM Message m
        LEFT JOIN m.chat as c
        WHERE c.id = :id
        ORDER BY m.date DESC
    """)
    List<Message> findLastNMessagesFromChat(Long id, Pageable pageable);
}
