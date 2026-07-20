package com.ironvault.payments.adapter.in.security;

import com.ironvault.payments.adapter.out.client.ApiKeyValidationClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyValidationClient apiKeyValidationClient;

    public ApiKeyAuthFilter(ApiKeyValidationClient apiKeyValidationClient) {
        this.apiKeyValidationClient = apiKeyValidationClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey != null && !apiKey.isBlank()) {
            if (apiKeyValidationClient.validate(apiKey)) {
                var auth = new UsernamePasswordAuthenticationToken("api-key-user",
                        null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("API Key authentication successful");
            } else {
                log.warn("Invalid API Key attempt");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"Invalid API Key\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
