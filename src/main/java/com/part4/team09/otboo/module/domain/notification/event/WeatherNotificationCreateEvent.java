package com.part4.team09.otboo.module.domain.notification.event;

public record WeatherNotificationCreateEvent(
  String locationId,
  String title,
  String content
) {

}
