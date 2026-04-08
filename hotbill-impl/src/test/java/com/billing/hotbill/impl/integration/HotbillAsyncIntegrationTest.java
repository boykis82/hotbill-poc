package com.billing.hotbill.impl.integration;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.api.enums.*;
import com.billing.hotbill.impl.HotbillImpl;
import com.billing.hotbill.impl.controller.ChargeDataController;
import com.billing.hotbill.impl.controller.HotbillRealtimeController;
import com.billing.hotbill.impl.controller.HotbillTerminationController;
import com.billing.hotbill.impl.orchestrator.HotbillOrchestrator;
import com.billing.hotbill.impl.orchestrator.template.HotbillTemplate;
import com.billing.hotbill.impl.service.BillingService;
import com.billing.hotbill.impl.service.CancellationService;
import com.billing.hotbill.impl.service.ChargeDataService;
import com.billing.hotbill.impl.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Hotbill 비동기 처리 통합 테스트
 *
 * <p>이동전화 계약 해지요금 조회의 전체 플로우를 검증합니다.</p>
 * <ul>
 *   <li>해지요금 요청 → 비동기 응답 (과금자료 대기)</li>
 *   <li>과금자료 등록 콜백 → 요금계산 완료</li>
 *   <li>결과 조회 → 청구정보 반환</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Hotbill 비동기 처리 통합 테스트")
class HotbillAsyncIntegrationTest {

    private HotbillTerminationController terminationController;
    private ChargeDataController chargeDataController;
    private HotbillRealtimeController realtimeController;

    @Mock
    private SettlementService settlementService;

    @Mock
    private CancellationService cancellationService;

    @Mock
    private BillingService billingService;

    @Mock
    private HotbillTemplate hotbillTemplate;

    @Mock
    private ChargeDataService chargeDataService;

    @BeforeEach
    void setUp() {
        // HotbillOrchestrator 생성
        HotbillOrchestrator orchestrator = new HotbillOrchestrator(hotbillTemplate);

        // HotbillImpl 생성
        Hotbill hotbill = new HotbillImpl(
            orchestrator,
            billingService,
            settlementService,
            cancellationService
        );

        // Controllers 생성
        terminationController = new HotbillTerminationController(hotbill);
        chargeDataController = new ChargeDataController(chargeDataService);
        realtimeController = new HotbillRealtimeController(hotbill);
    }

