package pl.szmidla.chatappbackend.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.szmidla.chatappbackend.config.PropertiesConfig;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTExtractorTest {

    String JWT_SECRET = "top-secret";
    JWTExtractor jwtExtractor;

    @BeforeEach
    void setUp() {
        PropertiesConfig propertiesConfig = Mockito.mock(PropertiesConfig.class);
        propertiesConfig.JWT_SECRET = JWT_SECRET;
        jwtExtractor = new JWTExtractor(propertiesConfig);
    }

    @Test
    void getSubjectFail() {
        String someJwt = "Bearer asdjla.asfsdf.sd";

        assertThrows( JWTVerificationException.class, () -> jwtExtractor.getSubject(someJwt) );
    }

    @Test
    void getSubjectFailNoBearerPrefix() {
        String expectedUsername = "username";
        String jwt = createJwt(expectedUsername);

        assertThrows( JWTVerificationException.class, () -> jwtExtractor.getSubject(jwt) );
    }

    @Test
    void getSubjectSuccess() {
        String expectedUsername = "username";
        String jwt = "Bearer " + createJwt(expectedUsername);

        String actualUsername = jwtExtractor.getSubject(jwt);

        assertEquals( expectedUsername, actualUsername );
    }

    private String createJwt(String username) {
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
        Date expirationDate = new Date( System.currentTimeMillis() + 100000000);
        String jwtToken = JWT.create()
                .withSubject(username)
                .withExpiresAt( expirationDate )
                .sign(algorithm);
        return jwtToken;
    }
}