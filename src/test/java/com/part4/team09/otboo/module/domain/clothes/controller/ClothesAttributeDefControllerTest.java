package com.part4.team09.otboo.module.domain.clothes.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefFindRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.part4.team09.otboo.module.domain.clothes.service.ClothesAttributeInfoService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@WebMvcTest(ClothesAttributeDefController.class)
class ClothesAttributeDefControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ClothesAttributeInfoService clothesAttributeInfoService;

  @Nested
  @DisplayName("의상 속성 등록")
  class Create {

    @Test
    @DisplayName("의상 속성 등록 성공")
    void create_success() throws Exception {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("색상",
          List.of("레드", "블랙"));

      ClothesAttributeDefDto response = new ClothesAttributeDefDto(UUID.randomUUID(),
          request.name(),
          request.selectableValues());

      given(clothesAttributeInfoService.create(request)).willReturn(response);

      // when, then
      mockMvc.perform(post("/api/clothes/attribute-defs")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(response.id().toString()))
          .andExpect(jsonPath("$.name").value(response.name()))
          .andExpect(jsonPath("$.selectableValues[0]").value(response.selectableValues().get(0)))
          .andExpect(jsonPath("$.selectableValues[1]").value(response.selectableValues().get(1)));
    }

    @Test
    @DisplayName("이름이 비어있을 경우 실패")
    void create_without_name() throws Exception {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("",
          List.of("레드", "블랙"));

      ClothesAttributeDefDto response = new ClothesAttributeDefDto(UUID.randomUUID(),
          request.name(),
          request.selectableValues());

      given(clothesAttributeInfoService.create(request)).willReturn(response);

      // when, then
      mockMvc.perform(post("/api/clothes/attribute-defs")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("속성이 없을 경우 실패")
    void create_without_value() throws Exception {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("색상",
          List.of());

      ClothesAttributeDefDto response = new ClothesAttributeDefDto(UUID.randomUUID(),
          request.name(),
          request.selectableValues());

      given(clothesAttributeInfoService.create(request)).willReturn(response);

      // when, then
      mockMvc.perform(post("/api/clothes/attribute-defs")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("의상 속성 조회")
  class FindByCursor {

    @Test
    @DisplayName("의상 속성 조회 성공")
    void find_by_cursor_success() throws Exception {

      // given
      int limit = 10;
      ClothesAttributeDefFindRequest request = new ClothesAttributeDefFindRequest(null, null,
          limit, "name", SortDirection.ASCENDING, null);

      ClothesAttributeDefDtoCursorResponse response = new ClothesAttributeDefDtoCursorResponse(
          List.of(), null, null, false, 0, "name",
          SortDirection.ASCENDING);

      given(clothesAttributeInfoService.findByCursor(request)).willReturn(response);

      // when, then
      mockMvc.perform(get("/api/clothes/attribute-defs")
              .param("limit", "10")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalCount").value(0))
          .andExpect(jsonPath("$.sortBy").value("name"))
          .andExpect(jsonPath("$.sortDirection").value(SortDirection.ASCENDING.name()));
    }

  }

  @Nested
  @DisplayName("의상 속성 수정")
  class Update {

    @Test
    @DisplayName("의상 속성 수정 성공")
    void update_success() throws Exception {

      // given
      UUID defId = UUID.randomUUID();
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("new name",
          List.of("new value"));

      ClothesAttributeDefDto response = new ClothesAttributeDefDto(defId, request.name(),
          request.selectableValues());

      given(clothesAttributeInfoService.update(defId, request)).willReturn(response);

      // when, then
      mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", defId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(defId.toString()))
          .andExpect(jsonPath("$.name").value("new name"));
    }
  }

  @Nested
  @DisplayName("의상 속성 삭제")
  class Delete {

    @Test
    @DisplayName("의상 속성 삭제 성공")
    void delete_success() throws Exception {

      // given
      UUID defId = UUID.randomUUID();

      // when, then
      mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", defId)
              .with(csrf()))
          .andExpect(status().isNoContent());
    }
  }
}