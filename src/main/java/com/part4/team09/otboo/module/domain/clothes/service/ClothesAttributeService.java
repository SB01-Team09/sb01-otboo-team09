package com.part4.team09.otboo.module.domain.clothes.service;

import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClothesAttributeService {

  private final ClothesAttributeRepository clothesAttributeRepository;

  public void deleteBySelectableValueIdIn(List<UUID> oldValueIds) {

    // 로직이 더 추가되면 테스트 작성하겠습니다.
    clothesAttributeRepository.deleteBySelectableValueIdIn(oldValueIds);
  }
}
