package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.SettlementRequest;
import com.billing.hotbill.api.dto.SettlementResult;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.entity.BillingInfoEntity;
import com.billing.hotbill.impl.entity.CancellationLogEntity;
import com.billing.hotbill.impl.entity.HotbillBillingInfoEntity;
import com.billing.hotbill.impl.entity.PostingHistoryEntity;
import com.billing.hotbill.impl.mapper.BillingInfoMapper;
import com.billing.hotbill.impl.mapper.CancellationLogMapper;
import com.billing.hotbill.impl.mapper.PostingHistoryMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapperRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 해지정산 처리 서비스
 *
 * Hotbill 완료 후 해지정산을 처리하고, 청구정보를 이관하는 서비스입니다.
 */
@Service
public class SettlementService {

    private final StatusService statusService;
    private final PostingHistoryMapper postingHistoryMapper;
    private final CancellationLogMapper cancellationLogMapper;
    private final BillingInfoMapper billingInfoMapper;
    private final TableUpdateMapperRegistry mapperRegistry;
    private final ObjectMapper objectMapper;

    public SettlementService(
        StatusService statusService,
        PostingHistoryMapper postingHistoryMapper,
        CancellationLogMapper cancellationLogMapper,
        BillingInfoMapper billingInfoMapper,
        TableUpdateMapperRegistry mapperRegistry
    ) {
        this.statusService = statusService;
        this.postingHistoryMapper = postingHistoryMapper;
        this.cancellationLogMapper = cancellationLogMapper;
        this.billingInfoMapper = billingInfoMapper;
        this.mapperRegistry = mapperRegistry;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 해지정산 처리
     *
     * @param request 해지정산 요청
     * @return 해지정산 결과
     * @throws IllegalStateException Hotbill이 완료되지 않은 경우
     */
    @Transactional
    public SettlementResult processSettlement(SettlementRequest request) {
        List<String> contractNumbers = request.getContractNumbers();

        // 1. 사전 검증: Hotbill 완료 확인
        validateSettlementReady(contractNumbers);

        LocalDate hotbillDate = LocalDate.now();

        // 2. 포스팅이력 조회 및 업무 테이블 갱신
        List<PostingHistoryEntity> postingHistories =
            postingHistoryMapper.selectByContracts(contractNumbers, hotbillDate);

        for (PostingHistoryEntity history : postingHistories) {
            // 2-1. 변경 전 스냅샷 저장
            saveSnapshotBeforeUpdate(history);

            // 2-2. 업무 테이블 UPDATE
            updateBusinessTable(history);
        }

        // 3. 핫빌청구정보 → 청구정보 이관
        migrateBillingInfo(contractNumbers, hotbillDate);

        // 4. 핫빌청구정보 해지정산여부 갱신
        billingInfoMapper.updateSettlementYn(contractNumbers, hotbillDate, "Y");

        // 5. 상태: 해지정산 완료
        statusService.updateStatus(contractNumbers, HotbillStatus.SETTLEMENT_COMPLETED);

        return SettlementResult.success(contractNumbers);
    }

    /**
     * 해지정산 준비 상태 검증
     *
     * @param contractNumbers 계약번호 목록
     * @throws IllegalStateException Hotbill이 완료되지 않은 계약이 있는 경우
     */
    private void validateSettlementReady(List<String> contractNumbers) {
        List<String> notCompletedContracts =
            statusService.findNotCompletedContracts(contractNumbers);

        if (!notCompletedContracts.isEmpty()) {
            throw new IllegalStateException(
                "Hotbill not completed: " + String.join(", ", notCompletedContracts)
            );
        }
    }

    /**
     * 변경 전 스냅샷 저장
     *
     * @param history 포스팅이력
     */
    private void saveSnapshotBeforeUpdate(PostingHistoryEntity history) {
        try {
            // JSON 파싱
            JsonNode postingData = objectMapper.readTree(history.getPostingData());
            String targetTable = postingData.get("targetTable").asText();
            JsonNode tablePk = postingData.get("tablePk");

            // 변경 전 데이터 조회 (stub - 실제로는 테이블별로 조회 필요)
            Map<String, Object> beforeData = queryBeforeData(targetTable, tablePk);

            // 스냅샷 JSON 생성
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("tableName", targetTable);
            snapshot.put("tablePk", convertJsonNodeToMap(tablePk));
            snapshot.put("beforeData", beforeData);

            String snapshotJson = objectMapper.writeValueAsString(snapshot);

            // 취소로그 저장
            CancellationLogEntity log = new CancellationLogEntity(
                history.getContractNumber(),
                history.getHotbillDate(),
                history.getSeqNo(),
                targetTable,
                snapshotJson
            );

            cancellationLogMapper.insert(log);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse posting data JSON", e);
        }
    }

    /**
     * 업무 테이블 UPDATE
     *
     * @param history 포스팅이력
     */
    private void updateBusinessTable(PostingHistoryEntity history) {
        try {
            // JSON 파싱
            JsonNode postingData = objectMapper.readTree(history.getPostingData());
            String targetTable = postingData.get("targetTable").asText();
            JsonNode tablePk = postingData.get("tablePk");
            JsonNode updates = postingData.get("updates");

            // 테이블별 Mapper 조회
            TableUpdateMapper mapper = mapperRegistry.getMapper(targetTable);

            // UPDATE 데이터 구성
            Map<String, Object> updateData = new HashMap<>();
            updateData.putAll(convertJsonNodeToMap(tablePk));

            // updates 배열 처리
            if (updates != null && updates.isArray()) {
                for (JsonNode update : updates) {
                    String column = update.get("column").asText();
                    String value = update.get("value").asText();

                    // 컬럼명을 camelCase로 변환 (TERMINATION_DATE -> terminationDate)
                    String camelCaseColumn = toCamelCase(column);
                    updateData.put(camelCaseColumn, value);
                }
            }

            // UPDATE 실행
            mapper.update(updateData);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse posting data JSON", e);
        }
    }

    /**
     * 핫빌청구정보 → 청구정보 이관
     *
     * @param contractNumbers 계약번호 목록
     * @param hotbillDate Hotbill일자
     */
    private void migrateBillingInfo(List<String> contractNumbers, LocalDate hotbillDate) {
        String billingMonth = hotbillDate.format(DateTimeFormatter.ofPattern("yyyyMM"));

        for (String contractNumber : contractNumbers) {
            // 핫빌청구정보 조회
            List<HotbillBillingInfoEntity> hotbillEntities =
                billingInfoMapper.selectHotbillBillingInfo(contractNumber, hotbillDate);

            // 청구정보로 이관
            for (HotbillBillingInfoEntity hotbillEntity : hotbillEntities) {
                BillingInfoEntity billingEntity = new BillingInfoEntity(
                    hotbillEntity.getContractNumber(),
                    billingMonth,
                    hotbillEntity.getSeqNo(),
                    hotbillEntity.getChargeCode(),
                    hotbillEntity.getAmount(),
                    "Y"  // HOTBILL_YN = 'Y'
                );

                billingInfoMapper.insertBillingInfo(billingEntity);
            }
        }
    }

    /**
     * 변경 전 데이터 조회 (stub)
     *
     * 실제 구현에서는 테이블별로 SELECT 쿼리를 실행해야 합니다.
     *
     * @param tableName 테이블명
     * @param tablePk 테이블 PK
     * @return 변경 전 데이터
     */
    private Map<String, Object> queryBeforeData(String tableName, JsonNode tablePk) {
        // Stub implementation
        Map<String, Object> beforeData = new HashMap<>();
        beforeData.put("terminationDate", null);
        beforeData.put("terminationYn", "N");
        return beforeData;
    }

    /**
     * JsonNode를 Map으로 변환
     */
    private Map<String, Object> convertJsonNodeToMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        node.fields().forEachRemaining(entry -> {
            map.put(entry.getKey(), entry.getValue().asText());
        });
        return map;
    }

    /**
     * SNAKE_CASE를 camelCase로 변환
     */
    private String toCamelCase(String snakeCase) {
        String[] parts = snakeCase.toLowerCase().split("_");
        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCase.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                camelCase.append(parts[i].substring(1));
            }
        }
        return camelCase.toString();
    }
}
