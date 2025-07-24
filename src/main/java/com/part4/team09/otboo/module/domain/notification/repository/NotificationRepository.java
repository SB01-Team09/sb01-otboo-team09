package com.part4.team09.otboo.module.domain.notification.repository;

import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
