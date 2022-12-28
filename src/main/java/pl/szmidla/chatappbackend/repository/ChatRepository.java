package pl.szmidla.chatappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.Chat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Page<Chat> findAll(Pageable pageable);
    Page<Chat> findAllByIdNotAndLastMessageDateBefore(Pageable pageable, long id, LocalDateTime date);
    boolean existsByUser1IdOrUser2Id(long user1Id, long user2Id);
}
