package com.part4.team09.otboo.module.common.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

  String upload(MultipartFile file);

  boolean remove(String path);
}
