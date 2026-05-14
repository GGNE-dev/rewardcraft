package org.ggne.rc.global.exception;

import lombok.Getter;

// 모든 업무 예외의 최상위 부모. ErrorCode를 통해 상태 코드와 메시지를 가진다.
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
