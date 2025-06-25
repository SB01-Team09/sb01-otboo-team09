package com.part4.team09.otboo.module.domain.clothes.controller;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class ClothesAttributeDefController {

  // 의상 속성 정의 서비스
  private final ClothesAttributeInfoService clothesAttributeInfoService;

  // 의상 속성 정의 등록
  @PostMapping
  ResponseEntity<ClothesAttributeDefDto> create(
    @Valid @RequestBody ClothesAttributeDefCreateRequest request) {

    ClothesAttributeDefDto response = clothesAttributeInfoService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
