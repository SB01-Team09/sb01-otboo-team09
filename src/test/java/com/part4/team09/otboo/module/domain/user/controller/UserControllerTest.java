package com.part4.team09.otboo.module.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void signUp() {
  }
}