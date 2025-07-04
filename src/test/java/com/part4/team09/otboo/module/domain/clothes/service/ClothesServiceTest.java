package com.part4.team09.otboo.module.domain.clothes.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.FileUploadFailedException;
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
import org.mockito.Spy;
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

  @Spy
  private ClothesMapper clothesMapper;

  @Spy
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
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "옷", ClothesType.TOP,
          attributes);
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
      ClothesAttributeWithDefDto dto = new ClothesAttributeWithDefDto(def.getId(), def.getName(),
          selectableItems, selectableValue1.getItem());

      // 4. clothesAttribute 생성
      ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes.getId(),
          selectableValue1.getId());
      List<ClothesAttribute> clothesAttributes = List.of(clothesAttribute);

      // clothesMapper
      List<ClothesAttributeWithDefDto> responseAttributes = List.of(dto);
      ClothesDto clothesDto = new ClothesDto(clothes.getId(), clothes.getOwnerId(),
          clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes);

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(url);
      given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
      given(clothesAttributeDefService.findAllByIds(defIds)).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(defIds)).willReturn(selectableValues);
      given(clothesAttributeWithDefMapper.toDto(def.getId(), def.getName(), selectableItems,
          selectableValue1.getItem())).willReturn(dto);
      given(clothesAttributeService.create(clothes.getId(), selectableValueIds)).willReturn(
          clothesAttributes);
      given(clothesMapper.toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(),
          clothes.getImageUrl(), clothes.getType(), responseAttributes)).willReturn(clothesDto);

      // when
      ClothesDto result = clothesService.create(request, image);

      // then
      assertEquals(result, clothesDto);
      then(userRepository).should().findById(ownerId);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should().findAllByIds(defIds);
      then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
      then(clothesAttributeWithDefMapper).should()
          .toDto(def.getId(), def.getName(), selectableItems, selectableValue1.getItem());
      then(clothesAttributeService).should().create(clothes.getId(), selectableValueIds);
      then(clothesMapper).should()
          .toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(),
              clothes.getType(), responseAttributes);
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
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "옷", ClothesType.TOP,
          attributes);
      MultipartFile image = null;

      // 이미지 업로드
      String url = null;

      // Clothes 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(request.ownerId(), request.name(), request.type(), url);
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // clothesMapper
      List<ClothesAttributeWithDefDto> responseAttributes = List.of();
      ClothesDto clothesDto = new ClothesDto(clothes.getId(), clothes.getOwnerId(),
          clothes.getName(), clothes.getImageUrl(), clothes.getType(), responseAttributes);

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));
      given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
      given(clothesMapper.toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(),
          clothes.getImageUrl(), clothes.getType(), responseAttributes)).willReturn(clothesDto);

      // when
      ClothesDto result = clothesService.create(request, image);

      // then
      assertEquals(result, clothesDto);
      then(userRepository).should().findById(ownerId);
      then(fileStorage).should(times(0)).upload(any(MultipartFile.class), any(FileDomain.class));
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should(times(0)).findAllByIds(anyList());
      then(selectableValueService).should(times(0)).findAllByAttributeDefIdIn(anyList());
      then(clothesAttributeWithDefMapper).should(times(0))
          .toDto(any(UUID.class), anyString(), anyList(), anyString());
      then(clothesAttributeService).should(times(0)).create(any(UUID.class), anyList());
      then(clothesMapper).should()
          .toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(),
              clothes.getType(), responseAttributes);
    }

    @Test
    @DisplayName("사용자가 없을 경우 예외처리")
    void create_not_found_user() {
      // given
      UUID ownerId = UUID.randomUUID();
      MultipartFile image = mock(MultipartFile.class);
      List<ClothesAttributeDto> attributes = List.of();
      ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "옷", ClothesType.TOP,
          attributes);

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
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "옷", ClothesType.TOP,
          attributes);
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
      assertThrows(SelectableValueNotFoundException.class,
          () -> clothesService.create(request, image));
      then(userRepository).should().findById(ownerId);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should().findAllByIds(defIds);
      then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
      then(clothesAttributeWithDefMapper).should(times(0))
          .toDto(def.getId(), def.getName(), selectableItems, selectableValue1.getItem());
      then(clothesAttributeService).should(times(0)).create(clothes.getId(), selectableValueIds);
      then(clothesMapper).should(times(0))
          .toDto(clothes.getId(), clothes.getOwnerId(), clothes.getName(), clothes.getImageUrl(),
              clothes.getType(), List.of());
    }
  }

  @Nested
  @DisplayName("의상 수정")
  class Update {

    @Test
    @DisplayName("의상 수정 성공 - 이미지 O")
    void update_success_with_image() {

      // given
      // 1. 사용자 생성
      UUID userId = UUID.randomUUID();
      User user = User.createUser("email", "test", "qwer123!");
      ReflectionTestUtils.setField(user, "id", userId);

      // 2. 의상 속성 생성
      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      UUID valueId1 = UUID.randomUUID();
      UUID valueId2 = UUID.randomUUID();
      SelectableValue value1 = SelectableValue.create(def.getId(), "S");
      SelectableValue value2 = SelectableValue.create(def.getId(), "M");
      ReflectionTestUtils.setField(value1, "id", valueId1);
      ReflectionTestUtils.setField(value2, "id", valueId2);

      // 3. 의상 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(user.getId(), "상의", ClothesType.TOP, "url");
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // 4. 의상 - 속성 값 생성 - 사이즈 : S
      UUID attributeId = UUID.randomUUID();
      ClothesAttribute attribute = ClothesAttribute.create(clothes.getId(), value1.getId());
      ReflectionTestUtils.setField(attribute, "id", attributeId);

      // 5. 수정 request
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def.getId(), "M")));
      MultipartFile image = mock(MultipartFile.class);

      // 6. 새 이미지 url
      String newUrl = "newUrl";

      // 7. 옷 - 속성 값 연관 생성
      List<ClothesAttributeDef> defs = List.of(def);
      List<SelectableValue> values = List.of(value1, value2);

      // 8. 반환 dto
      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(
          def.getId(),
          def.getName(),
          values.stream()
              .map(SelectableValue::getItem)
              .toList(),
          "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);
      ClothesDto dto = new ClothesDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes);

      given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
      given(userRepository.findById(clothes.getOwnerId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);
      given(clothesAttributeDefService.findAllByIds(List.of(defId))).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(defId))).willReturn(values);
      given(clothesAttributeWithDefMapper.toDto(
          def.getId(),
          def.getName(),
          values.stream()
              .map(SelectableValue::getItem)
              .toList(),
          "M")).willReturn(defDto);
      given(clothesMapper.toDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes)).willReturn(dto);

      // when
      ClothesDto result = clothesService.update(clothesId, request, image);

      // then
      assertEquals(result, dto);
      assertEquals(result.attributes(), attributes);

      then(clothesRepository).should().findById(clothes.getId());
      then(userRepository).should().findById(clothes.getOwnerId());
      then(fileStorage).should().remove("url");
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesAttributeDefService).should().findAllByIds(List.of(defId));
      then(selectableValueService).should().findAllByAttributeDefIdIn(List.of(defId));
      then(clothesAttributeWithDefMapper).should().toDto(
              def.getId(),
              def.getName(),
              values.stream()
                  .map(SelectableValue::getItem)
                  .toList(),
              "M");
    }

    @Test
    @DisplayName("의상 수정 성공 - 이미지 X")
    void update_success_no_image() {

      // given
      // 1. 사용자 생성
      UUID userId = UUID.randomUUID();
      User user = User.createUser("email", "test", "qwer123!");
      ReflectionTestUtils.setField(user, "id", userId);

      // 2. 의상 속성 생성
      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      UUID valueId1 = UUID.randomUUID();
      UUID valueId2 = UUID.randomUUID();
      SelectableValue value1 = SelectableValue.create(def.getId(), "S");
      SelectableValue value2 = SelectableValue.create(def.getId(), "M");
      ReflectionTestUtils.setField(value1, "id", valueId1);
      ReflectionTestUtils.setField(value2, "id", valueId2);

      // 3. 의상 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(user.getId(), "상의", ClothesType.TOP, null);
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // 4. 의상 - 속성 값 생성 - 사이즈 : S
      UUID attributeId = UUID.randomUUID();
      ClothesAttribute attribute = ClothesAttribute.create(clothes.getId(), value1.getId());
      ReflectionTestUtils.setField(attribute, "id", attributeId);

      // 5. 수정 request
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def.getId(), "M")));
      MultipartFile image = null;

      // 6. 새 이미지 url
      String newUrl = null;

      // 7. 옷 - 속성 값 연관 생성
      List<ClothesAttributeDef> defs = List.of(def);
      List<SelectableValue> values = List.of(value1, value2);

      // 8. 반환 dto
      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(
          def.getId(),
          def.getName(),
          values.stream()
              .map(SelectableValue::getItem)
              .toList(),
          "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);
      ClothesDto dto = new ClothesDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes);

      given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
      given(userRepository.findById(clothes.getOwnerId())).willReturn(Optional.of(user));
      given(clothesAttributeDefService.findAllByIds(List.of(defId))).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(defId))).willReturn(values);
      given(clothesAttributeWithDefMapper.toDto(
          def.getId(),
          def.getName(),
          values.stream()
              .map(SelectableValue::getItem)
              .toList(),
          "M")).willReturn(defDto);
      given(clothesMapper.toDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes)).willReturn(dto);

      // when
      ClothesDto result = clothesService.update(clothesId, request, image);

      // then
      assertEquals(result, dto);
      then(fileStorage).should(times(0)).remove(anyString());
      then(fileStorage).should(times(0)).upload(any(MultipartFile.class), any(FileDomain.class));
    }

    @Test
    @DisplayName("의상 수정 성공 - 속성 빈리스트")
    void update_success_not_select_values() {

      // given
      // 1. 사용자 생성
      UUID userId = UUID.randomUUID();
      User user = User.createUser("email", "test", "qwer123!");
      ReflectionTestUtils.setField(user, "id", userId);

      // 2. 의상 속성 생성
      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      UUID valueId1 = UUID.randomUUID();
      UUID valueId2 = UUID.randomUUID();
      SelectableValue value1 = SelectableValue.create(def.getId(), "S");
      SelectableValue value2 = SelectableValue.create(def.getId(), "M");
      ReflectionTestUtils.setField(value1, "id", valueId1);
      ReflectionTestUtils.setField(value2, "id", valueId2);

      // 3. 의상 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(user.getId(), "상의", ClothesType.TOP, "url");
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // 4. 의상 - 속성 값 생성 - 사이즈 : S
      UUID attributeId = UUID.randomUUID();
      ClothesAttribute attribute = ClothesAttribute.create(clothes.getId(), value1.getId());
      ReflectionTestUtils.setField(attribute, "id", attributeId);

      // 5. 수정 request
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      MultipartFile image = mock(MultipartFile.class);

      // 6. 새 이미지 url
      String newUrl = "newUrl";

      // 8. 반환 dto
      List<ClothesAttributeWithDefDto> attributes = List.of();
      ClothesDto dto = new ClothesDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes);

      given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
      given(userRepository.findById(clothes.getOwnerId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);
      given(clothesMapper.toDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes)).willReturn(dto);

      // when
      ClothesDto result = clothesService.update(clothesId, request, image);

      // then
      assertEquals(result, dto);
      assertEquals(result.attributes(), attributes);

      then(clothesAttributeDefService).should(times(0)).findAllByIds(anyList());
      then(selectableValueService).should(times(0)).findAllByAttributeDefIdIn(anyList());
      then(clothesAttributeWithDefMapper).should(times(0)).toDto(any(UUID.class), anyString(), anyList(), anyString());
    }

    @Test
    @DisplayName("잘못된 의상 id일 경우 의상 조회 실패")
    void update_fail_not_found_clothes() {

      // given
      UUID clothesId = UUID.randomUUID();
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      MultipartFile image = mock(MultipartFile.class);

      given(clothesRepository.findById(clothesId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesNotFoundException.class, () -> clothesService.update(clothesId, request, image));

      then(userRepository).should(times(0)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("잘못된 사용자 id일 경우 사용자 조회 실패")
    void update_fail_not_found_user() {

      // given
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(UUID.randomUUID(), "사이즈", ClothesType.TOP, null);
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      MultipartFile image = mock(MultipartFile.class);

      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));
      given(userRepository.findById(clothes.getOwnerId())).willReturn(Optional.empty());

      // when, then
      assertThrows(UserNotFoundException.class, () -> clothesService.update(clothesId, request, image));

      then(clothesAttributeService).should(times(0)).deleteAllByClothesId(clothesId);
    }

    @Test
    @DisplayName("잘못된 속성 선택일 경우 해당 속성값이 없어 실패")
    void update_fail_not_found_selectable_value() {

      // given
      // 1. 사용자 생성
      UUID userId = UUID.randomUUID();
      User user = User.createUser("email", "test", "qwer123!");
      ReflectionTestUtils.setField(user, "id", userId);

      // 2. 의상 속성 생성
      UUID defId = UUID.randomUUID();
      ClothesAttributeDef def = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def, "id", defId);

      UUID valueId1 = UUID.randomUUID();
      UUID valueId2 = UUID.randomUUID();
      SelectableValue value1 = SelectableValue.create(def.getId(), "S");
      SelectableValue value2 = SelectableValue.create(def.getId(), "M");
      ReflectionTestUtils.setField(value1, "id", valueId1);
      ReflectionTestUtils.setField(value2, "id", valueId2);

      // 3. 의상 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes = Clothes.create(user.getId(), "상의", ClothesType.TOP, "url");
      ReflectionTestUtils.setField(clothes, "id", clothesId);

      // 4. 의상 - 속성 값 생성 - 사이즈 : S
      UUID attributeId = UUID.randomUUID();
      ClothesAttribute attribute = ClothesAttribute.create(clothes.getId(), value1.getId());
      ReflectionTestUtils.setField(attribute, "id", attributeId);

      // 5. 수정 request
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def.getId(), "XL")));
      MultipartFile image = mock(MultipartFile.class);

      // 6. 새 이미지 url
      String newUrl = "newUrl";

      // 7. 옷 - 속성 값 연관 생성
      List<ClothesAttributeDef> defs = List.of(def);
      List<SelectableValue> values = List.of(value1, value2);

      // 8. 반환 dto
      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(
          def.getId(),
          def.getName(),
          values.stream()
              .map(SelectableValue::getItem)
              .toList(),
          "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);
      ClothesDto dto = new ClothesDto(clothes.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes);

      given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
      given(userRepository.findById(clothes.getOwnerId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);
      given(clothesAttributeDefService.findAllByIds(List.of(defId))).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(defId))).willReturn(values);

      // when, then
      assertThrows(SelectableValueNotFoundException.class, () -> clothesService.update(clothesId, request, image));

      then(clothesAttributeWithDefMapper).should(times(0)).toDto(
          any(UUID.class), anyString(), anyList(), anyString()
      );
    }
  }
}