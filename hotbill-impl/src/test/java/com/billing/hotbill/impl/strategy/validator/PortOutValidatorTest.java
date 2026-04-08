package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PortOutValidator 테스트
 */
class PortOutValidatorTest {

    private PortOutValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PortOutValidator();
    }

    @Test
    void 유효한_요청에_대해_검증이_성공해야_한다() {
        // Given
        ContractInfo contract = new ContractInfo("CTR001", ContractType.MOBILE_PHONE);

        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.PORT_OUT,
                List.of(contract),
                LocalDateTime.now()
        );

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void 계약_정보가_비어있으면_예외가_발생해야_한다() {
        // Given
        HotbillRequest request = new HotbillRequest(
                HotbillUsecase.PORT_OUT,
                List.of(),
                LocalDateTime.now()
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }
}
