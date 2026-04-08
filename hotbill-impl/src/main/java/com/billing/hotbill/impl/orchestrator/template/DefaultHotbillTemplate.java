package com.billing.hotbill.impl.orchestrator.template;

import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.impl.service.StatusService;
import com.billing.hotbill.impl.strategy.chargedata.ChargeDataStrategyFactory;
import com.billing.hotbill.impl.strategy.validator.UsecaseValidator;
import com.billing.hotbill.impl.strategy.validator.UsecaseValidatorFactory;
import org.springframework.stereotype.Component;

/**
 * 기본 HotbillTemplate 구현체
 * UsecaseValidatorFactory를 통해 validator를 조회하여 검증 수행
 */
@Component
public class DefaultHotbillTemplate extends HotbillTemplate {

    private final UsecaseValidatorFactory validatorFactory;

    public DefaultHotbillTemplate(
            StatusService statusService,
            ChargeDataStrategyFactory chargeDataStrategyFactory,
            UsecaseValidatorFactory validatorFactory) {
        super(statusService, chargeDataStrategyFactory);
        this.validatorFactory = validatorFactory;
    }

    /**
     * Usecase별 검증 로직
     * UsecaseValidatorFactory를 통해 적절한 validator를 조회하여 검증 수행
     *
     * @param request Hotbill 요청
     */
    @Override
    protected void validate(HotbillRequest request) {
        UsecaseValidator validator = validatorFactory.getValidator(request.getUsecase());
        validator.validate(request);
    }
}
