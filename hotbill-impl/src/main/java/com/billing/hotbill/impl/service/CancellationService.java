package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.CancellationRequest;
import com.billing.hotbill.api.dto.CancellationResult;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.entity.CancellationLogEntity;
import com.billing.hotbill.impl.mapper.BillingInfoMapper;
import com.billing.hotbill.impl.mapper.CancellationLogMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapperRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 해지정산 당일취소 처리 서비스
 *
 * 해지정산을 취소하고, 업무 테이블을 원복하는 서비스입니다.
 */
@Service
public class CancellationService {

    private final StatusService statusService;
    private final CancellationLogMapper cancellationLogMapper;
    private final BillingInfoMapper billingInfoMapper;
    private final TableUpdateMapperRegistry mapperRegistry;
    private final ObjectMapper objectMapper;

    public CancellationService(
        StatusService statusService,
        CancellationLogMapper cancellationLogMapper,
        BillingInfoMapper billingInfoMapper,
        TableUpdateMapperRegistry mapperRegistry
    ) {
        this.statusService = statusService;
        this.cancellationLogMapper = cancellationLogMapper;
        this.billingInfoMapper = billingInfoMapper;
        this.mapperRegistry = mapperRegistry;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 해지정산 당일취소 처리
     *
     * @param request 당일취소 요청
     * @return 당일취소 결과
     * @throws IllegalStateException 해지정산이 완료되지 않은 경우
     */
    @Transactional
    public CancellationResult processCancellation(CancellationRequest request) {
        List<String> contractNumbers = request.getContractNumbers();

        // 1. 사전 검증: 해지정산 완료 확인
        validateCancellationReady(contractNumbers);

        LocalDate hotbillDate = LocalDate.now();

        // 2. 취소로그 조회 및 데이터 원복
        List<CancellationLogEntity> logs =
            cancellationLogMapper.selectByContracts(contractNumbers, hotbillDate);

        for (CancellationLogEntity log : logs) {
            restoreBusinessTable(log);
        }

        // 3. 청구정보 테이블 데이터 삭제
        String billingMonth = hotbillDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
        billingInfoMapper.deleteHotbillData(contractNumbers, billingMonth);

        // 4. 핫빌청구정보 해지정산여부 갱신
        billingInfoMapper.updateSettlementYn(contractNumbers, hotbillDate, "N");

        // 5. 상태: 요금계산 완료로 원복
        statusService.updateStatus(contractNumbers, HotbillStatus.COMPLETED);

        return CancellationResult.success(contractNumbers);
    }

    /**
     * 취소 준비 상태 검증
     *
     * @param contractNumbers 계약번호 목록
     * @throws IllegalStateException 해지정산이 완료되지 않은 계약이 있는 경우
     */
    private void validateCancellationReady(List<String> contractNumbers) {
        List<String> notSettledContracts =
            statusService.findNotSettledContracts(contractNumbers);

        if (!notSettledContracts.isEmpty()) {
            throw new IllegalStateException(
                "Settlement not completed: " + String.join(", ", notSettledContracts)
            );
        }
    }

    /**
     * 업무 테이블 원복
     *
     * @param log 취소로그
     */
    private void restoreBusinessTable(CancellationLogEntity log) {
        try {
            // 스냅샷 데이터 파싱
            JsonNode snapshotData = objectMapper.readTree(log.getSnapshotData());
            String tableName = snapshotData.get("tableName").asText();
            JsonNode tablePk = snapshotData.get("tablePk");
            JsonNode beforeData = snapshotData.get("beforeData");

            // 테이블별 Mapper 조회
            TableUpdateMapper mapper = mapperRegistry.getMapper(tableName);

            // 원복 UPDATE 데이터 구성
            Map<String, Object> updateData = new HashMap<>();

            // PK 정보 추가
            updateData.putAll(convertJsonNodeToMap(tablePk));

            // beforeData 정보 추가
            if (beforeData != null) {
                updateData.putAll(convertJsonNodeToMap(beforeData));
            }

            // 원복 UPDATE 실행
            mapper.update(updateData);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse snapshot data JSON", e);
        }
    }

    /**
     * JsonNode를 Map으로 변환
     */
    private Map<String, Object> convertJsonNodeToMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        node.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();

            // null 값 처리
            if (value.isNull()) {
                map.put(entry.getKey(), null);
            } else {
                map.put(entry.getKey(), value.asText());
            }
        });
        return map;
    }
}
