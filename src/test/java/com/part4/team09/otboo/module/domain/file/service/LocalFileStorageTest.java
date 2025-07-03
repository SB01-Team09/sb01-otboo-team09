package com.part4.team09.otboo.module.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.FileUploadFailedException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageTest {

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private LocalFileStorage localFileStorage;

  @BeforeEach
  void setUp() {
    // 테스트용 값 주입
    ReflectionTestUtils.setField(localFileStorage, "resourcePath", "files");
    ReflectionTestUtils.setField(localFileStorage, "saveBasePath", Paths.get("fileStorage"));
  }

  @Nested
  @DisplayName("파일 업로드 메서드")
  class UploadTest {

    @Test
    @DisplayName("IOException 발생 시 FileUploadFailedException 발생한다.")
    void upload_failure_throwsException() throws Exception {
      // given
      when(multipartFile.getOriginalFilename()).thenReturn("fail.png");
      doThrow(new IOException("IO error")).when(multipartFile).transferTo(any(File.class));

      // when + then
      assertThrows(FileUploadFailedException.class,
        () -> localFileStorage.upload(multipartFile, FileDomain.PROFILE));
    }
  }

  @Nested
  @DisplayName("파일 저장 경로 추출 메서드")
  class ToStoragePathTest {

    @Test
    @DisplayName("올바른 URL에서 경로 추출 성공한다.")
    void toStoragePath_success() {
      // given
      String url = "http://localhost/files/profile/test.png";

      // when
      Path path = localFileStorage.toStoragePath(url);

      // then
      assertThat(path).isNotNull();
      assertThat(path.toString()).contains("profile");
    }

    @Test
    @DisplayName("잘못된 URL은 IllegalArgumentException")
    void toStoragePath_invalidUrl() {
      // given
      String url = "http://localhost/wrongpath/test.png";

      // when + then
      assertThrows(IllegalArgumentException.class, () -> localFileStorage.toStoragePath(url));
    }
  }
}

