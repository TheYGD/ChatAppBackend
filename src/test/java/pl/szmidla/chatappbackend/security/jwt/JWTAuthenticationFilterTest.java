package pl.szmidla.chatappbackend.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import pl.szmidla.chatappbackend.config.PropertiesConfig;
import pl.szmidla.chatappbackend.data.User;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class JWTAuthenticationFilterTest {

    String JWT_SECRET = "secret";
    int JWT_EXPIRATION_MS = 1000;
    JWTAuthenticationFilter filter;
    AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        PropertiesConfig propertiesConfig = Mockito.mock(PropertiesConfig.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        propertiesConfig.JWT_SECRET = JWT_SECRET;
        propertiesConfig.JWT_EXPIRATION_HOURS = JWT_EXPIRATION_MS;
        filter = new JWTAuthenticationFilter(authenticationManager, propertiesConfig);
    }

    @Test
    void attemptAuthentication() {
        String username = "username";
        String password = "password";
        HttpServletRequest request = Mockito.spy( HttpServletRequest.class );
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        when( request.getParameter("username") ).thenReturn( username );
        when( request.getParameter("password") ).thenReturn( password );

        filter.attemptAuthentication(request, null);

        verify( authenticationManager ).authenticate( captor.capture() );
        UsernamePasswordAuthenticationToken authenticationToken = captor.getValue();
        assertEquals( username, authenticationToken.getPrincipal() );
        assertEquals( password, authenticationToken.getCredentials() );
    }

    @Test
    void successfulAuthentication() throws ServletException, IOException {
        HttpServletRequest request = Mockito.spy( HttpServletRequest.class );
        HttpServletResponse response = Mockito.spy( HttpServletResponse.class );
        Authentication authentication = Mockito.spy( Authentication.class );
        ServletOutputStream servletOutputStream = Mockito.spy(ServletOutputStream.class);
        User user = createUser(1L, "username", "email@ema.com", "password");
        when( authentication.getPrincipal() ).thenReturn( user );
        when( request.getRequestURL() ).thenReturn( new StringBuffer("my-url") );
        when( response.getOutputStream() ).thenReturn( servletOutputStream );

        filter.successfulAuthentication(request, response, null, authentication);

        verify( response ).setContentType( APPLICATION_JSON_VALUE );
        verify( servletOutputStream ).write( any() );
    }

    private User createUser(long id, String username, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}