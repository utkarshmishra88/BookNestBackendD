package com.booknest.auth.resource;

import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminResourceTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminResource adminResource;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminResource).build();
    }

    @Test
    void testListUsers() throws Exception {
        when(authService.listUsersForAdmin()).thenReturn(List.of(UserResponse.builder().build()));
        mockMvc.perform(get("/auth/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    void testSetUserActive() throws Exception {
        when(authService.setUserActive(1, true)).thenReturn(UserResponse.builder().build());
        mockMvc.perform(patch("/auth/admin/users/1/active")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"active\": true}"))
                .andExpect(status().isOk());
    }
}
