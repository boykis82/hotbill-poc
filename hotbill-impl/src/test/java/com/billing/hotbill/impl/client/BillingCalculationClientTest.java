package com.billing.hotbill.impl.client;

import com.billing.hotbill.api.dto.BillingInfo;
import com.billing.hotbill.api.dto.ChargeItem;
import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BillingCalculationClient 테스트 (Stub)
 */
class BillingCalculationClientTest {

    private BillingCalculationClient billingCalculationClient;

    @BeforeEach
    void setUp() {
        billingCalculationClient = new BillingCalculationClient();
    }

    @Test
    void calculate_단일_계약_성공() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.INTERNET);

        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.SIMPLE_INQUIRY,
                List.of(contract),
                LocalDateTime.now()
        );

        // When
        HotbillResult result = billingCalculationClient.calculate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isAsyncProcessing());
        assertEquals(1, result.getSuccessList().size());
        assertTrue(result.getFailureList().isEmpty());

        BillingInfo billingInfo = result.getSuccessList().get(0);
        assertEquals("CTR001", billingInfo.getContractNumber());
        assertNotNull(billingInfo.getHotbillDate());
        assertNotNull(billingInfo.getTotalAmount());
        assertTrue(billingInfo.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        assertFalse(billingInfo.getChargeItems().isEmpty());
    }

    @Test
    void calculate_여러_계약_성공() {
        // Given
        List<ContractInfo> contracts = List.of(
            new ContractInfo("CTR001", ContractType.INTERNET),
            new ContractInfo("CTR002", ContractType.MOBILE_PHONE),
            new ContractInfo("CTR003", ContractType.IPTV)
        );

        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.TERMINATION_INQUIRY,
                contracts,
                LocalDateTime.now()
        );

        // When
        HotbillResult result = billingCalculationClient.calculate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isAsyncProcessing());
        assertEquals(3, result.getSuccessList().size());
        assertTrue(result.getFailureList().isEmpty());
    }

    @Test
    void calculate_청구항목_포함_확인() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.MOBILE_PHONE);

        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.TERMINATION_INQUIRY,
                List.of(contract),
                LocalDateTime.now()
        );

        // When
        HotbillResult result = billingCalculationClient.calculate(request);

        // Then
        BillingInfo billingInfo = result.getSuccessList().get(0);
        List<ChargeItem> chargeItems = billingInfo.getChargeItems();

        assertFalse(chargeItems.isEmpty());
        assertTrue(chargeItems.size() > 0);

        // 청구항목 검증
        for (ChargeItem item : chargeItems) {
            assertNotNull(item.getChargeCode());
            assertNotNull(item.getAmount());
            assertTrue(item.getAmount().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Test
    void calculate_총액_계산_확인() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.IPTV);

        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.SIMPLE_INQUIRY,
                List.of(contract),
                LocalDateTime.now()
        );

        // When
        HotbillResult result = billingCalculationClient.calculate(request);

        // Then
        BillingInfo billingInfo = result.getSuccessList().get(0);

        // 총액 = 청구항목들의 합
        BigDecimal calculatedTotal = billingInfo.getChargeItems().stream()
                .map(ChargeItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(calculatedTotal, billingInfo.getTotalAmount());
    }

    @Test
    void calculate_Hotbill일자_설정_확인() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.INTERNET);

        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.PORT_OUT,
                List.of(contract),
                LocalDateTime.now()
        );

        // When
        HotbillResult result = billingCalculationClient.calculate(request);

        // Then
        BillingInfo billingInfo = result.getSuccessList().get(0);
        LocalDate hotbillDate = billingInfo.getHotbillDate();

        assertNotNull(hotbillDate);
        // 오늘 날짜여야 함
        assertEquals(LocalDate.now(), hotbillDate);
    }

    @Test
    void calculate_빈_계약_리스트_처리() {
        // Given
        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.SIMPLE_INQUIRY,
                List.of(),
                LocalDateTime.now()
        );

        // When
        HotbillResult result = billingCalculationClient.calculate(request);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccessList().isEmpty());
        assertTrue(result.getFailureList().isEmpty());
    }
}
