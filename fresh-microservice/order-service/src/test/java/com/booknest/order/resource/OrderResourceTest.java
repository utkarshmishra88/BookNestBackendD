package com.booknest.order.resource;

import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.service.OrderService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderResourceTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderResource orderResource;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderResource).build();
    }

    @Test
    void testPlaceOrder() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingAddressId(1L);
        request.setPaymentMode(com.booknest.order.enums.PaymentMode.COD);

        OrderResponse response = OrderResponse.builder().orderId(123L).message("Success").build();
        when(orderService.placeOrder(eq(1L), any(PlaceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/orders/place/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(123));
    }

    @Test
    void testGetUserOrders() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(OrderResponse.builder().orderId(123L).build()));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(123));
    }

    @Test
    void testGetInvoice() throws Exception {
        when(orderService.getInvoicePdf(1L, 123L)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/orders/1/123/invoice"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void testUpdateStatus() throws Exception {
        when(orderService.updateOrderStatus(123L, OrderStatus.SHIPPED))
                .thenReturn(OrderResponse.builder().status(OrderStatus.SHIPPED).build());

        mockMvc.perform(put("/orders/admin/123/status")
                .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void testAdminListAll() throws Exception {
        when(orderService.getAllOrdersForAdmin()).thenReturn(List.of(OrderResponse.builder().orderId(123L).build()));

        mockMvc.perform(get("/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(123));
    }

    @Test
    void testVerifyPayment() throws Exception {
        com.booknest.order.dto.VerifyPaymentRequest request = new com.booknest.order.dto.VerifyPaymentRequest();
        request.setOrderId(1L);
        request.setPaymentId(123L);
        request.setRazorpayOrderId("rzp_order_123");
        request.setRazorpayPaymentId("rzp_pay_123");
        request.setRazorpaySignature("sig_123");

        OrderResponse response = OrderResponse.builder().orderId(1L).status(OrderStatus.CONFIRMED).build();
        when(orderService.verifyPayment(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/orders/verify/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void testGetOrder() throws Exception {
        when(orderService.getOrderById(1L, 123L)).thenReturn(OrderResponse.builder().orderId(123L).build());

        mockMvc.perform(get("/orders/1/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(123));
    }

    @Test
    void testGetReceipt() throws Exception {
        when(orderService.getReceiptPdf(1L, 123L)).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/orders/1/123/receipt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(new byte[]{4, 5, 6}));
    }
}
