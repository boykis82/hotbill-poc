package com.billing.hotbill.impl.controller;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.CancellationRequest;
import com.billing.hotbill.api.dto.CancellationResult;
import com.billing.hotbill.api.dto.SettlementRequest;
import com.billing.hotbill.api.dto.SettlementResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 해지정산 관련 REST API 컨트롤러
 *
 * <p>해지정산 요청 및 당일취소 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/hotbill/settlement")
public class HotbillSettlementController {

    private final Hotbill hotbill;

    public HotbillSettlementController(Hotbill hotbill) {
        this.hotbill = hotbill;
    }

    /**
     * 해지정산 요청
     *
     * @param request 해지정산 요청
     * @return 해지정산 결과
     */
    @PostMapping("/request")
    public ResponseEntity<SettlementResult> requestSettlement(
            @RequestBody SettlementRequest request) {
        SettlementResult result = hotbill.requestSettlement(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 해지정산 당일취소
     *
     * @param request 취소 요청
     * @return 취소 결과
     */
    @PostMapping("/cancel")
    public ResponseEntity<CancellationResult> cancelSettlement(
            @RequestBody CancellationRequest request) {
        CancellationResult result = hotbill.cancelSettlement(request);
        return ResponseEntity.ok(result);
    }
}
