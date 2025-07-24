package com.part4.team09.otboo.module.domain.file.service;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

  String upload(MultipartFile file, FileDomain domain);

  boolean remove(String path);
}
