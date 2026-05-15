package com.booknest.auth.resource;

import com.booknest.auth.dto.AddressResponse;
import com.booknest.auth.dto.AddressUpsertRequest;
import com.booknest.auth.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressResourceTest {

    private MockMvc mockMvc;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressResource addressResource;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressResource).build();
    }

    @Test
    void testGetUserAddresses() throws Exception {
        when(addressService.getUserAddresses(1)).thenReturn(List.of(AddressResponse.builder().addressId(1).build()));
        mockMvc.perform(get("/auth/addresses/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testCreateAddress() throws Exception {
        AddressUpsertRequest req = AddressUpsertRequest.builder()
                .userId(1)
                .line1("Line 1")
                .city("City")
                .state("State")
                .postalCode("123456")
                .country("Country")
                .mobileNumber("1234567890")
                .isDefault(true)
                .build();

        when(addressService.createAddress(any())).thenReturn(AddressResponse.builder().addressId(1).build());

        mockMvc.perform(post("/auth/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateAddress() throws Exception {
        AddressUpsertRequest req = AddressUpsertRequest.builder()
                .userId(1)
                .line1("Line 1")
                .city("City")
                .state("State")
                .postalCode("123456")
                .country("Country")
                .mobileNumber("1234567890")
                .isDefault(true)
                .build();

        when(addressService.updateAddress(eq(1), any())).thenReturn(AddressResponse.builder().addressId(1).build());

        mockMvc.perform(put("/auth/addresses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
