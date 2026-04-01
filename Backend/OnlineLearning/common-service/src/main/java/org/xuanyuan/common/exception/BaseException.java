package org.xuanyuan.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final Integer code;

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message) {
        this(500, message);
    }
}
