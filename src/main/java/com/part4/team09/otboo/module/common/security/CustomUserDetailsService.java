package com.part4.team09.otboo.module.common.security;

import com.part4.team09.otboo.module.domain.auth.mapper.AuthUserMapper;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 유저 정보를 조회하여 SecurityContext에 저장할 인증 객체 준비
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  public final UserRepository userRepository;
  public final AuthUserMapper authUserMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

    return CustomUserDetails.createWithPassword(
      authUserMapper.toAuthUserDto(user),
      user.getPassword());
  }
}
