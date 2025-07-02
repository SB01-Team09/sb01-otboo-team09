package com.part4.team09.otboo.module.common.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorage implements FileStorage {

  // retry 처리: 실패 시 알림 처리
  @Override
  public String upload(MultipartFile file) {
    return "저장 경로 리턴";
  }

  @Override
  public boolean remove(String path) {
    return false;
  }
}
