package com.part4.team09.otboo.module.common.security.userdetails;

import com.part4.team09.otboo.module.common.security.constants.LoginType;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;

public interface CustomUserPrincipal {

  UUID getId();

  AuthUserDto getAuthUserDto();

  LoginType getLoginType();

  Collection<? extends GrantedAuthority> getAuthorities();
}
