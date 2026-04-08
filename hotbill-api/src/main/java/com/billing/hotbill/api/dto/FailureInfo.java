package com.billing.hotbill.api.dto;

import java.util.Objects;

/**
 * Hotbill 처리 실패 정보 DTO
 */
public class FailureInfo {

    private final String contractNumber;
    private final String errorCode;
    private final String errorMessage;

    /**
     * 실패 정보 생성자
     *
     * @param contractNumber 계약번호 (필수)
     * @param errorCode 오류코드 (필수)
     * @param errorMessage 오류메시지
     */
    public FailureInfo(String contractNumber, String errorCode, String errorMessage) {
        this.contractNumber = Objects.requireNonNull(contractNumber, "계약번호는 필수입니다");
        this.errorCode = Objects.requireNonNull(errorCode, "오류코드는 필수입니다");
        this.errorMessage = errorMessage;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailureInfo that = (FailureInfo) o;
        return Objects.equals(contractNumber, that.contractNumber) &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumber, errorCode, errorMessage);
    }

    @Override
    public String toString() {
        return "FailureInfo{" +
               "contractNumber='" + contractNumber + '\'' +
               ", errorCode='" + errorCode + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}
