package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.BadRequestException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefAlreadyExistsException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesAttributeDefRepositoryQueryDSL;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClothesAttributeDefServiceTest {

  @InjectMocks
  private ClothesAttributeDefService clothesAttributeDefService;

  @Mock
  private ClothesAttributeDefRepository clothesAttributeDefRepository;

  @Mock
  private ClothesAttributeDefRepositoryQueryDSL clothesAttributeDefRepositoryQueryDSL;

  @Nested
  @DisplayName("의상 속성 정의 생성")
  class Create {

    @Test
    @DisplayName("생성 성공")
    void create_success() {

      // given
      String name = "사이즈";
      ClothesAttributeDef def = ClothesAttributeDef.create(name);

      given(clothesAttributeDefRepository.existsByName(name)).willReturn(false);
      given(clothesAttributeDefRepository.save(any(ClothesAttributeDef.class))).willReturn(def);

      // when
      ClothesAttributeDef result = clothesAttributeDefService.create(name);

      // then
      assertNotNull(result);
      assertEquals(result.getName(), def.getName());
      then(clothesAttributeDefRepository).should().existsByName(name);
      then(clothesAttributeDefRepository).should().save(any(ClothesAttributeDef.class));
    }

    @Test
    @DisplayName("이름이 중복일 경우 예외 처리")
    void create_duplicate_name() {

      // given
      String name = "사이즈";

      given(clothesAttributeDefRepository.existsByName(name)).willReturn(true);

      // when, then
      assertThrows(ClothesAttributeDefAlreadyExistsException.class,
          () -> clothesAttributeDefService.create(name));
      then(clothesAttributeDefRepository).should(times(0)).save(any(ClothesAttributeDef.class));
    }
  }

  @Nested
  @DisplayName("의상 속성 정의 조회")
  class FindById {

    @Test
    @DisplayName("조회 성공")
    void find_by_id_success() {

      // given
      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      given(clothesAttributeDefRepository.findById(def.getId())).willReturn(Optional.of(def));

      // when
      ClothesAttributeDef result = clothesAttributeDefService.findById(def.getId());

      // then
      assertNotNull(result);
      assertEquals(def, result);
      then(clothesAttributeDefRepository).should().findById(def.getId());
    }

    @Test
    @DisplayName("defId가 존재하지 않을 경우 예외 처리")
    void find_by_id_not_found_id() {

      // given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> clothesAttributeDefService.findById(defId));
    }
  }

  @Nested
  @DisplayName("키워드로 속성 정의 및 속성 값의 def id 조회")
  class FindIdsByKeyword {

    @Test
    @DisplayName("키워드가 있을 경우의 조회 성공")
    void find_ids_by_keyword_success() {

      // given
      String keyword = "사이즈";
      List<UUID> defIds = List.of(UUID.randomUUID());

      given(clothesAttributeDefRepositoryQueryDSL.findDefIdsByKeyword(keyword)).willReturn(defIds);

      // when
      List<UUID> result = clothesAttributeDefService.findIdsByKeyword(keyword);

      // then
      assertEquals(result, defIds);
      then(clothesAttributeDefRepository).should(times(0)).findAll();
      then(clothesAttributeDefRepositoryQueryDSL).should().findDefIdsByKeyword(keyword);
    }

    @Test
    @DisplayName("키워드가 없는 경우의 조회 성공")
    void find_ids_by_keyword_no_keyword() {

      // given
      String keyword = "";
      List<ClothesAttributeDef> defs = List.of(ClothesAttributeDef.create("사이즈"));

      given(clothesAttributeDefRepository.findAll()).willReturn(defs);

      // when
      List<UUID> result = clothesAttributeDefService.findIdsByKeyword(keyword);

      // then
      assertEquals(result, defs.stream().map(BaseEntity::getId).toList());
      then(clothesAttributeDefRepository).should().findAll();
      then(clothesAttributeDefRepositoryQueryDSL).should(times(0)).findDefIdsByKeyword(keyword);
    }
  }

  @Nested
  @DisplayName("커서 기반 페이지네이션")
  class FindByCursor {

    @Test
    @DisplayName("defIds가 있는 경우 조회 성공")
    void find_by_cursor_success() {

      // given
      List<UUID> defIds = List.of(UUID.randomUUID());
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(null, null, 10,
          "name", SortDirection.ASCENDING, "사이즈");
      List<ClothesAttributeDef> defs = List.of(def);

      given(clothesAttributeDefRepositoryQueryDSL.findByCursor(defIds, request)).willReturn(
          defs);
      // when
      List<ClothesAttributeDef> result = clothesAttributeDefService.findByCursor(defIds, request);

      // then
      assertEquals(result, defs);
      then(clothesAttributeDefRepositoryQueryDSL).should().findByCursor(defIds, request);
    }

    @Test
    @DisplayName("defIds가 없는 경우 조회 성공")
    void find_by_cursor_no_def_ids() {

      // given
      List<UUID> defIds = List.of();
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(null, null, 10,
          "name", SortDirection.ASCENDING, "사이즈");
      List<ClothesAttributeDef> defs = List.of();

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefService.findByCursor(defIds, request);

      // then
      assertEquals(result, defs);
      then(clothesAttributeDefRepositoryQueryDSL).should(times(0)).findByCursor(defIds, request);

    }

    @Test
    @DisplayName("limit가 0 이하일 경우 예외처리")
    void find_by_cursor_invalid_limit() {

      // given
      List<UUID> defIds = List.of();
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(null, null, 0,
          "name", SortDirection.ASCENDING, "사이즈");

      // when, then
      assertThrows(BadRequestException.class, () -> clothesAttributeDefService.findByCursor(defIds, request));
      then(clothesAttributeDefRepositoryQueryDSL).should(times(0)).findByCursor(defIds, request);

    }

    @Test
    @DisplayName("잘못된 sortBy 값일 경우 예외처리")
    void find_by_cursor_invalid_sort_by() {

      // given
      List<UUID> defIds = List.of();
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(null, null, 10,
          "invalidSortBy", SortDirection.ASCENDING, "사이즈");

      // when, then
      assertThrows(BadRequestException.class, () -> clothesAttributeDefService.findByCursor(defIds, request));
      then(clothesAttributeDefRepositoryQueryDSL).should(times(0)).findByCursor(defIds, request);
    }
  }

  @Nested
  @DisplayName("의상 속성 정의 id 리스트로 속성 정의 리스트 조회")
  class FindAllByIds {

    @Test
    @DisplayName("조회 성공")
    void find_all_by_ids_success() {

      // given
      List<UUID> defIds = List.of(UUID.randomUUID(), UUID.randomUUID());
      List<ClothesAttributeDef> defs = List.of(ClothesAttributeDef.create("사이즈"), ClothesAttributeDef.create("색상"));

      given(clothesAttributeDefRepository.findAllById(defIds)).willReturn(defs);

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefService.findAllByIds(defIds);

      // then
      assertNotNull(result);
      assertEquals(result, defs);
      then(clothesAttributeDefRepository).should().findAllById(defIds);
    }

    @Test
    @DisplayName("defIds가 비어있을 경우 빈 리스트 반환")
    void find_all_by_no_def_ids() {

      // given
      List<UUID> defIds = List.of();

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefService.findAllByIds(defIds);

      // then
      assertNotNull(result);
      assertEquals(result, List.of());
      then(clothesAttributeDefRepository).should(times(0)).findAllById(defIds);
    }
  }

  @Nested
  @DisplayName("의상 속성 정의 수정")
  class Update {

    @Test
    @DisplayName("수정 성공")
    void update_success() {

      // given
      UUID defId = UUID.randomUUID();
      String newName = "신축성";

      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");

      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.of(def));

      // when
      ClothesAttributeDef result = clothesAttributeDefService.update(defId, newName);

      // then
      assertEquals(result.getName(), newName);
      then(clothesAttributeDefRepository).should().findById(defId);
    }

    @Test
    @DisplayName("defId가 존재하지 않을 경우 예외 처리")
    void update_not_found_def_id() {

      // given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> clothesAttributeDefService.update(defId, "신축성"));
    }
  }

  @Nested
  @DisplayName("의상 속성 정의 삭제")
  class Delete {

    @Test
    @DisplayName("삭제 성공")
    void delete_success() {

      // given
      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");

      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.of(def));

      // when
      clothesAttributeDefService.delete(defId);

      // then
      then(clothesAttributeDefRepository).should().findById(defId);
      then(clothesAttributeDefRepository).should().deleteById(defId);
    }

    @Test
    @DisplayName("defId가 존재하지 않을 경우 예외 처리")
    void delete_fail_not_found_def() {

      // given
      UUID defId = UUID.randomUUID();

      given(clothesAttributeDefRepository.findById(defId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class, () -> clothesAttributeDefService.delete(defId));

      then(clothesAttributeDefRepository).should().findById(defId);
      then(clothesAttributeDefRepository).should(times(0)).deleteById(defId);
    }
  }
}