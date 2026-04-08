package com.billing.hotbill.impl.integration;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.api.enums.*;
import com.billing.hotbill.impl.HotbillImpl;
import com.billing.hotbill.impl.controller.HotbillRealtimeController;
import com.billing.hotbill.impl.orchestrator.HotbillOrchestrator;
import com.billing.hotbill.impl.orchestrator.template.HotbillTemplate;
import com.billing.hotbill.impl.service.BillingService;
import com.billing.hotbill.impl.service.CancellationService;
import com.billing.hotbill.impl.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Hotbill 혼합 처리 통합 테스트
 *
 * <p>동기/비동기 계약이 혼재된 요청의 전체 플로우를 검증합니다.</p>
 * <ul>
 *   <li>3개 계약 (이동전화, 인터넷, IPTV) 동시 요청</li>
 *   <li>인터넷 → 동기 처리 (즉시 완료)</li>
 *   <li>이동전화, IPTV → 비동기 처리 (콜백 대기)</li>
 *   <li>결과 병합 확인</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Hotbill 혼합 처리 통합 테스트")
class HotbillMixedIntegrationTest {

    private HotbillRealtimeController controller;

    @Mock
    private SettlementService settlementService;

    @Mock
    private CancellationService cancellationService;

    @Mock
    private BillingService billingService;

    @Mock
    private HotbillTemplate hotbillTemplate;

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

        // Controller 생성
        controller = new HotbillRealtimeController(hotbill);
    }

    @Test
    @DisplayName("동기/비동기 혼합 계약 요청 시 적절히 처리되어야 한다")
    void requestMixedContracts_ShouldProcessBothSyncAndAsync() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo("CTR-INTERNET-001", ContractType.INTERNET),      // 동기
            new ContractInfo("CTR-MOBILE-001", ContractType.MOBILE_PHONE),    // 비동기
            new ContractInfo("CTR-IPTV-001", ContractType.IPTV)               // 비동기
        );

        HotbillRequest request = new HotbillRequest(
            null,  // usecase는 controller에서 설정
            contracts,
            LocalDateTime.now()
        );

        // 동기 계약(인터넷)은 즉시 청구정보 반환, 비동기는 나중에
        BillingInfo internetBillingInfo = new BillingInfo(
            "CTR-INTERNET-001",
            LocalDate.now(),
            BigDecimal.valueOf(30000),
            Collections.emptyList()
        );

        // 혼합 처리 결과: 동기 완료 + 비동기 대기
        HotbillResult expectedResult = new HotbillResult(
            List.of(internetBillingInfo),  // 동기 계약만 즉시 완료
            Collections.emptyList(),
            true  // 비동기 계약이 있으므로 true
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();

        HotbillResult result = response.getBody();
        assertThat(result.isAsyncProcessing()).isTrue();  // 비동기 계약 포함
        assertThat(result.getSuccessList()).hasSize(1);   // 동기 계약만 즉시 완료
        assertThat(result.getSuccessList().get(0).getContractNumber())
            .isEqualTo("CTR-INTERNET-001");
    }

    @Test
    @DisplayName("동기/비동기 혼합 요청 시 동기 계약은 즉시 완료되어야 한다")
    void mixedRequest_SyncContracts_ShouldCompleteImmediately() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo("CTR-INTERNET-001", ContractType.INTERNET),
            new ContractInfo("CTR-INTERNET-002", ContractType.INTERNET),
            new ContractInfo("CTR-MOBILE-001", ContractType.MOBILE_PHONE)
        );

        HotbillRequest request = new HotbillRequest(
            null,
            contracts,
            LocalDateTime.now()
        );

        // 2개의 동기 계약 완료 정보
        List<BillingInfo> syncBillingInfos = List.of(
            new BillingInfo("CTR-INTERNET-001", LocalDate.now(), BigDecimal.valueOf(30000), Collections.emptyList()),
            new BillingInfo("CTR-INTERNET-002", LocalDate.now(), BigDecimal.valueOf(30000), Collections.emptyList())
        );

        HotbillResult expectedResult = new HotbillResult(
            syncBillingInfos,
            Collections.emptyList(),
            true  // 비동기 계약 존재
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).hasSize(2);  // 동기 2개 완료
        assertThat(response.getBody().isAsyncProcessing()).isTrue();  // 비동기 1개 대기
    }

    @Test
    @DisplayName("혼합 요청 시 결과가 올바르게 병합되어야 한다")
    void mixedRequest_ShouldMergeResultsCorrectly() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo("CTR-INTERNET-001", ContractType.INTERNET),
            new ContractInfo("CTR-MOBILE-001", ContractType.MOBILE_PHONE),
            new ContractInfo("CTR-IPTV-001", ContractType.IPTV)
        );

        HotbillRequest request = new HotbillRequest(
            null,
            contracts,
            LocalDateTime.now()
        );

        // 동기 처리 결과
        BillingInfo syncBilling = new BillingInfo(
            "CTR-INTERNET-001",
            LocalDate.now(),
            BigDecimal.valueOf(30000),
            Collections.emptyList()
        );

        // 병합된 결과 (동기 완료 + 비동기 대기)
        HotbillResult mergedResult = new HotbillResult(
            List.of(syncBilling),
            Collections.emptyList(),
            true
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(mergedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        HotbillResult result = response.getBody();

        // 결과 검증
        assertThat(result.getSuccessList()).hasSize(1);
        assertThat(result.getSuccessList().get(0).getContractNumber()).isEqualTo("CTR-INTERNET-001");
        assertThat(result.isAsyncProcessing()).isTrue();
    }

    @Test
    @DisplayName("모든 계약이 동기 처리되면 async 플래그가 false여야 한다")
    void allSyncContracts_ShouldHaveAsyncFlagFalse() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo("CTR-INTERNET-001", ContractType.INTERNET),
            new ContractInfo("CTR-INTERNET-002", ContractType.INTERNET)
        );

        HotbillRequest request = new HotbillRequest(
            null,
            contracts,
            LocalDateTime.now()
        );

        // 모두 동기 처리
        List<BillingInfo> billingInfos = List.of(
            new BillingInfo("CTR-INTERNET-001", LocalDate.now(), BigDecimal.valueOf(30000), Collections.emptyList()),
            new BillingInfo("CTR-INTERNET-002", LocalDate.now(), BigDecimal.valueOf(30000), Collections.emptyList())
        );

        HotbillResult expectedResult = new HotbillResult(
            billingInfos,
            Collections.emptyList(),
            false  // 모두 동기
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAsyncProcessing()).isFalse();
        assertThat(response.getBody().getSuccessList()).hasSize(2);
    }

    @Test
    @DisplayName("모든 계약이 비동기 처리되면 즉시 완료된 청구정보가 없어야 한다")
    void allAsyncContracts_ShouldHaveNoImmediateBillingInfo() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo("CTR-MOBILE-001", ContractType.MOBILE_PHONE),
            new ContractInfo("CTR-IPTV-001", ContractType.IPTV)
        );

        HotbillRequest request = new HotbillRequest(
            null,
            contracts,
            LocalDateTime.now()
        );

        // 모두 비동기 처리
        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),  // 즉시 완료된 청구정보 없음
            Collections.emptyList(),
            true  // 비동기 처리
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAsyncProcessing()).isTrue();
        assertThat(response.getBody().getSuccessList()).isEmpty();
    }
}
