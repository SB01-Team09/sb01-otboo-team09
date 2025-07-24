package com.part4.team09.otboo.module.domain.follow.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;

import java.util.Map;
import java.util.UUID;

public class SelfFollowNotAllowedException extends BaseException {
    public SelfFollowNotAllowedException(UUID followeeId) {
        super(FollowErrorCode.SELF_FOLLOW_NOT_ALLOWED, Map.of("followeeId", followeeId));
    }
}
