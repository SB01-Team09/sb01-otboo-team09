package com.part4.team09.otboo.module.domain.auth.service;

import com.part4.team09.otboo.module.common.security.jwt.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private AuthTokenRepository authTokenRepository;

}
