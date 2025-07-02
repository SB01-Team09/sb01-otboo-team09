package com.part4.team09.otboo.module.domain.clothes.service;

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

  public void deleteBySelectableValueIdIn(List<UUID> valueIds) {
    log.debug("의상 속성 값 - 의상 연관 삭제 시작: valueIdsSize = {}", valueIds.size());

    clothesAttributeRepository.deleteBySelectableValueIdIn(valueIds);

    log.debug("의상 속성 값 - 의상 연관 삭제 완료");
  }
}
