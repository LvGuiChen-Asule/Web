package com.campus.visitor.common.exception;

public class BlacklistException extends BizException {
    public BlacklistException(String message) {
        super(403, message);
    }
}
