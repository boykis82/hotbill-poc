package com.billing.hotbill.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Hotbill 결과조회 요청 DTO
 */
public class HotbillQueryRequest {

    private final List<String> contractNumbers;

    /**
     * Hotbill 결과조회 요청 생성자
     *
     * @param contractNumbers 계약번호 리스트 (필수, 최소 1개)
     * @throws NullPointerException 계약번호 리스트가 null인 경우
     * @throws IllegalArgumentException 계약번호 리스트가 비어있는 경우
     */
    public HotbillQueryRequest(List<String> contractNumbers) {
        Objects.requireNonNull(contractNumbers, "계약번호 리스트는 필수입니다");
        if (contractNumbers.isEmpty()) {
            throw new IllegalArgumentException("계약번호 리스트는 최소 1개 이상이어야 합니다");
        }
        this.contractNumbers = Collections.unmodifiableList(new ArrayList<>(contractNumbers));
    }

    public List<String> getContractNumbers() {
        return contractNumbers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotbillQueryRequest that = (HotbillQueryRequest) o;
        return Objects.equals(contractNumbers, that.contractNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumbers);
    }

    @Override
    public String toString() {
        return "HotbillQueryRequest{" +
               "contractNumbers=" + contractNumbers +
               '}';
    }
}
