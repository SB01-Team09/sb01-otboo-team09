package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OotdService {

  public List<OotdDto> create(UUID feedId, List<UUID> clothesIds) {
      return Collections.singletonList(new OotdDto
          (
              UUID.randomUUID(),
              "name",
              "imageUrl",
              ClothesType.BAG,
              List.of()
          ));
  }
}
