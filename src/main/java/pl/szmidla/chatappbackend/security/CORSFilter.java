package pl.szmidla.chatappbackend.security;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter extends OncePerRequestFilter {

    final static String FRONTEND_ORIGIN = "http://localhost:5173";

    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        if (origin.equals(FRONTEND_ORIGIN)) {
            response.setHeader("Access-Control-Allow-Origin", FRONTEND_ORIGIN);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        filterChain.doFilter(request, response);
    }
}
