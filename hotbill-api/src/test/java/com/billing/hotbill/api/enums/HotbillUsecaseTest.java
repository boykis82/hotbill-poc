package com.billing.hotbill.api.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HotbillUsecaseTest {

    @Test
    void 모든_Usecase가_존재해야_한다() {
        // Given & When
        HotbillUsecase[] usecases = HotbillUsecase.values();

        // Then
        assertEquals(4, usecases.length, "4개의 usecase가 존재해야 함");

        assertNotNull(HotbillUsecase.SIMPLE_INQUIRY);
        assertNotNull(HotbillUsecase.TERMINATION_INQUIRY);
        assertNotNull(HotbillUsecase.PORT_OUT);
        assertNotNull(HotbillUsecase.FORCED_TERMINATION);
    }

    @Test
    void 각_Usecase는_설명을_가져야_한다() {
        // Given & When & Then
        assertEquals("단순 요금 조회", HotbillUsecase.SIMPLE_INQUIRY.getDescription());
        assertEquals("해지 요금 조회", HotbillUsecase.TERMINATION_INQUIRY.getDescription());
        assertEquals("번호이동 portout", HotbillUsecase.PORT_OUT.getDescription());
        assertEquals("직권해지를 위한 요금 조회", HotbillUsecase.FORCED_TERMINATION.getDescription());
    }
}
