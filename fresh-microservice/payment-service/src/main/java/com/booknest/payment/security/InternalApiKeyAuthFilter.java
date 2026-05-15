package com.booknest.payment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Allows trusted calls from order-service using a shared secret header when JWT
 * validation did not establish an authentication (common with Feign + service hops).
 */
@Component
public class InternalApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Api-Key";

    @Value("${booknest.internal-api-key:}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (internalApiKey == null || internalApiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        var existing = SecurityContextHolder.getContext().getAuthentication();
        boolean hasUserJwt = existing != null
                && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken);
        if (hasUserJwt) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!HttpMethod.POST.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        if (!uri.contains("/api/payments/create-order") && !uri.contains("/api/payments/verify")) {
            filterChain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(HEADER);
        if (internalApiKey.equals(provided)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "order-service",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
