package com.part4.team09.otboo.module.domain.auth.exception;

public class AccountLockedException extends AuthException {

  public AccountLockedException() {
    super(AuthErrorCode.ACCOUNT_LOCKED);
  }

  public static AccountLockedException noDetail() {
    return new AccountLockedException();
  }
}
