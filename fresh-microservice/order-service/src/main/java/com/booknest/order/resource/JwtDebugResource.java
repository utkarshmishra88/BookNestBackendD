package com.booknest.order.resource;

import com.booknest.order.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug/jwt")
@RequiredArgsConstructor
public class JwtDebugResource {

    private final JwtUtil jwtUtil;

    @PostMapping("/decode")
    public ResponseEntity<Map<String, Object>> decodeJwt(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            String[] parts = token.split("\\.");
            
            if (parts.length != 3) {
                result.put("error", "Invalid JWT format. Expected 3 parts, got " + parts.length);
                return ResponseEntity.ok(result);
            }

            // Decode header
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            result.put("header", headerJson);

            // Decode payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            result.put("payload", payloadJson);

            // Try to extract claims
            try {
                var claims = jwtUtil.extractAllClaims(token);
                result.put("claims_parsed", "SUCCESS");
                result.put("user_id", claims.get("userId"));
                result.put("role", claims.get("role"));
            } catch (Exception e) {
                result.put("claims_parse_error", e.getMessage());
                result.put("error_class", e.getClass().getName());
            }

            result.put("signature", parts[2]);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("error_class", e.getClass().getName());
            return ResponseEntity.ok(result);
        }
    }
}
