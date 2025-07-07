package com.part4.team09.otboo.module.common.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.auth.dto.LoginRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  UserRepository userRepository;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  private String userEmail = "user@test.com";
  private String password = "password!";
  private UUID userId;

  @BeforeEach
  void setUpUser() {
    User user = User.createUser(
      userEmail,
      "testUser",
      passwordEncoder.encode(password)
    );
    userId = userRepository.save(user).getId();
  }

  @DisplayName("인증 관련 테스트")
  @Nested
  class AuthenticationTests {

    @DisplayName("로그인 성공 시 토큰 발급")
    @Test
    void login_success() throws Exception {
      LoginRequest request = new LoginRequest(userEmail, password);

      mockMvc.perform(post("/api/auth/sign-in")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("refresh_token"))
        .andExpect(content().string(Matchers.not("")))
        .andDo(print());
    }

    @DisplayName("잘못된 아이디로 로그인 실패 시 예외 발생")
    @Test
    void login_shouldThrowException_ifUserNotExist() throws Exception {
      LoginRequest request = new LoginRequest("faild@test.com", password);

      mockMvc.perform(post("/api/auth/sign-in")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.exceptionName").value("AuthenticationException"))
        .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."))
        .andDo(print());
    }

    @DisplayName("유효한 액세스 토큰으로 접근 시 인증 성공")
    @Test
    void accessToken_authentication_success() throws Exception {
      String accessToken = getAccessToken();

      mockMvc.perform(get("/api/users/" + userId + "/profiles")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());
    }
  }

  @DisplayName("유효하지 않은 액세스 토큰으로 접근 시 인증 실패와 함께 에외")
  @Test
  void accessToken_shouldThrowException_ifTokenInvalid() throws Exception {
    String accessToken = getAccessToken();

    mockMvc.perform(get("/api/users/" + userId + "/profiles")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken + "no"))
      .andExpect(status().isUnauthorized())
      .andDo(print());
  }

  private String getAccessToken() throws Exception {
    LoginRequest request = new LoginRequest(userEmail, password);

    MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .with(csrf()))
      .andExpect(status().isOk())
      .andReturn();

    return result.getResponse().getContentAsString().replace("\"", "");
  }
}
