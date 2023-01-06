package pl.szmidla.chatappbackend.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.security.jwt.JWTExtractor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class MyAuthorizationFilterTest {

    @Mock
    JWTExtractor jwtExtractor;
    @Mock
    UserDetailsServiceImpl userDetailsService;
    @InjectMocks
    MyAuthorizationFilter filter;
    @Spy
    HttpServletRequest request;
    @Spy
    HttpServletResponse response;
    @Spy
    FilterChain filterChain;

    @Test
    void doFilterInternalLogin() throws ServletException, IOException {
        when( request.getServletPath() ).thenReturn( "/api/login" );

        filter.doFilterInternal(request, response, filterChain);

        verify( request, times(0) ).getHeader(AUTHORIZATION);
        verify( filterChain ).doFilter(request, response);
    }

    @Test
    void doFilterInternalNoAuthHeader() throws ServletException, IOException {
        when( request.getServletPath() ).thenReturn( "/api/sth" );

        filter.doFilterInternal(request, response, filterChain);

        verify( request ).getHeader(AUTHORIZATION);
        verify( jwtExtractor, times(0) ).getSubject( anyString() );
        verify( filterChain ).doFilter(request, response);
    }

    @Test
    void doFilterInternalNoJWTAuthHeader() throws ServletException, IOException {
        String notBearerAuthHeader = "eyjdasdynl asdasdsa";
        when( request.getServletPath() ).thenReturn( "/api/sth" );
        when( request.getHeader(AUTHORIZATION) ).thenReturn(notBearerAuthHeader);

        filter.doFilterInternal(request, response, filterChain);

        verify( request ).getHeader(AUTHORIZATION);
        verify( jwtExtractor, times(0) ).getSubject( anyString() );
        verify( filterChain ).doFilter(request, response);
    }

    @Test
    void doFilterInternalWrongJWT() throws ServletException, IOException {
        String jwtHeader = "Bearer asdasd.asdsad.asdasd";
        when( request.getServletPath() ).thenReturn( "/api/sth" );
        when( request.getHeader(AUTHORIZATION) ).thenReturn(jwtHeader);
        when( jwtExtractor.getSubject(jwtHeader) ).thenThrow( new JWTVerificationException("jwt exception") );
        when( response.getOutputStream() ).thenReturn(Mockito.spy(ServletOutputStream.class) );

        filter.doFilterInternal(request, response, filterChain);

        verify( filterChain, times(0) ).doFilter(request, response);
    }

    @Test
    void doFilterInternalSuccess() throws ServletException, IOException {
        String jwtHeader = "Bearer asdasd.asdsad.asdasd";
        User user = createUser(1L, "user", "sth@email.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        when( request.getServletPath() ).thenReturn( "/api/sth" );
        when( request.getHeader(AUTHORIZATION) ).thenReturn( jwtHeader );
        when( jwtExtractor.getSubject(jwtHeader) ).thenReturn( user.getUsername() );
        when( userDetailsService.loadUserByUsername(user.getUsername()) ).thenReturn( user );

        filter.doFilterInternal(request, response, filterChain);

        assertEquals( user, securityContext.getAuthentication().getPrincipal() );
        verify( request ).getHeader(AUTHORIZATION);
        verify( jwtExtractor ).getSubject( anyString() );
        verify( filterChain ).doFilter(request, response);
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