package com.part4.team09.otboo.module.domain.notification.repository;

import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class NotificationRepositoryQueryDSLTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRepositoryQueryDSL notificationRepositoryQueryDSL;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_성공() {
        // given
        UUID receiverId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            Notification notification = Notification.create(
                    receiverId,
                    "제목" + i,
                    "내용" + i,
                    Notification.Level.INFO
            );
            ReflectionTestUtils.setField(notification, "createdAt", now.minusMinutes(4 - i));
            notificationRepository.save(notification);
        }

        em.flush();
        em.clear();

        // when
        List<Notification> result = notificationRepositoryQueryDSL.getNotifications(receiverId, null, null, 5);

        // then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getContent()).isEqualTo("내용4");
        assertThat(result.get(4).getContent()).isEqualTo("내용0");
    }

    @Test
    @DisplayName("알림 개수 조회 성공")
    void countNotificationsSuccess() {
        // given
        UUID receiverId = UUID.randomUUID();
        for (int i = 0; i < 3; i++) {
            Notification notification = Notification.create(
                    receiverId,
                    "제목" + i,
                    "내용" + i,
                    Notification.Level.WARNING
            );
            notificationRepository.save(notification);
        }

        // when
        int count = notificationRepositoryQueryDSL.countNotifications(receiverId);

        // then
        assertThat(count).isEqualTo(3);
    }
}
