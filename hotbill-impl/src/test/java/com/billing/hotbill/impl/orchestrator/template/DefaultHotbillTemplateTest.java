package com.billing.hotbill.impl.orchestrator.template;

import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import com.billing.hotbill.impl.service.StatusService;
import com.billing.hotbill.impl.strategy.chargedata.ChargeDataStrategyFactory;
import com.billing.hotbill.impl.strategy.validator.UsecaseValidator;
import com.billing.hotbill.impl.strategy.validator.UsecaseValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultHotbillTemplate 구현 테스트")
class DefaultHotbillTemplateTest {

    @Mock
    private StatusService statusService;

    @Mock
    private ChargeDataStrategyFactory chargeDataStrategyFactory;

    @Mock
    private UsecaseValidatorFactory validatorFactory;

    @Mock
    private UsecaseValidator simpleInquiryValidator;

    @Mock
    private UsecaseValidator terminationInquiryValidator;

    private DefaultHotbillTemplate defaultHotbillTemplate;

    @BeforeEach
    void setUp() {
        defaultHotbillTemplate = new DefaultHotbillTemplate(
            statusService,
            chargeDataStrategyFactory,
            validatorFactory
        );
    }

    @Test
    @DisplayName("validate()는 UsecaseValidatorFactory를 통해 적절한 validator를 조회해야 한다")
    void validate_should_get_validator_from_factory() {
        // Given
        HotbillRequest request = createRequest(HotbillUsecase.SIMPLE_INQUIRY);

        when(validatorFactory.getValidator(HotbillUsecase.SIMPLE_INQUIRY))
            .thenReturn(simpleInquiryValidator);

        // When
        defaultHotbillTemplate.validate(request);

        // Then
        verify(validatorFactory).getValidator(HotbillUsecase.SIMPLE_INQUIRY);
    }

    @Test
    @DisplayName("validate()는 조회한 validator의 validate()를 호출해야 한다")
    void validate_should_call_validator_validate() {
        // Given
        HotbillRequest request = createRequest(HotbillUsecase.SIMPLE_INQUIRY);

        when(validatorFactory.getValidator(HotbillUsecase.SIMPLE_INQUIRY))
            .thenReturn(simpleInquiryValidator);

        // When
        defaultHotbillTemplate.validate(request);

        // Then
        verify(simpleInquiryValidator).validate(request);
    }

    @Test
    @DisplayName("validate()는 각 usecase별로 적절한 validator를 호출해야 한다 - TERMINATION_INQUIRY")
    void validate_should_call_appropriate_validator_for_termination_inquiry() {
        // Given
        HotbillRequest request = createRequest(HotbillUsecase.TERMINATION_INQUIRY);

        when(validatorFactory.getValidator(HotbillUsecase.TERMINATION_INQUIRY))
            .thenReturn(terminationInquiryValidator);

        // When
        defaultHotbillTemplate.validate(request);

        // Then
        verify(validatorFactory).getValidator(HotbillUsecase.TERMINATION_INQUIRY);
        verify(terminationInquiryValidator).validate(request);
    }

    private HotbillRequest createRequest(HotbillUsecase usecase) {
        ContractInfo contract = new ContractInfo("CTR001", ContractType.INTERNET);

        return new HotbillRequest(
            usecase,
            Arrays.asList(contract),
            LocalDateTime.now()
        );
    }
}
