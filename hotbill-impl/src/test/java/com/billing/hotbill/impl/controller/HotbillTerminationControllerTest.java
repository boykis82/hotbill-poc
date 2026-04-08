package com.billing.hotbill.impl.controller;

import com.billing.hotbill.api.Hotbill;
import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * HotbillTerminationController 테스트
 *
 * <p>해지요금 관련 REST API를 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HotbillTerminationController 테스트")
class HotbillTerminationControllerTest {

    @Mock
    private Hotbill hotbill;

    private HotbillTerminationController controller;

    @BeforeEach
    void setUp() {
        controller = new HotbillTerminationController(hotbill);
    }

    @Test
    @DisplayName("POST /api/hotbill/termination/request - 해지요금 요청이 성공해야 한다")
    void requestTerminationBilling_ShouldReturnSuccess() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.MOBILE_PHONE);
        HotbillRequest request = new HotbillRequest(
            null, // usecase는 컨트롤러에서 설정
            List.of(contract),
            LocalDateTime.now()
        );

        BillingInfo billingInfo = new BillingInfo(
            "CTR001",
            LocalDate.now(),
            BigDecimal.valueOf(50000),
            Collections.emptyList()
        );

        HotbillResult expectedResult = new HotbillResult(
            List.of(billingInfo),
            Collections.emptyList(),
            false
        );

        when(hotbill.requestHotbill(any(HotbillRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestTerminationBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).hasSize(1);
        assertThat(response.getBody().getSuccessList().get(0).getContractNumber()).isEqualTo("CTR001");
        assertThat(response.getBody().getSuccessList().get(0).getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(response.getBody().isAsyncProcessing()).isFalse();

        verify(hotbill, times(1)).requestHotbill(any(HotbillRequest.class));
    }

    @Test
    @DisplayName("POST /api/hotbill/termination/request - usecase가 TERMINATION_INQUIRY로 설정되어야 한다")
    void requestTerminationBilling_ShouldSetUsecaseToTerminationInquiry() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.INTERNET);
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(contract),
            LocalDateTime.now()
        );

        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        when(hotbill.requestHotbill(any(HotbillRequest.class))).thenReturn(expectedResult);

        // When
        controller.requestTerminationBilling(request);

        // Then - usecase가 TERMINATION_INQUIRY로 설정되었는지 검증
        verify(hotbill).requestHotbill(argThat(req ->
            req.getUsecase() == HotbillUsecase.TERMINATION_INQUIRY
        ));
    }

    @Test
    @DisplayName("POST /api/hotbill/termination/request - 비동기 처리 결과를 반환해야 한다")
    void requestTerminationBilling_ShouldReturnAsyncResult() {
        // Given
        ContractInfo contract = new ContractInfo("CTR002", ContractType.MOBILE_PHONE);
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(contract),
            LocalDateTime.now()
        );

        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            true // 비동기 처리
        );

        when(hotbill.requestHotbill(any(HotbillRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.requestTerminationBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAsyncProcessing()).isTrue();
    }

    @Test
    @DisplayName("POST /api/hotbill/termination/query - 해지요금 결과조회가 성공해야 한다")
    void queryTerminationBilling_ShouldReturnBillingResult() {
        // Given
        HotbillQueryRequest request = new HotbillQueryRequest(List.of("CTR001", "CTR002"));

        BillingInfo billing1 = new BillingInfo(
            "CTR001",
            LocalDate.now(),
            BigDecimal.valueOf(30000),
            Collections.emptyList()
        );

        BillingInfo billing2 = new BillingInfo(
            "CTR002",
            LocalDate.now(),
            BigDecimal.valueOf(45000),
            Collections.emptyList()
        );

        HotbillResult expectedResult = new HotbillResult(
            List.of(billing1, billing2),
            Collections.emptyList(),
            false
        );

        when(hotbill.queryHotbillResult(any(HotbillQueryRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.queryTerminationBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).hasSize(2);
        assertThat(response.getBody().getSuccessList().get(0).getContractNumber()).isEqualTo("CTR001");
        assertThat(response.getBody().getSuccessList().get(0).getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(response.getBody().getSuccessList().get(1).getContractNumber()).isEqualTo("CTR002");
        assertThat(response.getBody().getSuccessList().get(1).getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(45000));

        verify(hotbill, times(1)).queryHotbillResult(any(HotbillQueryRequest.class));
    }

    @Test
    @DisplayName("POST /api/hotbill/termination/query - 빈 리스트 요청도 처리해야 한다")
    void queryTerminationBilling_ShouldHandleEmptyList() {
        // Given
        HotbillQueryRequest request = new HotbillQueryRequest(Collections.emptyList());

        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        when(hotbill.queryHotbillResult(any(HotbillQueryRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.queryTerminationBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).isEmpty();
    }

    @Test
    @DisplayName("컨트롤러는 Hotbill 서비스에 적절히 위임해야 한다")
    void controller_ShouldDelegateToHotbillService() {
        // Given
        ContractInfo contract = new ContractInfo("CTR003", ContractType.IPTV);
        HotbillRequest request = new HotbillRequest(
            null,
            List.of(contract),
            LocalDateTime.now()
        );

        HotbillResult expectedResult = new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        when(hotbill.requestHotbill(any(HotbillRequest.class))).thenReturn(expectedResult);

        // When
        controller.requestTerminationBilling(request);

        // Then
        verify(hotbill, times(1)).requestHotbill(any(HotbillRequest.class));
    }

    @Test
    @DisplayName("여러 계약에 대한 해지요금 조회를 처리해야 한다")
    void queryTerminationBilling_ShouldHandleMultipleContracts() {
        // Given
        HotbillQueryRequest request = new HotbillQueryRequest(
            List.of("CTR001", "CTR002", "CTR003")
        );

        HotbillResult expectedResult = new HotbillResult(
            List.of(
                new BillingInfo("CTR001", LocalDate.now(), BigDecimal.valueOf(10000), Collections.emptyList()),
                new BillingInfo("CTR002", LocalDate.now(), BigDecimal.valueOf(20000), Collections.emptyList()),
                new BillingInfo("CTR003", LocalDate.now(), BigDecimal.valueOf(30000), Collections.emptyList())
            ),
            Collections.emptyList(),
            false
        );

        when(hotbill.queryHotbillResult(any(HotbillQueryRequest.class))).thenReturn(expectedResult);

        // When
        ResponseEntity<HotbillResult> response = controller.queryTerminationBilling(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessList()).hasSize(3);

        verify(hotbill, times(1)).queryHotbillResult(request);
    }
}
