package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.dto.HotbillRequest;

/**
 * Hotbill Usecase별 검증 전략 인터페이스
 *
 * <p>각 Hotbill usecase별로 특화된 검증 로직을 구현하기 위한 인터페이스입니다.
 * Strategy Pattern을 적용하여 usecase별 검증 로직을 독립적으로 관리합니다.</p>
 */
public interface UsecaseValidator {

    /**
     * Hotbill 요청에 대한 검증을 수행합니다.
     *
     * @param request Hotbill 요청 정보
     * @throws IllegalArgumentException 검증 실패 시
     */
    void validate(HotbillRequest request);
}
