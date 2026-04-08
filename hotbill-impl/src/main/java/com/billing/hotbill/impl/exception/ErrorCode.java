package com.billing.hotbill.impl.exception;

/**
 * Hotbill 에러 코드
 *
 * <p>비즈니스 오류에 대한 표준화된 에러 코드를 정의합니다.</p>
 */
public enum ErrorCode {

    /**
     * 계약을 찾을 수 없음
     */
    CONTRACT_NOT_FOUND("CONTRACT_NOT_FOUND", "계약을 찾을 수 없습니다"),

    /**
     * 유효하지 않은 상태
     */
    INVALID_STATUS("INVALID_STATUS", "유효하지 않은 상태입니다"),

    /**
     * 요금계산이 완료되지 않음
     */
    CALCULATION_NOT_COMPLETED("CALCULATION_NOT_COMPLETED", "요금계산이 완료되지 않았습니다"),

    /**
     * 해지정산이 완료되지 않음
     */
    SETTLEMENT_NOT_COMPLETED("SETTLEMENT_NOT_COMPLETED", "해지정산이 완료되지 않았습니다"),

    /**
     * 외부 시스템 연동 오류
     */
    EXTERNAL_SYSTEM_ERROR("EXTERNAL_SYSTEM_ERROR", "외부 시스템 연동 오류");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드
     */
    public String getCode() {
        return code;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지
     */
    public String getMessage() {
        return message;
    }
}
