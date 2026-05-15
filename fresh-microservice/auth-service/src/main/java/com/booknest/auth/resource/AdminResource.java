package com.booknest.auth.resource;

import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminResource {

    private final AuthService authService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        System.out.println("AdminResource: listUsers called");
        return ResponseEntity.ok(authService.listUsersForAdmin());
    }

    @PatchMapping("/users/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> setUserActive(
            @PathVariable Integer userId,
            @RequestBody Map<String, Boolean> body) {
        Boolean active = body != null ? body.get("active") : null;
        return ResponseEntity.ok(authService.setUserActive(userId, active));
    }
}
