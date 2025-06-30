package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefAlreadyExistsException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
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

  private final ClothesAttributeDefRepository clothesAttributeDefRepository;

  // 의상 속성 정의 명 생성
  public ClothesAttributeDef create(String name) {

    log.debug("의상 속성 정의 명 생성 시작: name = {}", name);

    // 프로토타입에는 없지만 이름 중복 검사
    if (clothesAttributeDefRepository.existsByName(name)) {
      log.warn("이미 존재하는 의상 속성 정의 명입니다. name = {}", name);
      throw ClothesAttributeDefAlreadyExistsException.withName(name);
    }

    ClothesAttributeDef def = ClothesAttributeDef.create(name);

    log.debug("의상 속성 정의 명 생성 완료: defId = {}, name = {}", def.getId(), def.getName());
    return clothesAttributeDefRepository.save(def);
  }

  // 의상 속성 정의 명 수정
  public ClothesAttributeDef update(UUID defId, String newName) {

    log.debug("의상 속성 정의 명 수정 시작: defId = {}, newName = {}", defId, newName);

    // id 검사
    ClothesAttributeDef def = clothesAttributeDefRepository.findById(defId)
        .orElseThrow(() -> {
          log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
          return ClothesAttributeDefNotFoundException.withId(defId);
        });

    // 이름 변경
    def.update(newName);

    log.debug("의상 속성 정의 명 수정 완료: defId = {}, name = {}", def.getId(), def.getName());
    return def;
  }

  // 의상 속성 정의 명 찾기
  public ClothesAttributeDef findById(UUID defId) {
    log.debug("의상 속성 정의 명 찾기 시작: defId = {}", defId);

    return clothesAttributeDefRepository.findById(defId)
        .orElseThrow(() -> {
          log.warn("의상 속성 정의가 존재하지 않습니다. id = {}", defId);
          return ClothesAttributeDefNotFoundException.withId(defId);
        });
  }

  // 의상 속성 정의 명 삭제
  public void delete(UUID defId) {
    log.debug("의상 속성 정의 명 삭제 시작: defId = {}", defId);

    clothesAttributeDefRepository.deleteById(defId);
  }
}
