package com.part4.team09.otboo.module.common.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.security.basic.CustomDaoAuthenticationProvider;
import com.part4.team09.otboo.module.common.security.basic.CustomUserDetailsService;
import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.entity.UserTempPassword;
import com.part4.team09.otboo.module.domain.auth.repository.UserTempPasswordRepository;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CustomDaoAuthenticationProviderTest {

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private CustomUserDetailsService customUserDetailsService;
  @Mock
  private UserTempPasswordRepository tempPasswordRepository;

  @InjectMocks
  private CustomDaoAuthenticationProvider customDaoAuthenticationProvider;

  private UUID userId = UUID.randomUUID();
  private String rawPassword = "password!";
  private String encodedPassword = "encodedPassword!";

  @Test
  @DisplayName("비밀번호가 없는 경우 예외를 던진다.")
  void authCheck_throwsException_whenCredentialsIsNull() {
    // given
    UsernamePasswordAuthenticationToken authToken = createAuthToken(null);
    CustomUserDetails userDetails = createUserDetails();

    // when & then
    assertThrows(BadCredentialsException.class, () ->
      customDaoAuthenticationProvider.additionalAuthenticationChecks(userDetails, authToken));
  }

  @Test
  @DisplayName("임시 비밀번호가 일치하면 authenticatoin.details에 임시 비밀번호 정보가 추가된다.")
  void authCheck_addDetail_whenTempPasswordMatches() {
    // given
    CustomUserDetails userDetails = createUserDetails();
    UsernamePasswordAuthenticationToken auth = spy(createAuthToken(rawPassword));

    // 유효성 검사
    UserTempPassword tempPassword = mock(UserTempPassword.class);
    when(tempPasswordRepository.findByUserId(userId)).thenReturn(Optional.of(tempPassword));
    when(tempPassword.isExpired(any())).thenReturn(false);

    // 매칭 확인
    when(tempPassword.getTemporaryPassword()).thenReturn("encodedTempPw");
    when(passwordEncoder.matches(eq(rawPassword), eq("encodedTempPw"))).thenReturn(true);

    // when
    customDaoAuthenticationProvider.additionalAuthenticationChecks(userDetails, auth);

    // then
    verify(auth).setDetails(anyMap());
    verify(passwordEncoder, never()).matches(eq(rawPassword), eq(encodedPassword));
  }


  @Test
  @DisplayName("임시 비밀번호가 만료되면 임시 비밀번호 데이터 삭제 후 일반 로그인을 시도한다.")
  void authCheck_removeTempPassword_And_tryNomalLogin_whenTempPasswordExpired() {
    // given
    CustomUserDetails userDetails = createUserDetails();
    UsernamePasswordAuthenticationToken auth = spy(createAuthToken(rawPassword));

    UserTempPassword temp = mock(UserTempPassword.class);
    when(temp.isExpired(any())).thenReturn(true);
    when(tempPasswordRepository.findByUserId(userId)).thenReturn(Optional.of(temp));
    when(passwordEncoder.matches(eq(rawPassword), eq(encodedPassword))).thenReturn(true);

    // when
    customDaoAuthenticationProvider.additionalAuthenticationChecks(userDetails, auth);

    // then
    verify(tempPasswordRepository).delete(temp);
    verify(auth).setDetails(anyMap());
  }

  @Test
  @DisplayName("일반 비밀번호가 일치하지 않으면 예외가 발생한다")
  void authCheck_normal_password_throws_exception_whenMismatch() {
    CustomUserDetails userDetails = createUserDetails();
    UsernamePasswordAuthenticationToken auth = createAuthToken("wrongPassword");

    when(tempPasswordRepository.findByUserId(userId)).thenReturn(Optional.empty());
    when(passwordEncoder.matches(eq("wrongPassword"), eq(encodedPassword))).thenReturn(false);

    assertThrows(BadCredentialsException.class, () ->
      customDaoAuthenticationProvider.additionalAuthenticationChecks(userDetails, auth));
  }


  private CustomUserDetails createUserDetails() {
    return CustomUserDetails.createWithPassword(
      new AuthUserDto(userId, "email", "name", false, Role.USER),
      encodedPassword
    );
  }

  private UsernamePasswordAuthenticationToken createAuthToken(String password) {
    return new UsernamePasswordAuthenticationToken("email", password);
  }
}