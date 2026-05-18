package org.ggne.challenge.global.exception;

/**
 * 업무 로직 오류(BusinessException)와 달리, 직렬화 실패·외부 시스템 장애 등
 * 시스템/인프라 수준의 예기치 않은 오류를 표현하는 예외.
 */
public class SystemException extends RuntimeException {

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
