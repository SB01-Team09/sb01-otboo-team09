package com.part4.team09.otboo.module.domain.directmessage.mapper;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DirectMessageMapper {

  @Mapping(source = "directMessage.id", target = "id")
  @Mapping(source = "directMessage.createdAt", target = "createdAt")
  @Mapping(source = "directMessage.content", target = "content")
  DirectMessageDto toDto(DirectMessage directMessage, UserSummary sender, UserSummary receiver);
}
