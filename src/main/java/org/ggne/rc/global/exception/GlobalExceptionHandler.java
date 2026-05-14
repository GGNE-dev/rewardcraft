package org.ggne.rc.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.util.stream.Collectors.joining;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 업무 에러 — ErrorCode의 status + code + message 포함 응답
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        log.warn("BusinessException: [{}] {}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    // RewardCraft 시스템/인프라 에러 — Caused by 포함 전체 스택 기록, 500 응답
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystem(SystemException e) {
        log.error("SystemException: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("서버 오류가 발생했습니다."));
    }

    // @Valid 검증 실패 — 어떤 필드가 왜 실패했는지 메시지로 조합
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    // @PreAuthorize 권한 거부 — ErrorCode로 일관된 응답
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AuthorizationDeniedException e) {
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getStatus())
                .body(ApiResponse.error(ErrorCode.ACCESS_DENIED));
    }

    // 예상치 못한 에러 — 스택 트레이스를 로그에 남기고 500 응답
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("서버 오류가 발생했습니다."));
    }
}
