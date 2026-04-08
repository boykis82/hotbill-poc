package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChargeDataTest {

    @Test
    void 과금자료_객체생성_테스트() {
        // given
        String contractNumber = "CTR001";
        List<ChargeItem> chargeItems = Arrays.asList(
            new ChargeItem("BASIC_FEE", new BigDecimal("10000.00")),
            new ChargeItem("DATA_FEE", new BigDecimal("5000.00"))
        );

        // when
        ChargeData chargeData = new ChargeData(contractNumber, chargeItems, null, null);

        // then
        assertNotNull(chargeData);
        assertEquals(contractNumber, chargeData.getContractNumber());
        assertEquals(2, chargeData.getChargeItems().size());
        assertNull(chargeData.getErrorCode());
        assertNull(chargeData.getErrorMessage());
        assertFalse(chargeData.hasError());
    }

    @Test
    void 오류코드가_있으면_hasError가_true를_반환() {
        // given
        String contractNumber = "CTR001";
        List<ChargeItem> chargeItems = List.of();
        String errorCode = "ERR001";
        String errorMessage = "과금자료 조회 실패";

        // when
        ChargeData chargeData = new ChargeData(contractNumber, chargeItems, errorCode, errorMessage);

        // then
        assertTrue(chargeData.hasError());
        assertEquals(errorCode, chargeData.getErrorCode());
        assertEquals(errorMessage, chargeData.getErrorMessage());
    }

    @Test
    void 오류코드가_빈문자열이면_hasError가_false를_반환() {
        // given
        String contractNumber = "CTR001";
        List<ChargeItem> chargeItems = List.of();

        // when
        ChargeData chargeData = new ChargeData(contractNumber, chargeItems, "", "오류");

        // then
        assertFalse(chargeData.hasError());
    }

    @Test
    void 오류코드가_null이면_hasError가_false를_반환() {
        // given
        String contractNumber = "CTR001";
        List<ChargeItem> chargeItems = List.of();

        // when
        ChargeData chargeData = new ChargeData(contractNumber, chargeItems, null, null);

        // then
        assertFalse(chargeData.hasError());
    }
}
