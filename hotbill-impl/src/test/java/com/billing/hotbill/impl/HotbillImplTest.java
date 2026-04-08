package com.billing.hotbill.impl;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import com.billing.hotbill.impl.orchestrator.HotbillOrchestrator;
import com.billing.hotbill.impl.service.BillingService;
import com.billing.hotbill.impl.service.CancellationService;
import com.billing.hotbill.impl.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HotbillImpl 테스트
 *
 * <p>Hotbill 인터페이스 구현체가 각 서비스로 적절히 위임하는지 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HotbillImpl 테스트")
class HotbillImplTest {

    @Mock
    private HotbillOrchestrator orchestrator;

    @Mock
    private BillingService billingService;

    @Mock
    private SettlementService settlementService;

    @Mock
    private CancellationService cancellationService;

    private Hotbill hotbill;

    @BeforeEach
    void setUp() {
        hotbill = new HotbillImpl(orchestrator, billingService, settlementService, cancellationService);
    }

    @Test
    @DisplayName("requestHotbill()은 HotbillOrchestrator.executeHotbill()을 호출해야 한다")
    void requestHotbill_ShouldDelegateToOrchestrator() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.INTERNET);
        HotbillRequest request = new HotbillRequest(
            HotbillUsecase.SIMPLE_INQUIRY,
            List.of(contract),
            LocalDateTime.now()
        );
        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );
        when(orchestrator.executeHotbill(any(HotbillRequest.class))).thenReturn(expectedResult);

        // When
        HotbillResult result = hotbill.requestHotbill(request);

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(orchestrator, times(1)).executeHotbill(request);
    }

    @Test
    @DisplayName("queryHotbillResult()는 BillingService.queryHotbillResult()를 호출해야 한다")
    void queryHotbillResult_ShouldDelegateToBillingService() {
        // Given
        HotbillQueryRequest request = new HotbillQueryRequest(List.of("CTR001"));
        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );
        when(billingService.queryHotbillResult(any(HotbillQueryRequest.class))).thenReturn(expectedResult);

        // When
        HotbillResult result = hotbill.queryHotbillResult(request);

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(billingService, times(1)).queryHotbillResult(request);
    }

    @Test
    @DisplayName("requestSettlement()은 SettlementService.processSettlement()를 호출해야 한다")
    void requestSettlement_ShouldDelegateToSettlementService() {
        // Given
        SettlementRequest request = new SettlementRequest(List.of("CTR001"));
        SettlementResult expectedResult = new SettlementResult(
            List.of("CTR001"),
            Collections.emptyList()
        );
        when(settlementService.processSettlement(any(SettlementRequest.class))).thenReturn(expectedResult);

        // When
        SettlementResult result = hotbill.requestSettlement(request);

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(settlementService, times(1)).processSettlement(request);
    }

    @Test
    @DisplayName("cancelSettlement()은 CancellationService.processCancellation()을 호출해야 한다")
    void cancelSettlement_ShouldDelegateToCancellationService() {
        // Given
        CancellationRequest request = new CancellationRequest(List.of("CTR001"));
        CancellationResult expectedResult = new CancellationResult(
            List.of("CTR001"),
            Collections.emptyList()
        );
        when(cancellationService.processCancellation(any(CancellationRequest.class))).thenReturn(expectedResult);

        // When
        CancellationResult result = hotbill.cancelSettlement(request);

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(cancellationService, times(1)).processCancellation(request);
    }

    @Test
    @DisplayName("HotbillImpl은 Hotbill 인터페이스를 구현해야 한다")
    void hotbillImpl_ShouldImplementHotbillInterface() {
        // Then
        assertThat(hotbill).isInstanceOf(Hotbill.class);
    }
}
