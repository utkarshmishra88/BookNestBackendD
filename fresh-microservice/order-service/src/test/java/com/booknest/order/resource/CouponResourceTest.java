package com.booknest.order.resource;

import com.booknest.order.entity.Coupon;
import com.booknest.order.repository.CouponRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponResourceTest {

    private MockMvc mockMvc;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponResource couponResource;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(couponResource).build();
    }

    @Test
    void testGetAllCoupons() throws Exception {
        when(couponRepository.findAll()).thenReturn(List.of(new Coupon()));
        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testCreateCoupon_Success() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setDiscountPercentage(new BigDecimal(10));
        when(couponRepository.save(any())).thenReturn(coupon);

        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateCoupon_BadRequest() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setDiscountPercentage(new BigDecimal(2)); // < 5%

        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCoupon() throws Exception {
        mockMvc.perform(delete("/coupons/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testValidateCoupon_Success() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setActive(true);
        coupon.setMinOrderAmount(new BigDecimal(100));
        coupon.setDiscountAmount(new BigDecimal(10));
        coupon.setExpiryDate(LocalDate.now().plusDays(1));

        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        mockMvc.perform(get("/coupons/validate")
                .param("code", "SAVE10")
                .param("orderAmount", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    void testValidateCoupon_Expired() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setActive(true);
        coupon.setMinOrderAmount(new BigDecimal(100));
        coupon.setExpiryDate(LocalDate.now().minusDays(1));

        when(couponRepository.findByCode("OLD")).thenReturn(Optional.of(coupon));

        mockMvc.perform(get("/coupons/validate")
                .param("code", "OLD")
                .param("orderAmount", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(false));
    }

    @Test
    void testValidateCoupon_InvalidCode() throws Exception {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/coupons/validate")
                .param("code", "INVALID")
                .param("orderAmount", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(false));
    }
}
