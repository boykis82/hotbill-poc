package com.billing.hotbill.impl.strategy.chargedata;

import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import com.billing.hotbill.impl.orchestrator.template.ProcessingType;
import org.springframework.stereotype.Component;

/**
 * ChargeDataStrategy Factory (stub)
 */
@Component
public class ChargeDataStrategyFactory {

    private final SyncChargeDataStrategy syncStrategy;
    private final AsyncChargeDataStrategy asyncStrategy;

    public ChargeDataStrategyFactory(
            SyncChargeDataStrategy syncStrategy,
            AsyncChargeDataStrategy asyncStrategy) {
        this.syncStrategy = syncStrategy;
        this.asyncStrategy = asyncStrategy;
    }

    /**
     * 동기 전략 반환
     */
    public ChargeDataStrategy getSyncStrategy() {
        return syncStrategy;
    }

    /**
     * 비동기 전략 반환
     */
    public ChargeDataStrategy getAsyncStrategy() {
        return asyncStrategy;
    }

    /**
     * 계약유형과 usecase 조합으로 처리 타입 결정
     */
    public ProcessingType determineProcessingType(
            ContractType contractType,
            HotbillUsecase usecase) {
        // TODO: Phase 2에서 구현되었어야 하는 로직
        // 간단한 stub 구현
        boolean requiresExternalChargeData = switch (contractType) {
            case MOBILE_PHONE -> true;
            case IPTV -> true;
            case INTERNET -> false;
        };

        return requiresExternalChargeData ? ProcessingType.ASYNC : ProcessingType.SYNC;
    }
}