    @Test
    @DisplayName("이동전화 해지요금 요청 시 비동기 응답을 반환해야 한다")
    void requestTerminationBilling_MobilePhone_ShouldReturnAsyncResponse() {
        // Given
        String contractNumber = "CTR-MOBILE-001";
        HotbillRequest request = new HotbillRequest(
            null,  // usecase는 controller에서 설정됨
            List.of(
                new ContractInfo(
                    contractNumber,
                    ContractType.MOBILE_PHONE
                )
            ),
            LocalDateTime.now()
        );

        // HotbillTemplate Mock 설정 - 비동기 처리 결과 반환
        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),  // 비동기는 즉시 청구정보 없음
            Collections.emptyList(),
            true  // 비동기 처리
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = terminationController.requestTerminationBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAsyncProcessing()).isTrue();  // 비동기 처리
        assertThat(response.getBody().getSuccessList()).isEmpty();  // 아직 청구정보 없음

        // Template이 TERMINATION_INQUIRY usecase로 호출되었는지 확인
        ArgumentCaptor<HotbillRequest> requestCaptor = ArgumentCaptor.forClass(HotbillRequest.class);
        verify(hotbillTemplate).executeHotbill(requestCaptor.capture());

        HotbillRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUsecase()).isEqualTo(HotbillUsecase.TERMINATION_INQUIRY);
    }

    @Test
    @DisplayName("과금자료 등록 콜백을 성공적으로 처리해야 한다")
    void registerChargeData_ShouldProcessSuccessfully() {
        // Given
        String contractNumber = "CTR-MOBILE-002";
        List<ChargeData> chargeDataList = List.of(
            new ChargeData(
                contractNumber,
                List.of(
                    new ChargeItem("USAGE", BigDecimal.valueOf(50000))
                ),
                null,
                null
            )
        );

        doNothing().when(chargeDataService).registerChargeData(anyList());

        // When
        ResponseEntity<Void> response = chargeDataController.registerChargeData(chargeDataList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }

    @Test
    @DisplayName("비동기 처리 후 결과 조회가 가능해야 한다")
    void queryResult_AfterAsyncProcessing_ShouldReturnBillingInfo() {
        // Given
        String contractNumber = "CTR-MOBILE-003";
        HotbillQueryRequest queryRequest = new HotbillQueryRequest(
            List.of(contractNumber)
        );

        // BillingService Mock 설정 - 완료된 청구정보 반환
        BillingInfo billingInfo = new BillingInfo(
            contractNumber,
            LocalDate.now(),
            BigDecimal.valueOf(50000),
            Collections.emptyList()
        );
        HotbillResult expectedResult = new HotbillResult(
            List.of(billingInfo),
            Collections.emptyList(),
            false
        );
        when(billingService.queryHotbillResult(any(HotbillQueryRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = realtimeController.queryRealtimeBilling(queryRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).hasSize(1);
        assertThat(response.getBody().getSuccessList().get(0).getContractNumber())
            .isEqualTo(contractNumber);

        verify(billingService).queryHotbillResult(queryRequest);
    }

    @Test
    @DisplayName("비동기 처리 전체 플로우가 정상 동작해야 한다")
    void asyncFlow_EndToEnd_ShouldWorkCorrectly() {
        // Given - Step 1: 해지요금 요청 (비동기)
        String contractNumber = "CTR-MOBILE-004";
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(
                new ContractInfo(
                    contractNumber,
                    ContractType.MOBILE_PHONE
                )
            ),
            LocalDateTime.now()
        );

        HotbillResult asyncResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            true
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(asyncResult);

        // When - Step 1: 비동기 요청
        ResponseEntity<HotbillResult> asyncResponse = terminationController.requestTerminationBilling(request);

        // Then - Step 1: 비동기 응답 확인
        assertThat(asyncResponse.getBody()).isNotNull();
        assertThat(asyncResponse.getBody().isAsyncProcessing()).isTrue();

        // Given - Step 2: 과금자료 등록 (콜백)
        List<ChargeData> chargeDataList = List.of(
            new ChargeData(
                contractNumber,
                List.of(
                    new ChargeItem("USAGE", BigDecimal.valueOf(50000))
                ),
                null,
                null
            )
        );
        doNothing().when(chargeDataService).registerChargeData(anyList());

        // When - Step 2: 과금자료 콜백
        ResponseEntity<Void> callbackResponse = chargeDataController.registerChargeData(chargeDataList);

        // Then - Step 2: 콜백 성공 확인
        assertThat(callbackResponse.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService).registerChargeData(chargeDataList);

        // Given - Step 3: 결과 조회
        HotbillQueryRequest queryRequest = new HotbillQueryRequest(
            List.of(contractNumber)
        );

        BillingInfo billingInfo = new BillingInfo(
            contractNumber,
            LocalDate.now(),
            BigDecimal.valueOf(50000),
            Collections.emptyList()
        );
        HotbillResult queryResult = new HotbillResult(
            List.of(billingInfo),
            Collections.emptyList(),
            false
        );
        when(billingService.queryHotbillResult(any(HotbillQueryRequest.class)))
            .thenReturn(queryResult);

        // When - Step 3: 결과 조회
        ResponseEntity<HotbillResult> queryResponse = realtimeController.queryRealtimeBilling(queryRequest);

        // Then - Step 3: 완료된 청구정보 확인
        assertThat(queryResponse.getBody()).isNotNull();
        assertThat(queryResponse.getBody().getSuccessList()).hasSize(1);
        assertThat(queryResponse.getBody().getSuccessList().get(0).getTotalAmount())
            .isEqualTo(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("IPTV 계약도 비동기 처리되어야 한다")
    void requestBilling_IPTV_ShouldProcessAsynchronously() {
        // Given
        String contractNumber = "CTR-IPTV-001";
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(
                new ContractInfo(
                    contractNumber,
                    ContractType.IPTV
                )
            ),
            LocalDateTime.now()
        );

        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            true  // 비동기 처리
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = realtimeController.requestRealtimeBilling(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAsyncProcessing()).isTrue();
    }
}
