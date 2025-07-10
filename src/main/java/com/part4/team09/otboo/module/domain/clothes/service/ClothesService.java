package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.assembler.ClothesAttributeWithDefDtoAssembler;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.BadRequestException;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesDtoCursorResponseMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.FileUploadFailedException;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClothesService {

  private final SelectableValueService selectableValueService;
  private final ClothesAttributeService clothesAttributeService;

  private final ClothesRepository clothesRepository;
  private final UserRepository userRepository;
  private final OotdRepository ootdRepository;
  private final ClothesRepositoryQueryDSL clothesRepositoryQueryDSL;

  private final ClothesMapper clothesMapper;
  private final ClothesDtoCursorResponseMapper clothesDtoCursorResponseMapper;
  private final ClothesAttributeWithDefDtoAssembler clothesAttributeWithDefDtoAssembler;
  private final FileStorage fileStorage;

  public ClothesDto create(ClothesCreateRequest request, MultipartFile image) {

    log.debug("의상 생성 시작: ownerId = {}, name = {}", request.ownerId(), request.name());

    validateUserExists(request.ownerId());

    String url = uploadClothesImage(image);

    Clothes clothes = Clothes.create(request.ownerId(), request.name(), request.type(), url);
    Clothes savedClothes = clothesRepository.save(clothes);

    // 연관 생성
    List<ClothesAttributeWithDefDto> attributeDtos = List.of();

    if (!request.attributes().isEmpty()) {
      createClothesAttributes(request.attributes(), clothes.getId());
      attributeDtos = clothesAttributeWithDefDtoAssembler.assemble(clothes.getId());
    }

    ClothesDto response = clothesMapper.toDto(savedClothes.getId(), savedClothes.getOwnerId(),
        savedClothes.getName(), savedClothes.getImageUrl(), savedClothes.getType(), attributeDtos);

    log.debug(
        "의상 생성 완료: clothesId = {}, ownerId = {}, name = {}, url = {}, type = {}, attributesSize = {}",
        savedClothes.getId(), savedClothes.getOwnerId(), savedClothes.getName(),
        savedClothes.getImageUrl(), savedClothes.getType(), attributeDtos.size());
    return response;
  }

  @Transactional(readOnly = true)
  public ClothesDtoCursorResponse findByCursor(String cursor, UUID idAfter, int limit,
      ClothesType typeEqual, UUID ownerId) {
    log.debug("의상 조회 시작: cursor = {}, idAfter = {}, limit = {}, typeEqual = {}, ownerId = {}",
        cursor, idAfter, limit, typeEqual, ownerId);

    if (limit <= 0) {
      log.warn("유효하지 않은 limit입니다.: limit = {}", limit);
      throw BadRequestException.withLimit(limit);
    }

    validateUserExists(ownerId);

    // 프로토타입 기준
    if (typeEqual == null) {
      typeEqual = ClothesType.TOP;
    }
    String sortBy = "createdAt";
    SortDirection sortDirection = SortDirection.DESCENDING;

    List<Clothes> clothesList = clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit,
        typeEqual,
        ownerId, sortBy, sortDirection);

    boolean hasNext = clothesList.size() > limit;
    String nextCursor = null;
    UUID nexIdAfter = null;
    int totalCount = clothesRepository.countByOwnerIdAndType(ownerId, typeEqual);

    if (hasNext) {
      clothesList = clothesList.subList(0, limit);
      Clothes lastClothes = clothesList.get(clothesList.size() - 1);
      nextCursor = lastClothes.getCreatedAt().toString();
      nexIdAfter = lastClothes.getId();
    }

    List<ClothesDto> data = clothesList.isEmpty()
        ? List.of()
        : clothesList.stream()
            .map(clothes -> clothesMapper.toDto(clothes.getId(), clothes.getOwnerId(),
                clothes.getName(), clothes.getImageUrl(), clothes.getType(),
                clothesAttributeWithDefDtoAssembler.assemble(clothes.getId())))
            .toList();

    ClothesDtoCursorResponse response = clothesDtoCursorResponseMapper.toDto(data, nextCursor,
        nexIdAfter, hasNext, totalCount, sortBy, sortDirection);

    log.debug("의상 조회 완료: dataSize = {}, nexCursor = {}, nextIdAfter = {}, hasNext = {}, "
            + "totalCount = {}, sortBy = {}, sortDirection = {}", data, nextCursor, nexIdAfter, hasNext,
        totalCount, sortBy, sortDirection);
    return response;
  }

  public ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image) {

    Clothes clothes = clothesRepository.findById(clothesId).orElseThrow(() -> {
      log.warn("의상이 존재하지 않습니다. id = {}", clothesId);
      return ClothesNotFoundException.withId(clothesId);
    });

    validateUserExists(clothes.getOwnerId());

    // 1. 이미지 삭제
    if (clothes.getImageUrl() != null) {
      removeClothesImage(clothes.getImageUrl());
    }

    // 2. 이미지 업로드
    String newUrl = uploadClothesImage(image);

    // 3. clothes 엔티티 수정
    clothes.update(request.name(), request.type(), newUrl);

    // 4. clothesAttribute 삭제
    clothesAttributeService.deleteAllByClothesId(clothes.getId());

    List<ClothesAttributeWithDefDto> attributeDtos = List.of();

    if (!request.attributes().isEmpty()) {
      createClothesAttributes(request.attributes(), clothes.getId());
      attributeDtos = clothesAttributeWithDefDtoAssembler.assemble(clothes.getId());
    }

    ClothesDto response = clothesMapper.toDto(clothes.getId(), clothes.getOwnerId(),
        clothes.getName(), clothes.getImageUrl(), clothes.getType(), attributeDtos);

    log.debug(
        "의상 생성 완료: clothesId = {}, ownerId = {}, name = {}, url = {}, type = {}, attributesSize = {}",
        clothes.getId(), clothes.getOwnerId(), clothes.getName(),
        clothes.getImageUrl(), clothes.getType(), attributeDtos.size());
    return response;
  }

  public void delete(UUID clothesId) {

    log.debug("의상 삭제 시작: clothesId = {}", clothesId);

    // 1. 의상이 존재하는 지 확인
    Clothes clothes = clothesRepository.findById(clothesId).orElseThrow(() -> {
      log.warn("의상을 찾을 수 없습니다. clothesId = {}", clothesId);
      return ClothesNotFoundException.withId(clothesId);
    });

    // 2. ootd 삭제
    ootdRepository.deleteByClothesId(clothesId);

    // 3. 의상 정의 연관 삭제
    clothesAttributeService.deleteAllByClothesId(clothesId);

    // 4. 의상 이미지 삭제
    if (clothes.getImageUrl() != null) {
      fileStorage.remove(clothes.getImageUrl());
    }

    // 5. 의상 삭제
    clothesRepository.deleteById(clothes.getId());
    log.debug("의상 삭제 완료: clothesId = {}", clothesId);
  }

  private void createClothesAttributes(List<ClothesAttributeDto> attributeDtos,
      UUID clothesId) {

    List<UUID> defIds = attributeDtos.stream()
        .map(ClothesAttributeDto::definitionId)
        .distinct()
        .toList();

    Map<UUID, List<SelectableValue>> selectableValueMap = selectableValueService.findAllByAttributeDefIdIn(
            defIds).stream()
        .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

    List<SelectableValue> selectedValues = attributeDtos.stream()
        .map(attribute -> selectableValueMap.getOrDefault(attribute.definitionId(), List.of())
            .stream()
            .filter(value -> value.getItem().equals(attribute.value()))
            .findFirst()
            .orElseThrow(() -> {
              log.warn("해당 의상 속성 값이 없습니다. value = {}", attribute.value());
              return SelectableValueNotFoundException.withItem(attribute.value());
            }))
        .toList();

    clothesAttributeService.create(clothesId,
        selectedValues.stream()
            .map(SelectableValue::getId)
            .toList());
  }

  // 이미지 업로드
  private String uploadClothesImage(MultipartFile image) {

    log.debug("이미지 업로드 시작");

    if (image == null || image.isEmpty()) {
      log.debug("이미지가 없습니다. return = null");
      return null;
    }

    try {
      String url = fileStorage.upload(image, FileDomain.CLOTHES_IMAGE);

      log.debug("이미지 업로드 완료: url = {}", url);
      return url;
    } catch (FileUploadFailedException e) {

      log.warn("message = {}, details = {}", e.getMessage(), e.getDetails());
      return null;
    }
  }

  // 이미지 삭제
  private void removeClothesImage(String url) {
    log.debug("이미지 삭제 시작");

    if (url == null) {
      log.debug("이미지가 없습니다.");
    } else {

      try {
        fileStorage.remove(url);

        log.debug("이미지 삭제 완료: url = {}", url);
      } catch (FileUploadFailedException e) {

        log.warn("message = {}, details = {}", e.getMessage(), e.getDetails());
      }
    }
  }

  private void validateUserExists(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.warn("사용자가 존재하지 않습니다. id = {}", userId);
      throw UserNotFoundException.withId(userId);
    }
  }
}

