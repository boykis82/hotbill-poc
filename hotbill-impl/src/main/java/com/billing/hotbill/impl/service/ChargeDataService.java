package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.ChargeData;
import com.billing.hotbill.api.dto.ChargeItem;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.client.BillingCalculationClient;
import com.billing.hotbill.impl.entity.ChargeDataEntity;
import com.billing.hotbill.impl.mapper.ChargeDataMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 과금자료 등록 및 요금계산 처리 서비스
 *
 * 외부 과금 시스템으로부터 과금자료를 비동기로 전달받아
 * 저장하고 요금계산을 수행하는 서비스입니다.
 * 현재 날짜(LocalDate.now())를 hotbillDate로 사용합니다.
 */
@Service
public class ChargeDataService {

    private final ChargeDataMapper chargeDataMapper;
    private final StatusService statusService;
    private final BillingCalculationClient billingClient;

    public ChargeDataService(ChargeDataMapper chargeDataMapper,
                            StatusService statusService,
                            BillingCalculationClient billingClient) {
        this.chargeDataMapper = chargeDataMapper;
        this.statusService = statusService;
        this.billingClient = billingClient;
    }

    /**
     * 과금자료 등록 및 요금계산 처리
     *
     * 비동기 콜백으로 외부 시스템에서 호출됩니다.
     * 1. 기존 과금자료 삭제 후 신규 저장
     * 2. 오류 데이터는 CHARGE_DATA_ERROR 상태로 설정
     * 3. 정상 데이터는 요금계산 수행 후 결과에 따라 상태 갱신
     *
     * @param chargeDataList 과금자료 목록
     */
    @Transactional
    public void registerChargeData(List<ChargeData> chargeDataList) {
        LocalDate hotbillDate = LocalDate.now();

        for (ChargeData chargeData : chargeDataList) {
            // 1. 기존 데이터 삭제
            chargeDataMapper.deleteByContractNumber(
                chargeData.getContractNumber(),
                hotbillDate
            );

            // 2. 신규 데이터 저장 (ChargeData -> ChargeDataEntity 변환)
            List<ChargeDataEntity> entities = convertToEntities(chargeData, hotbillDate);
            for (ChargeDataEntity entity : entities) {
                chargeDataMapper.insert(entity);
            }

            // 3. 오류 체크
            if (chargeData.hasError()) {
                statusService.updateStatus(
                    chargeData.getContractNumber(),
                    HotbillStatus.CHARGE_DATA_ERROR
                );
                continue;
            }

            // 4. 상태: 요금계산 전
            statusService.updateStatus(
                chargeData.getContractNumber(),
                HotbillStatus.BEFORE_CALCULATION
            );
        }

        // 5. 정상 데이터만 요금계산 수행
        List<ChargeData> validData = chargeDataList.stream()
            .filter(data -> !data.hasError())
            .collect(Collectors.toList());

        if (!validData.isEmpty()) {
            calculateBilling(validData);
        }
    }

    /**
     * 요금계산 수행 및 결과별 상태 갱신
     *
     * @param chargeDataList 정상 과금자료 목록
     */
    private void calculateBilling(List<ChargeData> chargeDataList) {
        // 요금계산 API 호출
        HotbillResult result = billingClient.calculate(chargeDataList);

        // 결과별 상태 갱신
        result.getSuccessList().forEach(billing ->
            statusService.updateStatus(billing.getContractNumber(), HotbillStatus.COMPLETED)
        );
        result.getFailureList().forEach(failure ->
            statusService.updateStatus(failure.getContractNumber(), HotbillStatus.FAILED)
        );
    }

    /**
     * ChargeData DTO를 ChargeDataEntity 리스트로 변환
     *
     * 하나의 ChargeData는 여러 ChargeItem을 가지므로,
     * 각 ChargeItem마다 별도의 Entity 행을 생성합니다.
     * ChargeItem이 없어도 오류 정보는 저장해야 하므로 빈 Entity를 생성합니다.
     *
     * @param chargeData 변환할 과금자료 DTO
     * @param hotbillDate Hotbill일자
     * @return Entity 리스트
     */
    private List<ChargeDataEntity> convertToEntities(ChargeData chargeData, LocalDate hotbillDate) {
        if (chargeData.getChargeItems().isEmpty()) {
            // ChargeItem이 없어도 오류 정보는 저장해야 함
            ChargeDataEntity entity = new ChargeDataEntity();
            entity.setContractNumber(chargeData.getContractNumber());
            entity.setHotbillDate(hotbillDate);
            entity.setErrorCode(chargeData.getErrorCode());
            entity.setErrorMessage(chargeData.getErrorMessage());
            entity.setCreateDatetime(LocalDateTime.now());
            return List.of(entity);
        }

        return chargeData.getChargeItems().stream()
            .map(chargeItem -> {
                ChargeDataEntity entity = new ChargeDataEntity();
                entity.setContractNumber(chargeData.getContractNumber());
                entity.setHotbillDate(hotbillDate);
                entity.setChargeCode(chargeItem.getChargeCode());
                entity.setAmount(chargeItem.getAmount());
                entity.setErrorCode(chargeData.getErrorCode());
                entity.setErrorMessage(chargeData.getErrorMessage());
                entity.setCreateDatetime(LocalDateTime.now());
                return entity;
            })
            .collect(Collectors.toList());
    }
}
