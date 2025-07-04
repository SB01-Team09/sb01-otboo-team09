package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.BadRequestException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefAlreadyExistsException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesAttributeDefRepositoryQueryDSL;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ClothesAttributeDefService {

  private static final Set<String> VALID_SORT_BY = Set.of("name", "createdAt");

  private final ClothesAttributeDefRepository clothesAttributeDefRepository;
  private final ClothesAttributeDefRepositoryQueryDSL clothesAttributeDefRepositoryQueryDSL;

  // 의상 속성 정의 명 생성
  public ClothesAttributeDef create(String name) {

    log.debug("의상 속성 정의 명 생성 시작: name = {}", name);

    // 프로토타입에는 없지만 이름 중복 검사
    if (clothesAttributeDefRepository.existsByName(name)) {
      log.warn("이미 존재하는 의상 속성 정의 명입니다. name = {}", name);
      throw ClothesAttributeDefAlreadyExistsException.withName(name);
    }

    ClothesAttributeDef def = ClothesAttributeDef.create(name);

    ClothesAttributeDef savedDef = clothesAttributeDefRepository.save(def);

    log.debug("의상 속성 정의 명 생성 완료: defId = {}, name = {}", savedDef.getId(), savedDef.getName());
    return savedDef;
  }

  // 의상 속성 정의 명 찾기
  public ClothesAttributeDef findById(UUID defId) {
    log.debug("의상 속성 정의 명 조회 시작: defId = {}", defId);

    ClothesAttributeDef def = clothesAttributeDefRepository.findById(defId)
        .orElseThrow(() -> {
            log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
            return ClothesAttributeDefNotFoundException.withId(defId);
        });

    log.debug("의상 속성 정의 명 조회 완료: defId = {}, name = {}", def.getId(), def.getName());
    return def;
  }

  // 키워드로 id 찾기
  public List<UUID> findIdsByKeyword(String keyword) {

    log.debug("의상 속성 키워드로 조회 시작: keyword = {}", keyword);

    List<UUID> defIds;
    // 키워드가 없으면 전체 반환
    if (keyword == null || keyword.isEmpty()) {

      defIds = clothesAttributeDefRepository.findAll().stream()
          .map(BaseEntity::getId)
          .toList();

      log.debug("키워드가 없어 모든 defIds를 반환합니다: defIdsSize = {}", defIds.size());
      return defIds;
    }

    defIds = clothesAttributeDefRepositoryQueryDSL.findDefIdsByKeyword(keyword);
    log.debug("의상 속성 키워드로 조회 완료: defIdsSize = {}", defIds.size());
    return defIds;
  }

  // 커서 기반 페이지네이션
  public List<ClothesAttributeDef> findByCursor(List<UUID> defIds,
      ClothesAttributeDefFindRequest request) {

    log.debug("의상 속성 페이지네이션 시작: defIdsSize = {}, request = {}", defIds.size(), request);

    if (request.limit() <= 0) {
      log.warn("유효하지 않은 limit입니다.: limit = {}", request.limit());
      throw BadRequestException.withLimit(request.limit());
    }

    if (!VALID_SORT_BY.contains(request.sortBy())) {
      log.warn("잘못된 sortBy입니다: validSortBy = {}, sortBy = {}", VALID_SORT_BY, request.sortBy());
      throw BadRequestException.withSortBy(request.sortBy());
    }

    if (defIds.isEmpty()) {
      log.debug("defIds가 비어있습니다. return = {}", List.of());
      return List.of();
    }

    List<ClothesAttributeDef> defs = clothesAttributeDefRepositoryQueryDSL.findByCursor(defIds, request);
    log.debug("의상 속성 페이지네이션 완료: defsSize = {}", defs.size());
    return defs;
  }

  // 의상 속성 정의 명 수정
  public ClothesAttributeDef update(UUID defId, String newName) {

    log.debug("의상 속성 정의 명 수정 시작: defId = {}, newName = {}", defId, newName);

    // id 검사
    ClothesAttributeDef def = clothesAttributeDefRepository.findById(defId).orElseThrow(() -> {
      log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
      return ClothesAttributeDefNotFoundException.withId(defId);
    });

    // 이름 변경
    def.update(newName);

    log.debug("의상 속성 정의 명 수정 완료: defId = {}, name = {}", def.getId(), def.getName());
    return def;
  }

  // 의상 속성 정의 명 삭제
  public void delete(UUID defId) {
    log.debug("의상 속성 정의 명 삭제 시작: defId = {}", defId);

    clothesAttributeDefRepository.findById(defId)
        .orElseThrow(() -> {
          log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
          return ClothesAttributeDefNotFoundException.withId(defId);
        });

    clothesAttributeDefRepository.deleteById(defId);

    log.debug("의상 속성 정의 명 삭제 완료");
  }
}
