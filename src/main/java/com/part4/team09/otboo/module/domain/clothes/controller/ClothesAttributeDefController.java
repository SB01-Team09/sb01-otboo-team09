package com.part4.team09.otboo.module.domain.clothes.controller;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeInfoService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  // 의상 속성 정의 조회
  @GetMapping
  ResponseEntity<ClothesAttributeDefDtoCursorResponse> findByCursor(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "ASCENDING") SortDirection sortDirection,
      @RequestParam(required = false) String keywordLike
  ) {
    log.info("의상 속성 정의 조회 요청");

    ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(
        cursor, idAfter, limit, sortBy, sortDirection, keywordLike);

    ClothesAttributeDefDtoCursorResponse response = clothesAttributeInfoService.findByCursor(request);

    log.info("의상 속성 정의 조회 응답: {}", HttpStatus.OK.value());
    return ResponseEntity.status(HttpStatus.OK).body(response);
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

  // 의상 속성 정의 삭제
  @DeleteMapping("/{definitionId}")
  ResponseEntity<Void> delete(@PathVariable UUID definitionId) {
    log.info("의상 속성 정의 삭제 요청");

    clothesAttributeInfoService.delete(definitionId);

    log.info("의상 속성 정의 삭제 응답: {}", HttpStatus.NO_CONTENT.value());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
