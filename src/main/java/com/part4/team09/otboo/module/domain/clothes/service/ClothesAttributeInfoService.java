package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.domain.clothes.assembler.ClothesAttributeDefDtoAssembler;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefDtoCursorResponseMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
// 의상 속성 명, 속성 값을 조합해주는 클래스
public class ClothesAttributeInfoService {

  // 의상 속성 정의 관련 서비스
  private final ClothesAttributeDefService clothesAttributeDefService;
  private final SelectableValueService selectableValueService;
  private final ClothesAttributeService clothesAttributeService;

  private final ClothesAttributeDefRepository clothesAttributeDefRepository;

  private final ClothesAttributeDefMapper clothesAttributeDefMapper;
  private final ClothesAttributeDefDtoCursorResponseMapper clothesAttributeDefDtoCursorResponseMapper;

  private final ClothesAttributeDefDtoAssembler clothesAttributeDefDtoAssembler;

  // 의상 속성 정의 생성
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    log.debug("의상 속정 정의 생성 시작: name = {}, values = {}", request.name(), request.selectableValues());

    // 속성 정의 생성
    ClothesAttributeDef def = clothesAttributeDefService.create(request.name());

    // 속성 값 생성
    List<String> valueItems = selectableValueService.create(def.getId(), request.selectableValues()).stream()
        .map(SelectableValue::getItem)
        .toList();

    // 반환 값 생성
    ClothesAttributeDefDto response = clothesAttributeDefMapper.toDto(def.getId(), def.getName(),
        valueItems);

    log.debug("의상 속성 정의 생성 완료: defId = {}, name = {}, values = {}", response.id(), response.name(),
        response.selectableValues());

