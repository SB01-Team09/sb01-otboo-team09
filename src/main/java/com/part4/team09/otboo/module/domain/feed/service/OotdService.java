package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OotdService {

  private final OotdRepository ootdRepository;
  private final OotdDtoAssembler ootdDtoAssembler;

  private final ClothesRepository clothesRepository;

  @Transactional
  public void create(UUID feedId, List<UUID> clothesIds) {
    log.info("Ootd 생성 시작 - 피드ID: {}, 의류IDs: {}", feedId, clothesIds);

    getAllByClothesIdsOrThrow(clothesIds);
    List<Ootd> ootds = clothesIds.stream()
        .map(clothesId -> Ootd.create(feedId, clothesId))
        .toList();

    ootdRepository.saveAll(ootds);

    log.info("Ootd 생성 완료 - 피드ID: {}, 저장된 Ootd 개수: {}", feedId, ootds.size());
  }

  @Transactional(readOnly = true)
  public List<OotdDto> getOotds(UUID feedID) {
    log.info("Ootd 조회 요청 - 피드ID: {}", feedID);

    List<UUID> clothesIds = ootdRepository.findClothesIdsByFeedId(feedID);
    List<Clothes> selectedClothes = getAllByClothesIdsOrThrow(clothesIds);
    List<OotdDto> result = ootdDtoAssembler.assemble(selectedClothes);

    log.info("Ootd 조회 완료 - 피드ID: {}, 결과 개수: {}", feedID, result.size());
    return result;
  }

  @Transactional
  public void deleteAllByFeedId(UUID feedId) {
    log.warn("Ootd 전체 삭제 요청 - 피드ID: {}", feedId);

    ootdRepository.deleteAllByFeedId(feedId);

    log.info("Ootd 전체 삭제 완료 - 피드ID: {}", feedId);
  }

  private List<Clothes> getAllByClothesIdsOrThrow(List<UUID> clothesIds) {
    log.debug("의류 목록 조회 - 의류IDs: {}", clothesIds);
    List<Clothes> foundClothes = clothesRepository.findAllById(clothesIds);

    List<UUID> foundIds = foundClothes.stream()
        .map(Clothes::getId)
        .toList();

    List<UUID> missingIds = clothesIds.stream()
        .filter(id -> !foundIds.contains(id))
        .toList();

    if (!missingIds.isEmpty()) {
      log.error("의류 조회 실패 - 존재하지 않는 의류IDs: {}", missingIds);
      throw ClothesNotFoundException.withIds(missingIds);
    }

    return foundClothes;
  }
}
