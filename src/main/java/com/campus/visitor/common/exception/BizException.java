package com.campus.visitor.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private Integer code;

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
