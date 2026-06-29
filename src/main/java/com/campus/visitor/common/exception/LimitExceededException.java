package com.campus.visitor.common.exception;

public class LimitExceededException extends BizException {
    public LimitExceededException(String message) {
        super(429, message);
    }
}
