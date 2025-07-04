package com.part4.team09.otboo.module.domain.file.exception;

public class FileUploadFailedException extends FileStorageException {

  public FileUploadFailedException() {
    super(FileStorageErrorCode.LOCAL_STORAGE_FILE_UPLOAD_FAIL);
  }

  public static FileUploadFailedException withFileName(String fileName) {
    FileUploadFailedException exception = new FileUploadFailedException();
    exception.addDetail("fileName", fileName);
    return exception;
  }
}
