package com.part4.team09.otboo.module.domain.follow.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;

import java.util.Map;

public class NegativeLimitNotAllowed extends BaseException {
    public NegativeLimitNotAllowed(Integer limit) {
        super(FollowErrorCode.NEGATIVE_LIMIT_NOT_ALLOWED, Map.of("limit", limit));
    }
}
