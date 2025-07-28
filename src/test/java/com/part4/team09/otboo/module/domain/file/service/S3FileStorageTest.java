package com.part4.team09.otboo.module.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.file.FileDomain;
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
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

  @Mock
  private S3Client s3Client;

  @InjectMocks
  private S3FileStorage s3FileStorage;

  String bucket = "bucket";
  String region = "region";
  String imagePrefix = "images";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(s3FileStorage, "bucket", bucket);
    ReflectionTestUtils.setField(s3FileStorage, "region", region);
    ReflectionTestUtils.setField(s3FileStorage, "imagePrefix", imagePrefix);
  }

  @Test
  @DisplayName("s3 파일 업로드를 성공하면 url을 반환한다.")
  void upload_success() throws Exception {
    // given
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test.png");
    when(file.getBytes()).thenReturn("test".getBytes());

    // when
    String result = s3FileStorage.upload(file, FileDomain.PROFILE);

    // then
    String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/%s/", bucket, region,
      imagePrefix);
    assertThat(result).startsWith(baseUrl);
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Nested
  @DisplayName("파일 삭제")
  class RemoveFile {

    @Test
    @DisplayName("s3 파일 삭제를 성공하면 true를 반환한다.")
    void remove_shouldCallDeleteObject_andReturnTrue() {
      // given
      String key = "images/profile/abc.png";
      String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

      // when
      boolean result = s3FileStorage.remove(url);

      // then
      assertThat(result).isTrue();
      verify(s3Client).deleteObject(argThat((DeleteObjectRequest req) -> req.key().equals(key)));
    }

    @Test
    @DisplayName("유효하지 않은 url로 key가 null이면 true를 반환한다")
    void remove_returnsTrue_IfNullKey() {
      // given
      String url = "invalid-url";

      // when
      boolean result = s3FileStorage.remove(url);

      // then
      assertThat(result).isTrue();
      verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("접근 불가 등의 문제로 S3Exception 발생 시 false 반환")
    void remove_s3Exception_returnsFalse() {
      // given
      String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s/%s", bucket, region,
        imagePrefix, "test.png");

      doThrow(S3Exception.builder()
        .awsErrorDetails(AwsErrorDetails.builder()
          .errorCode("403")
          .errorMessage("Access Denied")
          .build())
        .build()
      ).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

      // when
      boolean result = s3FileStorage.remove(imageUrl);

      // then
      assertThat(result).isFalse();
    }
  }
}