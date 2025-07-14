package com.part4.team09.otboo.module.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.request.UserListRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.service.UserService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@EnableMethodSecurity
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  UUID userId = UUID.randomUUID();
  UserDto userDto = new UserDto(userId, LocalDateTime.now(), "test@email.com", "name", Role.ADMIN,
    null, false);

  @Nested
  @DisplayName("유저 계정 목록 조회")
  class GetUsers {

    @Test
    @DisplayName("ADMIN 권한으로 유저 계정 목록 조회 시 200")
    @WithMockUser(roles = {"ADMIN"})
    void getUsers_success() throws Exception {
      // given
      UserListRequest request = mock(UserListRequest.class);
      when(userService.changeRole(any(UUID.class), any(UserRoleUpdateRequest.class))).thenReturn(
        userDto);

      // when & then
      mockMvc.perform(get("/api/users", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isOk())
        .andDo(print());
    }

    @Test
    @DisplayName("USER 권한으로 유저 계정 목록 조회 시 403 인가 예외")
    @WithMockUser(roles = {"USER"})
    void getUsers_shouldThrow_withUserRole() throws Exception {
      // given
      UserListRequest request = mock(UserListRequest.class);
      when(userService.changeRole(any(UUID.class), any(UserRoleUpdateRequest.class))).thenReturn(
        userDto);

      // when & then
      mockMvc.perform(get("/api/users", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isForbidden())
        .andDo(print());
    }
  }

  @Nested
  @DisplayName("권한 변경 요청")
  class ChangeRole {

    @Test
    @DisplayName("ADMIN 권한으로 권한 변경 요청을 할 경우 200")
    @WithMockUser(roles = {"ADMIN"})
    void changeRole_success() throws Exception {
      // given
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
      when(userService.changeRole(any(UUID.class), any(UserRoleUpdateRequest.class))).thenReturn(
        userDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andDo(print());
    }

    @Test
    @DisplayName("USER 권한으로 권한 변경 요청을 할 경우 403 인가 예외")
    @WithMockUser(roles = {"USER"})
    void changeRole_shouldThrow_withUserRole() throws Exception {
      // given
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
      when(userService.changeRole(eq(userId), any(UserRoleUpdateRequest.class))).thenReturn(
        userDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isForbidden())
        .andDo(print());
    }
  }

  @Nested
  @DisplayName("유저 잠금 상태 변경 요청")
  class ChangeLockStatus {

    @Test
    @DisplayName("ADMIN 권한으로 유저 잠금 상태 변경 시 200")
    @WithMockUser(roles = {"ADMIN"})
    void changeLockStatus_success() throws Exception {
      // given
      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      when(userService.changeLockStatus(any(UUID.class), any(UserLockUpdateRequest.class)))
        .thenReturn(userDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isOk())
        .andDo(print());
    }

    @Test
    @DisplayName("USER 권한으로 유저 잠금 상태 변경 시 403 인가 예외")
    @WithMockUser(roles = {"USER"})
    void changeLockStatus_shouldThrow_withUserRole() throws Exception {
      // given
      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      when(userService.changeLockStatus(any(UUID.class), any(UserLockUpdateRequest.class)))
        .thenReturn(userDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isOk())
        .andDo(print());
    }
  }

}