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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Hotbill 동기 처리 통합 테스트
 *
 * <p>인터넷 계약 실시간요금 조회의 전체 플로우를 검증합니다.</p>
 * <ul>
 *   <li>Controller → HotbillImpl → Orchestrator → Template 플로우</li>
 *   <li>동기 처리 (INTERNET 상품유형, SIMPLE_INQUIRY)</li>
 *   <li>즉시 응답 및 상태 저장 검증</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Hotbill 동기 처리 통합 테스트")
class HotbillSyncIntegrationTest {

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
    @DisplayName("인터넷 계약 실시간요금 요청 시 동기로 즉시 응답해야 한다")
    void requestRealtimeBilling_InternetContract_ShouldReturnImmediately() {
        // Given
        String contractNumber = "CTR-INTERNET-001";
        HotbillRequest request = new HotbillRequest(
            null,  // usecase는 controller에서 설정
            List.of(
                new ContractInfo(
                    contractNumber,
                    ContractType.INTERNET
                )
            ),
            LocalDateTime.now()
        );

        // HotbillTemplate Mock 설정 - 동기 처리 결과 반환
        BillingInfo billingInfo = new BillingInfo(
            contractNumber,
            LocalDate.now(),
            BigDecimal.valueOf(30000),
            Collections.emptyList()
        );
        HotbillResult expectedResult = new HotbillResult(
            List.of(billingInfo),
            Collections.emptyList(),
            false  // 동기 처리
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();

        HotbillResult result = response.getBody();
        assertThat(result.isAsyncProcessing()).isFalse();  // 동기 처리
        assertThat(result.getSuccessList()).hasSize(1);
        assertThat(result.getSuccessList().get(0).getContractNumber()).isEqualTo(contractNumber);

        // Template이 SIMPLE_INQUIRY usecase로 호출되었는지 확인
        ArgumentCaptor<HotbillRequest> requestCaptor = ArgumentCaptor.forClass(HotbillRequest.class);
        verify(hotbillTemplate).executeHotbill(requestCaptor.capture());

        HotbillRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUsecase()).isEqualTo(HotbillUsecase.SIMPLE_INQUIRY);
    }

    @Test
    @DisplayName("실시간요금 요청 시 Controller가 Orchestrator를 통해 Template을 호출해야 한다")
    void requestRealtimeBilling_ShouldCallTemplateViaOrchestrator() {
        // Given
        String contractNumber = "CTR-INTERNET-002";
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(
                new ContractInfo(
                    contractNumber,
                    ContractType.INTERNET
                )
            ),
            LocalDateTime.now()
        );

        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        controller.requestRealtimeBilling(request);

        // Then
        verify(hotbillTemplate, times(1)).executeHotbill(any(HotbillRequest.class));
    }

    @Test
    @DisplayName("실시간요금 요청 시 응답이 성공 목록과 실패 목록을 포함해야 한다")
    void requestRealtimeBilling_ShouldContainSuccessAndFailureLists() {
        // Given
        String contractNumber = "CTR-INTERNET-003";
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(
                new ContractInfo(
                    contractNumber,
                    ContractType.INTERNET
                )
            ),
            LocalDateTime.now()
        );

        BillingInfo billingInfo = new BillingInfo(
            contractNumber,
            LocalDate.now(),
            BigDecimal.valueOf(30000),
            Collections.emptyList()
        );
        HotbillResult expectedResult = new HotbillResult(
            List.of(billingInfo),
            Collections.emptyList(),
            false
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).isNotNull();
        assertThat(response.getBody().getFailureList()).isNotNull();
    }

    @Test
    @DisplayName("여러 인터넷 계약을 동시에 요청할 수 있어야 한다")
    void requestRealtimeBilling_MultipleContracts_ShouldProcessAll() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo(
                "CTR-INTERNET-101",
                ContractType.INTERNET
            ),
            new ContractInfo(
                "CTR-INTERNET-102",
                ContractType.INTERNET
            )
        );

        HotbillRequest request = new HotbillRequest(
            null,
            contracts,
            LocalDateTime.now()
        );

        List<BillingInfo> billingInfoList = List.of(
            new BillingInfo(
                "CTR-INTERNET-101",
                LocalDate.now(),
                BigDecimal.valueOf(30000),
                Collections.emptyList()
            ),
            new BillingInfo(
                "CTR-INTERNET-102",
                LocalDate.now(),
                BigDecimal.valueOf(30000),
                Collections.emptyList()
            )
        );

        HotbillResult expectedResult = new HotbillResult(
            billingInfoList,
            Collections.emptyList(),
            false
        );
        when(hotbillTemplate.executeHotbill(any(HotbillRequest.class)))
            .thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestRealtimeBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAsyncProcessing()).isFalse();
        assertThat(response.getBody().getSuccessList()).hasSize(2);
    }
}
