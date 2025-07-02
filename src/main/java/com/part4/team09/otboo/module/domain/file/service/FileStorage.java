package com.part4.team09.otboo.module.domain.file.service;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

  // retry 처리: 실패 시 알림 처리
  String upload(MultipartFile file, FileDomain domain);

  boolean remove(String path);
}
