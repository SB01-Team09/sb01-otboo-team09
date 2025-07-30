package com.part4.team09.otboo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.security.Filter.JsonLoginAuthenticationFilter;
import com.part4.team09.otboo.module.common.security.Filter.JwtAuthenticationFilter;
import com.part4.team09.otboo.module.common.security.basic.CustomDaoAuthenticationProvider;
import com.part4.team09.otboo.module.common.security.basic.CustomUserDetailsService;
import com.part4.team09.otboo.module.common.security.basic.JsonLoginFailureHandler;
import com.part4.team09.otboo.module.common.security.basic.JsonLoginSuccessHandler;
import com.part4.team09.otboo.module.common.security.handler.CustomAccessDeniedHandler;
import com.part4.team09.otboo.module.common.security.handler.CustomAuthenticationEntryPoint;
import com.part4.team09.otboo.module.common.security.handler.CustomLogoutHandler;
import com.part4.team09.otboo.module.common.security.handler.CustomLogoutSuccessHandler;
import com.part4.team09.otboo.module.common.security.jwt.JwtProperty;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.common.security.oauth.CustomOAuth2SuccessHandler;
import com.part4.team09.otboo.module.common.security.oauth.CustomOAuth2UserService;
import com.part4.team09.otboo.module.domain.auth.repository.UserTempPasswordRepository;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperty.class)
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomLogoutHandler customLogoutHandler;
  private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
  private final CustomUserDetailsService customUserDetailsService;
  private final UserTempPasswordRepository tempPasswordRepository;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    JsonLoginAuthenticationFilter jsonLoginAuthenticationFilter,
    AuthenticationProvider customDaoAuthenticationProvider
  ) throws Exception {
    return http

      .cors(AbstractHttpConfigurer::disable)

      .csrf(AbstractHttpConfigurer::disable) // 정적 리소스 변경 후 활성화
//      .csrf(csrf -> csrf
//        .ignoringRequestMatchers("/api/auth/sign-out")
//        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))

      .authenticationProvider(customDaoAuthenticationProvider)

      // 인가 설정
      .authorizeHttpRequests(this::configureAuthorization)

      .logout(logout -> logout
        .logoutUrl("/api/auth/sign-out")
        .addLogoutHandler(customLogoutHandler)
        .logoutSuccessHandler(customLogoutSuccessHandler)
      )

      .oauth2Login(oauth2Login -> oauth2Login
        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
          .userService(customOAuth2UserService)
        )
        .successHandler(customOAuth2SuccessHandler)
        .failureUrl("/")
      )

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
      .requestMatchers("/api/sse").permitAll()

      .requestMatchers("/api/**").hasRole(Role.USER.name())

      .anyRequest().permitAll();
  }

  // 인증 매니저 (UserDetailsService 와 PasswordEncoder 가 자동 설정됨)
  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  // 인증 커스텀
  @Bean
  public AuthenticationProvider customDaoAuthenticationProvider() {
    return new CustomDaoAuthenticationProvider(
      customUserDetailsService,
      passwordEncoder,
      tempPasswordRepository
    );
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

  // 계층 설정
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy("""
      ROLE_ADMIN > ROLE_USER
      """);
  }

  // 메서드 보안 설정
  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
    RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }
}
