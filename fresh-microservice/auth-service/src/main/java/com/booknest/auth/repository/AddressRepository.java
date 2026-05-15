package com.booknest.auth.repository;

import com.booknest.auth.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUserUserIdOrderByIsDefaultDescAddressIdDesc(Integer userId);
}