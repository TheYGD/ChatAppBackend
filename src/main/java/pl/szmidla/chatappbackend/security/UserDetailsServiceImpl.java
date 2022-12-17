package pl.szmidla.chatappbackend.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.exception.ItemNotFoundException;
import pl.szmidla.chatappbackend.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * actually load users by username or email
     */
    @Override
    public User loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow( () -> new ItemNotFoundException("user") );
    }
}
