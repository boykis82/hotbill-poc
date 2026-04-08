package com.billing.hotbill.impl.strategy.chargedata;

import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import org.springframework.stereotype.Component;

/**
 * 비동기 과금자료 처리 전략 (stub)
 */
@Component
public class AsyncChargeDataStrategy implements ChargeDataStrategy {

    @Override
    public HotbillResult process(HotbillRequest request) {
        // TODO: Phase 2에서 구현
        return HotbillResult.asyncProcessing(request.getContractNumbers());
    }
}
