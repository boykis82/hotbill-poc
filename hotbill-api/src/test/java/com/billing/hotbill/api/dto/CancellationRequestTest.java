package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CancellationRequestTest {

    @Test
    void 해지정산취소요청_객체생성_테스트() {
        // given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");

        // when
        CancellationRequest request = new CancellationRequest(contractNumbers);

        // then
        assertNotNull(request);
        assertEquals(2, request.getContractNumbers().size());
    }

    @Test
    void 계약번호_리스트가_null이면_예외발생() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new CancellationRequest(null);
        });
    }

    @Test
    void 계약번호_리스트가_비어있으면_예외발생() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            new CancellationRequest(List.of());
        });
    }
}
