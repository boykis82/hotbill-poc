package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.enums.HotbillUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UsecaseValidatorFactory 테스트
 */
class UsecaseValidatorFactoryTest {

    private UsecaseValidatorFactory factory;

    @BeforeEach
    void setUp() {
        // 각 Validator 인스턴스 생성
        SimpleInquiryValidator simpleInquiryValidator = new SimpleInquiryValidator();
        TerminationInquiryValidator terminationInquiryValidator = new TerminationInquiryValidator();
        PortOutValidator portOutValidator = new PortOutValidator();
        ForcedTerminationValidator forcedTerminationValidator = new ForcedTerminationValidator();

        // Factory 생성
        factory = new UsecaseValidatorFactory(
                simpleInquiryValidator,
                terminationInquiryValidator,
                portOutValidator,
                forcedTerminationValidator
        );
    }

    @Test
    void SIMPLE_INQUIRY_usecase에_대해_SimpleInquiryValidator를_반환해야_한다() {
        // When
        UsecaseValidator validator = factory.getValidator(HotbillUsecase.SIMPLE_INQUIRY);

        // Then
        assertNotNull(validator);
        assertInstanceOf(SimpleInquiryValidator.class, validator);
    }

    @Test
    void TERMINATION_INQUIRY_usecase에_대해_TerminationInquiryValidator를_반환해야_한다() {
        // When
        UsecaseValidator validator = factory.getValidator(HotbillUsecase.TERMINATION_INQUIRY);

        // Then
        assertNotNull(validator);
        assertInstanceOf(TerminationInquiryValidator.class, validator);
    }

    @Test
    void PORT_OUT_usecase에_대해_PortOutValidator를_반환해야_한다() {
        // When
        UsecaseValidator validator = factory.getValidator(HotbillUsecase.PORT_OUT);

        // Then
        assertNotNull(validator);
        assertInstanceOf(PortOutValidator.class, validator);
    }

    @Test
    void FORCED_TERMINATION_usecase에_대해_ForcedTerminationValidator를_반환해야_한다() {
        // When
        UsecaseValidator validator = factory.getValidator(HotbillUsecase.FORCED_TERMINATION);

        // Then
        assertNotNull(validator);
        assertInstanceOf(ForcedTerminationValidator.class, validator);
    }

    @Test
    void 존재하지_않는_usecase에_대해_예외가_발생해야_한다() {
        // Given - null usecase

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> factory.getValidator(null));
    }

    @Test
    void 모든_usecase에_대해_validator가_매핑되어_있어야_한다() {
        // Given
        HotbillUsecase[] allUsecases = HotbillUsecase.values();

        // When & Then - 모든 usecase에 대해 validator가 반환되어야 함
        for (HotbillUsecase usecase : allUsecases) {
            assertDoesNotThrow(() -> factory.getValidator(usecase));
            assertNotNull(factory.getValidator(usecase));
        }
    }
}
