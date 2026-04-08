package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.dto.HotbillRequest;

/**
 * 해지 요금 조회 Usecase Validator
 *
 * <p>해지 요금 조회에 필요한 검증을 수행합니다.</p>
 */
public class TerminationInquiryValidator implements UsecaseValidator {

    @Override
    public void validate(HotbillRequest request) {
        validateContractsNotEmpty(request);
        validateContractTerminatable(request);
        // 추가적인 검증 로직은 필요시 확장
    }

    /**
     * 계약 정보가 비어있지 않은지 검증합니다.
     */
    private void validateContractsNotEmpty(HotbillRequest request) {
        if (request.getContracts() == null || request.getContracts().isEmpty()) {
            throw new IllegalArgumentException("계약 정보가 비어있습니다");
        }
    }

    /**
     * 계약이 해지 가능한지 검증합니다.
     * 현재는 stub으로 처리하며, 실제 구현 시 계약 상태를 확인해야 합니다.
     */
    private void validateContractTerminatable(HotbillRequest request) {
        // TODO: 실제 구현 시 계약 상태 확인 로직 추가
        // 예: 계약이 활성 상태인지, 이미 해지되지 않았는지 등
    }
}
