package com.billing.hotbill.api.dto;

import com.billing.hotbill.api.enums.HotbillUsecase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Hotbill 요청 DTO
 */
public class HotbillRequest {

    private final HotbillUsecase usecase;
    private final List<ContractInfo> contracts;
    private final LocalDateTime requestDateTime;

    /**
     * Hotbill 요청 생성자
     *
     * @param usecase Hotbill usecase (필수)
     * @param contracts 계약 리스트 (필수)
     * @param requestDateTime 요청일시 (필수)
     */
    public HotbillRequest(HotbillUsecase usecase, List<ContractInfo> contracts,
                         LocalDateTime requestDateTime) {
        this.usecase = Objects.requireNonNull(usecase, "Usecase는 필수입니다");
        this.contracts = Objects.requireNonNull(contracts, "계약 리스트는 필수입니다");
        this.requestDateTime = Objects.requireNonNull(requestDateTime, "요청일시는 필수입니다");
    }

    /**
     * 계약번호 리스트 반환
     *
     * @return 계약번호 리스트
     */
    public List<String> getContractNumbers() {
        return contracts.stream()
            .map(ContractInfo::getContractNumber)
            .collect(Collectors.toList());
    }

    /**
     * 특정 계약들로 새로운 요청 객체 생성 (동기/비동기 그룹핑용)
     *
     * @param newContracts 새로운 계약 리스트
     * @return 새로운 HotbillRequest 객체
     */
    public HotbillRequest withContracts(List<ContractInfo> newContracts) {
        return new HotbillRequest(this.usecase, newContracts, this.requestDateTime);
    }

    public HotbillUsecase getUsecase() {
        return usecase;
    }

    public List<ContractInfo> getContracts() {
        return Collections.unmodifiableList(contracts);
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotbillRequest that = (HotbillRequest) o;
        return usecase == that.usecase &&
               Objects.equals(contracts, that.contracts) &&
               Objects.equals(requestDateTime, that.requestDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usecase, contracts, requestDateTime);
    }

    @Override
    public String toString() {
        return "HotbillRequest{" +
               "usecase=" + usecase +
               ", contracts=" + contracts +
               ", requestDateTime=" + requestDateTime +
               '}';
    }
}
