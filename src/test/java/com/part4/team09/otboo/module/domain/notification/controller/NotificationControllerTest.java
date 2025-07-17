package com.part4.team09.otboo.module.domain.notification.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private NotificationService notificationService;

  @Nested
  @DisplayName("알림 삭제")
  public class DeleteNotificationTest {

    @Test
    @DisplayName("알림 삭제 성공")
    void delete_notification_success() throws Exception {
      // given
      UUID notificationId = UUID.randomUUID();

      // when & then
      mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
          .contentType(MediaType.APPLICATION_JSON)
          .with(csrf()))
        .andExpect(status().isNoContent());
    }
  }
}