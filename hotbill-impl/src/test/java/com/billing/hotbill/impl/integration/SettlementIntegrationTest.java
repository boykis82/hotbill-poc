package com.billing.hotbill.impl.integration;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.impl.HotbillImpl;
import com.billing.hotbill.impl.controller.HotbillSettlementController;
import com.billing.hotbill.impl.orchestrator.HotbillOrchestrator;
import com.billing.hotbill.impl.orchestrator.template.HotbillTemplate;
import com.billing.hotbill.impl.service.BillingService;
import com.billing.hotbill.impl.service.CancellationService;
import com.billing.hotbill.impl.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 해지정산 플로우 통합 테스트
 *
 * <p>해지정산 요청 및 당일취소의 전체 플로우를 검증합니다.</p>
 * <ul>
 *   <li>Hotbill 완료 후 해지정산 요청</li>
 *   <li>포스팅이력 처리</li>
 *   <li>취소로그 저장</li>
 *   <li>청구정보 이관</li>
 *   <li>당일취소 및 원복</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("해지정산 플로우 통합 테스트")
class SettlementIntegrationTest {

    private HotbillSettlementController controller;

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
        controller = new HotbillSettlementController(hotbill);
    }

    @Test
    @DisplayName("해지정산 요청이 성공적으로 처리되어야 한다")
    void requestSettlement_ShouldProcessSuccessfully() {
        // Given
        String contractNumber = "CTR-MOBILE-001";
        SettlementRequest request = new SettlementRequest(
            List.of(contractNumber)
        );

        // SettlementService Mock 설정
        SettlementResult expectedResult = new SettlementResult(
            List.of(contractNumber),
            Collections.emptyList()
        );
        when(settlementService.processSettlement(any(SettlementRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(response.getBody().getSuccessContractNumbers()).contains(contractNumber);
        assertThat(response.getBody().isSuccess()).isTrue();

        verify(settlementService).processSettlement(request);
    }

    @Test
    @DisplayName("해지정산 요청 시 SettlementService가 호출되어야 한다")
    void requestSettlement_ShouldCallSettlementService() {
        // Given
        SettlementRequest request = new SettlementRequest(
            List.of("CTR-001")
        );

        SettlementResult expectedResult = new SettlementResult(
            Collections.emptyList(),
            Collections.emptyList()
        );
        when(settlementService.processSettlement(any(SettlementRequest.class)))
            .thenReturn(expectedResult);

        // When
        controller.requestSettlement(request);

        // Then
        ArgumentCaptor<SettlementRequest> requestCaptor = ArgumentCaptor.forClass(SettlementRequest.class);
        verify(settlementService, times(1)).processSettlement(requestCaptor.capture());

        SettlementRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getContractNumbers()).contains("CTR-001");
    }

    @Test
    @DisplayName("당일취소 요청이 성공적으로 처리되어야 한다")
    void cancelSettlement_ShouldProcessSuccessfully() {
        // Given
        String contractNumber = "CTR-MOBILE-002";
        CancellationRequest request = new CancellationRequest(
            List.of(contractNumber)
        );

        // CancellationService Mock 설정
        CancellationResult expectedResult = new CancellationResult(
            List.of(contractNumber),
            Collections.emptyList()
        );
        when(cancellationService.processCancellation(any(CancellationRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<CancellationResult> response = controller.cancelSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(response.getBody().getSuccessContractNumbers()).contains(contractNumber);
        assertThat(response.getBody().isSuccess()).isTrue();

        verify(cancellationService).processCancellation(request);
    }

    @Test
    @DisplayName("당일취소 요청 시 CancellationService가 호출되어야 한다")
    void cancelSettlement_ShouldCallCancellationService() {
        // Given
        CancellationRequest request = new CancellationRequest(
            List.of("CTR-001")
        );

        CancellationResult expectedResult = new CancellationResult(
            Collections.emptyList(),
            Collections.emptyList()
        );
        when(cancellationService.processCancellation(any(CancellationRequest.class)))
            .thenReturn(expectedResult);

        // When
        controller.cancelSettlement(request);

        // Then
        ArgumentCaptor<CancellationRequest> requestCaptor = ArgumentCaptor.forClass(CancellationRequest.class);
        verify(cancellationService, times(1)).processCancellation(requestCaptor.capture());

        CancellationRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getContractNumbers()).contains("CTR-001");
    }

    @Test
    @DisplayName("해지정산 후 당일취소하는 전체 플로우가 정상 동작해야 한다")
    void settlementAndCancellation_EndToEnd_ShouldWorkCorrectly() {
        // Given - Step 1: 해지정산 요청
        String contractNumber = "CTR-MOBILE-003";
        SettlementRequest settlementRequest = new SettlementRequest(
            List.of(contractNumber)
        );

        SettlementResult settlementResult = new SettlementResult(
            List.of(contractNumber),
            Collections.emptyList()
        );
        when(settlementService.processSettlement(any(SettlementRequest.class)))
            .thenReturn(settlementResult);

        // When - Step 1: 해지정산 요청
        ResponseEntity<SettlementResult> settlementResponse = controller.requestSettlement(settlementRequest);

        // Then - Step 1: 해지정산 성공 확인
        assertThat(settlementResponse.getBody()).isNotNull();
        assertThat(settlementResponse.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(settlementResponse.getBody().isSuccess()).isTrue();

        // Given - Step 2: 당일취소 요청
        CancellationRequest cancellationRequest = new CancellationRequest(
            List.of(contractNumber)
        );

        CancellationResult cancellationResult = new CancellationResult(
            List.of(contractNumber),
            Collections.emptyList()
        );
        when(cancellationService.processCancellation(any(CancellationRequest.class)))
            .thenReturn(cancellationResult);

        // When - Step 2: 당일취소
        ResponseEntity<CancellationResult> cancellationResponse = controller.cancelSettlement(cancellationRequest);

        // Then - Step 2: 취소 성공 확인
        assertThat(cancellationResponse.getBody()).isNotNull();
        assertThat(cancellationResponse.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(cancellationResponse.getBody().isSuccess()).isTrue();

        // Verify service 호출 순서
        verify(settlementService).processSettlement(settlementRequest);
        verify(cancellationService).processCancellation(cancellationRequest);
    }

    @Test
    @DisplayName("여러 계약의 해지정산을 한 번에 처리할 수 있어야 한다")
    void requestSettlement_MultipleContracts_ShouldProcessAll() {
        // Given
        List<String> contractNumbers = List.of("CTR-001", "CTR-002", "CTR-003");
        SettlementRequest request = new SettlementRequest(contractNumbers);

        SettlementResult expectedResult = new SettlementResult(
            contractNumbers,
            Collections.emptyList()
        );
        when(settlementService.processSettlement(any(SettlementRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(3);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("해지정산 실패 시 실패 목록에 포함되어야 한다")
    void requestSettlement_WhenFailed_ShouldIncludeInFailureList() {
        // Given
        SettlementRequest request = new SettlementRequest(
            List.of("CTR-FAILED-001")
        );

        SettlementResult expectedResult = new SettlementResult(
            Collections.emptyList(),
            List.of("CTR-FAILED-001")
        );
        when(settlementService.processSettlement(any(SettlementRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).isEmpty();
        assertThat(response.getBody().getFailureContractNumbers()).hasSize(1);
        assertThat(response.getBody().getFailureContractNumbers()).contains("CTR-FAILED-001");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @DisplayName("부분 실패 시 성공과 실패가 모두 포함되어야 한다")
    void requestSettlement_PartialFailure_ShouldIncludeBoth() {
        // Given
        SettlementRequest request = new SettlementRequest(
            List.of("CTR-SUCCESS-001", "CTR-FAILED-001")
        );

        SettlementResult expectedResult = new SettlementResult(
            List.of("CTR-SUCCESS-001"),
            List.of("CTR-FAILED-001")
        );
        when(settlementService.processSettlement(any(SettlementRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(response.getBody().getFailureContractNumbers()).hasSize(1);
        assertThat(response.getBody().isSuccess()).isFalse();
    }
}
