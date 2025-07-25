package com.part4.team09.otboo.module.domain.file.service;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.S3FileUploadFailedException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@ConditionalOnProperty(name = "otboo.file.storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

  @Value("${otboo.file.storage.s3.region}")
  private String region;

  @Value("${otboo.file.storage.s3.bucket}")
  private String bucket;

  @Value("${otboo.file.storage.s3.image-prefix}")
  private String imagePrefix;

  private final S3Client s3Client;

  @Override
  public String upload(MultipartFile file, FileDomain domain) {

    String savedFileName = generateSavedFileName(file, domain);
    log.debug("파일명 생성: {}", savedFileName);

    String savedUrl = getUrl(savedFileName);
    log.debug("파일 저장된 url: {}", savedUrl);

    put(savedFileName, convertToBytes(file));
    log.debug("업로드 성공");

    return savedUrl;
  }

  @Override
  public boolean remove(String url) {

    String key = extractKeyFromUrl(url);

    if (key == null) {
      return true;
    }

    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build();

    try {
      s3Client.deleteObject(deleteObjectRequest);
      log.info("파일 삭제 성공: {}", key);
      return true;

    } catch (S3Exception e) {
      log.error("S3 예외 발생 | 삭제 실패 - key: {}, 코드: {}, 메시지: {}", key, e.awsErrorDetails().errorCode(),
        e.awsErrorDetails().errorMessage());

    } catch (SdkClientException e) {
      log.error("AWS SDK 예외 발생 | 삭제 요청 실패 - key: {}", key, e);

    } catch (Exception e) {
      log.error("파일 삭제 중 예기치 않은 오류 발생 - key : {}", key, e);
    }
    return false;
  }

  private void put(String key, byte[] bytes) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build();

    try {
      log.debug("업로드 시도 시작");
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

    } catch (S3Exception e) {
      log.error("S3 예외 발생 - 코드: {}, 메시지: {}", e.awsErrorDetails().errorCode(),
        e.awsErrorDetails().errorMessage());
      throw new S3FileUploadFailedException();

    } catch (SdkClientException e) {
      log.error("AWS SDK 예외 발생", e);
      throw new S3FileUploadFailedException();

    } catch (Exception e) {
      log.error("파일 업로드 중 예기치 않은 오류 발생", e);
      throw new S3FileUploadFailedException();
    }
  }


  /**
   * 이하 내부 유틸 메소드
   */

  private byte[] convertToBytes(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException e) {
      log.error("파일을 바이트 배열로 변환하는 중 오류 발생", e);
      throw new S3FileUploadFailedException();
    }
  }

  private String getBaseUrl() {
    return String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
  }

  // 저장된 이미지 url
  private String getUrl(String key) {
    return getBaseUrl() + key;
  }

  // 키 추출
  private String extractKeyFromUrl(String url) {
    String baseUrl = getBaseUrl();
    if (!url.startsWith(baseUrl)) {
      log.info("잘못된 S3 URL 형식이거나 외부 파일 url로 삭제 불가 : {}", url);
      return null;
    }
    return url.substring(baseUrl.length());
  }

  // 파일명(key) 생성
  private String generateSavedFileName(MultipartFile file, FileDomain domain) {
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    String uuid = UUID.randomUUID().toString();
    return String.join("/", imagePrefix, domain.getFolderName(), uuid + extension);
  }

}
