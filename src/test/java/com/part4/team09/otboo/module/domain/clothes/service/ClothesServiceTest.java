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
import com.part4.team09.otboo.module.domain.clothes.assembler.ClothesDtoAssembler;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeRowDto;
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
import com.part4.team09.otboo.module.domain.feed.repository.OotdRepository;
import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ClothesServiceTest {

  private static final Logger log = LoggerFactory.getLogger(ClothesServiceTest.class);
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
  private OotdRepository ootdRepository;

  @Mock
  private ClothesAttributeWithDefDtoAssembler clothesAttributeWithDefDtoAssembler;

  @Spy
  private ClothesMapper clothesMapper;

  @Spy
  private ClothesAttributeWithDefMapper clothesAttributeWithDefMapper;

  @Spy
  private ClothesDtoCursorResponseMapper clothesDtoCursorResponseMapper;

  @Mock
  private ClothesDtoAssembler clothesDtoAssembler;

  @Mock
  private FileStorage fileStorage;

  private User user;
  private UUID clothes1Id;
  private Clothes clothes1;
  private Clothes clothes2;
  private UUID clothesWithoutImageId;
  private Clothes clothesWithoutImage;
  private MultipartFile image;
  private String imageUrl;
  private ClothesAttributeDef def1;
  private SelectableValue value1;
  private SelectableValue value2;
  private ClothesAttributeDef def2;
  private SelectableValue value3;
  private SelectableValue value4;
  private ClothesAttribute clothesAttribute1;
  private ClothesAttribute clothesAttribute2;
  private ClothesAttribute clothesAttribute3;
  private ClothesAttribute clothesAttribute4;

  @BeforeEach
  void setUp() {

    // 사용자
    user = User.createUser("tes@gmail.com", "test", "test12");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    // 의상 이미지
    image = mock(MultipartFile.class);
    imageUrl = "testUrl";

    // 의상
    clothes1Id = UUID.randomUUID();
    clothes1 = Clothes.create(user.getId(), "상의", ClothesType.TOP, imageUrl);
    ReflectionTestUtils.setField(clothes1, "id", clothes1Id);
    ReflectionTestUtils.setField(clothes1, "createdAt", LocalDateTime.now());

    clothes2 = Clothes.create(user.getId(), "티셔츠", ClothesType.TOP, imageUrl);
    ReflectionTestUtils.setField(clothes2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(clothes2, "createdAt", LocalDateTime.now());

    clothesWithoutImageId = UUID.randomUUID();
    clothesWithoutImage = Clothes.create(user.getId(), "상의", ClothesType.TOP, null);
    ReflectionTestUtils.setField(clothesWithoutImage, "id", clothesWithoutImageId);

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

    // 의상 연관
    clothesAttribute1 = ClothesAttribute.create(clothes1.getId(), value1.getId());
    clothesAttribute2 = ClothesAttribute.create(clothes1.getId(), value3.getId());
    clothesAttribute3 = ClothesAttribute.create(clothes2.getId(), value2.getId());
    clothesAttribute4 = ClothesAttribute.create(clothes2.getId(), value4.getId());
  }

  @Nested
  @DisplayName("의상 등록")
  class Create {

    @Test
    @DisplayName("의상 등록 성공 - 속성 선택 O")
    void create_success_with_value() {

      // given
      UUID userId = user.getId();
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), clothes1.getName(),
          clothes1.getType(), List.of(new ClothesAttributeDto(def1.getId(), value1.getItem()),
          new ClothesAttributeDto(def2.getId(), value3.getItem())));

      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(imageUrl);
      given(clothesRepository.save(any(Clothes.class))).willAnswer(invocationOnMock -> {
        Clothes clothes = invocationOnMock.getArgument(0);
        ReflectionTestUtils.setField(clothes, "id", clothes1Id);
        return clothes;
      });

      List<UUID> defIds = List.of(def1.getId(), def2.getId());
      List<SelectableValue> values = List.of(value1, value2, value3, value4);
      given(selectableValueService.findAllByAttributeDefIdIn(defIds)).willReturn(values);

      List<UUID> selectedValueIds = List.of(value1.getId(), value3.getId());
      List<ClothesAttribute> clothesAttributes = List.of(clothesAttribute1, clothesAttribute2);
      given(clothesAttributeService.create(clothes1.getId(), selectedValueIds)).willReturn(clothesAttributes);

      List<ClothesAttributeRowDto> clothesWithAttributesDtos = List.of(
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def1.getId(),
          def1.getName(),
          value1.getItem()),
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def2.getId(),
          def2.getName(),
          value3.getItem()));
      given(clothesRepository.findByClothesId(clothes1.getId())).willReturn(
        clothesWithAttributesDtos);

      given(selectableValueService.findAll()).willReturn(values);

      List<ClothesAttributeWithDefDto> attributeDtos = List.of(
          new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), List.of(value1.getItem(), value2.getItem()),
              value1.getItem()),
          new ClothesAttributeWithDefDto(def2.getId(), def2.getName(), List.of(value3.getItem(), value4.getItem()),
              value3.getItem())
      );
      ClothesDto clothesDto = new ClothesDto(clothes1.getId(), user.getId(), clothes1.getName(),
        clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(), attributeDtos);
      given(clothesDtoAssembler.assemble(clothesWithAttributesDtos, values)).willReturn(clothesDto);

      // when
      ClothesDto result = clothesService.create(userId, request, image);

      // then
      assertEquals(clothesDto, result);

      then(userRepository).should().findById(user.getId());
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(selectableValueService).should().findAllByAttributeDefIdIn(defIds);
      then(clothesAttributeService).should().create(clothes1.getId(), selectedValueIds);
    }

    @Test
    @DisplayName("의상 등록 성공 - 속성 선택 X, 이미지 X")
    void create_success_without_value_and_image() {

      // given
      ClothesCreateRequest request = new ClothesCreateRequest(user.getId(), clothesWithoutImage.getName(),
          clothesWithoutImage.getType(), List.of());

      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(clothesRepository.save(any(Clothes.class))).willAnswer(invocationOnMock -> {
        Clothes clothes = invocationOnMock.getArgument(0);
        ReflectionTestUtils.setField(clothes, "id", clothesWithoutImageId);
        return clothes;
      });

      ClothesDto clothesDto = new ClothesDto(clothesWithoutImage.getId(), user.getId(), clothesWithoutImage.getName(),
        clothesWithoutImage.getImageUrl(), clothesWithoutImage.getType(),
        clothesWithoutImage.getCreatedAt(), List.of());

      // when
      ClothesDto result = clothesService.create(user.getId(), request, null);

      // then
      assertEquals(clothesDto, result);

      then(userRepository).should().findById(user.getId());
      then(fileStorage).should(times(0)).upload(image, FileDomain.CLOTHES_IMAGE);
      then(clothesRepository).should().save(any(Clothes.class));
      then(selectableValueService).should(times(0)).findAllByAttributeDefIdIn(anyList());
      then(clothesAttributeService).should(times(0)).create(any(UUID.class), anyList());
    }

    @Test
    @DisplayName("사용자가 없을 경우 예외처리")
    void create_not_found_user() {

      // given
      UUID invalidUserId = UUID.randomUUID();
      ClothesCreateRequest request = new ClothesCreateRequest(invalidUserId, "상의",
          ClothesType.TOP, List.of());

      given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class,
        () -> clothesService.create(invalidUserId, request, image));

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
          .willAnswer(invocationOnMock -> {
            Clothes clothes = invocationOnMock.getArgument(0);
            ReflectionTestUtils.setField(clothes, "id", clothes1Id);
            return clothes;
          });
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId())))
          .willReturn(List.of(value1, value2));

      // when,  the
      assertThrows(SelectableValueNotFoundException.class,
        () -> clothesService.create(user.getId(), request, image));

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

      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;
      List<Clothes> clothesList = List.of(clothes1, clothes2);
      List<ClothesAttributeRowDto> clothesWithAttributesDtos = List.of(
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def1.getId(),
          def1.getName(),
          value1.getItem()),
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def2.getId(),
          def2.getName(),
          value3.getItem()),
        new ClothesAttributeRowDto(clothes2.getId(), clothes2.getCreatedAt(),
          clothes2.getOwnerId(),
          clothes2.getName(), clothes2.getImageUrl(), clothes2.getType(), def1.getId(),
          def1.getName(),
          value2.getItem()),
        new ClothesAttributeRowDto(clothes2.getId(), clothes2.getCreatedAt(),
          clothes2.getOwnerId(),
          clothes2.getName(), clothes2.getImageUrl(), clothes2.getType(), def2.getId(),
          def2.getName(),
          value4.getItem())
      );
      given(clothesRepository.findByCursor(cursor, idAfter, limit, ClothesType.TOP, ownerId, sortBy,
        sortDirection))
        .willReturn(clothesWithAttributesDtos);

      List<SelectableValue> values = List.of(value1, value2, value3, value4);
      given(selectableValueService.findAll()).willReturn(values);

      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList1 = List.of(
        new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), List.of(value1.getItem(),
          value2.getItem()), value1.getItem()),
        new ClothesAttributeWithDefDto(def2.getId(), def2.getName(), List.of(value3.getItem(),
          value4.getItem()), value3.getItem()
        ));
      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList2 = List.of(
        new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), List.of(value1.getItem(),
          value2.getItem()), value2.getItem()),
        new ClothesAttributeWithDefDto(def2.getId(), def2.getName(), List.of(value3.getItem(),
          value4.getItem()), value4.getItem()
        ));
      ClothesDto data1 = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
        clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(),
        clothesAttributeWithDefDtoList1);
      ClothesDto data2 = new ClothesDto(clothes2.getId(), clothes2.getOwnerId(), clothes2.getName(),
        clothes2.getImageUrl(), clothes2.getType(), clothes2.getCreatedAt(),
        clothesAttributeWithDefDtoList2);

      List<ClothesDto> data = List.of(data1, data2);

      given(clothesDtoAssembler.assembleList(clothesWithAttributesDtos, values)).willReturn(data);
      boolean hasNext = clothesList.size() > limit;
      String nextCursor = null;
      UUID nexIdAfter = null;
      int totalCount = clothesList.size();
      given(clothesRepository.countByOwnerIdAndType(ownerId, ClothesType.TOP)).willReturn(totalCount);

      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(data, nextCursor, nexIdAfter,
          hasNext, totalCount, sortBy, sortDirection);

      // when
      ClothesDtoCursorResponse result = clothesService.findByCursor(user.getId(), cursor, idAfter,
        limit, typeEqual, ownerId);

      // then
      assertEquals(result, response);

      then(userRepository).should().findById(ownerId);
      then(clothesRepository).should().findByCursor(cursor, idAfter, limit, ClothesType.TOP,
          ownerId, sortBy, sortDirection);
      then(clothesRepository).should().countByOwnerIdAndType(ownerId, ClothesType.TOP);
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

      List<ClothesAttributeRowDto> clothesWithAttributesDtos = List.of(
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def1.getId(),
          def1.getName(),
          value1.getItem()),
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def2.getId(),
          def2.getName(),
          value3.getItem()),
        new ClothesAttributeRowDto(clothes2.getId(), clothes2.getCreatedAt(),
          clothes2.getOwnerId(),
          clothes2.getName(), clothes2.getImageUrl(), clothes2.getType(), def1.getId(),
          def1.getName(),
          value2.getItem()),
        new ClothesAttributeRowDto(clothes2.getId(), clothes2.getCreatedAt(),
          clothes2.getOwnerId(),
          clothes2.getName(), clothes2.getImageUrl(), clothes2.getType(), def2.getId(),
          def2.getName(),
          value4.getItem())
      );
      given(clothesRepository.findByCursor(cursor, idAfter, limit, ClothesType.TOP, ownerId, sortBy,
        sortDirection))
        .willReturn(clothesWithAttributesDtos);

      List<SelectableValue> values = List.of(value1, value2, value3, value4);
      given(selectableValueService.findAll()).willReturn(values);

      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList1 = List.of(
        new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), List.of(value1.getItem(),
          value2.getItem()), value1.getItem()),
        new ClothesAttributeWithDefDto(def2.getId(), def2.getName(), List.of(value3.getItem(),
          value4.getItem()), value3.getItem()
        ));
      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtoList2 = List.of(
        new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), List.of(value1.getItem(),
          value2.getItem()), value2.getItem()),
        new ClothesAttributeWithDefDto(def2.getId(), def2.getName(), List.of(value3.getItem(),
          value4.getItem()), value4.getItem()
        ));
      ClothesDto data1 = new ClothesDto(clothes1.getId(), clothes1.getOwnerId(), clothes1.getName(),
        clothes1.getImageUrl(), clothes1.getType(), clothes1.getCreatedAt(),
        clothesAttributeWithDefDtoList1);
      ClothesDto data2 = new ClothesDto(clothes2.getId(), clothes2.getOwnerId(), clothes2.getName(),
        clothes2.getImageUrl(), clothes2.getType(), clothes2.getCreatedAt(),
        clothesAttributeWithDefDtoList2);

      List<ClothesDto> data = List.of(data1, data2);

      given(clothesDtoAssembler.assembleList(clothesWithAttributesDtos, values)).willReturn(data);

      boolean hasNext = clothesList.size() > limit;
      String nextCursor = clothes1.getCreatedAt().toString();
      UUID nextIdAfter = clothes1.getId();
      int totalCount = 2;
      given(clothesRepository.countByOwnerIdAndType(ownerId, ClothesType.TOP)).willReturn(totalCount);

      data = data.subList(0, limit);
      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(data, nextCursor,
        nextIdAfter,
          hasNext, totalCount, sortBy, sortDirection);

      // when
      ClothesDtoCursorResponse result = clothesService.findByCursor(user.getId(), cursor, idAfter,
        limit, typeEqual, ownerId);

      // then
      assertEquals(response, result);

      then(userRepository).should().findById(ownerId);
      then(clothesRepository).should()
        .findByCursor(cursor, idAfter, limit, ClothesType.TOP, ownerId, sortBy, sortDirection);
      then(selectableValueService).should().findAll();
      then(clothesDtoAssembler).should().assembleList(clothesWithAttributesDtos, values);
      then(clothesRepository).should().countByOwnerIdAndType(ownerId, ClothesType.TOP);
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

      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

      String sortBy = "createdAt";
      SortDirection sortDirection = SortDirection.DESCENDING;
      List<Clothes> clothesList = List.of();
      given(clothesRepository.findByCursor(cursor, idAfter, limit, typeEqual, ownerId,
        sortBy, sortDirection)).willReturn(List.of());

      boolean hasNext = clothesList.size() > limit;
      String nextCursor = null;
      UUID nexIdAfter = null;
      int totalCount = clothesList.size();
      given(clothesRepository.countByOwnerIdAndType(ownerId, typeEqual)).willReturn(totalCount);

      List<ClothesDto> data = List.of();

      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(data, nextCursor, nexIdAfter,
          hasNext, totalCount, sortBy, sortDirection);

      // when
      ClothesDtoCursorResponse result = clothesService.findByCursor(user.getId(), cursor, idAfter,
        limit, typeEqual,
          ownerId);

      // then
      assertEquals(result, response);

      then(userRepository).should().findById(ownerId);
      then(clothesRepository).should().findByCursor(cursor, idAfter, limit, typeEqual,
          ownerId, sortBy, sortDirection);
      then(clothesRepository).should().countByOwnerIdAndType(ownerId, typeEqual);
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
      assertThrows(BadRequestException.class,
        () -> clothesService.findByCursor(user.getId(), cursor, idAfter,
          limit, typeEqual, ownerId));

      then(userRepository).should(times(0)).findById(ownerId);
    }

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
      assertThrows(UserNotFoundException.class,
        () -> clothesService.findByCursor(user.getId(), cursor, idAfter,
          limit, typeEqual, ownerId));

      then(clothesRepository).should(times(0))
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
          List.of(new ClothesAttributeDto(def1.getId(), "S")));
      String newUrl = "newUrl";

      given(clothesRepository.findById(clothes1.getId())).willReturn(Optional.of(clothes1));
      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);

      List<SelectableValue> values = List.of(value1, value2);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId()))).willReturn(values);

      List<UUID> selectedValueIds = List.of(value1.getId());
      List<ClothesAttribute> clothesAttributes = List.of(clothesAttribute1);
      given(clothesAttributeService.create(clothes1.getId(), selectedValueIds)).willReturn(clothesAttributes);

      List<ClothesAttributeRowDto> clothesWithAttributesDtos = List.of(
        new ClothesAttributeRowDto(clothes1.getId(), clothes1.getCreatedAt(),
          clothes1.getOwnerId(),
          clothes1.getName(), clothes1.getImageUrl(), clothes1.getType(), def1.getId(),
          def1.getName(), "S")
      );
      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);
      given(clothesRepository.findByClothesId(clothes1.getId())).willReturn(
        clothesWithAttributesDtos);
      given(selectableValueService.findAll()).willReturn(selectableValues);

      List<String> items = values.stream().map(SelectableValue::getItem).toList();
      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), items, "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);
      ClothesDto dto = new ClothesDto(clothes1.getId(), user.getId(), request.name(), newUrl,
        request.type(), clothes1.getCreatedAt(), attributes);
      given(clothesDtoAssembler.assemble(clothesWithAttributesDtos, selectableValues)).willReturn(
        dto);

      // when
      ClothesDto result = clothesService.update(user.getId(), clothes1.getId(), request, image);

      // then
      assertEquals(dto, result);
      assertEquals(attributes, result.attributes());

      then(clothesRepository).should().findById(clothes1.getId());
      then(userRepository).should().findById(user.getId());
      then(fileStorage).should().remove(imageUrl);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(selectableValueService).should().findAllByAttributeDefIdIn(List.of(def1.getId()));
    }

    @Test
    @DisplayName("의상 수정 성공 - 이미지 X")
    void update_success_no_image() {

      // given
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of(new ClothesAttributeDto(def1.getId(), "S")));
      String newUrl = "newUrl";

      given(clothesRepository.findById(clothesWithoutImage.getId())).willReturn(Optional.of(clothesWithoutImage));
      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);

      List<SelectableValue> values = List.of(value1, value2);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId()))).willReturn(values);

      List<UUID> selectedValueIds = List.of(value1.getId());
      List<ClothesAttribute> clothesAttributes = List.of(clothesAttribute1);
      given(clothesAttributeService.create(clothesWithoutImage.getId(), selectedValueIds)).willReturn(clothesAttributes);

      List<ClothesAttributeRowDto> clothesWithAttributesDtos = List.of(
        new ClothesAttributeRowDto(clothesWithoutImage.getId(),
          clothesWithoutImage.getCreatedAt(),
          clothesWithoutImage.getOwnerId(), clothesWithoutImage.getName(),
          clothesWithoutImage.getImageUrl(),
          clothesWithoutImage.getType(), def1.getId(), def1.getName(), "S")
      );
      List<SelectableValue> selectableValues = List.of(value1, value2, value3, value4);
      given(clothesRepository.findByClothesId(clothesWithoutImage.getId())).willReturn(
        clothesWithAttributesDtos);
      given(selectableValueService.findAll()).willReturn(selectableValues);

      List<String> items = values.stream().map(SelectableValue::getItem).toList();
      ClothesAttributeWithDefDto defDto = new ClothesAttributeWithDefDto(def1.getId(), def1.getName(), items, "M");
      List<ClothesAttributeWithDefDto> attributes = List.of(defDto);
      ClothesDto dto = new ClothesDto(clothesWithoutImage.getId(), user.getId(), request.name(),
        newUrl,
        request.type(), clothesWithoutImage.getCreatedAt(), attributes);
      given(clothesDtoAssembler.assemble(clothesWithAttributesDtos, selectableValues)).willReturn(
        dto);

      // when
      ClothesDto result = clothesService.update(user.getId(), clothesWithoutImage.getId(), request,
        image);

      // then
      assertEquals(dto, result);
      assertEquals(attributes, result.attributes());

      then(clothesRepository).should().findById(clothesWithoutImage.getId());
      then(userRepository).should().findById(user.getId());
      then(fileStorage).should(times(0)).remove(imageUrl);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(selectableValueService).should().findAllByAttributeDefIdIn(List.of(def1.getId()));
    }

    @Test
    @DisplayName("의상 수정 성공 - 속성 빈리스트")
    void update_success_not_select_values() {

      // given
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      String newUrl = "newUrl";

      given(clothesRepository.findById(clothes1.getId())).willReturn(Optional.of(clothes1));
      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(newUrl);

      ClothesDto dto = new ClothesDto(clothes1.getId(), user.getId(), request.name(), newUrl,
        request.type(),
        clothes1.getCreatedAt(), List.of());

      // when
      ClothesDto result = clothesService.update(user.getId(), clothes1.getId(), request, image);

      // then
      assertEquals(dto, result);
      assertEquals(dto.attributes(), result.attributes());

      then(clothesRepository).should().findById(clothes1.getId());
      then(userRepository).should().findById(user.getId());
      then(fileStorage).should().remove(imageUrl);
      then(fileStorage).should().upload(image, FileDomain.CLOTHES_IMAGE);
      then(selectableValueService).should(times(0)).findAllByAttributeDefIdIn(List.of(def1.getId()));
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
      assertThrows(ClothesNotFoundException.class,
        () -> clothesService.update(user.getId(), clothesId, request, image));

      then(userRepository).should(times(0)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("잘못된 사용자 id일 경우 사용자 조회 실패")
    void update_fail_not_found_user() {

      // given
      UUID clothesId = UUID.randomUUID();
      Clothes clothes1 = Clothes.create(user.getId(), "사이즈", ClothesType.TOP, null);
      ClothesUpdateRequest request = new ClothesUpdateRequest("하의", ClothesType.BOTTOM,
          List.of());
      MultipartFile image = mock(MultipartFile.class);

      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes1));
      given(userRepository.findById(user.getId())).willReturn(Optional.empty());

      // when, then
      assertThrows(UserNotFoundException.class,
        () -> clothesService.update(user.getId(), clothesId, request, image));

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
      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);
      given(fileStorage.upload(image, FileDomain.CLOTHES_IMAGE)).willReturn(imageUrl);
      given(selectableValueService.findAllByAttributeDefIdIn(List.of(def1.getId()))).willReturn(values);

      // when, then
      assertThrows(SelectableValueNotFoundException.class,
        () -> clothesService.update(user.getId(), clothes1.getId(), request, image));
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
      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(fileStorage.remove(clothes1.getImageUrl())).willReturn(true);

      // when
      clothesService.delete(user.getId(), requestClothesId);

      // then
      then(clothesRepository).should().findById(requestClothesId);
      then(userRepository).should().findById(user.getId());
      then(ootdRepository).should().deleteByClothesId(clothes1Id);
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
      given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

      // when
      clothesService.delete(user.getId(), requestClothesId);

      // then
      then(clothesRepository).should().findById(requestClothesId);
      then(ootdRepository).should().deleteByClothesId(requestClothesId);
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
      assertThrows(ClothesNotFoundException.class,
        () -> clothesService.delete(user.getId(), requestClothesId));

      then(clothesRepository).should().findById(requestClothesId);
      then(ootdRepository).should(times(0)).deleteByClothesId(clothes1Id);
      then(clothesAttributeService).should(times(0)).deleteAllByClothesId(requestClothesId);
      then(fileStorage).should(times(0)).remove(anyString());
      then(clothesRepository).should(times(0)).deleteById(requestClothesId);

    }
  }
}