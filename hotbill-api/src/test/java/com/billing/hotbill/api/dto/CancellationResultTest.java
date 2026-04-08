package com.billing.hotbill.api.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CancellationResultTest {

    @Test
    void 해지정산취소결과_객체생성_테스트() {
        // given
        List<String> successContractNumbers = Arrays.asList("CTR001", "CTR002");
        List<String> failureContractNumbers = List.of("CTR003");

        // when
        CancellationResult result = new CancellationResult(successContractNumbers, failureContractNumbers);

        // then
        assertNotNull(result);
        assertEquals(2, result.getSuccessContractNumbers().size());
        assertEquals(1, result.getFailureContractNumbers().size());
    }

    @Test
    void success_정적메서드_테스트() {
        // given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");

        // when
        CancellationResult result = CancellationResult.success(contractNumbers);

        // then
        assertNotNull(result);
        assertEquals(2, result.getSuccessContractNumbers().size());
        assertTrue(result.getFailureContractNumbers().isEmpty());
    }

    @Test
    void isSuccess_메서드_테스트() {
        // given
        CancellationResult successResult = CancellationResult.success(List.of("CTR001"));
        CancellationResult failureResult = new CancellationResult(
            List.of("CTR001"),
            List.of("CTR002")
        );

        // when & then
        assertTrue(successResult.isSuccess());
        assertFalse(failureResult.isSuccess());
    }
}
