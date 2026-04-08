package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.dto.BillingInfo;
import com.billing.hotbill.api.dto.ChargeItem;
import com.billing.hotbill.api.dto.HotbillQueryRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.impl.entity.HotbillBillingInfoEntity;
import com.billing.hotbill.impl.mapper.BillingInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * BillingService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillingService 테스트")
class BillingServiceTest {

    @Mock
    private StatusService statusService;

    @Mock
    private BillingInfoMapper billingInfoMapper;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(statusService, billingInfoMapper);
    }

    @Test
    @DisplayName("미완료 계약이 있을 때 예외 발생")
    void queryHotbillResult_NotCompleted_ThrowsException() {
        // Given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002", "CTR003");
        HotbillQueryRequest request = new HotbillQueryRequest(contractNumbers);

        // 미완료 계약 존재
        List<String> notCompletedContracts = Arrays.asList("CTR002", "CTR003");
        when(statusService.findNotCompletedContracts(contractNumbers))
            .thenReturn(notCompletedContracts);

        // When & Then
        assertThatThrownBy(() -> billingService.queryHotbillResult(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("요금계산 미완료");

        // Verify
        verify(statusService).findNotCompletedContracts(contractNumbers);
        verify(billingInfoMapper, never()).selectHotbillBillingInfo(any(), any());
    }

    @Test
    @DisplayName("모두 완료된 경우 청구정보 반환 - 단일 계약")
    void queryHotbillResult_AllCompleted_SingleContract() {
        // Given
        String contractNumber = "CTR001";
        HotbillQueryRequest request = new HotbillQueryRequest(
            Collections.singletonList(contractNumber)
        );

        // 모두 완료
        when(statusService.findNotCompletedContracts(request.getContractNumbers()))
            .thenReturn(Collections.emptyList());

        // 청구정보 데이터
        LocalDate hotbillDate = LocalDate.now();
        List<HotbillBillingInfoEntity> entities = Arrays.asList(
            createEntity(contractNumber, hotbillDate, 1, "CHG001", new BigDecimal("10000")),
            createEntity(contractNumber, hotbillDate, 2, "CHG002", new BigDecimal("5000"))
        );
        when(billingInfoMapper.selectHotbillBillingInfo(contractNumber, hotbillDate))
            .thenReturn(entities);

        // When
        HotbillResult result = billingService.queryHotbillResult(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAsyncProcessing()).isFalse();
        assertThat(result.getFailureList()).isEmpty();
        assertThat(result.getSuccessList()).hasSize(1);

        BillingInfo billingInfo = result.getSuccessList().get(0);
        assertThat(billingInfo.getContractNumber()).isEqualTo(contractNumber);
        assertThat(billingInfo.getHotbillDate()).isEqualTo(hotbillDate);
        assertThat(billingInfo.getTotalAmount()).isEqualTo(new BigDecimal("15000"));
        assertThat(billingInfo.getChargeItems()).hasSize(2);

        // Verify
        verify(statusService).findNotCompletedContracts(request.getContractNumbers());
        verify(billingInfoMapper).selectHotbillBillingInfo(contractNumber, hotbillDate);
    }

    @Test
    @DisplayName("모두 완료된 경우 청구정보 반환 - 다중 계약")
    void queryHotbillResult_AllCompleted_MultipleContracts() {
        // Given
        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");
        HotbillQueryRequest request = new HotbillQueryRequest(contractNumbers);

        // 모두 완료
        when(statusService.findNotCompletedContracts(contractNumbers))
            .thenReturn(Collections.emptyList());

        // 청구정보 데이터
        LocalDate hotbillDate = LocalDate.now();
        List<HotbillBillingInfoEntity> entities1 = Arrays.asList(
            createEntity("CTR001", hotbillDate, 1, "CHG001", new BigDecimal("10000"))
        );
        List<HotbillBillingInfoEntity> entities2 = Arrays.asList(
            createEntity("CTR002", hotbillDate, 1, "CHG002", new BigDecimal("20000"))
        );
        when(billingInfoMapper.selectHotbillBillingInfo("CTR001", hotbillDate))
            .thenReturn(entities1);
        when(billingInfoMapper.selectHotbillBillingInfo("CTR002", hotbillDate))
            .thenReturn(entities2);

        // When
        HotbillResult result = billingService.queryHotbillResult(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAsyncProcessing()).isFalse();
        assertThat(result.getFailureList()).isEmpty();
        assertThat(result.getSuccessList()).hasSize(2);

        // 첫 번째 계약
        BillingInfo billingInfo1 = result.getSuccessList().get(0);
        assertThat(billingInfo1.getContractNumber()).isEqualTo("CTR001");
        assertThat(billingInfo1.getTotalAmount()).isEqualTo(new BigDecimal("10000"));

        // 두 번째 계약
        BillingInfo billingInfo2 = result.getSuccessList().get(1);
        assertThat(billingInfo2.getContractNumber()).isEqualTo("CTR002");
        assertThat(billingInfo2.getTotalAmount()).isEqualTo(new BigDecimal("20000"));

        // Verify
        verify(statusService).findNotCompletedContracts(contractNumbers);
        verify(billingInfoMapper).selectHotbillBillingInfo("CTR001", hotbillDate);
        verify(billingInfoMapper).selectHotbillBillingInfo("CTR002", hotbillDate);
    }

    @Test
    @DisplayName("청구정보가 없는 계약은 건너뛰기")
    void queryHotbillResult_NoBillingInfo_Skip() {
        // Given
        String contractNumber = "CTR001";
        HotbillQueryRequest request = new HotbillQueryRequest(
            Collections.singletonList(contractNumber)
        );

        // 모두 완료
        when(statusService.findNotCompletedContracts(request.getContractNumbers()))
            .thenReturn(Collections.emptyList());

        // 청구정보 없음
        when(billingInfoMapper.selectHotbillBillingInfo(contractNumber, LocalDate.now()))
            .thenReturn(Collections.emptyList());

        // When
        HotbillResult result = billingService.queryHotbillResult(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSuccessList()).isEmpty();
        assertThat(result.getFailureList()).isEmpty();

        // Verify
        verify(statusService).findNotCompletedContracts(request.getContractNumbers());
        verify(billingInfoMapper).selectHotbillBillingInfo(contractNumber, LocalDate.now());
    }

    /**
     * 테스트용 엔티티 생성 헬퍼 메서드
     */
    private HotbillBillingInfoEntity createEntity(
        String contractNumber,
        LocalDate hotbillDate,
        Integer seqNo,
        String chargeCode,
        BigDecimal amount
    ) {
        return new HotbillBillingInfoEntity(
            contractNumber,
            hotbillDate,
            seqNo,
            chargeCode,
            amount
        );
    }
}
