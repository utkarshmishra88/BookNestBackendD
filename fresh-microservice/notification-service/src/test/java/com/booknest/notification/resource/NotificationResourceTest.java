package com.booknest.notification.resource;

import com.booknest.notification.dto.OrderDocumentsEmailRequest;
import com.booknest.notification.service.NotificationService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationResourceTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationResource notificationResource;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationResource, "internalApiKey", "test-key");
        mockMvc = MockMvcBuilders.standaloneSetup(notificationResource).build();
    }

    @Test
    void testSendOtp() throws Exception {
        mockMvc.perform(post("/notifications/send-otp")
                .param("email", "test@example.com")
                .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP Email request accepted."));

        verify(notificationService).sendOtpEmail(eq("test@example.com"), eq("123456"), any());
    }

    @Test
    void testSendOtp_WithMobile() throws Exception {
        mockMvc.perform(post("/notifications/send-otp")
                .param("email", "test@example.com")
                .param("otp", "123456")
                .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk());

        verify(notificationService).sendOtpEmail("test@example.com", "123456", "9876543210");
    }

    @Test
    void testSendUpdate() throws Exception {
        mockMvc.perform(post("/notifications/send-update")
                .param("email", "test@example.com")
                .param("subject", "Sub")
                .param("message", "Msg"))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Email request accepted."));

        verify(notificationService).sendUpdateEmail(eq("test@example.com"), eq("Sub"), eq("Msg"), any());
    }

    @Test
    void testSendUpdate_WithMobile() throws Exception {
        mockMvc.perform(post("/notifications/send-update")
                .param("email", "test@example.com")
                .param("subject", "Sub")
                .param("message", "Msg")
                .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk());

        verify(notificationService).sendUpdateEmail("test@example.com", "Sub", "Msg", "9876543210");
    }

    @Test
    void testSendBroadcast() throws Exception {
        Map<String, String> body = Map.of("subject", "Sub", "message", "Msg");

        mockMvc.perform(post("/notifications/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("Broadcast request accepted."));

        verify(notificationService).sendBroadcast("Sub", "Msg");
    }

    @Test
    void testSendBroadcast_MissingParams() throws Exception {
        Map<String, String> body = Map.of("subject", "Sub");

        mockMvc.perform(post("/notifications/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing subject or message"));
    }

    @Test
    void testSendOrderDocumentsInternal_Success() throws Exception {
        OrderDocumentsEmailRequest request = new OrderDocumentsEmailRequest();
        request.setToEmail("test@example.com");
        request.setSubject("Subject");

        mockMvc.perform(post("/notifications/internal/order-documents")
                .header("X-Internal-Api-Key", "test-key") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Queued"));

        verify(notificationService).sendOrderDocumentsEmail(eq("test@example.com"), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void testSendOrderDocumentsInternal_Unauthorized() throws Exception {
        OrderDocumentsEmailRequest request = new OrderDocumentsEmailRequest();
        
        mockMvc.perform(post("/notifications/internal/order-documents")
                .header("X-Internal-Api-Key", "wrong-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void testSendOrderDocumentsInternal_MissingEmail() throws Exception {
        OrderDocumentsEmailRequest request = new OrderDocumentsEmailRequest();
        request.setToEmail(""); 

        mockMvc.perform(post("/notifications/internal/order-documents")
                .header("X-Internal-Api-Key", "test-key") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing email"));
    }
}
