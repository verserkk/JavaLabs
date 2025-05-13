package com.example.animeservice.config;

import com.example.animeservice.service.VisitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class VisitFilter extends OncePerRequestFilter {
    private final VisitService visitService;

    public VisitFilter(VisitService visitService) {
        this.visitService = visitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String url = request.getRequestURI();
        visitService.incrementVisit(url);
        filterChain.doFilter(request, response);
    }
}