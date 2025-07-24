package com.part4.team09.otboo.module.domain.file.exception;

public class S3FileUploadFailedException extends FileStorageException {

  public S3FileUploadFailedException() {
    super(FileStorageErrorCode.FILE_UPLOAD_FAIL);
  }

  public static S3FileUploadFailedException withFileName(String fileName) {
    S3FileUploadFailedException exception = new S3FileUploadFailedException();
    exception.addDetail("fileName", fileName);
    return exception;
  }
}