package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.dto.HotbillRequest;

/**
 * 직권해지 Usecase Validator
 *
 * <p>직권해지를 위한 요금 조회에 필요한 검증을 수행합니다.</p>
 */
public class ForcedTerminationValidator implements UsecaseValidator {

    @Override
    public void validate(HotbillRequest request) {
        validateContractsNotEmpty(request);
        validateForcedTerminationEligibility(request);
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
     * 직권해지 자격이 있는지 검증합니다.
     * 현재는 stub으로 처리하며, 실제 구현 시 직권해지 가능 여부를 확인해야 합니다.
     */
    private void validateForcedTerminationEligibility(HotbillRequest request) {
        // TODO: 실제 구현 시 직권해지 가능 여부 확인 로직 추가
        // 예: 미납 기간, 요금 체납액 등 확인
    }
}
