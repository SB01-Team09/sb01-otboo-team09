package com.part4.team09.otboo.module.domain.follow.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;

import java.util.Map;
import java.util.UUID;

public class FollowNotFoundException extends BaseException {
    public FollowNotFoundException(UUID followId) {
        super(FollowErrorCode.FOLLOW_NOT_FOUND, Map.of("followId", followId));
    }
}
