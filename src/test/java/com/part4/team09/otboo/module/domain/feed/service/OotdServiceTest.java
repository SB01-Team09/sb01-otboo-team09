package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import com.part4.team09.otboo.module.domain.feed.exception.like.LikeAlreadyExistsException;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdDtoAssembler;
import com.part4.team09.otboo.module.domain.feed.mapper.OotdMapper;
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import java.util.List;
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
  private OotdDtoAssembler ootdDtoAssembler;

  @Mock
  private ClothesRepository clothesRepository;

  @InjectMocks
  private OotdService ootdService;

  @Nested
  @DisplayName("오오티디 생성")
  public class CreateOotdTest {

    @Test
    @DisplayName("오오티디 생성 성공")
    void create_ootd_success() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID clothedId = UUID.randomUUID();
      Clothes mockClothes = mock(Clothes.class);
      List<UUID> clothesIds = List.of(clothedId);

      given(clothesRepository.findAllById(clothesIds)).willReturn(List.of(mockClothes));
      given(mockClothes.getId()).willReturn(clothedId);

      // when
      ootdService.create(feedId, clothesIds);

      // then
      verify(ootdRepository).saveAll(any());
    }

    @Test
    @DisplayName("오오티디 생성 실패 - 존재하지 않는 의상 ID")
    void create_ootd_throwsClothesNotFoundException_whenClothesDoseNotExist() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID nonExistClothesId = UUID.randomUUID();
      List<UUID> clothesIds = List.of(nonExistClothesId);

      given(clothesRepository.findAllById(clothesIds)).willReturn(List.of());

      // when & then
      assertThrows(ClothesNotFoundException.class,
          () -> ootdService.create(feedId, clothesIds));
    }
  }

  @Nested
  @DisplayName("오오티디 조회")
  public class GetOotdsTest {

    @Test
    @DisplayName("오오티디 조회 성공")
    void get_ootds_success() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID clothesId = UUID.randomUUID();
      Clothes mockClothes = mock(Clothes.class);
      OotdDto mockOotdDto = mock(OotdDto.class);

      List<UUID> clothesIds = List.of(clothesId);
      List<Clothes> clothes = List.of(mockClothes);
      List<OotdDto> ootdDtos = List.of(mockOotdDto);

      given(ootdRepository.findClothesIdsByFeedId(feedId)).willReturn(clothesIds);
      given(clothesRepository.findAllById(clothesIds)).willReturn(clothes);
      given(mockClothes.getId()).willReturn(clothesId);
      given(ootdDtoAssembler.assemble(clothes)).willReturn(ootdDtos);

      // when
      List<OotdDto> result = ootdService.getOotds(feedId);

      //then
      assertThat(result).isEqualTo(ootdDtos);
    }

    @Test
    @DisplayName("오오티디 조회 실패 - 존재하지 않는 의상 ID")
    void get_ootds_throwsClothesNotFoundException_whenClothesDoseNotExist() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID nonExistClothesId = UUID.randomUUID();
      List<UUID> clothesIds = List.of(nonExistClothesId);

      given(clothesRepository.findAllById(clothesIds)).willReturn(List.of());

      // when & then
      assertThrows(ClothesNotFoundException.class,
          () -> ootdService.create(feedId, clothesIds));
    }
  }

  @Nested
  @DisplayName("피드 아이디로 오오티디 모두 삭제")
  public class DeleteAllByFeedIdTest {

    @Test
    @DisplayName("오오티디 삭제 성공")
    void delete_all_by_feedId_success() {
      // given
      UUID feedId = UUID.randomUUID();

      // when
      ootdService.deleteAllByFeedId(feedId);

      // then
      verify(ootdRepository).deleteAllByFeedId(feedId);
    }
  }
}