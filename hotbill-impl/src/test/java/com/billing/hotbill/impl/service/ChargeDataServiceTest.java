package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.*;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.client.BillingCalculationClient;
import com.billing.hotbill.impl.mapper.ChargeDataMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChargeDataService 테스트
 */
@ExtendWith(MockitoExtension.class)
class ChargeDataServiceTest {

    @Mock
    private ChargeDataMapper chargeDataMapper;

    @Mock
    private StatusService statusService;

    @Mock
    private BillingCalculationClient billingClient;

    @InjectMocks
    private ChargeDataService chargeDataService;

    @Test
    @DisplayName("과금자료 등록 - 정상 케이스")
    void registerChargeData_success() {
        // given
        ChargeItem chargeItem1 = new ChargeItem("BASIC_FEE", BigDecimal.valueOf(10000));

        ChargeData chargeData1 = new ChargeData(
            "CTR001",
            List.of(chargeItem1),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData1);

        BillingInfo billingInfo = new BillingInfo(
            "CTR001",
            LocalDate.now(),
            BigDecimal.valueOf(10000),
            List.of(chargeItem1)
        );

        HotbillResult calculationResult = new HotbillResult(
            List.of(billingInfo),
            List.of(),
            false
        );

        when(billingClient.calculate(anyList())).thenReturn(calculationResult);

        // when
        chargeDataService.registerChargeData(chargeDataList);

        // then
        verify(chargeDataMapper, times(1)).deleteByContractNumber(eq("CTR001"), any(LocalDate.class));
        verify(chargeDataMapper, times(1)).insert(any());
        verify(statusService, times(1)).updateStatus("CTR001", HotbillStatus.BEFORE_CALCULATION);
        verify(billingClient, times(1)).calculate(chargeDataList);
        verify(statusService, times(1)).updateStatus("CTR001", HotbillStatus.COMPLETED);
    }

    @Test
    @DisplayName("과금자료 등록 - 기존 데이터 삭제 후 저장")
    void registerChargeData_deleteBeforeInsert() {
        // given
        ChargeData chargeData = new ChargeData(
            "CTR002",
            List.of(),
            null,
            null
        );

        HotbillResult calculationResult = new HotbillResult(
            List.of(),
            List.of(),
            false
        );

        when(billingClient.calculate(anyList())).thenReturn(calculationResult);

        // when
        chargeDataService.registerChargeData(List.of(chargeData));

        // then
        // 삭제가 먼저 호출되어야 함
        verify(chargeDataMapper, times(1)).deleteByContractNumber(eq("CTR002"), any(LocalDate.class));
        verify(chargeDataMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("과금자료 등록 - 오류 데이터 처리")
    void registerChargeData_withError() {
        // given
        ChargeData errorData = new ChargeData(
            "CTR003",
            List.of(),
            "ERR001",
            "과금자료 조회 실패"
        );

        List<ChargeData> chargeDataList = List.of(errorData);

        // when
        chargeDataService.registerChargeData(chargeDataList);

        // then
        verify(chargeDataMapper, times(1)).deleteByContractNumber(eq("CTR003"), any(LocalDate.class));
        verify(chargeDataMapper, times(1)).insert(any());
        verify(statusService, times(1)).updateStatus("CTR003", HotbillStatus.CHARGE_DATA_ERROR);
        // 오류 데이터는 요금계산을 수행하지 않음
        verify(billingClient, never()).calculate(anyList());
    }

    @Test
    @DisplayName("과금자료 등록 - 정상/오류 데이터 혼합")
    void registerChargeData_mixed() {
        // given
        ChargeData normalData = new ChargeData(
            "CTR004",
            List.of(),
            null,
            null
        );

        ChargeData errorData = new ChargeData(
            "CTR005",
            List.of(),
            "ERR001",
            "오류"
        );

        List<ChargeData> chargeDataList = List.of(normalData, errorData);

        BillingInfo billingInfo = new BillingInfo(
            "CTR004",
            LocalDate.now(),
            BigDecimal.ZERO,
            List.of()
        );

        HotbillResult calculationResult = new HotbillResult(
            List.of(billingInfo),
            List.of(),
            false
        );

        when(billingClient.calculate(anyList())).thenReturn(calculationResult);

        // when
        chargeDataService.registerChargeData(chargeDataList);

        // then
        // 오류 데이터는 CHARGE_DATA_ERROR 상태로
        verify(statusService, times(1)).updateStatus("CTR005", HotbillStatus.CHARGE_DATA_ERROR);
        // 정상 데이터는 BEFORE_CALCULATION → COMPLETED
        verify(statusService, times(1)).updateStatus("CTR004", HotbillStatus.BEFORE_CALCULATION);
        // 정상 데이터만 요금계산 수행
        verify(billingClient, times(1)).calculate(argThat((List<ChargeData> list) ->
            list.size() == 1 && list.get(0).getContractNumber().equals("CTR004")
        ));
        verify(statusService, times(1)).updateStatus("CTR004", HotbillStatus.COMPLETED);
    }

    @Test
    @DisplayName("과금자료 등록 - 요금계산 실패 케이스")
    void registerChargeData_calculationFailure() {
        // given
        ChargeData chargeData = new ChargeData(
            "CTR006",
            List.of(),
            null,
            null
        );

        List<ChargeData> chargeDataList = List.of(chargeData);

        FailureInfo failureInfo = new FailureInfo(
            "CTR006",
            "CALC_ERROR",
            "계산 오류"
        );

        HotbillResult calculationResult = new HotbillResult(
            List.of(),
            List.of(failureInfo),
            false
        );

        when(billingClient.calculate(anyList())).thenReturn(calculationResult);

        // when
        chargeDataService.registerChargeData(chargeDataList);

        // then
        verify(statusService, times(1)).updateStatus("CTR006", HotbillStatus.BEFORE_CALCULATION);
        verify(billingClient, times(1)).calculate(chargeDataList);
        verify(statusService, times(1)).updateStatus("CTR006", HotbillStatus.FAILED);
    }

    @Test
    @DisplayName("과금자료 등록 - 모두 오류 데이터인 경우")
    void registerChargeData_allErrors() {
        // given
        ChargeData errorData1 = new ChargeData(
            "CTR007",
            List.of(),
            "ERR001",
            "오류1"
        );

        ChargeData errorData2 = new ChargeData(
            "CTR008",
            List.of(),
            "ERR002",
            "오류2"
        );

        List<ChargeData> chargeDataList = List.of(errorData1, errorData2);

        // when
        chargeDataService.registerChargeData(chargeDataList);

        // then
        verify(statusService, times(1)).updateStatus("CTR007", HotbillStatus.CHARGE_DATA_ERROR);
        verify(statusService, times(1)).updateStatus("CTR008", HotbillStatus.CHARGE_DATA_ERROR);
        // 모두 오류이므로 요금계산 수행하지 않음
        verify(billingClient, never()).calculate(anyList());
    }
}
