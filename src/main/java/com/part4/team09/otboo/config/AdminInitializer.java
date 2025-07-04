package com.part4.team09.otboo.config;

import com.part4.team09.otboo.module.domain.user.dto.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminInitializer implements ApplicationRunner {

  private final UserService userService;
  private final String adminName;
  private final String adminPassword;
  private final String adminEmail;

  public AdminInitializer(
    UserService userService,
    @Value("${otboo.admin.name}") String adminName,
    @Value("${otboo.admin.email}") String adminEmail,
    @Value("${otboo.admin.password}") String adminPassword
  ) {
    this.userService = userService;
    this.adminName = adminName;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
//    initializeAdmin();
  }

  private void initializeAdmin() {

    UserCreateRequest request = new UserCreateRequest(
      adminEmail, adminName, adminPassword
    );

    try {
      userService.createAdmin(request);
      log.info("관리자 계정이 생성되었습니다. (email: {})", adminEmail);
    } catch (EmailAlreadyExistsException e) {
      log.info("관리자 계정이 이미 존재합니다. (email: {})", adminEmail);
    }
  }
}
