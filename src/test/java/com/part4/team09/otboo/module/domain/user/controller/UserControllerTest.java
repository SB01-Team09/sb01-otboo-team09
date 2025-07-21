package com.part4.team09.otboo.module.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.service.AuthService;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDtoCursorResponse;
import com.part4.team09.otboo.module.domain.user.dto.request.PasswordUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest;
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
import org.springframework.mock.web.MockMultipartFile;
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

  @MockitoBean
  private AuthService authService;

  UUID userId = UUID.randomUUID();
  UserDto userDto = new UserDto(userId, LocalDateTime.now(), "test@email.com", "name", Role.ADMIN,
    null, false);

  @Test
  @DisplayName("본인 계정일 때 프로필 수정 200")
  void updateProfile_success() throws Exception {
    // given
    ProfileUpdateRequest updateRequest = mock(ProfileUpdateRequest.class);
    ProfileDto response = mock(ProfileDto.class);

    AuthUserDto authUserDto = new AuthUserDto(userId, "email", "name", false, Role.USER);
    CustomUserDetails userDetails = CustomUserDetails.create(authUserDto);

    when(userService.updateProfile(any(UUID.class), any(ProfileUpdateRequest.class),
      isNull())).thenReturn(response);

    MockMultipartFile jsonPart = new MockMultipartFile(
      "request",
      "",
      "application/json",
      objectMapper.writeValueAsBytes(updateRequest)
    );

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
        .file(jsonPart)
        .with(user(userDetails))
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .with(request -> {
          request.setMethod("PATCH");
          return request;
        })
        .with(csrf()))
      .andExpect(status().isOk())
      .andDo(print());
  }

  @Test
  @DisplayName("본인 계정이 아닐 때 비밀번호 변경 403 인가 예외")
  void updatePassword_shouldThrow_IsNotOwner() throws Exception {
    // given
    PasswordUpdateRequest request = mock(PasswordUpdateRequest.class);

    UUID anotherUserId = UUID.randomUUID();
    AuthUserDto authUserDto = new AuthUserDto(anotherUserId, "email", "name", false, Role.USER);
    CustomUserDetails userDetails = CustomUserDetails.create(authUserDto);

    doNothing().when(userService).updatePassword(any(UUID.class), any(PasswordUpdateRequest.class));

    // when & then
    mockMvc.perform(get("/api/users", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .with(user(userDetails))
        .with(csrf()))
      .andExpect(status().isForbidden())
      .andDo(print());
  }


  @Nested
  @DisplayName("유저 계정 목록 조회")
  class GetUsers {

    @Test
    @DisplayName("ADMIN 권한으로 유저 계정 목록 조회 시 200")
    @WithMockUser(roles = {"ADMIN"})
    void getUsers_success() throws Exception {
      // given
      UserListRequest request = mock(UserListRequest.class);
      UserDtoCursorResponse response = mock(UserDtoCursorResponse.class);
      when(userService.getUsers(any(UserListRequest.class))).thenReturn(response);

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
      UserDtoCursorResponse response = mock(UserDtoCursorResponse.class);
      when(userService.getUsers(any(UserListRequest.class))).thenReturn(response);

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
        .andExpect(status().isForbidden())
        .andDo(print());
    }
  }

}