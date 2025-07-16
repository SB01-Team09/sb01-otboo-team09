package com.part4.team09.otboo.module.domain.auth.dto;

public record EmailMessage(
  String to,
  String subject,
  String message
) {

}
