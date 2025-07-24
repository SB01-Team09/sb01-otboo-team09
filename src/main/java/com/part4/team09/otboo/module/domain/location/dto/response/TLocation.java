package com.part4.team09.otboo.module.domain.location.dto.response;

public record TLocation (
  String code, // kor
  String category, // 11100000
  String level1, // 서울특별시
  String level2, // 종로구
  String level3, // 용호동
  int gridX, // 60
  int gridY, // 127
  int lonD, // 126
  int lonM, // 58
  double lonS, // 48.03
  int latD, // 37
  int latM, // 33
  double latS, // 48.85
  double longitude, // 126.9999998877
  double latitude, // 37.5683949
  String locationUpdated // 위치업데이트
){
}

