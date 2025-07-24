package com.part4.team09.otboo.module.common.security;

import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import java.util.UUID;

public interface CustomUserPrincipal {

  UUID getId();

  AuthUserDto getAuthUserDto();

  LoginType getLoginType();
}
