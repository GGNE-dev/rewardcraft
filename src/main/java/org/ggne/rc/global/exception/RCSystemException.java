package org.ggne.rc.global.exception;

// RewardCraft 내부 시스템/인프라 오류를 래핑한다.
// cause를 반드시 전달해야 로그의 Caused by 절에 원래 예외가 표시된다.
public class RCSystemException extends RuntimeException {

    public RCSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
