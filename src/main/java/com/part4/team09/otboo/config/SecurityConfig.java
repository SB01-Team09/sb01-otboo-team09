package com.part4.team09.otboo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.security.Filter.JsonLoginAuthenticationFilter;
import com.part4.team09.otboo.module.common.security.Filter.JwtAuthenticationFilter;
import com.part4.team09.otboo.module.common.security.handler.CustomAccessDeniedHandler;
import com.part4.team09.otboo.module.common.security.handler.CustomAuthenticationEntryPoint;
import com.part4.team09.otboo.module.common.security.handler.JsonLoginFailureHandler;
import com.part4.team09.otboo.module.common.security.handler.JsonLoginSuccessHandler;
import com.part4.team09.otboo.module.common.security.jwt.JwtProperty;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperty.class)
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    JsonLoginAuthenticationFilter jsonLoginAuthenticationFilter) throws Exception {
    return http

      .cors(AbstractHttpConfigurer::disable)
      .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))

      // 인가 설정
      .authorizeHttpRequests(this::configureAuthorization)

      // 예외 핸들러
      .exceptionHandling(ex -> ex
        .accessDeniedHandler(customAccessDeniedHandler)
        .authenticationEntryPoint(customAuthenticationEntryPoint)
      )

      // 필터 추가
      .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
      .addFilterAt(jsonLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

      .build();
  }

  // 인가 설정
  private void configureAuthorization(
    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
  ) {
    auth
      .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
      .requestMatchers("/api/auth/**").permitAll()
      .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
      .requestMatchers("/file/**").permitAll()

      .requestMatchers("/api/**").hasRole("USER")

      .anyRequest().permitAll();
  }

  // 비밀번호 암호화
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 인증 매니저 (UserDetailsService 와 PasswordEncoder 가 자동 설정됨)
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

  // jwt 필터 등록
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenProvider, customAuthenticationEntryPoint);
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy("""
      ROLE_ADMIN > ROLE_USER
      """);
  }
}
