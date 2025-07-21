package com.part4.team09.otboo.module.domain.auth.event;

public record TemporaryPasswordIssuedEvent(
  String email,
  String temporaryPassword
) {

}
