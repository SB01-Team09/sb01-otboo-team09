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
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefAlreadyExistsException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.ClothesAttributeDefNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesAttributeDefRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesAttributeDefRepositoryQueryDSL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("이름 중복")
    void create_duplicate_name() {

      // given
      String name = "사이즈";

      given(clothesAttributeDefRepository.existsByName(name)).willReturn(true);

      // when, then
      assertThrows(ClothesAttributeDefAlreadyExistsException.class,
          () -> clothesAttributeDefService.create(name));
    }
  }

  @Nested
  @DisplayName("의상 속성 정의 찾기")
  class FindById {

    @Test
    @DisplayName("찾기 성공")
    void find_by_id_success() {

      // given
      UUID id = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");

      given(clothesAttributeDefRepository.findById(id)).willReturn(Optional.of(def));

      // when
      ClothesAttributeDef result = clothesAttributeDefService.findById(id);

      // then
      assertNotNull(result);
      assertEquals(def, result);
      then(clothesAttributeDefRepository).should().findById(id);
    }

    @Test
    @DisplayName("찾기 실패 - 잘못된 id")
    void find_by_id_not_found_id() {

      // given
      UUID id = UUID.randomUUID();

      given(clothesAttributeDefRepository.findById(id)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesAttributeDefNotFoundException.class,
          () -> clothesAttributeDefService.findById(id));
    }
  }

  @Nested
  @DisplayName("키워드로 속성 정의 찾기")
  class FindIdsByKeyword {

    @Test
    @DisplayName("키워드로 찾기 성공")
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
    @DisplayName("키워드가 없음")
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
    @DisplayName("찾기 성공")
    void find_by_cursor_success() {

      // given
      Set<UUID> defIds = new HashSet<>(List.of(UUID.randomUUID()));
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
    @DisplayName("defIds가 없음")
    void find_by_cursor_no_def_ids() {

      // given
      Set<UUID> defIds = Set.of();
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(null, null, 10,
          "name", SortDirection.ASCENDING, "사이즈");
      List<ClothesAttributeDef> defs = List.of();

      // when
      List<ClothesAttributeDef> result = clothesAttributeDefService.findByCursor(defIds, request);

      // then
      assertEquals(result, defs);
      then(clothesAttributeDefRepositoryQueryDSL).should(times(0)).findByCursor(defIds, request);

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
    @DisplayName("속성 정의 id가 존재하지 않음")
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

      // when
      clothesAttributeDefService.delete(defId);

      // then
      then(clothesAttributeDefRepository).should().deleteById(defId);
    }
  }
}