package pl.szmidla.chatappbackend.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.service.EmailService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Profile("aws-dream")
@Service
@Slf4j
public class AWSEmailService implements EmailService {

    private final SesClient sesClient;
    private static final String SOURCE_EMAIL = "jakub.szmidla@o2.pl";

    public AWSEmailService() {
        sesClient = SesClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
    }

    @Override
    public void sendEmail(String subject, String receiver, String body) {
        Message message =  Message.builder()
            .body( Body.builder()
                .html( Content.builder()
                    .charset("UTF-8")
                    .data(body)
                    .build() )
                .build())
            .subject( Content.builder()
                .charset("UTF-8")
                .data(subject )
                .build() )
            .build();


        SendEmailRequest request = SendEmailRequest.builder()
            .destination( Destination.builder().toAddresses(receiver).build() )
            .message( message )
            .source(SOURCE_EMAIL)
            .build();

        sesClient.sendEmail( request );
    }
}
