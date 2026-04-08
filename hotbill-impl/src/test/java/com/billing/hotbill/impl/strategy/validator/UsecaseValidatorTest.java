package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.dto.HotbillRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UsecaseValidator 인터페이스 테스트
 */
class UsecaseValidatorTest {

    @Test
    void UsecaseValidator_인터페이스는_validate_메서드를_가져야_한다() throws NoSuchMethodException {
        // Given
        Class<UsecaseValidator> validatorClass = UsecaseValidator.class;

        // When
        var method = validatorClass.getDeclaredMethod("validate", HotbillRequest.class);

        // Then
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
    }
}
