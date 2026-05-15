package com.booknest.auth.service.impl;

import com.booknest.auth.dto.AddressResponse;
import com.booknest.auth.dto.AddressUpsertRequest;
import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.AddressRepository;
import com.booknest.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void testGetUserAddresses() {
        User user = User.builder().userId(1).build();
        Address addr = Address.builder().addressId(1).user(user).isDefault(true).build();
        when(addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdDesc(1)).thenReturn(List.of(addr));

        List<AddressResponse> results = addressService.getUserAddresses(1);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsDefault());
    }

    @Test
    void testCreateAddress() {
        User user = User.builder().userId(1).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        
        AddressUpsertRequest req = new AddressUpsertRequest();
        req.setUserId(1);
        req.setMobileNumber("1234567890");
        req.setIsDefault(true);

        Address addr = Address.builder().addressId(1).user(user).mobileNumber("123").build();
        when(addressRepository.save(any(Address.class))).thenReturn(addr);

        AddressResponse resp = addressService.createAddress(req);
        assertNotNull(resp);
        verify(addressRepository).findByUserUserIdOrderByIsDefaultDescAddressIdDesc(1);
    }

    @Test
    void testUpdateAddress() {
        User user = User.builder().userId(1).build();
        Address addr = Address.builder().addressId(1).user(user).isDefault(false).build();
        when(addressRepository.findById(1)).thenReturn(Optional.of(addr));
        
        AddressUpsertRequest req = new AddressUpsertRequest();
        req.setUserId(1);
        req.setMobileNumber("999");
        req.setIsDefault(true);

        when(addressRepository.save(any(Address.class))).thenReturn(addr);

        addressService.updateAddress(1, req);
        verify(addressRepository).findByUserUserIdOrderByIsDefaultDescAddressIdDesc(1);
    }

    @Test
    void testCreateAddress_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        AddressUpsertRequest req = new AddressUpsertRequest();
        req.setUserId(1);
        assertThrows(RuntimeException.class, () -> addressService.createAddress(req));
    }

    @Test
    void testUpdateAddress_NotFound() {
        when(addressRepository.findById(99)).thenReturn(Optional.empty());
        AddressUpsertRequest req = new AddressUpsertRequest();
        assertThrows(RuntimeException.class, () -> addressService.updateAddress(99, req));
    }

    @Test
    void testUpdateAddress_WrongUser() {
        User user1 = User.builder().userId(1).build();
        Address addr = Address.builder().addressId(1).user(user1).build();
        when(addressRepository.findById(1)).thenReturn(Optional.of(addr));
        
        AddressUpsertRequest req = new AddressUpsertRequest();
        req.setUserId(2); // different user
        
        assertThrows(RuntimeException.class, () -> addressService.updateAddress(1, req));
    }
}
