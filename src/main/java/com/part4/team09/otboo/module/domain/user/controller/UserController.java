package com.part4.team09.otboo.module.domain.user.controller;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.common.security.constants.AuthCookieNames;
import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.util.CookieUtils;
import com.part4.team09.otboo.module.domain.auth.exception.MissingRefreshTokenException;
import com.part4.team09.otboo.module.domain.auth.service.AuthService;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDtoCursorResponse;
import com.part4.team09.otboo.module.domain.user.dto.request.PasswordUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserListRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final AuthService authService;

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
  @PreAuthorize("principal.id == #userId")
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
  @PreAuthorize("principal.id == #userId")
  @PatchMapping("/{userId}/password")
  public ResponseEntity<Void> updatePassword(
    @PathVariable UUID userId,
    @Valid @RequestBody PasswordUpdateRequest request,
    @CookieValue(name = AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
    HttpServletResponse response
  ) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw MissingRefreshTokenException.noDetail();
    }
    userService.updatePassword(userId, request);

    // 새로운 토큰 발급
    GeneratedToken generatedToken = authService.refreshTokens(refreshToken);

    // 쿠키 설정
    Cookie refreshCookie = CookieUtils.createRefreshTokenCookie(generatedToken.refreshToken());
    response.addCookie(refreshCookie);

    return ResponseEntity.noContent().build();
  }

  // 계정 목록 조회: 어드민 권한
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<UserDtoCursorResponse> getUsers(
    @RequestParam(required = false) String cursor,
    @RequestParam(required = false) UUID idAfter,
    @RequestParam(defaultValue = "20") @Min(value = 1, message = "limit은 0보다 커야합니다.") int limit,
    @RequestParam(defaultValue = "email") String sortBy,
    @RequestParam(defaultValue = "ASCENDING") SortDirection sortDirection,
    @RequestParam(required = false) String emailLike,
    @RequestParam(required = false) User.Role roleEqual,
    @RequestParam(required = false) Boolean locked
  ) {

    UserListRequest request = new UserListRequest(cursor, idAfter, limit, sortBy, sortDirection,
      emailLike, roleEqual, locked);

    UserDtoCursorResponse response = userService.getUsers(request);
    return ResponseEntity.ok(response);
  }

  // 유저 권한 변경: 어드민 권한
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDto> changeRole(
    @PathVariable UUID userId,
    @Valid @RequestBody UserRoleUpdateRequest request) {

    UserDto userDto = userService.changeRole(userId, request);
    return ResponseEntity.ok(userDto);
  }

  // 유저 잠금 상태 변경: 어드민 권한
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{userId}/lock")
  public ResponseEntity<UserDto> changeLockStatus(
    @PathVariable UUID userId,
    @Valid @RequestBody UserLockUpdateRequest request) {

    UserDto userDto = userService.changeLockStatus(userId, request);
    return ResponseEntity.ok(userDto);
  }
}
