package com.part4.team09.otboo.module.domain.clothes.service;


import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefDtoCursorResponseMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeDefMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  // 의상 속성 값 관련 서비스
  private final SelectableValueService selectableValueService;
  private final ClothesAttributeService clothesAttributeService;
  private final ClothesAttributeDefMapper clothesAttributeDefMapper;
  private final ClothesAttributeDefDtoCursorResponseMapper clothesAttributeDefDtoCursorResponseMapper;

  // 의상 속성 정의 생성
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    log.debug("의상 속정 정의 생성 시작: name = {}, values = {}", request.name(), request.selectableValues());

    ClothesAttributeDef def = clothesAttributeDefService.create(request.name());

    List<String> valueItems = selectableValueService.create(def.getId(), request.selectableValues())
        .stream().map(SelectableValue::getItem).toList();

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

    // 1. 키워드에 해당하는 id 저장
    List<UUID> defIds = clothesAttributeDefService.findIdsByKeyword(request.keywordLike());

    if (defIds.isEmpty()) {
      log.debug("조회 결과가 없습니다.");
      return clothesAttributeDefDtoCursorResponseMapper.toDto(
          List.of(), null, null, false, defIds.size(), request.sortBy(), request.sortDirection());
    }

    // 2 커서 기반 페이지네이션
    List<ClothesAttributeDef> defs = clothesAttributeDefService.findByCursor(defIds, request);

    // 2.1 hasNext 판단, 마지막 값 제거
    boolean hasNext = defs.size() > request.limit();
    if (hasNext) {
      defs = defs.subList(0, request.limit());
    }

    // 2.2 nextCursor, nextIdAfter, totalCount
    // 다음 커서 - 프로토타입에서 다음 페이지를 나타내는 것으로 보임, 사용자 커서처럼 마지막 정의 명을 반환
    String nextCursor = hasNext ? defs.get(defs.size() - 1).getName() : null;
    UUID nextIdAfter = hasNext ? defs.get(defs.size() - 1).getId() : null;
    int totalCount = defIds.size();

    // 3. 2에서 가져온 def로 의상 속성 값 가져오기
    List<UUID> pagedDefIds = defs.stream()
        .map(BaseEntity::getId)
        .toList();

    // 4. 해당 속성 값 가져오기, dto로 변환
    Map<UUID, List<SelectableValue>> valueMap = selectableValueService.findAllByAttributeDefIdIn(
            pagedDefIds).stream()
        .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

    List<ClothesAttributeDefDto> data = defs.stream().map(
            def -> clothesAttributeDefMapper.toDto(def.getId(), def.getName(),
                valueMap.get(def.getId()).stream()
                    .map(SelectableValue::getItem)
                    .toList()))
        .toList();

    ClothesAttributeDefDtoCursorResponse response = clothesAttributeDefDtoCursorResponseMapper.toDto(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        request.sortBy(),
        request.sortDirection());

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

    ClothesAttributeDef def = clothesAttributeDefService.findById(defId);
    String oldName = def.getName();
    String newName = request.name();

    ClothesAttributeDefDto response = newName.equals(oldName)
        ? updateWhenNameSame(def, request)
        : updateWhenNameChanged(def, request);

    log.debug("의상 속성 정의 수정 완료: defId = {}, name = {}, valuesSize = {}", response.id(),
        response.name(),
        response.selectableValues().size());
    return response;
  }

  // 의상 속성 정의 삭제
  public void delete(UUID defId) {
    ClothesAttributeDef def = clothesAttributeDefService.findById(defId);

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

  private ClothesAttributeDefDto updateWhenNameSame(ClothesAttributeDef def,
      ClothesAttributeDefUpdateRequest request) {

    // 1. 속성 값 가져와서 새로 수정할 값과 비교
    List<SelectableValue> oldValues = selectableValueService.findAllByAttributeDefId(def.getId());
    Set<String> newValuesSet = new HashSet<>(request.selectableValues());

    List<UUID> valueIdsForDelete = oldValues.stream()
        .filter(oldValue -> !newValuesSet.contains(oldValue.getItem())).map(BaseEntity::getId)
        .toList();

    // 2. clothesAttribute 삭제
    clothesAttributeService.deleteBySelectableValueIdIn(valueIdsForDelete);

    // 3. 속성 값 새로 생성
    List<String> newValueItems = selectableValueService.updateWhenNameSame(def.getId(),
            valueIdsForDelete, request.selectableValues()).stream().map(SelectableValue::getItem)
        .toList();

    return clothesAttributeDefMapper.toDto(def.getId(), def.getName(), newValueItems);
  }

  private ClothesAttributeDefDto updateWhenNameChanged(ClothesAttributeDef def,
      ClothesAttributeDefUpdateRequest request) {

    List<SelectableValue> oldValues = selectableValueService.findAllByAttributeDefId(def.getId());

    // 1. 정의명 변경
    ClothesAttributeDef updatedDef = clothesAttributeDefService.update(def.getId(), request.name());

    // 2. clothesAttribute 삭제
    clothesAttributeService.deleteBySelectableValueIdIn(
        oldValues.stream().map(BaseEntity::getId).toList());

    // 3. 새 속성 값 전부 생성
    List<String> newValueItems = selectableValueService.updateWhenNameChanged(updatedDef.getId(),
        request.selectableValues()).stream().map(SelectableValue::getItem).toList();

    return clothesAttributeDefMapper.toDto(updatedDef.getId(), updatedDef.getName(), newValueItems);
  }
}
