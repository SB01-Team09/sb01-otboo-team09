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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OotdService {

  private final OotdRepository ootdRepository;
  private final OotdDtoAssembler ootdDtoAssembler;

  private final ClothesRepository clothesRepository;

  @Transactional
  public void create(UUID feedId, List<UUID> clothesIds) {
    getAllByClothesIdsOrThrow(clothesIds);
    List<Ootd> ootds = clothesIds.stream()
        .map(clothesId -> Ootd.create(feedId, clothesId))
        .toList();

    ootdRepository.saveAll(ootds);
  }

  @Transactional(readOnly = true)
  public List<OotdDto> getOotds(UUID feedID) {
    List<UUID> clothesIds = ootdRepository.findClothesIdsByFeedId(feedID);
    List<Clothes> selectedClothes = getAllByClothesIdsOrThrow(clothesIds);

    return ootdDtoAssembler.assemble(selectedClothes);
  }

  @Transactional
  public void deleteAllByFeedId(UUID feedId) {
    ootdRepository.deleteAllByFeedId(feedId);
  }

  private List<Clothes> getAllByClothesIdsOrThrow(List<UUID> clothesIds) {
    List<Clothes> foundClothes = clothesRepository.findAllById(clothesIds);

    List<UUID> foundIds = foundClothes.stream()
        .map(Clothes::getId)
        .toList();

    List<UUID> missingIds = clothesIds.stream()
        .filter(id -> !foundIds.contains(id))
        .toList();

    if (!missingIds.isEmpty()) {
      throw ClothesNotFoundException.withIds(missingIds);
    }

    return foundClothes;
  }
}
