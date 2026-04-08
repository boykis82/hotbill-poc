package com.billing.hotbill.impl.controller;

import com.billing.hotbill.api.dto.ChargeData;
import com.billing.hotbill.impl.service.ChargeDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 과금자료 등록 REST API 컨트롤러
 *
 * <p>외부 시스템에서 호출하는 과금자료 등록 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/hotbill/chargedata")
public class ChargeDataController {

    private final ChargeDataService chargeDataService;

    public ChargeDataController(ChargeDataService chargeDataService) {
        this.chargeDataService = chargeDataService;
    }

    /**
     * 과금자료 등록 (외부 시스템 콜백)
     *
     * @param chargeDataList 과금자료 리스트
     * @return 처리 결과
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerChargeData(
            @RequestBody List<ChargeData> chargeDataList) {
        chargeDataService.registerChargeData(chargeDataList);
        return ResponseEntity.ok().build();
    }
}
