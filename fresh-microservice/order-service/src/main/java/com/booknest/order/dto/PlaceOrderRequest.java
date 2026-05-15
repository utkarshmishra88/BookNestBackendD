package com.booknest.order.dto;

import com.booknest.order.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotNull
    private PaymentMode paymentMode;

    private Long shippingAddressId;

    // Explicit getters (Lombok @Data backup)
    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }

    public Long getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    private String couponCode;
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}