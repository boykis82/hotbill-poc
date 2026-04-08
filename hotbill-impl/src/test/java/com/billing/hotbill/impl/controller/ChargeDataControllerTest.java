package com.billing.hotbill.impl.controller;

import com.billing.hotbill.api.dto.ChargeData;
import com.billing.hotbill.api.dto.ChargeItem;
import com.billing.hotbill.impl.service.ChargeDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ChargeDataController 테스트
 *
 * <p>과금자료 등록 REST API를 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChargeDataController 테스트")
class ChargeDataControllerTest {

    @Mock
    private ChargeDataService chargeDataService;

    private ChargeDataController controller;

    @BeforeEach
    void setUp() {
        controller = new ChargeDataController(chargeDataService);
    }

    @Test
    @DisplayName("POST /api/hotbill/chargedata/register - 과금자료 등록이 성공해야 한다")
    void registerChargeData_ShouldReturnSuccess() {
        // Given
        ChargeItem item1 = new ChargeItem("CHARGE001", BigDecimal.valueOf(10000));
        ChargeItem item2 = new ChargeItem("CHARGE002", BigDecimal.valueOf(5000));

        ChargeData chargeData = new ChargeData(
            "CTR001",
            List.of(item1, item2),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData);

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        ResponseEntity<Void> response = controller.registerChargeData(chargeDataList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }

    @Test
    @DisplayName("POST /api/hotbill/chargedata/register - 여러 계약의 과금자료를 처리해야 한다")
    void registerChargeData_ShouldHandleMultipleContracts() {
        // Given
        ChargeData chargeData1 = new ChargeData(
            "CTR001",
            List.of(new ChargeItem("CHARGE001", BigDecimal.valueOf(10000))),
            null,
            null
        );

        ChargeData chargeData2 = new ChargeData(
            "CTR002",
            List.of(new ChargeItem("CHARGE002", BigDecimal.valueOf(20000))),
            null,
            null
        );

        ChargeData chargeData3 = new ChargeData(
            "CTR003",
            List.of(new ChargeItem("CHARGE003", BigDecimal.valueOf(30000))),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData1, chargeData2, chargeData3);

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        ResponseEntity<Void> response = controller.registerChargeData(chargeDataList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }

    @Test
    @DisplayName("POST /api/hotbill/chargedata/register - 빈 리스트도 처리해야 한다")
    void registerChargeData_ShouldHandleEmptyList() {
        // Given
        List<ChargeData> emptyList = Collections.emptyList();

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        ResponseEntity<Void> response = controller.registerChargeData(emptyList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(emptyList);
    }

    @Test
    @DisplayName("POST /api/hotbill/chargedata/register - 오류가 포함된 과금자료도 처리해야 한다")
    void registerChargeData_ShouldHandleDataWithErrors() {
        // Given
        ChargeData normalData = new ChargeData(
            "CTR001",
            List.of(new ChargeItem("CHARGE001", BigDecimal.valueOf(10000))),
            null,
            null
        );

        ChargeData errorData = new ChargeData(
            "CTR002",
            Collections.emptyList(),
            "ERR001",
            "과금자료 조회 실패"
        );

        List<ChargeData> chargeDataList = List.of(normalData, errorData);

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        ResponseEntity<Void> response = controller.registerChargeData(chargeDataList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }

    @Test
    @DisplayName("POST /api/hotbill/chargedata/register - 여러 과금항목을 가진 데이터를 처리해야 한다")
    void registerChargeData_ShouldHandleMultipleChargeItems() {
        // Given
        ChargeData chargeData = new ChargeData(
            "CTR001",
            List.of(
                new ChargeItem("CHARGE001", BigDecimal.valueOf(10000)),
                new ChargeItem("CHARGE002", BigDecimal.valueOf(5000)),
                new ChargeItem("CHARGE003", BigDecimal.valueOf(3000)),
                new ChargeItem("CHARGE004", BigDecimal.valueOf(2000))
            ),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData);

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        ResponseEntity<Void> response = controller.registerChargeData(chargeDataList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }

    @Test
    @DisplayName("컨트롤러는 ChargeDataService에 적절히 위임해야 한다")
    void controller_ShouldDelegateToChargeDataService() {
        // Given
        ChargeData chargeData = new ChargeData(
            "CTR001",
            List.of(new ChargeItem("CHARGE001", BigDecimal.valueOf(10000))),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData);

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        controller.registerChargeData(chargeDataList);

        // Then
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }

    @Test
    @DisplayName("POST /api/hotbill/chargedata/register - 동일 계약의 과금자료가 여러번 들어와도 처리해야 한다")
    void registerChargeData_ShouldHandleDuplicateContracts() {
        // Given - 실제로는 외부 시스템에서 동일 계약번호를 중복으로 보내지 않지만, 방어적 처리
        ChargeData chargeData1 = new ChargeData(
            "CTR001",
            List.of(new ChargeItem("CHARGE001", BigDecimal.valueOf(10000))),
            null,
            null
        );

        ChargeData chargeData2 = new ChargeData(
            "CTR001",
            List.of(new ChargeItem("CHARGE002", BigDecimal.valueOf(20000))),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData1, chargeData2);

        doNothing().when(chargeDataService).registerChargeData(any());

        // When
        ResponseEntity<Void> response = controller.registerChargeData(chargeDataList);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(chargeDataService, times(1)).registerChargeData(chargeDataList);
    }
}
