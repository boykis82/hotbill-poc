package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ChargeItemTest {

    @Test
    void 과금항목_객체생성_테스트() {
        // given
        String chargeCode = "BASIC_FEE";
        BigDecimal amount = new BigDecimal("10000.00");

        // when
        ChargeItem chargeItem = new ChargeItem(chargeCode, amount);

        // then
        assertNotNull(chargeItem);
        assertEquals(chargeCode, chargeItem.getChargeCode());
        assertEquals(amount, chargeItem.getAmount());
    }

    @Test
    void 과금항목_필드가_null이면_안됨() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new ChargeItem(null, new BigDecimal("10000.00"));
        });

        assertThrows(NullPointerException.class, () -> {
            new ChargeItem("BASIC_FEE", null);
        });
    }

    @Test
    void 과금금액은_음수가_될_수_없음() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            new ChargeItem("BASIC_FEE", new BigDecimal("-1000"));
        });
    }
}
