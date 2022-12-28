package pl.szmidla.chatappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByChatOrderByIdAsc(Chat chat, Pageable pageable);

    long countByChat(Chat chat);
}
