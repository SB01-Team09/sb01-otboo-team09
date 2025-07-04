package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdMapper;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OotdService {

  private final OotdRepository ootdRepository;
  private final OotdMapper ootdMapper;

  private final ClothesRepository clothesRepository;

  @Transactional
  public List<OotdDto> create(UUID feedId, List<UUID> clothesIds) {
    List<Ootd> ootds = new ArrayList<>();
    List<Clothes> selectedClothes = new ArrayList<>();

    for (UUID clothesId : clothesIds) {
      Clothes clothes = getClothesOrThrow(clothesId);
      selectedClothes.add(clothes);

      Ootd ootd = Ootd.create(feedId, clothesId);
      ootds.add(ootd);
    }

    ootdRepository.saveAll(ootds);

    return selectedClothes.stream()
        .map(ootdMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<OotdDto> getOotds(UUID feedID) {
    List<UUID> clothesIds = ootdRepository.findClothesIdsByFeedId(feedID);
    List<Clothes> selectedClothes = getAllByClothesIdsOrThrow(clothesIds);

    return selectedClothes.stream()
        .map(ootdMapper::toDto)
        .toList();
  }

  private List<Clothes> getAllByClothesIdsOrThrow(List<UUID> clothesIds) {
    List<Clothes> foundClothes = clothesRepository.findAllById(clothesIds);

    List<UUID> foundIds = foundClothes.stream()
        .map(Clothes::getId)
        .toList();

    List<UUID> missingIds = clothesIds.stream()
        .filter(id -> !foundIds.contains(id))
        .toList();

    // TODO: 의상 커스텀 예외로 변경
    if (!missingIds.isEmpty()) {
      throw new EntityNotFoundException();
    }

    return foundClothes;
  }

  // TODO: 의상 커스텀 예외로 변경
  private Clothes getClothesOrThrow(UUID clothesId) {
    return clothesRepository.findById(clothesId)
        .orElseThrow(() -> new EntityNotFoundException());
  }
}
