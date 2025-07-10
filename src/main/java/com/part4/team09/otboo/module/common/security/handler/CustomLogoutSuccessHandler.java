package com.part4.team09.otboo.module.common.security.handler;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutSuccessHandler extends HttpStatusReturningLogoutSuccessHandler {

  public CustomLogoutSuccessHandler() {
    super(HttpStatus.NO_CONTENT);
  }
}
