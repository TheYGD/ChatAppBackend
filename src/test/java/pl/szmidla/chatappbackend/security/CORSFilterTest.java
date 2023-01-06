package pl.szmidla.chatappbackend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CORSFilterTest {

    CORSFilter corsFilter = new CORSFilter();
    HttpServletRequest request = Mockito.spy(HttpServletRequest.class);
    HttpServletResponse response = Mockito.spy(HttpServletResponse.class);
    FilterChain filterChain = Mockito.spy(FilterChain.class);

    @Test
    void doFilterInternalFrontendOrigin() throws ServletException, IOException {
        String originHeader = CORSFilter.FRONTEND_ORIGIN;
        when( request.getHeader("Origin") ).thenReturn( originHeader );

        corsFilter.doFilterInternal(request, response, filterChain);

        verify( response ).setHeader("Access-Control-Allow-Origin", CORSFilter.FRONTEND_ORIGIN);
        verify( response ).setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void doFilterInternalDifferentOrigin() throws ServletException, IOException {
        String originHeader = "http://something.com";
        when( request.getHeader("Origin") ).thenReturn( originHeader );

        corsFilter.doFilterInternal(request, response, filterChain);

        verify( response, times(0) ).setHeader( anyString(), anyString() );
    }
}