package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettlementRequestTest {

    @Test
    void 해지정산요청_객체생성_테스트() {
        // given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002", "CTR003");

        // when
        SettlementRequest request = new SettlementRequest(contractNumbers);

        // then
        assertNotNull(request);
        assertEquals(3, request.getContractNumbers().size());
        assertEquals("CTR001", request.getContractNumbers().get(0));
    }

    @Test
    void 계약번호_리스트가_null이면_예외발생() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new SettlementRequest(null);
        });
    }

    @Test
    void 계약번호_리스트가_비어있으면_예외발생() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            new SettlementRequest(List.of());
        });
    }
}
