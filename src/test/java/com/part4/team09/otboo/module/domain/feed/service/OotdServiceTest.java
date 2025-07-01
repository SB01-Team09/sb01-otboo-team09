package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdMapper;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  @DisplayName("오오티디 생성 테스트")
  public class CreateOotdTest {
    
    @Test
    @DisplayName("오오티디 생성 성공")
    void create_ootd_success() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID clothedID = UUID.randomUUID();

      OotdDto ootdDto = new OotdDto(
          clothedID,
          "name",
          "imageUrl",
          ClothesType.BAG,
          List.of()
      );

      List<UUID> clothesIds = List.of(clothedID);
      List<OotdDto> ootdDtos = List.of(ootdDto);

      Clothes mockClothes = mock(Clothes.class);

      given(clothesRepository.findById(any())).willReturn(Optional.of(mockClothes));
      given(ootdMapper.toDto(any(Clothes.class))).willReturn(ootdDto);

      // when
      List<OotdDto> result = ootdService.create(feedId, clothesIds);

      // then
      assertThat(result).isEqualTo(ootdDtos);
      verify(ootdRepository).saveAll(any());
    }
  }
}