package com.billing.hotbill.impl.strategy.chargedata;

import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;

/**
 * 과금자료 처리 전략 인터페이스
 */
public interface ChargeDataStrategy {

    /**
     * 과금자료 처리
     *
     * @param request Hotbill 요청
     * @return Hotbill 결과
     */
    HotbillResult process(HotbillRequest request);
}
