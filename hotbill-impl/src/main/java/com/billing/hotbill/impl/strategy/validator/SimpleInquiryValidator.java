package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.dto.HotbillRequest;

/**
 * 단순 요금 조회 Usecase Validator
 *
 * <p>단순 요금 조회에 필요한 기본적인 검증을 수행합니다.</p>
 */
public class SimpleInquiryValidator implements UsecaseValidator {

    @Override
    public void validate(HotbillRequest request) {
        validateContractsNotEmpty(request);
        // 추가적인 검증 로직은 필요시 확장
    }

    /**
     * 계약 정보가 비어있지 않은지 검증합니다.
     *
     * @param request Hotbill 요청
     * @throws IllegalArgumentException 계약 정보가 null이거나 비어있는 경우
     */
    private void validateContractsNotEmpty(HotbillRequest request) {
        if (request.getContracts() == null || request.getContracts().isEmpty()) {
            throw new IllegalArgumentException("계약 정보가 비어있습니다");
        }
    }
}
