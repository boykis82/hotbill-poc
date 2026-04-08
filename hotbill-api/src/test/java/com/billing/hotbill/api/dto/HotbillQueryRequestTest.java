package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotbillQueryRequestTest {

    @Test
    void Hotbill결과조회요청_객체생성_테스트() {
        // given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002", "CTR003");

        // when
        HotbillQueryRequest request = new HotbillQueryRequest(contractNumbers);

        // then
        assertNotNull(request);
        assertEquals(3, request.getContractNumbers().size());
        assertEquals("CTR001", request.getContractNumbers().get(0));
    }

    @Test
    void 계약번호_리스트가_null이면_예외발생() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new HotbillQueryRequest(null);
        });
    }

    @Test
    void 계약번호_리스트가_비어있으면_예외발생() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            new HotbillQueryRequest(List.of());
        });
    }
}
