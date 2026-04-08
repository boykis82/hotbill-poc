package com.billing.hotbill.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 해지정산 결과 DTO
 */
public class SettlementResult {

    private final List<String> successContractNumbers;
    private final List<String> failureContractNumbers;

    /**
     * 해지정산 결과 생성자
     *
     * @param successContractNumbers 성공한 계약번호 리스트
     * @param failureContractNumbers 실패한 계약번호 리스트
     */
    public SettlementResult(List<String> successContractNumbers, List<String> failureContractNumbers) {
        this.successContractNumbers = successContractNumbers != null
            ? Collections.unmodifiableList(new ArrayList<>(successContractNumbers))
            : Collections.emptyList();
        this.failureContractNumbers = failureContractNumbers != null
            ? Collections.unmodifiableList(new ArrayList<>(failureContractNumbers))
            : Collections.emptyList();
    }

    /**
     * 성공 결과 생성
     *
     * @param contractNumbers 성공한 계약번호 리스트
     * @return 성공 결과 객체
     */
    public static SettlementResult success(List<String> contractNumbers) {
        return new SettlementResult(contractNumbers, Collections.emptyList());
    }

    /**
     * 모든 계약이 성공했는지 확인
     *
     * @return 실패한 계약이 없으면 true
     */
    public boolean isSuccess() {
        return failureContractNumbers.isEmpty();
    }

    public List<String> getSuccessContractNumbers() {
        return successContractNumbers;
    }

    public List<String> getFailureContractNumbers() {
        return failureContractNumbers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettlementResult that = (SettlementResult) o;
        return Objects.equals(successContractNumbers, that.successContractNumbers) &&
               Objects.equals(failureContractNumbers, that.failureContractNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successContractNumbers, failureContractNumbers);
    }

    @Override
    public String toString() {
        return "SettlementResult{" +
               "successContractNumbers=" + successContractNumbers +
               ", failureContractNumbers=" + failureContractNumbers +
               '}';
    }
}
