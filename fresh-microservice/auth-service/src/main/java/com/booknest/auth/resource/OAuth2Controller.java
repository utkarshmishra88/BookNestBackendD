package com.booknest.auth.resource;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 callback endpoint.
 *
 * After successful OAuth2 login Spring Security will redirect here (configured in SecurityConfig).
 * This endpoint issues a JWT and then redirects the browser back to the frontend with the token.
 */
@RestController
@RequiredArgsConstructor
public class OAuth2Controller {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Frontend callback URL — adjust if you prefer gateway host
    private final String FRONTEND_CALLBACK = "http://localhost:5173/oauth/callback";

    @GetMapping("/oauth2/success")
    public ResponseEntity<Void> oauth2Success(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());

        Object emailAttr = attributes.get("email");
        Object nameAttr = attributes.get("name");
        Object subAttr = attributes.get("sub");

        String emailStr = emailAttr != null ? String.valueOf(emailAttr) : String.valueOf(subAttr);
        String nameStr = nameAttr != null ? String.valueOf(nameAttr) : (attributes.get("login") != null ? String.valueOf(attributes.get("login")) : emailStr);

        // Find or create user
        User user = userRepository.findByEmailIgnoreCase(emailStr.toLowerCase().trim())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(emailStr.toLowerCase().trim())
                            .fullName(nameStr)
                            .role("CUSTOMER")
                            .provider("OAUTH2")
                            .active(true)
                            .passwordHash("OAUTH2_USER") // Placeholder
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user);

        // Redirect to frontend with token (URL-encoded)
        String redirectUrl = FRONTEND_CALLBACK + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }
}