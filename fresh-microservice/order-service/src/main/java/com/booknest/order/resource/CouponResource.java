package com.booknest.order.resource;

import com.booknest.order.entity.Coupon;
import com.booknest.order.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponResource {

    private final CouponRepository couponRepository;

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        if (coupon.getCreatedAt() == null) coupon.setCreatedAt(LocalDateTime.now());
        if (coupon.getDiscountAmount() == null) coupon.setDiscountAmount(BigDecimal.ZERO);
        if (coupon.getDiscountPercentage() == null) coupon.setDiscountPercentage(BigDecimal.ZERO);
        if (coupon.getMaxDiscountAmount() == null) coupon.setMaxDiscountAmount(BigDecimal.ZERO);
        
        if (coupon.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0 && 
            coupon.getDiscountPercentage().compareTo(new BigDecimal(5)) < 0) {
            return ResponseEntity.badRequest().body("Minimum percentage discount is 5%");
        }
        
        return ResponseEntity.ok(couponRepository.save(coupon));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        
        return couponRepository.findByCode(code)
                .map(c -> {
                    boolean isValid = c.isActive() && 
                                     (c.getExpiryDate() == null || c.getExpiryDate().isAfter(LocalDate.now())) &&
                                     orderAmount.compareTo(c.getMinOrderAmount()) >= 0;
                    
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("isValid", isValid);
                    if (isValid) {
                        BigDecimal calculatedDiscount = c.getDiscountAmount();
                        if (c.getDiscountPercentage() != null && c.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                            calculatedDiscount = orderAmount.multiply(c.getDiscountPercentage()).divide(new BigDecimal(100));
                            
                            // Apply MAX CAP
                            if (c.getMaxDiscountAmount() != null && c.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                                if (calculatedDiscount.compareTo(c.getMaxDiscountAmount()) > 0) {
                                    calculatedDiscount = c.getMaxDiscountAmount();
                                }
                            }
                        }
                        response.put("discountAmount", calculatedDiscount);
                        response.put("message", "Coupon applied successfully!");
                    } else if (!c.isActive()) {
                        response.put("message", "Coupon is inactive.");
                    } else if (c.getExpiryDate() != null && c.getExpiryDate().isBefore(LocalDate.now())) {
                        response.put("message", "Coupon has expired.");
                    } else {
                        response.put("message", "Minimum order amount of ₹" + c.getMinOrderAmount() + " required.");
                    }
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("isValid", false);
                    response.put("message", "Invalid coupon code.");
                    return ResponseEntity.ok(response);
                });
    }
}
