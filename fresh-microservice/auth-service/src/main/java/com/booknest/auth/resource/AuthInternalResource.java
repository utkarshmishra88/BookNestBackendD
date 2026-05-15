package com.booknest.auth.resource;

import com.booknest.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * Service-to-service endpoint for looking up a user's email (e.g. order confirmation PDFs).
 */
@RestController
@RequestMapping("/auth/internal")
@RequiredArgsConstructor
public class AuthInternalResource {

    private final UserRepository userRepository;
    private final com.booknest.auth.repository.AddressRepository addressRepository;

    @Value("${booknest.internal-api-key:}")
    private String internalApiKey;

    @GetMapping("/users/{userId}/email")
    public ResponseEntity<Map<String, String>> getUserEmail(
            @PathVariable Integer userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(key)) {
            return ResponseEntity.status(401).build();
        }
        return userRepository.findById(userId)
                .map(u -> {
                    Map<String, String> result = new java.util.HashMap<>();
                    result.put("email", u.getEmail());
                    if (u.getMobileNumber() != null) {
                        result.put("mobileNumber", u.getMobileNumber());
                    }
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, Object>> getAddress(
            @PathVariable Integer addressId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(key)) {
            return ResponseEntity.status(401).build();
        }
        return addressRepository.findById(addressId)
                .map(a -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("line1", a.getLine1());
                    map.put("line2", a.getLine2());
                    map.put("city", a.getCity());
                    map.put("state", a.getState());
                    map.put("postalCode", a.getPostalCode());
                    map.put("country", a.getCountry());
                    map.put("mobileNumber", a.getMobileNumber());
                    return ResponseEntity.ok(map);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/all-contacts")
    public ResponseEntity<List<Map<String, String>>> getAllUserContacts(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(key)) {
            return ResponseEntity.status(401).build();
        }
        List<Map<String, String>> users = userRepository.findAll().stream()
                .filter(u -> u.getActive() != null && u.getActive())
                .map(u -> {
                    Map<String, String> m = new java.util.HashMap<>();
                    m.put("email", u.getEmail());
                    if (u.getMobileNumber() != null) m.put("mobileNumber", u.getMobileNumber());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(users);
    }
}
