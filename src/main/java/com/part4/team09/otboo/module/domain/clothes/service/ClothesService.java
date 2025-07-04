package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
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

  private final ClothesAttributeDefService clothesAttributeDefService;
  private final SelectableValueService selectableValueService;
  private final ClothesAttributeService clothesAttributeService;

  private final ClothesRepository clothesRepository;
  private final UserRepository userRepository;

  private final ClothesMapper clothesMapper;
  private final ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;
  private final FileStorage fileStorage;

  public ClothesDto create(ClothesCreateRequest request, MultipartFile image) {

    log.debug("의상 생성 시작: ownerId = {}, name = {}", request.ownerId(), request.name());

    userRepository.findById(request.ownerId())
        .orElseThrow(() -> UserNotFoundException.withId(request.ownerId()));

    // 1. 이미지 업로드
    String url = uploadClothesImage(image);

    // 2. Clothes 엔티티 생성
    Clothes clothes = Clothes.create(request.ownerId(), request.name(), request.type(), url);
    Clothes savedClothes = clothesRepository.save(clothes);

    // 3. 선택한 속성 값이 있으면 clothesAttribute 생성
    List<ClothesAttributeWithDefDto> attributes = request.attributes().isEmpty()
        ? List.of()
        : createClothesAttributes(request, savedClothes.getId());

    ClothesDto response = clothesMapper.toDto(savedClothes.getId(), savedClothes.getOwnerId(),
        savedClothes.getName(), savedClothes.getImageUrl(), savedClothes.getType(), attributes);

    log.debug("의상 생성 완료: clothesId = {}, ownerId = {}, name = {}, url = {}, type = {}, attributesSize = {}",
        savedClothes.getId(), savedClothes.getOwnerId(), savedClothes.getName(), savedClothes.getImageUrl(),
        savedClothes.getType(), attributes.size());
    return response;
  }

  private List<ClothesAttributeWithDefDto> createClothesAttributes(
      ClothesCreateRequest request, UUID savedClothesId) {

    // 1. defIds 및 def 조회
    List<UUID> defIds = request.attributes().stream()
        .map(ClothesAttributeDto::definitionId)
        .toList();

    // 2. 정의 id, 정의 명으로 구성
    Map<UUID, String> defMap = clothesAttributeDefService.findAllByIds(defIds).stream()
        .collect(Collectors.toMap(ClothesAttributeDef::getId, ClothesAttributeDef::getName));

    // 3. 정의 id, 선택 가능한 속성으로 구성
    Map<UUID, List<SelectableValue>> selectableValueMap = selectableValueService.findAllByAttributeDefIdIn(
            defIds).stream()
        .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

    // 4. attribute 등록을 위해 selectableValue의 id 찾기
    List<UUID> selectedValueIds = request.attributes().stream()
        .map(attribute -> {
          UUID defId = attribute.definitionId();
          String valueItem = attribute.value();

          List<SelectableValue> selectableValues = selectableValueMap.getOrDefault(defId, List.of());

          SelectableValue selectedValue = selectableValues.stream()
              .filter(selectableValue -> selectableValue.getItem().equals(valueItem))
              .findFirst()
              .orElseThrow(() -> SelectableValueNotFoundException.withItem(valueItem));

          return selectedValue.getId();
        })
        .toList();

    // 5. 의상 속성 찾기
    List<ClothesAttributeWithDefDto> attributes = request.attributes().stream()
        .map(attribute -> {
          UUID defId = attribute.definitionId();
          String defName = defMap.get(defId);
          String valueItem = attribute.value();

          List<SelectableValue> selectableValues = selectableValueMap.getOrDefault(
              defId, List.of());

          // 선택하려는 값과 같은 속성 값 찾기 - 값이 없으면 예외처리
          SelectableValue selectedValue = selectableValues.stream()
              .filter(selectableValue -> selectableValue.getItem().equals(valueItem))
              .findFirst()
              .orElseThrow(() -> SelectableValueNotFoundException.withItem(valueItem));

          return clothesAttributeWithDefMapper.toDto(
              defId,
              defName,
              selectableValues.stream().map(SelectableValue::getItem).toList(),
              selectedValue.getItem()
          );
        })
        .toList();

    // 6. clothesAttribute 생성
    clothesAttributeService.create(savedClothesId, selectedValueIds);
    return attributes;
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
}
