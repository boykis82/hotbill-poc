package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillingInfoTest {

    @Test
    void 청구정보_객체생성_테스트() {
        // given
        String contractNumber = "CTR001";
        LocalDate hotbillDate = LocalDate.now();
        BigDecimal totalAmount = new BigDecimal("15000.00");
        List<ChargeItem> chargeItems = Arrays.asList(
            new ChargeItem("BASIC_FEE", new BigDecimal("10000.00")),
            new ChargeItem("DATA_FEE", new BigDecimal("5000.00"))
        );

        // when
        BillingInfo billingInfo = new BillingInfo(contractNumber, hotbillDate, totalAmount, chargeItems);

        // then
        assertNotNull(billingInfo);
        assertEquals(contractNumber, billingInfo.getContractNumber());
        assertEquals(hotbillDate, billingInfo.getHotbillDate());
        assertEquals(totalAmount, billingInfo.getTotalAmount());
        assertEquals(2, billingInfo.getChargeItems().size());
    }

    @Test
    void 필수필드가_null이면_예외발생() {
        // given
        LocalDate hotbillDate = LocalDate.now();
        BigDecimal totalAmount = new BigDecimal("10000");
        List<ChargeItem> chargeItems = List.of();

        // when & then
        assertThrows(NullPointerException.class, () -> {
            new BillingInfo(null, hotbillDate, totalAmount, chargeItems);
        });

        assertThrows(NullPointerException.class, () -> {
            new BillingInfo("CTR001", null, totalAmount, chargeItems);
        });

        assertThrows(NullPointerException.class, () -> {
            new BillingInfo("CTR001", hotbillDate, null, chargeItems);
        });
    }
}
