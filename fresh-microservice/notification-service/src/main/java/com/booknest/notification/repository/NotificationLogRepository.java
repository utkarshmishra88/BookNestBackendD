package com.booknest.notification.repository;

import com.booknest.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Integer> {}