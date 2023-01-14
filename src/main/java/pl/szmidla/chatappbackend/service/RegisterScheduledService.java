package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.data.NotActivatedUser;
import pl.szmidla.chatappbackend.repository.NotActivatedUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@AllArgsConstructor
public class RegisterScheduledService {

    private final NotActivatedUserRepository notActivatedUserRepository;


    /** removing account which haven't been activated for 24h,
     * scheduled every hour */
    @Scheduled(fixedDelayString = "#{1 * 60 * 1000}")
    public void removeNotActivatedAccountsSchedule() {
        LocalDateTime time24hAgo = LocalDateTime.now(ZoneOffset.UTC).minusHours(24L);
        List<NotActivatedUser> notActivatedAfter24hUsers = notActivatedUserRepository.findAllBefore(time24hAgo);
        notActivatedUserRepository.deleteAll(notActivatedAfter24hUsers);
    }
}
