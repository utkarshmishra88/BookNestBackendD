package com.booknest.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;
    private String recipientEmail;
    private String mobileNumber;
    private String type; // "OTP" or "UPDATE"
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String messageBody;
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() { this.sentAt = LocalDateTime.now(); }
}