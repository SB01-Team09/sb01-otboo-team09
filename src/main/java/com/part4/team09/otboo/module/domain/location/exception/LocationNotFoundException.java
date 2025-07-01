package com.part4.team09.otboo.module.domain.location.exception;

public class LocationNotFoundException extends LocationException {

  public LocationNotFoundException() {
    super((LocationErrorCode.LOCATION_NOF_FOUND));
  }

  public static LocationNotFoundException withNameAndId(String name, Object id) {
    LocationNotFoundException exception = new LocationNotFoundException();
    exception.addDetail("name", name);
    exception.addDetail("id", id);
    return exception;
  }
}
