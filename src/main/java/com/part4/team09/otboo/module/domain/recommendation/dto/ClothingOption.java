package com.part4.team09.otboo.module.domain.recommendation.dto;

import java.util.List;

public record ClothingOption(
  String attribute,
  List<String> values
) {

}

