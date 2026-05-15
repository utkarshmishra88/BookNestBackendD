package com.booknest.notification.service;

import com.booknest.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationLogRepository repository;

    @Mock
    private com.booknest.notification.client.AuthClient authClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    }

    @Test
    void testSendOtpEmail() {
        notificationService.sendOtpEmail("test@example.com", "123456", "1234567890");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(repository, times(1)).save(any());
    }

    @Test
    void testSendUpdateEmail_WithMobile() {
        notificationService.sendUpdateEmail("test@example.com", "Subject", "Message", "9876543210");
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        verify(repository).save(argThat(log -> "9876543210".equals(log.getMobileNumber())));
    }

    @Test
    void testSendOrderDocumentsEmail() {
        notificationService.sendOrderDocumentsEmail("test@example.com", "Subject", "Body", new byte[]{1}, new byte[]{2}, "1234567890");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(repository, times(1)).save(any());
    }

    @Test
    void testSendBroadcast() {
        java.util.List<java.util.Map<String, String>> users = java.util.List.of(
            java.util.Map.of("email", "user1@example.com"),
            java.util.Map.of("email", "user2@example.com")
        );
        when(authClient.getAllUserContacts(any())).thenReturn(users);

        notificationService.sendBroadcast("Broadcast Subject", "Broadcast Message");

        verify(mailSender, times(2)).send(any(MimeMessage.class));
        verify(repository, times(2)).save(any());
    }

    @Test
    void testSendOrderDocumentsEmail_Failure() {
        doThrow(new RuntimeException("Fail")).when(mailSender).send(any(MimeMessage.class));
        notificationService.sendOrderDocumentsEmail("test@example.com", "Sub", "Body", new byte[]{1}, new byte[]{2}, "123");
        verify(repository, never()).save(any());
    }

    @Test
    void testSendHtmlAndLog_LongBody() {
        String longBody = "a".repeat(3000);
        notificationService.sendOrderDocumentsEmail("test@example.com", "Sub", longBody, null, null, "123");
        verify(repository).save(argThat(log -> log.getMessageBody().length() == 2000));
    }
}
