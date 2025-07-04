package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ClothesServiceTest {

  @InjectMocks
  private ClothesService clothesService;

  @Mock
  private ClothesAttributeDefService clothesAttributeDefService;

  @Mock
  private SelectableValueService selectableValueService;

  @Mock
  private ClothesAttributeService clothesAttributeService;

  @Mock
  private ClothesRepository clothesRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ClothesMapper clothesMapper;

  @Mock
  private ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  @Mock
  private FileStorage fileStorage;

  @Nested
  @DisplayName("의상 등록")
  class Create {

    @Test
    @DisplayName("의상 등록 성공")
    void create_success() {

      // given
      // 사용자, def, selectableValue 생성
      UUID ownerId = UUID.randomUUID();
      User user = User.createUser("test@gmail.com", "test", "1234");
      ReflectionTestUtils.setField(user, "id", ownerId);

      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      UUID selectableValueId1 = UUID.randomUUID();
      UUID selectableValueId2 = UUID.randomUUID();
      SelectableValue selectableValue1 = SelectableValue.create(def.getId(), "S");
      SelectableValue selectableValue2 = SelectableValue.create(def.getId(), "M");
      ReflectionTestUtils.setField(selectableValue1, "id", selectableValueId1);
      ReflectionTestUtils.setField(selectableValue2, "id", selectableValueId2);

      // request, image
      List<ClothesAttributeDto> attributes = List.of(new ClothesAttributeDto(def.getId(), "S"));
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "옷", ClothesType.TOP, attributes);
      MultipartFile image = mock(MultipartFile.class);

      // 이미지 업로드
      String url = "test url";

      // Clothes 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(request.ownerId(), request.name(), request.type(), url);
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // ClothesAttributes 생성
      // 1. def 조회
      List<UUID> defIds = List.of(def.getId());
      List<ClothesAttributeDef> defs = List.of(def);

      // 2. selectableValue 조회
      List<SelectableValue> selectableValues = List.of(selectableValue1);
      List<UUID> selectableValueIds = selectableValues.stream()
          .map(SelectableValue::getId)
          .toList();

      // 3. ClothesAttributeWithDefDtoMapper
      List<String> selectableItems = selectableValues.stream()
          .map(SelectableValue::getItem)
          .toList();
      ClothesAttributeWithDefDto dto = new ClothesAttributeWithDefDto(def.getId(), def.getName(), selectableItems, selectableValue1.getItem());

      // 4. clothesAttribute 생성
      ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes.getId(), selectableValue1.getId());
      List<ClothesAttribute> clothesAttributes = List.of(clothesAttribute);

      // clothesMapper
      List<ClothesAttributeWithDefDto> responseAttributes = List.of(dto);
      ClothesDto clothesDto = new ClothesDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes);

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(url);
      given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
      given(clothesAttributeDefService.findAllByIds(defIds)).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(defIds)).willReturn(selectableValues);
      given(clothesAttributeWithDefMapper.toDto(def.getId(), def.getName(), selectableItems, selectableValue1.getItem())).willReturn(dto);
      given(clothesAttributeService.create(clothes.getId(), selectableValueIds)).willReturn(clothesAttributes);
      given(clothesMapper.toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes)).willReturn(clothesDto);

      // when
      ClothesDto result = clothesService.create(request, image);

      // then
      assertEquals(result, clothesDto);
      then(userRepository).should().findById(ownerId);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should().findAllByIds(defIds);
      then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
      then(clothesAttributeWithDefMapper).should().toDto(def.getId(), def.getName(), selectableItems, selectableValue1.getItem());
      then(clothesAttributeService).should().create(clothes.getId(), selectableValueIds);
      then(clothesMapper).should().toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes);
    }

    @Test
    @DisplayName("이미지가 없을 경우 null 반환, 선택한 값이 없을 경우 빈 리스트 반환")
    void create_no_image_and_no_attributes() {

      // given
      // 사용자, def, selectableValue 생성
      UUID ownerId = UUID.randomUUID();
      User user = User.createUser("test@gmail.com", "test", "1234");
      ReflectionTestUtils.setField(user, "id", ownerId);

      // request, image
      List<ClothesAttributeDto> attributes = List.of();
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "옷", ClothesType.TOP, attributes);
      MultipartFile image = null;

      // 이미지 업로드
      String url = null;

      // Clothes 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(request.ownerId(), request.name(), request.type(), url);
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // clothesMapper
      List<ClothesAttributeWithDefDto> responseAttributes = List.of();
      ClothesDto clothesDto = new ClothesDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes);

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));
      given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
      given(clothesMapper.toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes)).willReturn(clothesDto);

      // when
      ClothesDto result = clothesService.create(request, image);

      // then
      assertEquals(result, clothesDto);
      then(userRepository).should().findById(ownerId);
      then(fileStorage).should(times(0)).upload(any(MultipartFile.class), any(FileDomain.class));
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should(times(0)).findAllByIds(anyList());
      then(selectableValueService).should(times(0)).findAllByAttributeDefIdIn(anyList());
      then(clothesAttributeWithDefMapper).should(times(0)).toDto(any(UUID.class), anyString(), anyList(), anyString());
      then(clothesAttributeService).should(times(0)).create(any(UUID.class), anyList());
      then(clothesMapper).should().toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes);
    }

    @Test
    @DisplayName("사용자가 없을 경우 예외처리")
    void create_not_found_user() {
      // given
      UUID ownerId = UUID.randomUUID();
      MultipartFile image = mock(MultipartFile.class);
      List<ClothesAttributeDto> attributes = List.of();
      ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "옷", ClothesType.TOP, attributes);

      given(userRepository.findById(ownerId)).willReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> clothesService.create(request, image));

      then(userRepository).should().findById(ownerId);
      then(clothesRepository).should(times(0)).save(any(Clothes.class));
    }

    @Test
    @DisplayName("선택한 값이 잘못된 이름일 경우 선택 가능한 값 조회 실패 - 사이즈에 S, M 이 있지만 XL을 받았을 경우 빈 리스트 반환")
    void create_selectable_value_not_found() {

      // given
      // 사용자, def, selectableValue 생성
      UUID ownerId = UUID.randomUUID();
      User user = User.createUser("test@gmail.com", "test", "1234");
      ReflectionTestUtils.setField(user, "id", ownerId);

      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      UUID selectableValueId1 = UUID.randomUUID();
      UUID selectableValueId2 = UUID.randomUUID();
      SelectableValue selectableValue1 = SelectableValue.create(def.getId(), "S");
      SelectableValue selectableValue2 = SelectableValue.create(def.getId(), "M");
      ReflectionTestUtils.setField(selectableValue1, "id", selectableValueId1);
      ReflectionTestUtils.setField(selectableValue2, "id", selectableValueId2);

      // request, image
      List<ClothesAttributeDto> attributes = List.of(new ClothesAttributeDto(def.getId(), "XL"));
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "옷", ClothesType.TOP, attributes);
      MultipartFile image = mock(MultipartFile.class);

      // 이미지 업로드
      String url = "test url";

      // Clothes 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(request.ownerId(), request.name(), request.type(), url);
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // ClothesAttributes 생성
      // 1. def 조회
      List<UUID> defIds = List.of(def.getId());
      List<ClothesAttributeDef> defs = List.of(def);

      // 2. selectableValue 조회
      List<SelectableValue> selectableValues = List.of(selectableValue1);
      List<UUID> selectableValueIds = selectableValues.stream()
          .map(SelectableValue::getId)
          .toList();

      // 3. ClothesAttributeWithDefDtoMapper
      List<String> selectableItems = selectableValues.stream()
          .map(SelectableValue::getItem)
          .toList();

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(url);
      given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
      given(clothesAttributeDefService.findAllByIds(defIds)).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(defIds)).willReturn(selectableValues);

      // when,  then
      assertThrows(SelectableValueNotFoundException.class, () -> clothesService.create(request, image));
      then(userRepository).should().findById(ownerId);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should().findAllByIds(defIds);
      then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
      then(clothesAttributeWithDefMapper).should(times(0)).toDto(def.getId(), def.getName(), selectableItems, selectableValue1.getItem());
      then(clothesAttributeService).should(times(0)).create(clothes.getId(), selectableValueIds);
      then(clothesMapper).should(times(0)).toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(), clothes.getType(), List.of());
    }
  }
}