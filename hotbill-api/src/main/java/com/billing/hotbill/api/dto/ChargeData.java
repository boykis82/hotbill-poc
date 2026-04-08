package com.billing.hotbill.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 과금자료 DTO
 * 외부 과금 시스템으로부터 전달받는 과금자료 정보
 */
public class ChargeData {

    private final String contractNumber;
    private final List<ChargeItem> chargeItems;
    private final String errorCode;
    private final String errorMessage;

    /**
     * 과금자료 생성자
     *
     * @param contractNumber 계약번호 (필수)
     * @param chargeItems 과금항목 리스트
     * @param errorCode 오류코드 (오류 발생 시)
     * @param errorMessage 오류메시지 (오류 발생 시)
     */
    public ChargeData(String contractNumber, List<ChargeItem> chargeItems,
                     String errorCode, String errorMessage) {
        this.contractNumber = Objects.requireNonNull(contractNumber, "계약번호는 필수입니다");
        this.chargeItems = chargeItems != null
            ? Collections.unmodifiableList(new ArrayList<>(chargeItems))
            : Collections.emptyList();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 오류 발생 여부 확인
     *
     * @return 오류코드가 존재하면 true, 그렇지 않으면 false
     */
    public boolean hasError() {
        return errorCode != null && !errorCode.isEmpty();
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public List<ChargeItem> getChargeItems() {
        return chargeItems;
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
        ChargeData that = (ChargeData) o;
        return Objects.equals(contractNumber, that.contractNumber) &&
               Objects.equals(chargeItems, that.chargeItems) &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumber, chargeItems, errorCode, errorMessage);
    }

    @Override
    public String toString() {
        return "ChargeData{" +
               "contractNumber='" + contractNumber + '\'' +
               ", chargeItems=" + chargeItems +
               ", errorCode='" + errorCode + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}
