package com.billing.hotbill.impl.controller;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.CancellationRequest;
import com.billing.hotbill.api.dto.CancellationResult;
import com.billing.hotbill.api.dto.SettlementRequest;
import com.billing.hotbill.api.dto.SettlementResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HotbillSettlementController 테스트
 *
 * <p>해지정산 관련 REST API를 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HotbillSettlementController 테스트")
class HotbillSettlementControllerTest {

    @Mock
    private Hotbill hotbill;

    private HotbillSettlementController controller;

    @BeforeEach
    void setUp() {
        controller = new HotbillSettlementController(hotbill);
    }

    @Test
    @DisplayName("POST /api/hotbill/settlement/request - 해지정산 요청이 성공해야 한다")
    void requestSettlement_ShouldReturnSuccess() {
        // Given
        SettlementRequest request = new SettlementRequest(List.of("CTR001", "CTR002"));

        SettlementResult expectedResult = new SettlementResult(
            List.of("CTR001", "CTR002"),
            Collections.emptyList()
        );

        when(hotbill.requestSettlement(any(SettlementRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(2);
        assertThat(response.getBody().getSuccessContractNumbers()).containsExactly("CTR001", "CTR002");
        assertThat(response.getBody().getFailureContractNumbers()).isEmpty();

        verify(hotbill, times(1)).requestSettlement(request);
    }

    @Test
    @DisplayName("POST /api/hotbill/settlement/request - 단일 계약 해지정산을 처리해야 한다")
    void requestSettlement_ShouldHandleSingleContract() {
        // Given
        SettlementRequest request = new SettlementRequest(List.of("CTR001"));

        SettlementResult expectedResult = new SettlementResult(
            List.of("CTR001"),
            Collections.emptyList()
        );

        when(hotbill.requestSettlement(any(SettlementRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(response.getBody().getSuccessContractNumbers()).contains("CTR001");

        verify(hotbill, times(1)).requestSettlement(request);
    }

    @Test
    @DisplayName("POST /api/hotbill/settlement/request - 부분 실패 결과를 반환해야 한다")
    void requestSettlement_ShouldReturnPartialFailure() {
        // Given
        SettlementRequest request = new SettlementRequest(List.of("CTR001", "CTR002", "CTR003"));

        SettlementResult expectedResult = new SettlementResult(
            List.of("CTR001", "CTR002"),
            List.of("CTR003")
        );

        when(hotbill.requestSettlement(any(SettlementRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<SettlementResult> response = controller.requestSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(2);
        assertThat(response.getBody().getFailureContractNumbers()).hasSize(1);
        assertThat(response.getBody().getFailureContractNumbers()).contains("CTR003");

        verify(hotbill, times(1)).requestSettlement(request);
    }

    @Test
    @DisplayName("POST /api/hotbill/settlement/cancel - 해지정산 당일취소가 성공해야 한다")
    void cancelSettlement_ShouldReturnSuccess() {
        // Given
        CancellationRequest request = new CancellationRequest(List.of("CTR001", "CTR002"));

        CancellationResult expectedResult = new CancellationResult(
            List.of("CTR001", "CTR002"),
            Collections.emptyList()
        );

        when(hotbill.cancelSettlement(any(CancellationRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<CancellationResult> response = controller.cancelSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(2);
        assertThat(response.getBody().getSuccessContractNumbers()).containsExactly("CTR001", "CTR002");
        assertThat(response.getBody().getFailureContractNumbers()).isEmpty();

        verify(hotbill, times(1)).cancelSettlement(request);
    }

    @Test
    @DisplayName("POST /api/hotbill/settlement/cancel - 단일 계약 취소를 처리해야 한다")
    void cancelSettlement_ShouldHandleSingleContract() {
        // Given
        CancellationRequest request = new CancellationRequest(List.of("CTR001"));

        CancellationResult expectedResult = new CancellationResult(
            List.of("CTR001"),
            Collections.emptyList()
        );

        when(hotbill.cancelSettlement(any(CancellationRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<CancellationResult> response = controller.cancelSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(1);
        assertThat(response.getBody().getSuccessContractNumbers()).contains("CTR001");

        verify(hotbill, times(1)).cancelSettlement(request);
    }

    @Test
    @DisplayName("POST /api/hotbill/settlement/cancel - 부분 실패 결과를 반환해야 한다")
    void cancelSettlement_ShouldReturnPartialFailure() {
        // Given
        CancellationRequest request = new CancellationRequest(List.of("CTR001", "CTR002", "CTR003"));

        CancellationResult expectedResult = new CancellationResult(
            List.of("CTR001", "CTR002"),
            List.of("CTR003")
        );

        when(hotbill.cancelSettlement(any(CancellationRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<CancellationResult> response = controller.cancelSettlement(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessContractNumbers()).hasSize(2);
        assertThat(response.getBody().getFailureContractNumbers()).hasSize(1);
        assertThat(response.getBody().getFailureContractNumbers()).contains("CTR003");

        verify(hotbill, times(1)).cancelSettlement(request);
    }

    @Test
    @DisplayName("컨트롤러는 Hotbill 서비스에 적절히 위임해야 한다")
    void controller_ShouldDelegateToHotbillService() {
        // Given
        SettlementRequest settlementRequest = new SettlementRequest(List.of("CTR001"));
        CancellationRequest cancellationRequest = new CancellationRequest(List.of("CTR001"));

        SettlementResult settlementResult = new SettlementResult(
            List.of("CTR001"),
            Collections.emptyList()
        );

        CancellationResult cancellationResult = new CancellationResult(
            List.of("CTR001"),
            Collections.emptyList()
        );

        when(hotbill.requestSettlement(any(SettlementRequest.class))).thenReturn(settlementResult);
        when(hotbill.cancelSettlement(any(CancellationRequest.class))).thenReturn(cancellationResult);

        // When
        controller.requestSettlement(settlementRequest);
        controller.cancelSettlement(cancellationRequest);

        // Then
        verify(hotbill, times(1)).requestSettlement(settlementRequest);
        verify(hotbill, times(1)).cancelSettlement(cancellationRequest);
    }
}
