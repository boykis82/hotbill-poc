package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotbillResultTest {

    @Test
    void HotbillResult_객체생성_테스트() {
        // given
        List<BillingInfo> successList = List.of(
            createBillingInfo("CTR001", new BigDecimal("10000"))
        );
        List<FailureInfo> failureList = List.of(
            new FailureInfo("CTR002", "ERR001", "오류 발생")
        );

        // when
        HotbillResult result = new HotbillResult(successList, failureList, false);

        // then
        assertNotNull(result);
        assertEquals(1, result.getSuccessList().size());
        assertEquals(1, result.getFailureList().size());
        assertFalse(result.isAsyncProcessing());
    }

    @Test
    void merge_메서드_결과병합_테스트() {
        // given
        HotbillResult result1 = new HotbillResult(
            List.of(createBillingInfo("CTR001", new BigDecimal("10000"))),
            List.of(new FailureInfo("CTR002", "ERR001", "오류1")),
            false
        );

        HotbillResult result2 = new HotbillResult(
            List.of(createBillingInfo("CTR003", new BigDecimal("20000"))),
            List.of(new FailureInfo("CTR004", "ERR002", "오류2")),
            false
        );

        // when
        result1.merge(result2);

        // then
        assertEquals(2, result1.getSuccessList().size());
        assertEquals(2, result1.getFailureList().size());
        assertFalse(result1.isAsyncProcessing());
    }

    @Test
    void merge_메서드_비동기플래그_병합_테스트() {
        // given
        HotbillResult syncResult = new HotbillResult(
            List.of(createBillingInfo("CTR001", new BigDecimal("10000"))),
            List.of(),
            false
        );

        HotbillResult asyncResult = new HotbillResult(
            List.of(),
            List.of(),
            true
        );

        // when
        syncResult.merge(asyncResult);

        // then
        assertTrue(syncResult.isAsyncProcessing(), "하나라도 비동기면 비동기로 표시되어야 함");
    }

    @Test
    void asyncProcessing_정적메서드_테스트() {
        // given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");

        // when
        HotbillResult result = HotbillResult.asyncProcessing(contractNumbers);

        // then
        assertNotNull(result);
        assertTrue(result.isAsyncProcessing());
        assertTrue(result.getSuccessList().isEmpty());
        assertTrue(result.getFailureList().isEmpty());
    }

    private BillingInfo createBillingInfo(String contractNumber, BigDecimal totalAmount) {
        return new BillingInfo(
            contractNumber,
            LocalDate.now(),
            totalAmount,
            List.of()
        );
    }
}
