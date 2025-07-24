package com.part4.team09.otboo.module.domain.clothes.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesService;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@WebMvcTest(ClothesController.class)
class ClothesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ClothesService clothesService;

  private CustomUserDetails userDetails;

  @BeforeEach
  void setUp() {

    userDetails = CustomUserDetails.create(
        new AuthUserDto(UUID.randomUUID(), "test@gmail.com", "test", false, Role.USER));
  }

  @Nested
  @DisplayName("의상 생성")
  class Create {

    @Test
    @DisplayName("의상 생성 성공")
    void create_success() throws Exception {

      // given
      MockMultipartFile imagePart = new MockMultipartFile(
          "image",
          "test-image.jpg",
          MediaType.IMAGE_JPEG_VALUE,
          "test".getBytes()
      );

      ClothesCreateRequest request = new ClothesCreateRequest(userDetails.getId(), "clothes1",
          ClothesType.TOP, List.of());
      MockMultipartFile requestPart = new MockMultipartFile(
          "request",
          null,
          MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsString(request).getBytes());

      ClothesDto response = new ClothesDto(UUID.randomUUID(), request.ownerId(), request.name(),
          "url", request.type(), LocalDateTime.now(), List.of());

      given(clothesService.create(request.ownerId(), request, imagePart)).willReturn(response);

      // when, then
      mockMvc.perform(multipart("/api/clothes")
              .file(requestPart)
              .file(imagePart)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .accept(MediaType.APPLICATION_JSON)
              .with(user(userDetails))
              .with(csrf()))
          .andExpect(status().isCreated());

    }
  }

  @Nested
  @DisplayName("의상 조회")
  class FindByCursor {

    @Test
    @DisplayName("의상 조회 성공")
    void find_by_cursor() throws Exception {

      // given
      int limit = 10;
      UUID ownerId = userDetails.getId();

      ClothesDtoCursorResponse response = new ClothesDtoCursorResponse(List.of(), null, null, false,
          0, "createdAt", SortDirection.ASCENDING);

      given(clothesService.findByCursor(userDetails.getId(), null, null, limit, ClothesType.TOP,
          ownerId)).willReturn(response);

      // when, then
      mockMvc.perform(get("/api/clothes")
              .param("limit", "10")
              .param("typeEqual", ClothesType.TOP.name())
              .param("ownerId", ownerId.toString())
              .accept(MediaType.APPLICATION_JSON)
              .with(user(userDetails)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalCount").value(0))
          .andExpect(jsonPath("$.sortBy").value(response.sortBy()))
          .andExpect(jsonPath("$.sortDirection").value(SortDirection.ASCENDING.name()));
    }
  }

  @Nested
  @DisplayName("의상 수정")
  class Update {

    @Test
    @DisplayName("의상 수정 성공")
    void update_success() throws Exception {

      // given
      UUID clothesId = UUID.randomUUID();

      MockMultipartFile imagePart = new MockMultipartFile("image", "test-image.jpg",
          MediaType.IMAGE_JPEG_VALUE, "test".getBytes());

      ClothesUpdateRequest request = new ClothesUpdateRequest("new", ClothesType.BOTTOM, List.of());
      MockMultipartFile requestPart = new MockMultipartFile(
          "request",
          null,
          MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsString(request).getBytes());

      ClothesDto response = new ClothesDto(clothesId, userDetails.getId(), request.name(),
          "new url",
          request.type(), LocalDateTime.now(), List.of());

      given(clothesService.update(userDetails.getId(), clothesId, request, imagePart)).willReturn(
          response);
      // when, then
      mockMvc.perform(multipart("/api/clothes/{clothesId}", clothesId)
              .file(requestPart)
              .file(imagePart)
              .with(r -> {
                r.setMethod("PATCH"); // 👈 여기 필수
                return r;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .accept(MediaType.APPLICATION_JSON)
              .with(user(userDetails))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(clothesId.toString()))
          .andExpect(jsonPath("$.ownerId").value(userDetails.getId().toString()))
          .andExpect(jsonPath("$.name").value("new"));
    }
  }

  @Nested
  @DisplayName("의상 삭제")
  class Delete {

    @Test
    @DisplayName("의상 삭제 성공")
    void delete_success() throws Exception {

      // given
      UUID clothesId = UUID.randomUUID();

      // when, then
      mockMvc.perform(delete("/api/clothes/{clothesId}", clothesId)
              .with(user(userDetails))
              .with(csrf()))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("구매 링크로 의상 정보 불러오기")
  class Extraction {

    @Test
    @DisplayName("정보 불러오기 성공")
    void extraction_success() throws Exception {

      // given
      String url = "url";

      ClothesDto response = new ClothesDto(null, userDetails.getId(), "name", "imageUrl",
          ClothesType.TOP, null, List.of());

      given(clothesService.extraction(userDetails.getId(), url)).willReturn(response);

      // when, then
      mockMvc.perform(get("/api/clothes/extractions")
              .param("url", url)
              .accept(MediaType.APPLICATION_JSON)
              .with(user(userDetails))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.ownerId").value(userDetails.getId().toString()));
    }
  }
}