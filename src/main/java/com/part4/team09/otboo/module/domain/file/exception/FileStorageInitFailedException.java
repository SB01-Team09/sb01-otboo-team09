package com.part4.team09.otboo.module.domain.file.exception;

public class FileStorageInitFailedException extends FileStorageException {

  public FileStorageInitFailedException() {
    super(FileStorageErrorCode.LOCAL_STORAGE_FILE_UPLOAD_FAIL);
  }

  public static FileStorageInitFailedException withFilePath(String filePath) {
    FileStorageInitFailedException exception = new FileStorageInitFailedException();
    exception.addDetail("filePath", filePath);
    return exception;
  }
}
