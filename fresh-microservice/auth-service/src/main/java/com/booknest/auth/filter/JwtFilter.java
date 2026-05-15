package com.booknest.auth.filter;

import com.booknest.auth.repository.TokenBlacklistRepository;
import com.booknest.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (token.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            if (tokenBlacklistRepository.existsByToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid (logged out)");
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            Claims claims = jwtUtil.extractAllClaims(token);

            Integer userId = null;
            Object userIdClaim = claims.get("userId");
            if (userIdClaim != null) {
                userId = Integer.valueOf(String.valueOf(userIdClaim));
            } else if (claims.getSubject() != null) {
                try {
                    userId = Integer.valueOf(claims.getSubject());
                } catch (NumberFormatException ignored) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token subject");
                    return;
                }
            }

            String role = claims.get("role", String.class);
            if (role != null && !role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("JwtFilter: Claims in token: " + claims);
                List<GrantedAuthority> authorities =
                        role != null
                                ? Collections.singletonList(new SimpleGrantedAuthority(role))
                                : Collections.emptyList();
                
                System.out.println("JwtFilter: User ID: " + userId + ", Extracted Role: " + role + ", Authorities: " + authorities);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}