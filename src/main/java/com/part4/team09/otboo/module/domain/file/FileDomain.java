package com.part4.team09.otboo.module.domain.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileDomain {

  PROFILE("profile"),
  CLOTHES_IMAGE("clothes_image");

  private final String folderName;

}
