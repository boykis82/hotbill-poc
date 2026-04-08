package com.billing.hotbill.impl.strategy.validator;

import com.billing.hotbill.api.enums.HotbillUsecase;

import java.util.Map;

/**
 * Usecase별 Validator Factory
 *
 * <p>각 Hotbill usecase에 해당하는 Validator를 반환합니다.
 * Strategy Pattern을 적용하여 usecase별 검증 로직을 독립적으로 관리합니다.</p>
 */
public class UsecaseValidatorFactory {

    private final Map<HotbillUsecase, UsecaseValidator> validatorMap;

    /**
     * UsecaseValidatorFactory 생성자
     *
     * @param simpleInquiryValidator 단순 요금 조회 Validator
     * @param terminationInquiryValidator 해지 요금 조회 Validator
     * @param portOutValidator 번호이동 PortOut Validator
     * @param forcedTerminationValidator 직권해지 Validator
     */
    public UsecaseValidatorFactory(
            SimpleInquiryValidator simpleInquiryValidator,
            TerminationInquiryValidator terminationInquiryValidator,
            PortOutValidator portOutValidator,
            ForcedTerminationValidator forcedTerminationValidator) {

        this.validatorMap = Map.of(
                HotbillUsecase.SIMPLE_INQUIRY, simpleInquiryValidator,
                HotbillUsecase.TERMINATION_INQUIRY, terminationInquiryValidator,
                HotbillUsecase.PORT_OUT, portOutValidator,
                HotbillUsecase.FORCED_TERMINATION, forcedTerminationValidator
        );
    }

    /**
     * 주어진 usecase에 해당하는 Validator를 반환합니다.
     *
     * @param usecase Hotbill usecase
     * @return 해당 usecase의 Validator
     * @throws IllegalArgumentException usecase가 null이거나 매핑되지 않은 경우
     */
    public UsecaseValidator getValidator(HotbillUsecase usecase) {
        if (usecase == null) {
            throw new IllegalArgumentException("Usecase는 필수입니다");
        }

        UsecaseValidator validator = validatorMap.get(usecase);
        if (validator == null) {
            throw new IllegalArgumentException("Unknown usecase: " + usecase);
        }

        return validator;
    }
}
