package pl.szmidla.chatappbackend.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;
import pl.szmidla.chatappbackend.config.PropertiesConfig;

@Component
public class JWTExtractor {

    private static String JWT_SECRET;
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    public JWTExtractor(PropertiesConfig propertiesConfig) {
        JWT_SECRET = propertiesConfig.JWT_SECRET;;
    }

    public String getSubject(String jwtHeaderValue) {
        String token = jwtHeaderValue.substring(JWT_TOKEN_PREFIX.length());
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getSubject();
    }
}
