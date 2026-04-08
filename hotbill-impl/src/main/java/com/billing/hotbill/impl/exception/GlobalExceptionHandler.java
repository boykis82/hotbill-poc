package com.billing.hotbill.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 핸들러
 *
 * <p>REST API에서 발생하는 예외를 처리하고 적절한 HTTP 응답을 반환합니다.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * HotbillException 처리
     *
     * @param ex HotbillException
     * @return 에러 응답
     */
    @ExceptionHandler(HotbillException.class)
    public ResponseEntity<ErrorResponse> handleHotbillException(HotbillException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode().getCode(),
            ex.getMessage()
        );

        HttpStatus status = getHttpStatusForErrorCode(ex.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리
     *
     * @param ex IllegalArgumentException
     * @return 에러 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "BAD_REQUEST",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 일반 예외 처리
     *
     * @param ex Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * ErrorCode에 따른 HTTP 상태 코드 반환
     *
     * @param errorCode 에러 코드
     * @return HTTP 상태 코드
     */
    private HttpStatus getHttpStatusForErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case CONTRACT_NOT_FOUND, INVALID_STATUS -> HttpStatus.BAD_REQUEST;
            case CALCULATION_NOT_COMPLETED, SETTLEMENT_NOT_COMPLETED -> HttpStatus.CONFLICT;
            case EXTERNAL_SYSTEM_ERROR -> HttpStatus.BAD_GATEWAY;
        };
    }
}