    return response;
  }

  // 커서 기반 의상 속성 정의 탐색
  @Transactional(readOnly = true)
  public ClothesAttributeDefDtoCursorResponse findByCursor(ClothesAttributeDefFindRequest request) {

    log.debug("의상 속성 정의 찾기 시작: cursor = {}, idAfter = {}, limit = {}, sortBy = {}, "
            + "sortDirection = {}, keywordLike = {}", request.cursor(), request.idAfter(),
        request.limit(), request.sortBy(), request.sortDirection(), request.keywordLike());

    // 키워드를 포한하는 속성 명 / 속성 값 조회
    List<UUID> defIds = clothesAttributeDefService.findIdsByKeyword(request.keywordLike());

    // 키워드를 포함하는 속성이 없을 경우 빈리스트 반환
    if (defIds.isEmpty()) {
      log.debug("조회 결과가 없습니다.");
      return clothesAttributeDefDtoCursorResponseMapper.toDto(
          List.of(),
          null,
          null,
          false, defIds.size(),
          request.sortBy(),
          request.sortDirection()
      );
    }

    // 커서 기반 페이지네이션
    List<ClothesAttributeDef> defs = clothesAttributeDefService.findByCursor(defIds, request);

    // 반환 값 생성
    List<ClothesAttributeDefDto> data = clothesAttributeDefDtoAssembler.assemble(defs);

    boolean hasNext = defs.size() > request.limit();
    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext) {
      defs = defs.subList(0, request.limit());
      ClothesAttributeDef lastDef = defs.get(defs.size() - 1);
      nextCursor = lastDef.getName();
      nextIdAfter = lastDef.getId();
    }
    int totalCount = defIds.size();

    ClothesAttributeDefDtoCursorResponse response = clothesAttributeDefDtoCursorResponseMapper.toDto(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        request.sortBy(),
        request.sortDirection()
    );

    log.debug("의상 속성 정의 조회 완료: dataSize = {}, nextCursor = {}, nextIdAfter = {}, hasNext = {}, "
            + "totalCount = {}, sortBy = {}, sortDirection = {}", response.data().size(),
        response.nextCursor(), response.nextIdAfter(), response.hasNext(), response.totalCount(),
        response.sortBy(), response.sortDirection());
    return response;
  }

  // 의상 속성 정의 수정
  public ClothesAttributeDefDto update(UUID defId, ClothesAttributeDefUpdateRequest request) {
    log.debug("의상 속성 정의 수정 시작: defId = {}, newName = {}, newValuesSize = {}", defId, request.name(),
        request.selectableValues().size());

    ClothesAttributeDef def = getClothesAttributeDefOrThrow(defId);

    // 이전에 존재하던 속성 값 조회
    List<SelectableValue> oldValues = selectableValueService.findAllByAttributeDefId(def.getId());

    // 새로 생성 할 속성 값을 선택하기 위한 자료구조
    Set<String> oldValueNameSet = oldValues.stream()
        .map(SelectableValue::getItem)
        .collect(Collectors.toSet());

    // 새로 생성할 속성 값 id
    List<String> newSelectableValues = request.selectableValues().stream()
        .filter(value -> !oldValueNameSet.contains(value))
        .toList();

    // 삭제할 속성 값을 선택하기 위한 자료구조
    Set<String> newValueNameSet = new HashSet<>(request.selectableValues());

    // 삭제할 속성 값 id
    List<UUID> valueIdsForDelete = oldValues.stream()
        .filter(oldValue -> !newValueNameSet.contains(oldValue.getItem()))
        .map(BaseEntity::getId)
        .toList();

    // 속성 정의 명이 업데이트 되는 경우
    if (!request.name().equals(def.getName())) {

      // 속성 정의 명 업데이트
      clothesAttributeDefService.update(def.getId(), request.name());

      // 속성 명이 바뀌면 속성 값 연관 전부 삭제
      List<UUID> oldValueIds = oldValues.stream()
          .map(BaseEntity::getId)
          .toList();
      clothesAttributeService.deleteBySelectableValueIdIn(oldValueIds);
    } else {

      // 속성 명이 바뀌지 않았으면 바뀐 속성 값 연관만 삭제
      clothesAttributeService.deleteBySelectableValueIdIn(valueIdsForDelete);
    }

    // 리퀘스트에 없는 속성 값 삭제
    selectableValueService.deleteByIdIn(valueIdsForDelete);

    // 리퀘스트에만 있는 속성 값 생성
    selectableValueService.create(def.getId(), newSelectableValues);

    // 선택 가능한 값 조회
    List<String> values = selectableValueService.findAllByAttributeDefId(def.getId()).stream()
        .map(SelectableValue::getItem)
        .toList();

    ClothesAttributeDefDto response = clothesAttributeDefMapper.toDto(defId, def.getName(), values);

    log.debug("의상 속성 정의 수정 완료: defId = {}, name = {}, valuesSize = {}", response.id(),
        response.name(),
        response.selectableValues().size());
    return response;
  }

  // 의상 속성 정의 삭제
  public void delete(UUID defId) {
    ClothesAttributeDef def = getClothesAttributeDefOrThrow(defId);

    log.debug("의상 속성 정의 삭제 시작: defId = {}, name = {}", def.getId(), def.getName());

    List<UUID> valueIds = selectableValueService.findAllByAttributeDefId(defId).stream()
        .map(BaseEntity::getId).toList();

    // 연관된 ClothesAttribute 삭제
    clothesAttributeService.deleteBySelectableValueIdIn(valueIds);

    // SelectValue 삭제
    selectableValueService.deleteByIdIn(valueIds);

    // def 삭제
    clothesAttributeDefService.delete(defId);

    log.debug("의상 속성 정의 삭제 완료: defId = {}", defId);
  }

  private ClothesAttributeDef getClothesAttributeDefOrThrow(UUID defId) {

    return clothesAttributeDefRepository.findById(defId)
        .orElseThrow(() -> {
          log.warn("의상 속성 정의를 찾을 수 없습니다. defId = {}", defId);
          return ClothesAttributeDefNotFoundException.withId(defId);
        });
  }
}
