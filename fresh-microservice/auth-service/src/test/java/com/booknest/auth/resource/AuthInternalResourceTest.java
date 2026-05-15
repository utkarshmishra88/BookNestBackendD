package com.booknest.auth.resource;

import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthInternalResourceTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.booknest.auth.repository.AddressRepository addressRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthInternalResource authInternalResource;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authInternalResource).build();
    }

    @Test
    void testGetUserEmail() throws Exception {
        org.springframework.test.util.ReflectionTestUtils.setField(authInternalResource, "internalApiKey", "secret");
        when(userRepository.findById(1)).thenReturn(Optional.of(User.builder().email("test@ex.com").build()));
        
        mockMvc.perform(get("/auth/internal/users/1/email").header("X-Internal-Api-Key", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@ex.com"));
    }

    @Test
    void testGetAllUserContacts() throws Exception {
        org.springframework.test.util.ReflectionTestUtils.setField(authInternalResource, "internalApiKey", "secret");
        when(userRepository.findAll()).thenReturn(java.util.List.of(User.builder().active(true).email("test@ex.com").build()));

        mockMvc.perform(get("/auth/internal/users/all-contacts").header("X-Internal-Api-Key", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetAddress() throws Exception {
        org.springframework.test.util.ReflectionTestUtils.setField(authInternalResource, "internalApiKey", "secret");
        com.booknest.auth.entity.Address addr = com.booknest.auth.entity.Address.builder()
                .line1("L1").city("C1").build();
        when(addressRepository.findById(1)).thenReturn(Optional.of(addr));

        mockMvc.perform(get("/auth/internal/addresses/1").header("X-Internal-Api-Key", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.line1").value("L1"));
    }
}
