package com.part4.team09.otboo.module.domain.follow.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FollowErrorCode implements ErrorCode {

    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    NEGATIVE_LIMIT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "limit은 0보다 커야합니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 팔로우값이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    FollowErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }


}
