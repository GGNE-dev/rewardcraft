package org.ggne.rc.global.exception;

import lombok.Getter;

// 모든 비즈니스 예외의 최상위 부모. HTTP 상태 코드를 함께 가진다.
@Getter
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }
}
