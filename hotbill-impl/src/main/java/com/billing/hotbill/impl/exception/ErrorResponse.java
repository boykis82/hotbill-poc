package com.billing.hotbill.impl.exception;

import java.time.LocalDateTime;

/**
 * API 에러 응답
 *
 * <p>REST API에서 발생한 오류를 표준화된 형식으로 클라이언트에 전달합니다.</p>
 */
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;

    /**
     * ErrorResponse 생성
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지
     */
    public String getMessage() {
        return message;
    }

    /**
     * 타임스탬프 반환
     *
     * @return 타임스탬프
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
