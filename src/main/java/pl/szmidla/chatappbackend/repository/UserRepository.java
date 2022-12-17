package pl.szmidla.chatappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.szmidla.chatappbackend.data.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Long, User> {

    User save(User user);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findAllContainingUsernameIgnoreCase(String phrase, Pageable pageable);
}
