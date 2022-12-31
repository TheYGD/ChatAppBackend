package pl.szmidla.chatappbackend.security;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter extends OncePerRequestFilter {

    final static String FRONTEND_ORIGIN = "http://localhost:5173";

    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String origin = req.getHeader("Origin");
        if (origin.equals(FRONTEND_ORIGIN)) {
            res.setHeader("Access-Control-Allow-Origin", FRONTEND_ORIGIN);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        }
        chain.doFilter(req, res);
    }
}
