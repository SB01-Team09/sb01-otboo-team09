package com.part4.team09.otboo.module.domain.user.controller;

import com.part4.team09.otboo.module.domain.user.dto.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> signUp(@Valid @RequestBody UserCreateRequest request) {

    UserDto userDto = userService.createUser(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }
}
