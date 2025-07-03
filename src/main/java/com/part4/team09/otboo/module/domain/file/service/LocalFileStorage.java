package com.part4.team09.otboo.module.domain.file.service;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.FileUploadFailedException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Component
@ConditionalOnProperty(name = "otboo.file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

  // url 에서 파일 접근 경로 (ex: files/)
  @Value("${otboo.resource.path}")
  private String resourcePath;

  // 기본 경로 (ex: fileStorage)
  @Value("${otboo.file.storage.path}")
  private Path saveBasePath;

  @PostConstruct
  public void init() {
    if (!Files.exists(saveBasePath)) {
      try {
        Files.createDirectories(saveBasePath);

        for (FileDomain domain : FileDomain.values()) {
          Path domainPath = saveBasePath.resolve(domain.getFolderName());
          if (!Files.exists(domainPath)) {
            Files.createDirectories(domainPath);
            log.info("도메인 폴더가 생성되었습니다.({})", domainPath.toAbsolutePath());
          } else {
            log.info("도메인 폴더가 이미 존재합니다: ({})", domainPath.toAbsolutePath());
          }
        }
      } catch (IOException e) {
        log.warn("파일 저장 폴더 생성에 실패하였습니다.({})", saveBasePath.toAbsolutePath());
        throw FileUploadFailedException.withFileName(saveBasePath.toUri().toString());
      }
    }
  }

  @Retryable(
    value = FileUploadFailedException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2),
    recover = "uploadRecover"
  )
  @Override
  public String upload(MultipartFile file, FileDomain domain) {

    // 파일명 생성
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

    String uuid = UUID.randomUUID().toString();
    String savedFileName = uuid + extension;

    // 파일 저장
    Path savePath = saveBasePath.resolve(domain.getFolderName()).resolve(savedFileName);
    log.info("파일 저장 경로 ({})", savePath);
    try {
      file.transferTo(savePath.toFile());
    } catch (IOException e) {
      log.warn("파일 저장 실패 ({})", String.valueOf(e.fillInStackTrace()));
      throw FileUploadFailedException.withFileName(originalFilename);
    }

    // 리소스 접근할 url 설정
    String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
      .pathSegment(resourcePath, domain.getFolderName(), savedFileName)
      .toUriString();

    return fileUrl;
  }

  @Override
  public boolean remove(String path) {
    Path filePath = toStoragePath(path);
    try {
      Files.deleteIfExists(filePath);
      log.info("파일 삭제에 성공하였습니다.(path: {})", filePath);
      return true;
    } catch (IOException e) {
      log.warn("파일이 존재하지 않습니다.(path: {})", filePath);
    }
    return false;
  }

  @Recover
  public String uploadRecover(FileUploadFailedException e, MultipartFile file, FileDomain domain) {
    log.warn("파일 저장 최종 실패 ({})", String.valueOf(e.fillInStackTrace()));
    throw e;
  }

  // 삭제를 위한 파일 경로 추출
  Path toStoragePath(String fileUrl) {

    String resourcePrefix = resourcePath + "/";
    int index = fileUrl.indexOf(resourcePrefix);
    if (index == -1) {
      throw new IllegalArgumentException("잘못된 파일 URL : " + resourcePrefix + "/ 경로가 포함되어 있지 않습니다.");
    }

    String relativePath = fileUrl.substring(index + resourcePrefix.length());
    log.debug("경로 추출 완료: {}", relativePath);

    return saveBasePath.resolve(relativePath);
  }
}
