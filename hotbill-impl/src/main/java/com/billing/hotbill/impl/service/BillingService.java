package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.BillingInfo;
import com.billing.hotbill.api.dto.ChargeItem;
import com.billing.hotbill.api.dto.HotbillQueryRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.impl.entity.HotbillBillingInfoEntity;
import com.billing.hotbill.impl.mapper.BillingInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 청구정보 조회 서비스
 *
 * Hotbill 결과를 조회하고 반환하는 서비스입니다.
 */
@Service
public class BillingService {

    private final StatusService statusService;
    private final BillingInfoMapper billingInfoMapper;

    public BillingService(StatusService statusService, BillingInfoMapper billingInfoMapper) {
        this.statusService = statusService;
        this.billingInfoMapper = billingInfoMapper;
    }

    /**
     * Hotbill 결과 조회
     *
     * @param request 조회 요청 (계약번호 목록)
     * @return Hotbill 결과
     * @throws IllegalStateException 미완료 계약이 있는 경우
     */
    @Transactional(readOnly = true)
    public HotbillResult queryHotbillResult(HotbillQueryRequest request) {
        List<String> contractNumbers = request.getContractNumbers();

        // 1. 미완료 계약 확인
        List<String> notCompletedContracts = statusService.findNotCompletedContracts(contractNumbers);
        if (!notCompletedContracts.isEmpty()) {
            throw new IllegalStateException(
                "요금계산 미완료: " + String.join(", ", notCompletedContracts)
            );
        }

        // 2. 각 계약별 청구정보 조회
        LocalDate hotbillDate = LocalDate.now();
        List<BillingInfo> billingInfoList = new ArrayList<>();

        for (String contractNumber : contractNumbers) {
            List<HotbillBillingInfoEntity> entities =
                billingInfoMapper.selectHotbillBillingInfo(contractNumber, hotbillDate);

            // 청구정보가 없는 경우 건너뛰기
            if (entities.isEmpty()) {
                continue;
            }

            // 3. Entity를 DTO로 변환
            BillingInfo billingInfo = convertToBillingInfo(entities);
            billingInfoList.add(billingInfo);
        }

        // 4. HotbillResult 생성 및 반환
        return new HotbillResult(billingInfoList, Collections.emptyList(), false);
    }

    /**
     * Entity 목록을 BillingInfo DTO로 변환
     *
     * @param entities 같은 계약번호의 청구정보 엔티티 목록
     * @return BillingInfo DTO
     */
    private BillingInfo convertToBillingInfo(List<HotbillBillingInfoEntity> entities) {
        if (entities.isEmpty()) {
            throw new IllegalArgumentException("청구정보 엔티티 목록이 비어있습니다");
        }

        // 첫 번째 엔티티에서 공통 정보 추출
        HotbillBillingInfoEntity first = entities.get(0);
        String contractNumber = first.getContractNumber();
        LocalDate hotbillDate = first.getHotbillDate();

        // 과금항목 목록 생성
        List<ChargeItem> chargeItems = entities.stream()
            .map(entity -> new ChargeItem(entity.getChargeCode(), entity.getAmount()))
            .collect(Collectors.toList());

        // 총 청구금액 계산
        BigDecimal totalAmount = entities.stream()
            .map(HotbillBillingInfoEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BillingInfo(contractNumber, hotbillDate, totalAmount, chargeItems);
    }
}
