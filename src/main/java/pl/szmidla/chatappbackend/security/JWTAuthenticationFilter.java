package pl.szmidla.chatappbackend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.szmidla.chatappbackend.config.PropertiesConfig;
import pl.szmidla.chatappbackend.data.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static String JWT_SECRET;
    private static int JWT_EXPIRATION_MS;

    private final AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, PropertiesConfig propertiesConfig) {
        this.authenticationManager = authenticationManager;
        JWT_SECRET = propertiesConfig.JWT_SECRET;
        JWT_EXPIRATION_MS = propertiesConfig.JWT_EXPIRATION_HOURS * 60 * 60 * 1000;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        User user = (User) authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

        String jwtToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt( new Date( System.currentTimeMillis() + JWT_EXPIRATION_MS) )
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("jwt", jwtToken);

        response.setContentType(APPLICATION_JSON_VALUE);
        response.getOutputStream().write( new ObjectMapper().writeValueAsBytes(tokens) );
    }
}
