package pl.szmidla.chatappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.Chat;
import pl.szmidla.chatappbackend.data.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
        SELECT c FROM Chat c 
        INNER JOIN c.user1 as u1
        INNER JOIN c.user2 as u2
        WHERE u1 = :user OR u2 =:user
    """)
    Page<Chat> findAllWithUser(@Param("user") User user, Pageable pageable);

    @Query("""
        SELECT c FROM Chat c 
        INNER JOIN c.user1 as u1
        INNER JOIN c.user2 as u2
        LEFT JOIN c.lastMessage  as m
        WHERE c.id <> :id AND 
            (u1 = :user OR u2 =:user) AND
            (m is NULL OR m.date < :date)
    """)
    Page<Chat> findAllWithUserBeforeGivenDateAndExceptId(@Param("user") User user, @Param("date") LocalDateTime lastLoadedDate,
                                                              @Param("id") long lastLoadedId, Pageable pageable);
    boolean existsByUser1IdAndUser2Id(long user1Id, long user2Id);
}
