package com.part4.team09.otboo.module.domain.recommendation.dto.request;

import java.util.List;

public record ContentRequest(
  List<Content> contents
) {

  public record Content(
    List<Part> parts
  ) {

  }

  public record Part(
    String text
  ) {

  }
}


