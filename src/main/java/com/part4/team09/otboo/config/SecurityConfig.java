package com.part4.team09.otboo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.security.Filter.JsonLoginAuthenticationFilter;
import com.part4.team09.otboo.module.domain.auth.handler.JsonLoginFailureHandler;
import com.part4.team09.otboo.module.domain.auth.handler.JsonLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    JsonLoginAuthenticationFilter jsonLoginAuthenticationFilter
  ) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable) // disable
      .authorizeHttpRequests(this::configureAuthorization) // 인가 정책

      // 필터 등록
      .addFilterAt(jsonLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    ;
    return http.build();
  }

  // 인가 설정
  private void configureAuthorization(
    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
  ) {
    auth
      .anyRequest().permitAll();
  }

  // 비밀번호 암호화
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 인증 매니저 (UserDetailsService 와 PasswordEncoder 가 자동설정)
  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  // json 로그인 필터 등록
  @Bean
  public JsonLoginAuthenticationFilter jsonLoginAuthenticationFilter(
    AuthenticationManager authManager,
    ObjectMapper objectMapper,
    JsonLoginSuccessHandler successHandler,
    JsonLoginFailureHandler failureHandler
  ) {
    JsonLoginAuthenticationFilter filter = new JsonLoginAuthenticationFilter(authManager,
      objectMapper);
    filter.setAuthenticationSuccessHandler(successHandler);
    filter.setAuthenticationFailureHandler(failureHandler);

    return filter;
  }
}
