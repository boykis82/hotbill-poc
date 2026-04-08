package com.billing.hotbill.api;

import com.billing.hotbill.api.dto.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hotbill 인터페이스 테스트
 */
class HotbillTest {

    @Test
    void Hotbill_인터페이스는_4개의_메서드를_가져야_한다() {
        // Given
        Class<Hotbill> hotbillClass = Hotbill.class;

        // When
        int methodCount = hotbillClass.getDeclaredMethods().length;

        // Then
        assertEquals(4, methodCount, "Hotbill 인터페이스는 4개의 메서드를 가져야 함");
    }

    @Test
    void requestHotbill_메서드_시그니처가_올바른지_확인() throws NoSuchMethodException {
        // Given
        Class<Hotbill> hotbillClass = Hotbill.class;

        // When
        var method = hotbillClass.getDeclaredMethod("requestHotbill", HotbillRequest.class);

        // Then
        assertNotNull(method);
        assertEquals(HotbillResult.class, method.getReturnType());
    }

    @Test
    void queryHotbillResult_메서드_시그니처가_올바른지_확인() throws NoSuchMethodException {
        // Given
        Class<Hotbill> hotbillClass = Hotbill.class;

        // When
        var method = hotbillClass.getDeclaredMethod("queryHotbillResult", HotbillQueryRequest.class);

        // Then
        assertNotNull(method);
        assertEquals(HotbillResult.class, method.getReturnType());
    }

    @Test
    void requestSettlement_메서드_시그니처가_올바른지_확인() throws NoSuchMethodException {
        // Given
        Class<Hotbill> hotbillClass = Hotbill.class;

        // When
        var method = hotbillClass.getDeclaredMethod("requestSettlement", SettlementRequest.class);

        // Then
        assertNotNull(method);
        assertEquals(SettlementResult.class, method.getReturnType());
    }

    @Test
    void cancelSettlement_메서드_시그니처가_올바른지_확인() throws NoSuchMethodException {
        // Given
        Class<Hotbill> hotbillClass = Hotbill.class;

        // When
        var method = hotbillClass.getDeclaredMethod("cancelSettlement", CancellationRequest.class);

        // Then
        assertNotNull(method);
        assertEquals(CancellationResult.class, method.getReturnType());
    }
}
