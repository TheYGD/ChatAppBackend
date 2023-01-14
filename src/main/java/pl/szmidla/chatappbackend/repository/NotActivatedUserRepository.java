package pl.szmidla.chatappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.NotActivatedUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotActivatedUserRepository extends JpaRepository<NotActivatedUser, Long> {
    Optional<NotActivatedUser> findById(long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
        SELECT u FROM NotActivatedUser u
        WHERE u.creationDate <= :date
    """)
    List<NotActivatedUser> findAllBefore(@Param("date") LocalDateTime time24hAgo);
}
