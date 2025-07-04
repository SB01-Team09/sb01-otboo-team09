package com.part4.team09.otboo.module.domain.clothes.controller;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

  private final ClothesService clothesService;

  @PostMapping
  public ResponseEntity<ClothesDto> create(
      @RequestPart("request") @Valid ClothesCreateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    log.info("의상 생성 요청");

    ClothesDto response = clothesService.create(request, image);

    log.info("의상 생성 응답: {}", HttpStatus.CREATED.value());
    return  ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

}
