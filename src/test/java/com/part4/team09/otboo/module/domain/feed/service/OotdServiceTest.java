package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdMapper;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OotdServiceTest {

  @Mock
  private OotdRepository ootdRepository;

  @Mock
  private OotdMapper ootdMapper;

  @Mock
  private ClothesRepository clothesRepository;

  @InjectMocks
  private OotdService ootdService;

  @Nested
  @DisplayName("create")
  public class createdTest {
    @Test
    void create_success() {
      // given
      UUID feedId = UUID.randomUUID();
      List<UUID> clothesIds = List.of();
      List<OotdDto> ootdDtos = List.of();

      // when
      List<OotdDto> result = ootdService.create(feedId, clothesIds);

      // then
      assertThat(result).isEqualTo(ootdDtos);
      verify(ootdRepository).saveAll(any());
    }
  }
}