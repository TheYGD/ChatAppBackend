package pl.szmidla.chatappbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.szmidla.chatappbackend.config.PropertiesConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CORSFilterTest {

    String originHeader = "http://something.com";
    CORSFilter corsFilter;
    HttpServletRequest request;
    HttpServletResponse response;
    FilterChain filterChain;

    @BeforeEach
    void setUp() {
        PropertiesConfig propertiesConfig = new PropertiesConfig();
        propertiesConfig.FRONTEND_ORIGIN = originHeader;
        corsFilter = new CORSFilter(propertiesConfig);
        request = Mockito.spy(HttpServletRequest.class);
        response = Mockito.spy(HttpServletResponse.class);
        filterChain = Mockito.spy(FilterChain.class);
    }

    @Test
    void doFilterInternalFrontendOrigin() throws ServletException, IOException {
        when( request.getHeader("Origin") ).thenReturn( originHeader );

        corsFilter.doFilterInternal(request, response, filterChain);

        verify( response ).setHeader("Access-Control-Allow-Origin", corsFilter.FRONTEND_ORIGIN);
        verify( response ).setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void doFilterInternalDifferentOrigin() throws ServletException, IOException {
        String differentOrigin = "someorigin";
        when( request.getHeader("Origin") ).thenReturn( differentOrigin );

        corsFilter.doFilterInternal(request, response, filterChain);

        verify( response, times(0) ).setHeader( anyString(), anyString() );
    }
}