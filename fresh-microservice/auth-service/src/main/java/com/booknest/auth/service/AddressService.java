package com.booknest.auth.service;

import com.booknest.auth.dto.AddressResponse;
import com.booknest.auth.dto.AddressUpsertRequest;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getUserAddresses(Integer userId);
    AddressResponse createAddress(AddressUpsertRequest request);
    AddressResponse updateAddress(Integer addressId, AddressUpsertRequest request);
}