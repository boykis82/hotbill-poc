package com.billing.hotbill.impl.orchestrator.template;

import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.service.StatusService;
import com.billing.hotbill.impl.strategy.chargedata.ChargeDataStrategy;
import com.billing.hotbill.impl.strategy.chargedata.ChargeDataStrategyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hotbill 처리 템플릿 추상 클래스
 * Template Method Pattern 적용
 */
public abstract class HotbillTemplate {

    private final StatusService statusService;
    private final ChargeDataStrategyFactory chargeDataStrategyFactory;

    protected HotbillTemplate(
            StatusService statusService,
            ChargeDataStrategyFactory chargeDataStrategyFactory) {
        this.statusService = statusService;
        this.chargeDataStrategyFactory = chargeDataStrategyFactory;
    }

    /**
     * Hotbill 요청 처리 템플릿 메서드
     *
     * @param request Hotbill 요청
     * @return Hotbill 결과
     */
    public final HotbillResult executeHotbill(HotbillRequest request) {
        // 1. 사전 검증 (확장 포인트)
        validate(request);

        // 2. 상태 초기화
        statusService.updateStatus(request.getContractNumbers(), HotbillStatus.REQUESTING);

        // 3. 계약별로 동기/비동기 그룹핑
        Map<ProcessingType, List<ContractInfo>> groupedContracts =
            groupContractsByProcessingType(request);

        HotbillResult result = new HotbillResult(
            new ArrayList<>(),
            new ArrayList<>(),
            false
        );

        // 4. 비동기 처리 대상 계약들
        List<ContractInfo> asyncContracts = groupedContracts.get(ProcessingType.ASYNC);
        if (asyncContracts != null && !asyncContracts.isEmpty()) {
            HotbillRequest asyncRequest = request.withContracts(asyncContracts);
            ChargeDataStrategy asyncStrategy = chargeDataStrategyFactory.getAsyncStrategy();
            HotbillResult asyncResult = asyncStrategy.process(asyncRequest);
            result.merge(asyncResult);
        }

        // 5. 동기 처리 대상 계약들
        List<ContractInfo> syncContracts = groupedContracts.get(ProcessingType.SYNC);
        if (syncContracts != null && !syncContracts.isEmpty()) {
            HotbillRequest syncRequest = request.withContracts(syncContracts);
            ChargeDataStrategy syncStrategy = chargeDataStrategyFactory.getSyncStrategy();
            HotbillResult syncResult = syncStrategy.process(syncRequest);
            result.merge(syncResult);
        }

        // 6. 사후 처리 (확장 포인트)
        postProcess(request, result);

        return result;
    }

    /**
     * 계약들을 동기/비동기 처리 타입별로 그룹핑
     *
     * @param request Hotbill 요청
     * @return 처리 타입별 계약 목록
     */
    private Map<ProcessingType, List<ContractInfo>> groupContractsByProcessingType(
            HotbillRequest request) {

        return request.getContracts().stream()
            .collect(Collectors.groupingBy(contract ->
                determineProcessingType(contract.getContractType(), request.getUsecase())
            ));
    }

    /**
     * 계약유형과 usecase 조합으로 처리 타입 결정
     *
     * @param contractType 계약 유형
     * @param usecase Hotbill usecase
     * @return 처리 타입 (SYNC/ASYNC)
     */
    private ProcessingType determineProcessingType(
            com.billing.hotbill.api.enums.ContractType contractType,
            com.billing.hotbill.api.enums.HotbillUsecase usecase) {

        return chargeDataStrategyFactory.determineProcessingType(contractType, usecase);
    }

    /**
     * Usecase별 검증 로직 (확장 포인트)
     *
     * @param request Hotbill 요청
     */
    protected abstract void validate(HotbillRequest request);

    /**
     * Usecase별 사후 처리 로직 (확장 포인트)
     *
     * @param request Hotbill 요청
     * @param result Hotbill 결과
     */
    protected void postProcess(HotbillRequest request, HotbillResult result) {
        // 기본 구현: 아무것도 하지 않음
    }
}
