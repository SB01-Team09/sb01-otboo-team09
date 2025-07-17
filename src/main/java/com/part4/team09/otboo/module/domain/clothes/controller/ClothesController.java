package com.part4.team09.otboo.module.domain.clothes.controller;

import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

  private final ClothesService clothesService;

  // 의상 생성
  @PostMapping
  public ResponseEntity<ClothesDto> create(
    @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") @Valid ClothesCreateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    log.info("의상 생성 요청: name = {}, type = {}, attributeSize = {}, imageIsNull = {}",
      request.name(), request.type(), request.attributes().size(), image == null);

    UUID userId = userDetails.getId();
    ClothesDto response = clothesService.create(userId, request, image);

    log.info("의상 생성 응답: name = {}, type = {}, attributeSize = {}, imageUrl = {}",
      response.name(), response.type(), response.attributes().size(), response.imageUrl());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 의상 조회
  @GetMapping
  public ResponseEntity<ClothesDtoCursorResponse> findByCursor(
    @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit,
      @RequestParam(required = false) ClothesType typeEqual,
      @RequestParam UUID ownerId
  ) {
    log.info("의상 조회 요청: cursor = {}, idAfter = {}, limit = {}, typeEqual = {}",
      cursor, idAfter, limit, typeEqual);

    UUID userId = userDetails.getId();
    ClothesDtoCursorResponse response = clothesService.findByCursor(userId, cursor, idAfter, limit,
      typeEqual, ownerId);

    log.info("의상 조회 응답: clothesListSize = {}, nextCursor = {}, nextIdAfter = {}, hasNext = {}, "
        + "totalCount = {}, sortBy = {}, sortDirection = {}", response.data().size(),
      response.nextCursor(),
      response.nextIdAfter(), response.hasNext(), response.totalCount(), response.sortBy(),
      response.sortDirection());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // 의상 수정
  @PatchMapping("/{clothesId}")
  public ResponseEntity<ClothesDto> update(
    @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID clothesId,
      @RequestPart("request") @Valid ClothesUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    log.info("의상 수정 요청: name = {}, type = {}, attributeSize = {}, imageIsNull = {}",
      request.name(), request.type(), request.attributes().size(), image == null);

    UUID userId = userDetails.getId();
    ClothesDto response = clothesService.update(userId, clothesId, request, image);

    log.info("의상 수정 응답: name = {}, type = {}, attributeSize = {}, imageUrl = {}",
      response.name(), response.type(), response.attributes().size(), response.imageUrl());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @DeleteMapping("/{clothesId}")
  public ResponseEntity<Void> delete(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable UUID clothesId) {
    log.info("의상 삭제 요청");

    UUID userId = userDetails.getId();
    clothesService.delete(userId, clothesId);

    log.info("의상 삭제 응답: {}", HttpStatus.NO_CONTENT.value());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
