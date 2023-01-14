package pl.szmidla.chatappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.NotActivatedUser;

import java.util.Optional;

@Repository
public interface NotActivatedUserRepository extends JpaRepository<NotActivatedUser, Long> {
    Optional<NotActivatedUser> findById(long id);
}
