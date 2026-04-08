package com.billing.hotbill.impl.orchestrator;

import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.impl.orchestrator.template.HotbillTemplate;
import org.springframework.stereotype.Component;

/**
 * Hotbill Orchestrator
 * HotbillTemplate에 처리를 위임하는 오케스트레이터 계층
 */
@Component
public class HotbillOrchestrator {

    private final HotbillTemplate hotbillTemplate;

    public HotbillOrchestrator(HotbillTemplate hotbillTemplate) {
        this.hotbillTemplate = hotbillTemplate;
    }

    /**
     * Hotbill 요청 처리
     *
     * @param request Hotbill 요청
     * @return Hotbill 결과
     */
    public HotbillResult executeHotbill(HotbillRequest request) {
        return hotbillTemplate.executeHotbill(request);
    }
}
