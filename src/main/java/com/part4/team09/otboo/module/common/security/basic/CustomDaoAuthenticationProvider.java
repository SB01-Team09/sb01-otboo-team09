package com.part4.team09.otboo.module.common.security.basic;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.dto.TempPasswordMetadata;
import com.part4.team09.otboo.module.domain.auth.entity.UserTempPassword;
import com.part4.team09.otboo.module.domain.auth.repository.UserTempPasswordRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

  private final UserTempPasswordRepository tempPasswordRepository;

  public CustomDaoAuthenticationProvider(CustomUserDetailsService userDetailsService,
    PasswordEncoder passwordEncoder, UserTempPasswordRepository tempPasswordRepository) {
    super(userDetailsService);
    setPasswordEncoder(passwordEncoder);
    this.tempPasswordRepository = tempPasswordRepository;
  }

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
    UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    if (authentication.getCredentials() == null) {
      throw new BadCredentialsException("비밀번호는 필수입니다.");
    }

    String requestPassword = authentication.getCredentials().toString();
    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
    UUID userId = customUserDetails.getId();

    Optional<UserTempPassword> userTempPassword = tempPasswordRepository.findByUserId(userId);
    if (userTempPassword.isPresent()) {
      UserTempPassword tempPassword = userTempPassword.get();
      log.info("임시 비밀번호 존재: {} | {}", userId, tempPassword.getIssuedAt());

      // 만료된 경우, 임시 비밀번호 데이터 삭제
      if (tempPassword.isExpired(LocalDateTime.now())) {
        tempPasswordRepository.delete(tempPassword);

      } else if (getPasswordEncoder().matches(requestPassword,
        tempPassword.getTemporaryPassword())) {

        // 임시 비밀번호 로그인 성공
        Map<String, Object> details = new HashMap<>();
        TempPasswordMetadata metadata = new TempPasswordMetadata(true, tempPassword.getExpiresAt());
        details.put("tempPassword", metadata);
        authentication.setDetails(details);

        log.info("임시 비밀번호 로그인 성공");
        return;
      }
    }

    if (!getPasswordEncoder().matches(requestPassword, userDetails.getPassword())) {
      log.info("기존 비밀번호 매칭 시도");
      throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
    }

    Map<String, Object> details = new HashMap<>();
    TempPasswordMetadata metadata = TempPasswordMetadata.notUsed();
    details.put("tempPassword", metadata);
    authentication.setDetails(details);
  }
}
