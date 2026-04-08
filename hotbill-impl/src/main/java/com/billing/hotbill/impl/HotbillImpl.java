package com.billing.hotbill.impl;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.impl.orchestrator.HotbillOrchestrator;
import com.billing.hotbill.impl.service.BillingService;
import com.billing.hotbill.impl.service.CancellationService;
import com.billing.hotbill.impl.service.SettlementService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Hotbill 서비스 구현체
 *
 * <p>Hotbill 인터페이스의 구현체로, 각 서비스 계층으로 작업을 위임합니다.</p>
 *
 * <ul>
 *   <li>requestHotbill() → HotbillOrchestrator</li>
 *   <li>queryHotbillResult() → BillingService</li>
 *   <li>requestSettlement() → SettlementService</li>
 *   <li>cancelSettlement() → CancellationService</li>
 * </ul>
 */
@Service
public class HotbillImpl implements Hotbill {

    private final HotbillOrchestrator orchestrator;
    private final BillingService billingService;
    private final SettlementService settlementService;
    private final CancellationService cancellationService;

    /**
     * HotbillImpl 생성자
     *
     * @param orchestrator Hotbill 오케스트레이터 (필수)
     * @param billingService 청구 서비스 (필수)
     * @param settlementService 해지정산 서비스 (필수)
     * @param cancellationService 해지정산 취소 서비스 (필수)
     */
    public HotbillImpl(HotbillOrchestrator orchestrator,
                      BillingService billingService,
                      SettlementService settlementService,
                      CancellationService cancellationService) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "Orchestrator는 필수입니다");
        this.billingService = Objects.requireNonNull(billingService, "BillingService는 필수입니다");
        this.settlementService = Objects.requireNonNull(settlementService, "SettlementService는 필수입니다");
        this.cancellationService = Objects.requireNonNull(cancellationService, "CancellationService는 필수입니다");
    }

    @Override
    public HotbillResult requestHotbill(HotbillRequest request) {
        return orchestrator.executeHotbill(request);
    }

    @Override
    public HotbillResult queryHotbillResult(HotbillQueryRequest request) {
        return billingService.queryHotbillResult(request);
    }

    @Override
    public SettlementResult requestSettlement(SettlementRequest request) {
        return settlementService.processSettlement(request);
    }

    @Override
    public CancellationResult cancelSettlement(CancellationRequest request) {
        return cancellationService.processCancellation(request);
    }
}
