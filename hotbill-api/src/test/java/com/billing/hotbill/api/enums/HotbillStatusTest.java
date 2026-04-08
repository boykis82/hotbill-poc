package com.billing.hotbill.api.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HotbillStatusTest {

    @Test
    void 모든_상태값이_존재해야_한다() {
        // Given & When
        HotbillStatus[] statuses = HotbillStatus.values();

        // Then
        assertEquals(6, statuses.length, "6개의 상태값이 존재해야 함");

        assertNotNull(HotbillStatus.REQUESTING);
        assertNotNull(HotbillStatus.BEFORE_CALCULATION);
        assertNotNull(HotbillStatus.CHARGE_DATA_ERROR);
        assertNotNull(HotbillStatus.COMPLETED);
        assertNotNull(HotbillStatus.FAILED);
        assertNotNull(HotbillStatus.SETTLEMENT_COMPLETED);
    }

    @Test
    void 각_상태값은_설명을_가져야_한다() {
        // Given & When & Then
        assertEquals("요청중", HotbillStatus.REQUESTING.getDescription());
        assertEquals("요금계산 전", HotbillStatus.BEFORE_CALCULATION.getDescription());
        assertEquals("과금자료 오류", HotbillStatus.CHARGE_DATA_ERROR.getDescription());
        assertEquals("요금계산 완료", HotbillStatus.COMPLETED.getDescription());
        assertEquals("실패", HotbillStatus.FAILED.getDescription());
        assertEquals("해지정산 완료", HotbillStatus.SETTLEMENT_COMPLETED.getDescription());
    }
}
