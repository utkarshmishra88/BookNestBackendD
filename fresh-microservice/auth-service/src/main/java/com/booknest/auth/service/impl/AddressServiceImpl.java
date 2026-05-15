package com.booknest.auth.service.impl;

import com.booknest.auth.dto.AddressResponse;
import com.booknest.auth.dto.AddressUpsertRequest;
import com.booknest.auth.entity.Address;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.AddressRepository;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getUserAddresses(Integer userId) {
        return addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressUpsertRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForUser(user.getUserId());
        }

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .mobileNumber(request.getMobileNumber().trim())
                .isDefault(request.getIsDefault())
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Integer addressId, AddressUpsertRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getUserId().equals(request.getUserId())) {
            throw new RuntimeException("Address does not belong to user");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForUser(request.getUserId());
        }

        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setMobileNumber(request.getMobileNumber().trim());
        address.setIsDefault(request.getIsDefault());

        return toResponse(addressRepository.save(address));
    }

    private void clearDefaultForUser(Integer userId) {
        List<Address> userAddresses = addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdDesc(userId);
        for (Address a : userAddresses) {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
            }
        }
        addressRepository.saveAll(userAddresses);
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .addressId(a.getAddressId())
                .userId(a.getUser().getUserId())
                .label(a.getLabel())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .mobileNumber(a.getMobileNumber())
                .isDefault(a.getIsDefault())
                .build();
    }
}