package com.ironvault.payments.adapter.in.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationFilter implements Filter {

    private static final String HEADER = "x-Correlation-Id";

    @Override
    public void doFilter(ServletRequest request,
         ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = httpRequest.getHeader(HEADER);

        String path = httpRequest.getRequestURI();
        String type = path.contains("/payments") ? "payment" : "req";

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "ironvault-" + type + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        else if (!correlationId.startsWith("ironvault-")) {
            correlationId = "ironvault-" + type + "-" + correlationId;
        }

        MDC.put("correlationId", correlationId);

        // 🔥 3. Retorna no response (importante!)
        httpResponse.setHeader(HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
