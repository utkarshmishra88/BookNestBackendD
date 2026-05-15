package com.booknest.auth.config;

import com.booknest.auth.filter.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            // Allow Spring Security to create a session when required by OAuth2 (authorization request)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/register",
                    "/auth/verify",
                    "/auth/login",
                    "/auth/forgot-password",
                    "/auth/reset-password",
                    "/auth/internal/**",
                    "/error",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/oauth2/authorization/**",
                    "/oauth2/success",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )

            .exceptionHandling(exc -> exc
                .authenticationEntryPoint((req, res, ex) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied"))
                .accessDeniedHandler(accessDeniedHandler())
            )

            // OAuth2 login: use HttpSession repository (default) and redirect to our success endpoint
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint ->
                    endpoint.authorizationRequestRepository(new HttpSessionOAuth2AuthorizationRequestRepository())
                )
                .defaultSuccessUrl("/oauth2/success", true)
            );

        // Keep JWT filter for API requests (it will only be used when a Bearer token is present)
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }
}