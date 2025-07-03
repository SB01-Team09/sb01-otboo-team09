package com.part4.team09.otboo.module.domain.user.controller;

import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.request.PasswordUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  // 유저 생성
  @PostMapping
  public ResponseEntity<UserDto> signUp(@Valid @RequestBody UserCreateRequest request) {
    UserDto userDto = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  // 프로필 조회
  @GetMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> getProfile(@PathVariable UUID userId) {
    ProfileDto profileDto = userService.getProfile(userId);
    return ResponseEntity.ok(profileDto);
  }

  // 프로필 수정
  @PatchMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> updateProfile(
    @PathVariable UUID userId,
    @Valid @RequestPart(name = "request") ProfileUpdateRequest request,
    @RequestPart(name = "image", required = false) MultipartFile image
  ) {
    ProfileDto profileDto = userService.updateProfile(userId, request, image);
    return ResponseEntity.ok(profileDto);
  }

  // 비밀번호 변경
  @PatchMapping("/{userId}/password")
  public ResponseEntity<Void> updatePassword(
    @PathVariable UUID userId,
    @Valid @RequestBody PasswordUpdateRequest request
  ) {
    userService.updatePassword(userId, request);
    return ResponseEntity.noContent().build();
  }

  // 유저 권한 변경: 어드민 권한
  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDto> changeRole(
    @PathVariable UUID userId,
    @Valid @RequestBody UserRoleUpdateRequest request) {

    UserDto userDto = userService.changeRole(userId, request);
    return ResponseEntity.ok(userDto);
  }

  // 유저 잠금 상태 변경: 어드민 권한
  @PatchMapping("/{userId}/lock")
  public ResponseEntity<UserDto> changeLockStatus(
    @PathVariable UUID userId,
    @Valid @RequestBody UserLockUpdateRequest request) {

    UserDto userDto = userService.changeLockStatus(userId, request);
    return ResponseEntity.ok(userDto);
  }
}
