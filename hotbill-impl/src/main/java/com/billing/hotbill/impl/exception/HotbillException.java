package com.billing.hotbill.impl.exception;

/**
 * Hotbill 비즈니스 예외
 *
 * <p>Hotbill 처리 중 발생하는 비즈니스 예외를 표현합니다.</p>
 */
public class HotbillException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode와 메시지로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public HotbillException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode만으로 예외 생성 (기본 메시지 사용)
     *
     * @param errorCode 에러 코드
     */
    public HotbillException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode, 메시지, 원인 예외로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public HotbillException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
