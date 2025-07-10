package com.part4.team09.otboo.module.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.assembler.ClothesAttributeWithDefDtoAssembler;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttribute;
import com.part4.team09.otboo.module.domain.clothes.entity.ClothesAttributeDef;
import com.part4.team09.otboo.module.domain.clothes.entity.SelectableValue;
import com.part4.team09.otboo.module.domain.clothes.exception.Clothes.ClothesNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef.BadRequestException;
import com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue.SelectableValueNotFoundException;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesAttributeWithDefMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesDtoCursorResponseMapper;
import com.part4.team09.otboo.module.domain.clothes.mapper.ClothesMapper;
import com.part4.team09.otboo.module.domain.clothes.repository.ClothesRepository;
import com.part4.team09.otboo.module.domain.clothes.repository.custom.ClothesRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
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

  @Mock
  private ClothesRepositoryQueryDSL clothesRepositoryQueryDSL;

  @Mock
  private ClothesAttributeWithDefDtoAssembler clothesAttributeWithDefDtoAssembler;

  @Spy
  private ClothesMapper clothesMapper;

  @Spy
  private ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  @Spy
  private ClothesDtoCursorResponseMapper clothesDtoCursorResponseMapper;

  @Mock
  private FileStorage fileStorage;

  private User user;
  private Clothes clothes1;
  private Clothes clothes2;
  private Clothes clothesWithoutImage;
  private MultipartFile image;
  private String imageUrl;
  private ClothesAttributeDef def1;
  private SelectableValue value1;
  private SelectableValue value2;
  private ClothesAttributeDef def2;
  private SelectableValue value3;
  private SelectableValue value4;

  @BeforeEach
  void setUp() {

    // 사용자
    user = User.createUser("tes@gmail.com", "test", "test12");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    // 의상 이미지
    image = mock(MultipartFile.class);
    imageUrl = "testUrl";

    // 의상
    clothes1 = Clothes.create(user.getId(), "상의", ClothesType.TOP, imageUrl);
    ReflectionTestUtils.setField(clothes1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothes1, "createdAt", LocalDateTime.now());

    clothes2 = Clothes.create(user.getId(), "티셔츠", ClothesType.TOP, imageUrl);
    ReflectionTestUtils.setField(clothes2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothes2, "createdAt", LocalDateTime.now());

    clothesWithoutImage = Clothes.create(user.getId(), "상의", ClothesType.TOP, null);
    ReflectionTestUtils.setField(clothesWithoutImage, "id", UUID.randomUUID());

    // 사이즈
    // 의상 속성 정의
    def1 = ClothesAttributeDef.create("사이즈");
    ReflectionTestUtils.setField(def1, "id", UUID.randomUUID());

    // 의상 속성 값
    value1 = SelectableValue.create(def1.getId(), "S");
    value2 = SelectableValue.create(def1.getId(), "M");
    ReflectionTestUtils.setField(value1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value2, "id", UUID.randomUUID());

    // 색상
    // 의상 속성 정의
    def2 = ClothesAttributeDef.create("색상");
    ReflectionTestUtils.setField(def2, "id", UUID.randomUUID());

    // 의상 속성 값
    value3 = SelectableValue.create(def2.getId(), "레드");
    value4 = SelectableValue.create(def2.getId(), "블루");
    ReflectionTestUtils.setField(value3, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(value4, "id", UUID.randomUUID());
  }

  @Nested
  @DisplayName("의상 등록")
  class Create {

    @Test
    @DisplayName("의상 등록 성공 - 속성 선택 O")
    void create_success_with_value() {

      // given
      List<ClothesAttributeDto> attributes = List.of(new ClothesAttributeDto(def1.getId(), "S"));
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "상의", ClothesType.TOP,
          attributes);

      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(imageUrl);
      given(clothesRepository.save(any(Clothes.class)))
          .willAnswer(invocation -> {
            Clothes param = invocation.getArgument(0);
            ReflectionTestUtils.setField(param, "id", clothes1.getId());
            return param;
          });

      List<UUID> defIds = List.of(def1.getId());
      List<SelectableValue> values = List.of(value1, value2);
      given(selectableValueService.findAllByAttributeDefIdIn(defIds)).willReturn(values);

      // createClothesAttributes 메서드
      List<SelectableValue> selectableValues = List.of(value1);
      List<UUID> selectableValueIds = selectableValues.stream()
          .map(SelectableValue::getId)
          .toList();
      ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes1.getId(),
          value1.getId());
      List<ClothesAttribute> clothesAttributes = List.of(clothesAttribute);
      given(clothesAttributeService.create(clothes1.getId(), selectableValueIds)).willReturn(
          clothesAttributes);

      // createAttributeDtos 메서드
      List<ClothesAttributeDef> defs = List.of(def1);
      given(clothesAttributeDefService.findAllByIds(defIds)).willReturn(defs);

      Map<UUID, String> defMap = defs.stream()
          .collect(Collectors.toMap(ClothesAttributeDef::getId, ClothesAttributeDef::getName));
      Map<UUID, List<SelectableValue>> selectableValueMap = values.stream()
          .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));
      List<String> selectableItems = selectableValues.stream()
          .map(SelectableValue::getItem)
          .toList();
      List<ClothesAttributeWithDefDto> dtos = List.of(new ClothesAttributeWithDefDto(def1.getId(),
          def1.getName(), selectableItems, value1.getItem()));
      given(clothesAttributeWithDefMapper.toDto(request.attributes(), defMap, selectableValueMap))
          .willReturn(dtos);

      ClothesDto clothesDto = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), dtos);
      given(clothesMapper.toDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), dtos)).willReturn(clothesDto);

      // when
      ClothesDto result = clothesService.create(request, image);

      // then
      assertEquals(result, clothesDto);
      then(userRepository).should().findById(user.getId());
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(clothesAttributeDefService).should().findAllByIds(defIds);
      then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
      then(clothesAttributeWithDefMapper).should().toDto(request.attributes(), defMap, selectableValueMap);
      then(clothesAttributeService).should().create(clothes1.getId(), selectableValueIds);
      then(clothesMapper).should().toDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), dtos);
    }

    @Test
    @DisplayName("의상 등록 성공 - 속성 선택 X, 이미지 X")
    void create_success_without_value_and_image() {

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
      Clothes clothes1 = Clothes.create(request.ownerId(), request.name(), request.type(), url);
      ReflectionTestUtils.setField(clothes1, "id", clothesId);

      // clothesMapper
      List<ClothesAttributeWithDefDto> responseAttributes = List.of();
      ClothesDto clothesDto = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), responseAttributes);

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));
      given(clothesRepository.save(any(Clothes.class))).willReturn(clothes1);
      given(clothesMapper.toDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
          clothes1.getImageUrl(), clothes1.getType(), responseAttributes)).willReturn(clothesDto);

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
          .toDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(), clothes1.getImageUrl(),
              clothes1.getType(), responseAttributes);
    }

    @Test
    @DisplayName("사용자가 없을 경우 예외처리")
    void create_not_found_user() {
      // given
      UUID invalidUserId = UUID.randomUUID();
      List<ClothesAttributeDto> attributes = List.of();
      ClothesCreateRequest request = new ClothesCreateRequest(invalidUserId, "상의",
          ClothesType.TOP, attributes);

      given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> clothesService.create(request, image));

      then(userRepository).should().findById(invalidUserId);
      then(clothesRepository).should(times(0)).save(any(Clothes.class));
    }

    @Test
    @DisplayName("선택한 값이 잘못된 이름일 경우 선택 가능한 값 조회 실패 - 사이즈에 S, M 이 있지만 XL을 받았을 경우 빈 리스트 반환")
    void create_selectable_value_not_found() {

      // given
      List<ClothesAttributeDto> attributes = List.of(new ClothesAttributeDto(def1.getId(), "XL"));
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), "상의", ClothesType.TOP,
          attributes);

      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(imageUrl);
      given(clothesRepository.save(any(Clothes.class)))
          .willAnswer(invocation -> {
            Clothes param = invocation.getArgument(0);
            ReflectionTestUtils.setField(param, "id", clothes1.getId());
            return param;
          });
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId())))
          .willReturn(List.of(value1, value2));

      // when,  the
      assertThrows(SelectableValueNotFoundException.class,
          () -> clothesService.create(request, image));

      then(userRepository).should().findById(user.getId());
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(selectableValueService).should().findAllByAttributeDefIdIn(List.of(def1.getId()));

    }
  }
  
  @Nested
  @DisplayName("의상 조회")
  class FindByCursor {
    
    // 1. 조회 성공 - hasNext가 없을 경우
    @Test
    @DisplayName("조회 성공 - hasNext가 없을 경우")
    void find_by_cursor_success_no_has_next() {
      
      // given
      String cursor = null;
      UUID idAfter = null;
      int limit = 10;
      ClothesType typeEqual = null;
      UUID ownerId = user.getId();

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));

      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;
      List<Clothes> clothesList = List.of(clothes1, clothes2);
      given(clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, ClothesType.TOP, ownerId,
          sortBy, sortDirection)).willReturn(clothesList);

      boolean hasNext = clothesList.size() > limit;
      String nextCursor = null;
      UUID nexIdAfter = null;
      int totalCount = clothesList.size();
      given(clothesRepository.countByOwnerIdAndType(ownerId, ClothesType.TOP)).willReturn(totalCount);

      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList1 = List.of(
          new ClothesAttributeWithDefDto(
              def1.getId(),
              def1.getName(),
              List.of(value1.getItem(), value2.getItem()),
              value1.getItem()),
          new ClothesAttributeWithDefDto(
              def2.getId(),
              def2.getName(),
              List.of(value3.getItem(), value4.getItem()),
              value3.getItem()
          ));
      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList2 = List.of(
          new ClothesAttributeWithDefDto(
              def1.getId(),
              def1.getName(),
              List.of(value1.getItem(), value2.getItem()),
              value2.getItem()),
          new ClothesAttributeWithDefDto(
              def2.getId(),
              def2.getName(),
              List.of(value3.getItem(), value4.getItem()),
              value4.getItem()
          ));
      ClothesDto data1 = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(), clothes1.getImageUrl(),
          clothes1.getType(), clothesAttributeWithDefDtoList1);
      ClothesDto data2 = new ClothesDto(clothes2.getId(), clothes2.getOwnerId(), clothes2.getName(), clothes2.getImageUrl(),
          clothes2.getType(), clothesAttributeWithDefDtoList2);

      List<ClothesDto> data = List.of(data1, data2);
      given(clothesAttributeWithDefDtoAssembler.assemble(clothes1.getId())).willReturn(clothesAttributeWithDefDtoList1);
      given(clothesAttributeWithDefDtoAssembler.assemble(clothes2.getId())).willReturn(clothesAttributeWithDefDtoList2);

      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(data, nextCursor, nexIdAfter,
          hasNext, totalCount, sortBy, sortDirection);

      // when
      ClothesDtoCursorResponse result = clothesService.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId);

      // then
      assertEquals(result, response);

      then(userRepository).should().findById(ownerId);
      then(clothesRepositoryQueryDSL).should().findByCursor(cursor, idAfter, limit, ClothesType.TOP,
          ownerId, sortBy, sortDirection);
      then(clothesRepository).should().countByOwnerIdAndType(ownerId, ClothesType.TOP);
      then(clothesAttributeWithDefDtoAssembler).should().assemble(clothes1.getId());
      then(clothesAttributeWithDefDtoAssembler).should().assemble(clothes2.getId());
    }

    @Test
    @DisplayName("조회 성공 - hasNext가 있을 경우")
    void find_by_cursor_success_with_has_next() {

      //given
      String cursor = null;
      UUID idAfter = null;
      int limit = 1;
      ClothesType typeEqual = null;
      UUID ownerId = user.getId();

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));

      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;
      List<Clothes> clothesList = List.of(clothes1, clothes2);
      given(clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, ClothesType.TOP, ownerId,
          sortBy, sortDirection)).willReturn(clothesList);

      boolean hasNext = clothesList.size() > limit;
      clothesList = clothesList.subList(0, limit);
      Clothes lastClothes = clothesList.get(clothesList.size() - 1);
      String nextCursor = lastClothes.getCreatedAt().toString();
      UUID nexIdAfter = lastClothes.getId();
      int totalCount = clothesList.size();
      given(clothesRepository.countByOwnerIdAndType(ownerId, ClothesType.TOP)).willReturn(totalCount);

      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList1 = List.of(
          new ClothesAttributeWithDefDto(
              def1.getId(),
              def1.getName(),
              List.of(value1.getItem(), value2.getItem()),
              value1.getItem()),
          new ClothesAttributeWithDefDto(
              def2.getId(),
              def2.getName(),
              List.of(value3.getItem(), value4.getItem()),
              value3.getItem()
          ));
      ClothesDto data1 = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(), clothes1.getImageUrl(),
          clothes1.getType(), clothesAttributeWithDefDtoList1);

      List<ClothesDto> data = List.of(data1);
      given(clothesAttributeWithDefDtoAssembler.assemble(clothes1.getId())).willReturn(clothesAttributeWithDefDtoList1);

      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(data, nextCursor, nexIdAfter,
          hasNext, totalCount, sortBy, sortDirection);

      // when
      ClothesDtoCursorResponse result = clothesService.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId);

      // then
      assertEquals(result, response);

      then(userRepository).should().findById(ownerId);
      then(clothesRepositoryQueryDSL).should().findByCursor(cursor, idAfter, limit, ClothesType.TOP,
          ownerId, sortBy, sortDirection);
      then(clothesRepository).should().countByOwnerIdAndType(ownerId, ClothesType.TOP);
      then(clothesAttributeWithDefDtoAssembler).should().assemble(clothes1.getId());
      then(clothesAttributeWithDefDtoAssembler).should(times(0)).assemble(clothes2.getId());
    }

    @Test
    @DisplayName("조회 성공 - 조회한 옷이 없을 경우 빈 리스트 반환")
    void find_by_cursor_success_with_empty_list() {

      // given
      String cursor = null;
      UUID idAfter = null;
      int limit = 10;
      ClothesType typeEqual = ClothesType.BOTTOM;
      UUID ownerId = user.getId();

      given(userRepository.findById(ownerId)).willReturn(Optional.of(user));

      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;
      List<Clothes> clothesList = List.of();
      given(clothesRepositoryQueryDSL.findByCursor(cursor, idAfter, limit, typeEqual, ownerId,
          sortBy, sortDirection)).willReturn(clothesList);

      boolean hasNext = clothesList.size() > limit;
      String nextCursor = null;
      UUID nexIdAfter = null;
      int totalCount = clothesList.size();
      given(clothesRepository.countByOwnerIdAndType(ownerId, typeEqual)).willReturn(totalCount);

      List<ClothesDto> data = List.of();

      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(data, nextCursor, nexIdAfter,
          hasNext, totalCount, sortBy, sortDirection);

      // when
      ClothesDtoCursorResponse result = clothesService.findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId);

      // then
      assertEquals(result, response);

      then(userRepository).should().findById(ownerId);
      then(clothesRepositoryQueryDSL).should().findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);
      then(clothesRepository).should().countByOwnerIdAndType(ownerId, typeEqual);
      then(clothesAttributeWithDefDtoAssembler).should(times(0)).assemble(any(UUID.class));
    }

    @Test
    @DisplayName("0이하의 limit를 받을 경우")
    void find_by_cursor_invalid_limit() {

      //given
      String cursor = null;
      UUID idAfter = null;
      int limit = 0;
      ClothesType typeEqual = null;
      UUID ownerId = user.getId();

      // when, then
      assertThrows(BadRequestException.class, () -> clothesService.findByCursor(cursor, idAfter,
          limit, typeEqual, ownerId));

      then(userRepository).should(times(0)).findById(ownerId);
    }
    // 5. 사용자가 없을 경우

    @Test
    @DisplayName("사용자가 없을 경우")
    void find_by_cursor_not_found_user() {

      //given
      String cursor = null;
      UUID idAfter = null;
      int limit = 1;
      ClothesType typeEqual = null;
      UUID ownerId = user.getId();
      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;

      // when, then
      assertThrows(UserNotFoundException.class, () -> clothesService.findByCursor(cursor, idAfter,
          limit, typeEqual, ownerId));

      then(clothesRepositoryQueryDSL).should(times(0))
          .findByCursor(cursor, idAfter, limit, ClothesType.TOP, ownerId, sortBy, sortDirection);
    }
  }

  @Nested
  @DisplayName("의상 수정")
  class Update {

    @Test
    @DisplayName("의상 수정 성공 - 이미지 O")
    void update_success_with_image() {

      // given
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def1.getId(), "M")));
      String newUrl = "newUrl";

      List<ClothesAttributeDef> defs = List.of(def1);
      List<SelectableValue> values = List.of(value1, value2);
      List<String> items = values.stream().map(SelectableValue::getItem).toList();

      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), items, "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);

      ClothesDto dto = new ClothesDto(clothes1.getId(), user.getId(), request.name(), newUrl, request.type(), attributes);

      Map<UUID, String> defMap = Map.of(def1.getId(), def1.getName());
      Map<UUID, List<SelectableValue>> selectableValueMap = values.stream()
          .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

      given(clothesRepository.findById(clothes1.getId())).willReturn(Optional.of(clothes1));
      given(userRepository.findById(clothes1.getOwnerId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);
      given(clothesAttributeDefService.findAllByIds(List.of(def1.getId()))).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId()))).willReturn(values);
      given(clothesAttributeWithDefMapper.toDto(request.attributes(), defMap, selectableValueMap)).willReturn(attributes);
      given(clothesMapper.toDto(clothes1.getId(), user.getId(), request.name(), newUrl, request.type(), attributes)).willReturn(dto);


      // when
      ClothesDto result = clothesService.update(clothes1.getId(), request, image);

      // then
      assertEquals(dto, result);
      assertEquals(attributes, result.attributes());

      then(clothesRepository).should().findById(clothes1.getId());
      then(userRepository).should().findById(clothes1.getOwnerId());
      then(fileStorage).should().remove(imageUrl);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesAttributeDefService).should().findAllByIds(List.of(def1.getId()));
      then(selectableValueService).should().findAllByAttributeDefIdIn(List.of(def1.getId()));
      then(clothesAttributeWithDefMapper).should().toDto(request.attributes(), defMap, selectableValueMap);
      then(clothesMapper).should().toDto(clothes1.getId(), user.getId(), request.name(), newUrl, request.type(), attributes);
    }

    @Test
    @DisplayName("의상 수정 성공 - 이미지 X")
    void update_success_no_image() {

      // given
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def1.getId(), "M")));
      MultipartFile image = null;
      String newUrl = null;

      List<ClothesAttributeDef> defs = List.of(def1);
      List<SelectableValue> values = List.of(value1, value2);
      List<String> items = values.stream().map(SelectableValue::getItem).toList();

      Map<UUID, String> defMap = Map.of(def1.getId(), def1.getName());
      Map<UUID, List<SelectableValue>> selectableValueMap = values.stream()
          .collect(Collectors.groupingBy(SelectableValue::getAttributeDefId));

      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), items, "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);

      ClothesDto dto = new ClothesDto(clothesWithoutImage.getId(), user.getId(), request.name(), newUrl, request.type(), attributes);

      given(clothesRepository.findById(clothesWithoutImage.getId())).willReturn(Optional.of(clothesWithoutImage));
      given(userRepository.findById(clothesWithoutImage.getOwnerId())).willReturn(Optional.of(user));
      given(clothesAttributeDefService.findAllByIds(List.of(def1.getId()))).willReturn(defs);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId()))).willReturn(values);
      given(clothesAttributeWithDefMapper.toDto(request.attributes(), defMap, selectableValueMap)).willReturn(attributes);
      given(clothesMapper.toDto(clothesWithoutImage.getId(), user.getId(), request.name(), newUrl, request.type(), attributes)).willReturn(dto);

      // when
      ClothesDto result = clothesService.update(clothesWithoutImage.getId(), request, image);

      // then
      assertEquals(dto, result);
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
      ClothesAttributeDef def1 = ClothesAttributeDef.create("사이즈");
      ReflectionTestUtils.setField(def1, "id", defId);

      UUID valueId1 = UUID.randomUUID();
      UUID valueId2 = UUID.randomUUID();
      SelectableValue value1 = SelectableValue.create(def1.getId(), "S");
      SelectableValue value2 = SelectableValue.create(def1.getId(), "M");
      ReflectionTestUtils.setField(value1, "id", valueId1);
      ReflectionTestUtils.setField(value2, "id", valueId2);

      // 3. 의상 생성
      UUID clothesId = UUID.randomUUID();
      Clothes clothes1 = Clothes.create(user.getId(), "상의", ClothesType.TOP, "url");
      ReflectionTestUtils.setField(clothes1, "id", clothesId);

      // 4. 의상 - 속성 값 생성 - 사이즈 : S
      UUID attributeId = UUID.randomUUID();
      ClothesAttribute attribute = ClothesAttribute.create(clothes1.getId(), value1.getId());
      ReflectionTestUtils.setField(attribute, "id", attributeId);

      // 5. 수정 request
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      MultipartFile image = mock(MultipartFile.class);

      // 6. 새 이미지 url
      String newUrl = "newUrl";

      // 8. 반환 dto
      List<ClothesAttributeWithDefDto> attributes = List.of();
      ClothesDto dto = new ClothesDto(clothes1.getId(), user.getId(), request.name(),
          newUrl, request.type(), attributes);

      given(clothesRepository.findById(clothes1.getId())).willReturn(Optional.of(clothes1));
      given(userRepository.findById(clothes1.getOwnerId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);
      given(clothesMapper.toDto(clothes1.getId(), user.getId(), request.name(),
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
      Clothes clothes1 = Clothes.create(UUID.randomUUID(), "사이즈", ClothesType.TOP, null);
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      MultipartFile image = mock(MultipartFile.class);

      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes1));
      given(userRepository.findById(clothes1.getOwnerId())).willReturn(Optional.empty());

      // when, then
      assertThrows(UserNotFoundException.class, () -> clothesService.update(clothesId, request, image));

      then(clothesAttributeService).should(times(0)).deleteAllByClothesId(clothesId);
    }

    @Test
    @DisplayName("잘못된 속성 선택일 경우 해당 속성값이 없어 실패")
    void update_fail_not_found_selectable_value() {

      // given
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def1.getId(), "XL")));

      // 7. 옷 - 속성 값 연관 생성
      List<ClothesAttributeDef> defs = List.of(def1);
      List<SelectableValue> values = List.of(value1, value2);

      given(clothesRepository.findById(clothes1.getId())).willReturn(Optional.of(clothes1));
      given(userRepository.findById(clothes1.getOwnerId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(imageUrl);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId()))).willReturn(values);

      // when, then
      assertThrows(SelectableValueNotFoundException.class,
          () -> clothesService.update(clothes1.getId(), request, image));
    }
  }

  @Nested
  @DisplayName("의상 삭제")
  class Delete {

    @Test
    @DisplayName("의상 삭제 성공 - 연관 관계, 이미지 삭제")
    void delete_success() {

      // given
      UUID requestClothesId = clothes1.getId();

      given(clothesRepository.findById(requestClothesId)).willReturn(Optional.of(clothes1));
      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);

      // when
      clothesService.delete(requestClothesId);

      // then
      then(clothesRepository).should().findById(requestClothesId);
      then(clothesAttributeService).should().deleteAllByClothesId(requestClothesId);
      then(fileStorage).should().remove(clothes1.getImageUrl());
      then(clothesRepository).should().deleteById(requestClothesId);

    }

    @Test
    @DisplayName("의상 삭제 성공 - 이미지가 없을 경우 fileStorage 접근 X")
    void delete_success_no_image() {

      // given
      UUID requestClothesId = clothesWithoutImage.getId();

      given(clothesRepository.findById(requestClothesId)).willReturn(Optional.of(clothesWithoutImage));

      // when
      clothesService.delete(requestClothesId);

      // then
      then(clothesRepository).should().findById(requestClothesId);
      then(clothesAttributeService).should().deleteAllByClothesId(requestClothesId);
      then(fileStorage).should(times(0)).remove(clothes1.getImageUrl());
      then(clothesRepository).should().deleteById(requestClothesId);

    }

    @Test
    @DisplayName("해당 의상이 없는 경우 실패")
    void delete_not_found_clothes() {

      // given
      UUID requestClothesId = UUID.randomUUID();

      given(clothesRepository.findById(requestClothesId)).willReturn(Optional.empty());

      // when, then
      assertThrows(ClothesNotFoundException.class, () -> clothesService.delete(requestClothesId));

      then(clothesRepository).should().findById(requestClothesId);
      then(clothesAttributeService).should(times(0)).deleteAllByClothesId(requestClothesId);
      then(fileStorage).should(times(0)).remove(anyString());
      then(clothesRepository).should(times(0)).deleteById(requestClothesId);

    }
  }
}