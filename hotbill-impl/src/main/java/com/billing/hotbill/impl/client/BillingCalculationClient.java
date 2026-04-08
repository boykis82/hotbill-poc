package com.billing.hotbill.impl.client;

import com.billing.hotbill.api.dto.BillingInfo;
import com.billing.hotbill.api.dto.ChargeData;
import com.billing.hotbill.api.dto.ChargeItem;
import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 요금계산 API 클라이언트 (Stub)
 *
 * 실제로는 별도 모듈의 요금계산 API를 호출하지만,
 * 여기서는 stub으로 더미 데이터를 반환
 */
@Component
public class BillingCalculationClient {

    private static final Logger log = LoggerFactory.getLogger(BillingCalculationClient.class);

    private final Random random = new Random();

    /**
     * 요금계산 수행 (HotbillRequest 기반)
     *
     * @param request Hotbill 요청
     * @return 요금계산 결과
     */
    public HotbillResult calculate(HotbillRequest request) {
        log.info("요금계산 시작 - 계약 수: {}, usecase: {}",
                request.getContracts().size(), request.getUsecase());

        List<BillingInfo> successList = new ArrayList<>();

        for (ContractInfo contract : request.getContracts()) {
            BillingInfo billingInfo = calculateForContract(contract);
            successList.add(billingInfo);
        }

        log.info("요금계산 완료 - 성공: {}, 실패: 0", successList.size());

        return new HotbillResult(successList, new ArrayList<>(), false);
    }

    /**
     * 요금계산 수행 (ChargeData 리스트 기반 - 비동기 콜백용)
     *
     * @param chargeDataList 과금자료 리스트
     * @return 요금계산 결과
     */
    public HotbillResult calculate(List<ChargeData> chargeDataList) {
        log.info("과금자료 기반 요금계산 시작 - 과금자료 수: {}", chargeDataList.size());

        List<BillingInfo> successList = new ArrayList<>();

        for (ChargeData chargeData : chargeDataList) {
            BillingInfo billingInfo = calculateFromChargeData(chargeData);
            successList.add(billingInfo);
        }

        log.info("과금자료 기반 요금계산 완료 - 성공: {}, 실패: 0", successList.size());

        return new HotbillResult(successList, new ArrayList<>(), false);
    }

    /**
     * 계약별 요금계산 (Stub - 더미 데이터 생성)
     */
    private BillingInfo calculateForContract(ContractInfo contract) {
        // 청구항목 생성 (더미 데이터)
        List<ChargeItem> chargeItems = generateDummyChargeItems();

        // 총액 계산
        BigDecimal totalAmount = chargeItems.stream()
                .map(ChargeItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BillingInfo(
                contract.getContractNumber(),
                LocalDate.now(),
                totalAmount,
                chargeItems
        );
    }

    /**
     * 과금자료 기반 요금계산 (Stub)
     */
    private BillingInfo calculateFromChargeData(ChargeData chargeData) {
        // 과금자료의 청구항목 사용
        List<ChargeItem> chargeItems = chargeData.getChargeItems();

        // 총액 계산
        BigDecimal totalAmount = chargeItems.stream()
                .map(ChargeItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BillingInfo(
                chargeData.getContractNumber(),
                LocalDate.now(),
                totalAmount,
                chargeItems
        );
    }

    /**
     * 더미 청구항목 생성 (테스트용)
     */
    private List<ChargeItem> generateDummyChargeItems() {
        List<ChargeItem> items = new ArrayList<>();

        // 기본료
        items.add(new ChargeItem("BASIC_FEE", new BigDecimal("30000")));

        // 사용료
        items.add(new ChargeItem("USAGE_FEE", new BigDecimal("15000")));

        // 부가서비스료 (랜덤)
        if (random.nextBoolean()) {
            items.add(new ChargeItem("ADDITIONAL_FEE", new BigDecimal("5000")));
        }

        return items;
    }
}
