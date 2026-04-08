package com.billing.hotbill.impl.controller;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.HotbillQueryRequest;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.api.enums.HotbillUsecase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 실시간요금 관련 REST API 컨트롤러
 *
 * <p>실시간요금 요청 및 결과조회 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/hotbill/realtime")
public class HotbillRealtimeController {

    private final Hotbill hotbill;

    public HotbillRealtimeController(Hotbill hotbill) {
        this.hotbill = hotbill;
    }

    /**
     * 실시간요금 요청
     *
     * @param request Hotbill 요청
     * @return 요금계산 결과
     */
    @PostMapping("/request")
    public ResponseEntity<HotbillResult> requestRealtimeBilling(
            @RequestBody HotbillRequest request) {
        // usecase를 SIMPLE_INQUIRY로 설정한 새 객체 생성
        HotbillRequest requestWithUsecase = new HotbillRequest(
            HotbillUsecase.SIMPLE_INQUIRY,
            request.getContracts(),
            request.getRequestDateTime()
        );
        HotbillResult result = hotbill.requestHotbill(requestWithUsecase);
        return ResponseEntity.ok(result);
    }

    /**
     * 실시간요금 결과조회
     *
     * @param request 조회 요청
     * @return 요금계산 결과
     */
    @PostMapping("/query")
    public ResponseEntity<HotbillResult> queryRealtimeBilling(
            @RequestBody HotbillQueryRequest request) {
        HotbillResult result = hotbill.queryHotbillResult(request);
        return ResponseEntity.ok(result);
    }
}
