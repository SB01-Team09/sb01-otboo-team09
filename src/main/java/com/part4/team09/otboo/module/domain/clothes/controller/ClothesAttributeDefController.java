package com.part4.team09.otboo.module.domain.clothes.controller;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeInfoService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    log.info("의상 속성 정의 생성 요청");

    ClothesAttributeDefDto response = clothesAttributeInfoService.create(request);

    log.info("의상 속성 정의 생성 응답: {}", HttpStatus.CREATED.value());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 의상 속성 정의 수정
  @PatchMapping("/{definitionId}")
  ResponseEntity<ClothesAttributeDefDto> update(@PathVariable UUID definitionId,
      @Valid @RequestBody ClothesAttributeDefUpdateRequest request) {
    log.info("의상 속성 정의 수정 요청");

    ClothesAttributeDefDto response = clothesAttributeInfoService.update(definitionId, request);

    log.info("의상 속성 정의 수정 응답: {}", HttpStatus.OK.value());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
