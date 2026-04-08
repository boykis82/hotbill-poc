package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.SettlementRequest;
import com.billing.hotbill.api.dto.SettlementResult;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.entity.CancellationLogEntity;
import com.billing.hotbill.impl.entity.HotbillBillingInfoEntity;
import com.billing.hotbill.impl.entity.PostingHistoryEntity;
import com.billing.hotbill.impl.mapper.BillingInfoMapper;
import com.billing.hotbill.impl.mapper.CancellationLogMapper;
import com.billing.hotbill.impl.mapper.PostingHistoryMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapperRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SettlementService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementService 테스트")
class SettlementServiceTest {

    @Mock
    private StatusService statusService;

    @Mock
    private PostingHistoryMapper postingHistoryMapper;

    @Mock
    private CancellationLogMapper cancellationLogMapper;

    @Mock
    private BillingInfoMapper billingInfoMapper;

    @Mock
    private TableUpdateMapperRegistry mapperRegistry;

    @Mock
    private TableUpdateMapper tableUpdateMapper;

    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(
            statusService,
            postingHistoryMapper,
            cancellationLogMapper,
            billingInfoMapper,
            mapperRegistry
        );
    }

    @Test
    @DisplayName("해지정산 완료되지 않은 계약이 있으면 예외 발생")
    void processSettlement_NotCompleted_ThrowsException() {
        // Given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");
        SettlementRequest request = new SettlementRequest(contractNumbers);

        // 미완료 계약 존재
        List<String> notCompletedContracts = Collections.singletonList("CTR002");
        when(statusService.findNotCompletedContracts(contractNumbers))
            .thenReturn(notCompletedContracts);

        // When & Then
        assertThatThrownBy(() -> settlementService.processSettlement(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Hotbill not completed");

        // Verify
        verify(statusService).findNotCompletedContracts(contractNumbers);
        verify(postingHistoryMapper, never()).selectByContracts(any(), any());
    }

    @Test
    @DisplayName("해지정산 처리 - 전체 플로우 정상 케이스")
    void processSettlement_Success() {
        // Given
        String contractNumber = "CTR001";
        SettlementRequest request = new SettlementRequest(
            Collections.singletonList(contractNumber)
        );

        // 1. 모두 완료
        when(statusService.findNotCompletedContracts(request.getContractNumbers()))
            .thenReturn(Collections.emptyList());

        // 2. 포스팅이력 조회
        LocalDate hotbillDate = LocalDate.now();
        String postingDataJson = "{\"targetTable\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"updates\":[{\"column\":\"TERMINATION_DATE\",\"value\":\"2025-03-09\"},{\"column\":\"TERMINATION_YN\",\"value\":\"Y\"}]}";
        PostingHistoryEntity postingHistory = new PostingHistoryEntity(
            contractNumber, hotbillDate, 1, postingDataJson
        );
        when(postingHistoryMapper.selectByContracts(request.getContractNumbers(), hotbillDate))
            .thenReturn(Collections.singletonList(postingHistory));

        // 3. TableUpdateMapper 설정
        when(mapperRegistry.getMapper("TB_CONTRACT"))
            .thenReturn(tableUpdateMapper);

        // 4. 핫빌청구정보 조회
        List<HotbillBillingInfoEntity> billingEntities = Arrays.asList(
            new HotbillBillingInfoEntity(contractNumber, hotbillDate, 1, "CHG001", new BigDecimal("10000"))
        );
        when(billingInfoMapper.selectHotbillBillingInfo(contractNumber, hotbillDate))
            .thenReturn(billingEntities);

        // When
        SettlementResult result = settlementService.processSettlement(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessContractNumbers()).containsExactly(contractNumber);
        assertThat(result.getFailureContractNumbers()).isEmpty();

        // Verify - 각 단계가 호출되었는지 확인
        verify(statusService).findNotCompletedContracts(request.getContractNumbers());
        verify(postingHistoryMapper).selectByContracts(request.getContractNumbers(), hotbillDate);
        verify(cancellationLogMapper).insert(any(CancellationLogEntity.class));
        verify(tableUpdateMapper).update(any());
        verify(billingInfoMapper).selectHotbillBillingInfo(contractNumber, hotbillDate);
        verify(billingInfoMapper).insertBillingInfo(any());
        verify(billingInfoMapper).updateSettlementYn(
            eq(request.getContractNumbers()),
            eq(hotbillDate),
            eq("Y")
        );
        verify(statusService).updateStatus(
            request.getContractNumbers(),
            HotbillStatus.SETTLEMENT_COMPLETED
        );
    }

    @Test
    @DisplayName("해지정산 처리 - 다중 계약")
    void processSettlement_MultipleContracts() {
        // Given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");
        SettlementRequest request = new SettlementRequest(contractNumbers);

        // 1. 모두 완료
        when(statusService.findNotCompletedContracts(contractNumbers))
            .thenReturn(Collections.emptyList());

        // 2. 포스팅이력 조회
        LocalDate hotbillDate = LocalDate.now();
        String postingDataJson1 = "{\"targetTable\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"updates\":[]}";
        String postingDataJson2 = "{\"targetTable\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR002\"},\"updates\":[]}";
        List<PostingHistoryEntity> postingHistories = Arrays.asList(
            new PostingHistoryEntity("CTR001", hotbillDate, 1, postingDataJson1),
            new PostingHistoryEntity("CTR002", hotbillDate, 1, postingDataJson2)
        );
        when(postingHistoryMapper.selectByContracts(contractNumbers, hotbillDate))
            .thenReturn(postingHistories);

        // 3. TableUpdateMapper 설정
        when(mapperRegistry.getMapper("TB_CONTRACT"))
            .thenReturn(tableUpdateMapper);

        // 4. 핫빌청구정보 조회
        List<HotbillBillingInfoEntity> billingEntities1 = Collections.singletonList(
            new HotbillBillingInfoEntity("CTR001", hotbillDate, 1, "CHG001", new BigDecimal("10000"))
        );
        List<HotbillBillingInfoEntity> billingEntities2 = Collections.singletonList(
            new HotbillBillingInfoEntity("CTR002", hotbillDate, 1, "CHG002", new BigDecimal("20000"))
        );
        when(billingInfoMapper.selectHotbillBillingInfo("CTR001", hotbillDate))
            .thenReturn(billingEntities1);
        when(billingInfoMapper.selectHotbillBillingInfo("CTR002", hotbillDate))
            .thenReturn(billingEntities2);

        // When
        SettlementResult result = settlementService.processSettlement(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessContractNumbers()).containsExactlyInAnyOrder("CTR001", "CTR002");

        // Verify - 각 계약별로 처리되었는지 확인
        verify(cancellationLogMapper, times(2)).insert(any(CancellationLogEntity.class));
        verify(tableUpdateMapper, times(2)).update(any());
        verify(billingInfoMapper, times(2)).insertBillingInfo(any());
        verify(billingInfoMapper, times(2)).selectHotbillBillingInfo(anyString(), eq(hotbillDate));
    }

    @Test
    @DisplayName("취소로그 저장 확인")
    void processSettlement_SavesCancellationLog() {
        // Given
        String contractNumber = "CTR001";
        SettlementRequest request = new SettlementRequest(
            Collections.singletonList(contractNumber)
        );

        // Setup
        when(statusService.findNotCompletedContracts(any()))
            .thenReturn(Collections.emptyList());

        LocalDate hotbillDate = LocalDate.now();
        String postingDataJson = "{\"targetTable\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"updates\":[{\"column\":\"TERMINATION_DATE\",\"value\":\"2025-03-09\"}]}";
        PostingHistoryEntity postingHistory = new PostingHistoryEntity(
            contractNumber, hotbillDate, 1, postingDataJson
        );
        when(postingHistoryMapper.selectByContracts(any(), any()))
            .thenReturn(Collections.singletonList(postingHistory));

        when(mapperRegistry.getMapper("TB_CONTRACT"))
            .thenReturn(tableUpdateMapper);

        List<HotbillBillingInfoEntity> billingEntities = Collections.singletonList(
            new HotbillBillingInfoEntity(contractNumber, hotbillDate, 1, "CHG001", new BigDecimal("10000"))
        );
        when(billingInfoMapper.selectHotbillBillingInfo(contractNumber, hotbillDate))
            .thenReturn(billingEntities);

        // When
        settlementService.processSettlement(request);

        // Then - 취소로그가 저장되었는지 확인
        ArgumentCaptor<CancellationLogEntity> captor = ArgumentCaptor.forClass(CancellationLogEntity.class);
        verify(cancellationLogMapper).insert(captor.capture());

        CancellationLogEntity savedLog = captor.getValue();
        assertThat(savedLog.getContractNumber()).isEqualTo(contractNumber);
        assertThat(savedLog.getHotbillDate()).isEqualTo(hotbillDate);
        assertThat(savedLog.getTableName()).isEqualTo("TB_CONTRACT");
        assertThat(savedLog.getSnapshotData()).isNotNull();
    }

    @Test
    @DisplayName("포스팅이력이 없으면 정상 처리 (이관만 수행)")
    void processSettlement_NoPostingHistory() {
        // Given
        String contractNumber = "CTR001";
        SettlementRequest request = new SettlementRequest(
            Collections.singletonList(contractNumber)
        );

        // Setup
        when(statusService.findNotCompletedContracts(any()))
            .thenReturn(Collections.emptyList());

        LocalDate hotbillDate = LocalDate.now();
        when(postingHistoryMapper.selectByContracts(any(), any()))
            .thenReturn(Collections.emptyList());

        List<HotbillBillingInfoEntity> billingEntities = Collections.singletonList(
            new HotbillBillingInfoEntity(contractNumber, hotbillDate, 1, "CHG001", new BigDecimal("10000"))
        );
        when(billingInfoMapper.selectHotbillBillingInfo(contractNumber, hotbillDate))
            .thenReturn(billingEntities);

        // When
        SettlementResult result = settlementService.processSettlement(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();

        // Verify - 포스팅이력 처리는 스킵되고 이관만 수행
        verify(cancellationLogMapper, never()).insert(any());
        verify(tableUpdateMapper, never()).update(any());
        verify(billingInfoMapper).insertBillingInfo(any());
        verify(statusService).updateStatus(
            request.getContractNumbers(),
            HotbillStatus.SETTLEMENT_COMPLETED
        );
    }
}
