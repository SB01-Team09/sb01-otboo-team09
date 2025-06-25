package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefAlreadyExistsException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothesAttributeDefService {

  private final ClothesAttributeDefRepository clothesAttributeDefRepository;

  public ClothesAttributeDef create(String name) {

    // 프로토타입에는 없지만 이름 중복 검사
    if (clothesAttributeDefRepository.existsByName(name)) {
      throw ClothesAttributeDefAlreadyExistsException.withName(name);
    }

    ClothesAttributeDef def = ClothesAttributeDef.create(name);

    return clothesAttributeDefRepository.save(def);
  }
}
