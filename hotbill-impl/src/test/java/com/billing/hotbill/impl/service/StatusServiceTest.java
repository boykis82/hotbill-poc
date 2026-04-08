package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.mapper.HotbillStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * StatusService 테스트
 */
@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private HotbillStatusMapper hotbillStatusMapper;

    @InjectMocks
    private StatusService statusService;

    @Test
    @DisplayName("단건 상태 갱신 - 성공")
    void updateStatus_single_success() {
        // given
        String contractNumber = "CTR001";
        HotbillStatus status = HotbillStatus.REQUESTING;

        // when
        statusService.updateStatus(contractNumber, status);

        // then
        verify(hotbillStatusMapper, times(1)).updateStatus(
            eq(contractNumber),
            any(LocalDate.class),
            eq(status)
        );
    }

    @Test
    @DisplayName("다건 상태 갱신 - 성공")
    void updateStatus_batch_success() {
        // given
        List<String> contractNumbers = List.of("CTR001", "CTR002", "CTR003");
        HotbillStatus status = HotbillStatus.COMPLETED;

        // when
        statusService.updateStatus(contractNumbers, status);

        // then
        verify(hotbillStatusMapper, times(1)).updateStatusBatch(
            eq(contractNumbers),
            any(LocalDate.class),
            eq(status)
        );
    }

    @Test
    @DisplayName("완료되지 않은 계약 조회 - 미완료 계약이 있는 경우")
    void findNotCompletedContracts_withNotCompletedContracts() {
        // given
        List<String> contractNumbers = List.of("CTR001", "CTR002", "CTR003");
        List<String> notCompletedContracts = List.of("CTR001", "CTR003");

        when(hotbillStatusMapper.findNotCompletedContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        )).thenReturn(notCompletedContracts);

        // when
        List<String> result = statusService.findNotCompletedContracts(contractNumbers);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("CTR001", "CTR003");
        verify(hotbillStatusMapper, times(1)).findNotCompletedContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("완료되지 않은 계약 조회 - 모두 완료된 경우")
    void findNotCompletedContracts_allCompleted() {
        // given
        List<String> contractNumbers = List.of("CTR001", "CTR002");

        when(hotbillStatusMapper.findNotCompletedContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        )).thenReturn(List.of());

        // when
        List<String> result = statusService.findNotCompletedContracts(contractNumbers);

        // then
        assertThat(result).isEmpty();
        verify(hotbillStatusMapper, times(1)).findNotCompletedContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("정산되지 않은 계약 조회 - 미정산 계약이 있는 경우")
    void findNotSettledContracts_withNotSettledContracts() {
        // given
        List<String> contractNumbers = List.of("CTR001", "CTR002", "CTR003");
        List<String> notSettledContracts = List.of("CTR002");

        when(hotbillStatusMapper.findNotSettledContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        )).thenReturn(notSettledContracts);

        // when
        List<String> result = statusService.findNotSettledContracts(contractNumbers);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("CTR002");
        verify(hotbillStatusMapper, times(1)).findNotSettledContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("정산되지 않은 계약 조회 - 모두 정산된 경우")
    void findNotSettledContracts_allSettled() {
        // given
        List<String> contractNumbers = List.of("CTR001", "CTR002");

        when(hotbillStatusMapper.findNotSettledContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        )).thenReturn(List.of());

        // when
        List<String> result = statusService.findNotSettledContracts(contractNumbers);

        // then
        assertThat(result).isEmpty();
        verify(hotbillStatusMapper, times(1)).findNotSettledContracts(
            eq(contractNumbers),
            any(LocalDate.class)
        );
    }
}
