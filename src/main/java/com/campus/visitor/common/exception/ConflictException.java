package com.campus.visitor.common.exception;

public class ConflictException extends BizException {
    public ConflictException(String message) {
        super(409, message);
    }
}
