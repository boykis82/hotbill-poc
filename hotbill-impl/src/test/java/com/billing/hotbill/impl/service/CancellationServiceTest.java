package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.CancellationRequest;
import com.billing.hotbill.api.dto.CancellationResult;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.entity.CancellationLogEntity;
import com.billing.hotbill.impl.mapper.BillingInfoMapper;
import com.billing.hotbill.impl.mapper.CancellationLogMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapper;
import com.billing.hotbill.impl.mapper.update.TableUpdateMapperRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CancellationService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancellationService 테스트")
class CancellationServiceTest {

    @Mock
    private StatusService statusService;

    @Mock
    private CancellationLogMapper cancellationLogMapper;

    @Mock
    private BillingInfoMapper billingInfoMapper;

    @Mock
    private TableUpdateMapperRegistry mapperRegistry;

    @Mock
    private TableUpdateMapper tableUpdateMapper;

    private CancellationService cancellationService;

    @BeforeEach
    void setUp() {
        cancellationService = new CancellationService(
            statusService,
            cancellationLogMapper,
            billingInfoMapper,
            mapperRegistry
        );
    }

    @Test
    @DisplayName("해지정산 완료되지 않은 계약이 있으면 예외 발생")
    void processCancellation_NotSettled_ThrowsException() {
        // Given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");
        CancellationRequest request = new CancellationRequest(contractNumbers);

        // 미정산 계약 존재
        List<String> notSettledContracts = Collections.singletonList("CTR002");
        when(statusService.findNotSettledContracts(contractNumbers))
            .thenReturn(notSettledContracts);

        // When & Then
        assertThatThrownBy(() -> cancellationService.processCancellation(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Settlement not completed");

        // Verify
        verify(statusService).findNotSettledContracts(contractNumbers);
        verify(cancellationLogMapper, never()).selectByContracts(any(), any());
    }

    @Test
    @DisplayName("해지정산 당일취소 처리 - 전체 플로우 정상 케이스")
    void processCancellation_Success() {
        // Given
        String contractNumber = "CTR001";
        CancellationRequest request = new CancellationRequest(
            Collections.singletonList(contractNumber)
        );

        // 1. 모두 정산 완료
        when(statusService.findNotSettledContracts(request.getContractNumbers()))
            .thenReturn(Collections.emptyList());

        // 2. 취소로그 조회
        LocalDate hotbillDate = LocalDate.now();
        String snapshotJson = "{\"tableName\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"beforeData\":{\"terminationDate\":null,\"terminationYn\":\"N\"}}";
        CancellationLogEntity cancellationLog = new CancellationLogEntity(
            contractNumber, hotbillDate, 1, "TB_CONTRACT", snapshotJson
        );
        when(cancellationLogMapper.selectByContracts(request.getContractNumbers(), hotbillDate))
            .thenReturn(Collections.singletonList(cancellationLog));

        // 3. TableUpdateMapper 설정
        when(mapperRegistry.getMapper("TB_CONTRACT"))
            .thenReturn(tableUpdateMapper);

        // When
        CancellationResult result = cancellationService.processCancellation(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessContractNumbers()).containsExactly(contractNumber);
        assertThat(result.getFailureContractNumbers()).isEmpty();

        // Verify - 각 단계가 호출되었는지 확인
        verify(statusService).findNotSettledContracts(request.getContractNumbers());
        verify(cancellationLogMapper).selectByContracts(request.getContractNumbers(), hotbillDate);
        verify(tableUpdateMapper).update(any());
        verify(billingInfoMapper).deleteHotbillData(
            eq(request.getContractNumbers()),
            any()
        );
        verify(billingInfoMapper).updateSettlementYn(
            eq(request.getContractNumbers()),
            eq(hotbillDate),
            eq("N")
        );
        verify(statusService).updateStatus(
            request.getContractNumbers(),
            HotbillStatus.COMPLETED
        );
    }

    @Test
    @DisplayName("해지정산 당일취소 처리 - 다중 계약")
    void processCancellation_MultipleContracts() {
        // Given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");
        CancellationRequest request = new CancellationRequest(contractNumbers);

        // 1. 모두 정산 완료
        when(statusService.findNotSettledContracts(contractNumbers))
            .thenReturn(Collections.emptyList());

        // 2. 취소로그 조회
        LocalDate hotbillDate = LocalDate.now();
        String snapshotJson1 = "{\"tableName\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"beforeData\":{}}";
        String snapshotJson2 = "{\"tableName\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR002\"},\"beforeData\":{}}";
        List<CancellationLogEntity> cancellationLogs = Arrays.asList(
            new CancellationLogEntity("CTR001", hotbillDate, 1, "TB_CONTRACT", snapshotJson1),
            new CancellationLogEntity("CTR002", hotbillDate, 1, "TB_CONTRACT", snapshotJson2)
        );
        when(cancellationLogMapper.selectByContracts(contractNumbers, hotbillDate))
            .thenReturn(cancellationLogs);

        // 3. TableUpdateMapper 설정
        when(mapperRegistry.getMapper("TB_CONTRACT"))
            .thenReturn(tableUpdateMapper);

        // When
        CancellationResult result = cancellationService.processCancellation(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessContractNumbers()).containsExactlyInAnyOrder("CTR001", "CTR002");

        // Verify - 각 계약별로 처리되었는지 확인
        verify(tableUpdateMapper, times(2)).update(any());
        verify(billingInfoMapper).deleteHotbillData(eq(contractNumbers), any());
    }

    @Test
    @DisplayName("취소로그가 없으면 정상 처리 (원복만 스킵)")
    void processCancellation_NoCancellationLog() {
        // Given
        String contractNumber = "CTR001";
        CancellationRequest request = new CancellationRequest(
            Collections.singletonList(contractNumber)
        );

        // Setup
        when(statusService.findNotSettledContracts(any()))
            .thenReturn(Collections.emptyList());

        LocalDate hotbillDate = LocalDate.now();
        when(cancellationLogMapper.selectByContracts(any(), any()))
            .thenReturn(Collections.emptyList());

        // When
        CancellationResult result = cancellationService.processCancellation(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();

        // Verify - 취소로그 처리는 스킵되고 나머지만 수행
        verify(tableUpdateMapper, never()).update(any());
        verify(billingInfoMapper).deleteHotbillData(any(), any());
        verify(billingInfoMapper).updateSettlementYn(any(), any(), eq("N"));
        verify(statusService).updateStatus(
            request.getContractNumbers(),
            HotbillStatus.COMPLETED
        );
    }

    @Test
    @DisplayName("업무 테이블 원복 확인")
    void processCancellation_RestoresBusinessTable() {
        // Given
        String contractNumber = "CTR001";
        CancellationRequest request = new CancellationRequest(
            Collections.singletonList(contractNumber)
        );

        // Setup
        when(statusService.findNotSettledContracts(any()))
            .thenReturn(Collections.emptyList());

        LocalDate hotbillDate = LocalDate.now();
        String snapshotJson = "{\"tableName\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"beforeData\":{\"terminationDate\":null,\"terminationYn\":\"N\"}}";
        CancellationLogEntity cancellationLog = new CancellationLogEntity(
            contractNumber, hotbillDate, 1, "TB_CONTRACT", snapshotJson
        );
        when(cancellationLogMapper.selectByContracts(any(), any()))
            .thenReturn(Collections.singletonList(cancellationLog));

        when(mapperRegistry.getMapper("TB_CONTRACT"))
            .thenReturn(tableUpdateMapper);

        // When
        cancellationService.processCancellation(request);

        // Then - 업무 테이블이 원복되었는지 확인
        verify(tableUpdateMapper).update(any());
    }

    @Test
    @DisplayName("청구정보 삭제 확인")
    void processCancellation_DeletesBillingInfo() {
        // Given
        String contractNumber = "CTR001";
        CancellationRequest request = new CancellationRequest(
            Collections.singletonList(contractNumber)
        );

        // Setup
        when(statusService.findNotSettledContracts(any()))
            .thenReturn(Collections.emptyList());

        when(cancellationLogMapper.selectByContracts(any(), any()))
            .thenReturn(Collections.emptyList());

        // When
        cancellationService.processCancellation(request);

        // Then - 청구정보가 삭제되었는지 확인
        verify(billingInfoMapper).deleteHotbillData(any(), any());
    }
}
