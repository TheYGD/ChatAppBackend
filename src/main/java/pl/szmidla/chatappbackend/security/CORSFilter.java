package pl.szmidla.chatappbackend.security;

import org.springframework.web.filter.OncePerRequestFilter;
import pl.szmidla.chatappbackend.config.PropertiesConfig;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter extends OncePerRequestFilter {

    public String FRONTEND_ORIGIN;

    public CORSFilter(PropertiesConfig propertiesConfig) {
        this.FRONTEND_ORIGIN = propertiesConfig.FRONTEND_ORIGIN;
    }

    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        if (origin != null && origin.equals(FRONTEND_ORIGIN)) {
            response.setHeader("Access-Control-Allow-Origin", FRONTEND_ORIGIN);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        filterChain.doFilter(request, response);
    }
}
