package com.billing.hotbill.api.dto;

import com.billing.hotbill.api.enums.ContractType;

import java.util.Objects;

/**
 * 계약 정보 DTO
 */
public class ContractInfo {

    private final String contractNumber;
    private final ContractType contractType;

    /**
     * 계약 정보 생성자
     *
     * @param contractNumber 계약번호 (필수)
     * @param contractType 계약 유형 (필수)
     * @throws NullPointerException 필수 파라미터가 null인 경우
     */
    public ContractInfo(String contractNumber, ContractType contractType) {
        this.contractNumber = Objects.requireNonNull(contractNumber, "계약번호는 필수입니다");
        this.contractType = Objects.requireNonNull(contractType, "계약 유형은 필수입니다");
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public ContractType getContractType() {
        return contractType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractInfo that = (ContractInfo) o;
        return Objects.equals(contractNumber, that.contractNumber) &&
               contractType == that.contractType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumber, contractType);
    }

    @Override
    public String toString() {
        return "ContractInfo{" +
               "contractNumber='" + contractNumber + '\'' +
               ", contractType=" + contractType +
               '}';
    }
}
