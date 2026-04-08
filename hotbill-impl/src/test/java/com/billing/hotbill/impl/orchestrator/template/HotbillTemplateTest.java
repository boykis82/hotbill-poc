package com.billing.hotbill.impl.orchestrator.template;

import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.api.enums.HotbillUsecase;
import com.billing.hotbill.impl.service.StatusService;
import com.billing.hotbill.impl.strategy.chargedata.ChargeDataStrategy;
import com.billing.hotbill.impl.strategy.chargedata.ChargeDataStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HotbillTemplate 추상 클래스 테스트")
class HotbillTemplateTest {

    @Mock
    private StatusService statusService;

    @Mock
    private ChargeDataStrategyFactory chargeDataStrategyFactory;

    @Mock
    private ChargeDataStrategy syncStrategy;

    @Mock
    private ChargeDataStrategy asyncStrategy;

    private TestHotbillTemplate hotbillTemplate;

    @BeforeEach
    void setUp() {
        hotbillTemplate = new TestHotbillTemplate(
            statusService,
            chargeDataStrategyFactory
        );
    }

    @Test
    @DisplayName("executeHotbill()은 validate()를 호출해야 한다")
    void executeHotbill_should_call_validate() {
        // Given
        HotbillRequest request = createTestRequest(
            ContractType.INTERNET, ContractType.INTERNET
        );

        when(chargeDataStrategyFactory.getSyncStrategy()).thenReturn(syncStrategy);
        when(chargeDataStrategyFactory.determineProcessingType(any(), any()))
            .thenReturn(ProcessingType.SYNC);
        when(syncStrategy.process(any())).thenReturn(createEmptySyncResult());

        // When
        hotbillTemplate.executeHotbill(request);

        // Then
        assertThat(hotbillTemplate.validateCalled).isTrue();
    }

    @Test
    @DisplayName("executeHotbill()은 상태를 REQUESTING으로 초기화해야 한다")
    void executeHotbill_should_initialize_status_to_requesting() {
        // Given
        HotbillRequest request = createTestRequest(
            ContractType.INTERNET, ContractType.INTERNET
        );

        when(chargeDataStrategyFactory.getSyncStrategy()).thenReturn(syncStrategy);
        when(chargeDataStrategyFactory.determineProcessingType(any(), any()))
            .thenReturn(ProcessingType.SYNC);
        when(syncStrategy.process(any())).thenReturn(createEmptySyncResult());

        // When
        hotbillTemplate.executeHotbill(request);

        // Then
        verify(statusService).updateStatus(
            request.getContractNumbers(),
            HotbillStatus.REQUESTING
        );
    }

    @Test
    @DisplayName("executeHotbill()은 계약을 동기/비동기로 그룹핑해야 한다")
    void executeHotbill_should_group_contracts_by_processing_type() {
        // Given
        HotbillRequest request = createMixedRequest();

        when(chargeDataStrategyFactory.determineProcessingType(ContractType.MOBILE_PHONE, HotbillUsecase.SIMPLE_INQUIRY))
            .thenReturn(ProcessingType.ASYNC);
        when(chargeDataStrategyFactory.determineProcessingType(ContractType.INTERNET, HotbillUsecase.SIMPLE_INQUIRY))
            .thenReturn(ProcessingType.SYNC);

        when(chargeDataStrategyFactory.getAsyncStrategy()).thenReturn(asyncStrategy);
        when(chargeDataStrategyFactory.getSyncStrategy()).thenReturn(syncStrategy);
        when(asyncStrategy.process(any())).thenReturn(HotbillResult.asyncProcessing(Arrays.asList("CTR001")));
        when(syncStrategy.process(any())).thenReturn(createEmptySyncResult());

        // When
        HotbillResult result = hotbillTemplate.executeHotbill(request);

        // Then
        verify(asyncStrategy).process(any());
        verify(syncStrategy).process(any());
    }

    @Test
    @DisplayName("executeHotbill()은 동기 Strategy만 호출해야 한다 (모두 인터넷)")
    void executeHotbill_should_call_only_sync_strategy_for_internet() {
        // Given
        HotbillRequest request = createTestRequest(
            ContractType.INTERNET, ContractType.INTERNET
        );

        when(chargeDataStrategyFactory.determineProcessingType(any(), any()))
            .thenReturn(ProcessingType.SYNC);
        when(chargeDataStrategyFactory.getSyncStrategy()).thenReturn(syncStrategy);
        when(syncStrategy.process(any())).thenReturn(createEmptySyncResult());

        // When
        hotbillTemplate.executeHotbill(request);

        // Then
        verify(syncStrategy).process(any());
        verify(asyncStrategy, never()).process(any());
    }

    @Test
    @DisplayName("executeHotbill()은 비동기 Strategy만 호출해야 한다 (모두 이동전화)")
    void executeHotbill_should_call_only_async_strategy_for_mobile() {
        // Given
        HotbillRequest request = createTestRequest(
            ContractType.MOBILE_PHONE, ContractType.MOBILE_PHONE
        );

        when(chargeDataStrategyFactory.determineProcessingType(any(), any()))
            .thenReturn(ProcessingType.ASYNC);
        when(chargeDataStrategyFactory.getAsyncStrategy()).thenReturn(asyncStrategy);
        when(asyncStrategy.process(any())).thenReturn(HotbillResult.asyncProcessing(Arrays.asList("CTR001", "CTR002")));

        // When
        hotbillTemplate.executeHotbill(request);

        // Then
        verify(asyncStrategy).process(any());
        verify(syncStrategy, never()).process(any());
    }

    @Test
    @DisplayName("executeHotbill()은 결과를 merge해야 한다")
    void executeHotbill_should_merge_results() {
        // Given
        HotbillRequest request = createMixedRequest();

        when(chargeDataStrategyFactory.determineProcessingType(ContractType.MOBILE_PHONE, HotbillUsecase.SIMPLE_INQUIRY))
            .thenReturn(ProcessingType.ASYNC);
        when(chargeDataStrategyFactory.determineProcessingType(ContractType.INTERNET, HotbillUsecase.SIMPLE_INQUIRY))
            .thenReturn(ProcessingType.SYNC);

        when(chargeDataStrategyFactory.getAsyncStrategy()).thenReturn(asyncStrategy);
        when(chargeDataStrategyFactory.getSyncStrategy()).thenReturn(syncStrategy);

        HotbillResult asyncResult = HotbillResult.asyncProcessing(Arrays.asList("CTR001"));
        HotbillResult syncResult = createEmptySyncResult();

        when(asyncStrategy.process(any())).thenReturn(asyncResult);
        when(syncStrategy.process(any())).thenReturn(syncResult);

        // When
        HotbillResult result = hotbillTemplate.executeHotbill(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAsyncProcessing()).isTrue();
    }

    @Test
    @DisplayName("executeHotbill()은 postProcess()를 호출해야 한다")
    void executeHotbill_should_call_postProcess() {
        // Given
        HotbillRequest request = createTestRequest(
            ContractType.INTERNET, ContractType.INTERNET
        );

        when(chargeDataStrategyFactory.getSyncStrategy()).thenReturn(syncStrategy);
        when(chargeDataStrategyFactory.determineProcessingType(any(), any()))
            .thenReturn(ProcessingType.SYNC);
        when(syncStrategy.process(any())).thenReturn(createEmptySyncResult());

        // When
        hotbillTemplate.executeHotbill(request);

        // Then
        assertThat(hotbillTemplate.postProcessCalled).isTrue();
    }

    // 테스트용 구체 클래스
    static class TestHotbillTemplate extends HotbillTemplate {
        boolean validateCalled = false;
        boolean postProcessCalled = false;

        public TestHotbillTemplate(
                StatusService statusService,
                ChargeDataStrategyFactory chargeDataStrategyFactory) {
            super(statusService, chargeDataStrategyFactory);
        }

        @Override
        protected void validate(HotbillRequest request) {
            validateCalled = true;
        }

        @Override
        protected void postProcess(HotbillRequest request, HotbillResult result) {
            postProcessCalled = true;
        }
    }

    private HotbillRequest createTestRequest(ContractType... types) {
        List<ContractInfo> contracts = Arrays.stream(types)
            .map((type) -> {
                int index = Arrays.asList(types).indexOf(type) + 1;
                String contractNumber = "CTR" + String.format("%03d", index);
                return new ContractInfo(contractNumber, type);
            })
            .toList();

        return new HotbillRequest(
            HotbillUsecase.SIMPLE_INQUIRY,
            contracts,
            LocalDateTime.now()
        );
    }

    private HotbillRequest createMixedRequest() {
        ContractInfo contract1 = new ContractInfo("CTR001", ContractType.MOBILE_PHONE);
        ContractInfo contract2 = new ContractInfo("CTR002", ContractType.INTERNET);

        return new HotbillRequest(
            HotbillUsecase.SIMPLE_INQUIRY,
            Arrays.asList(contract1, contract2),
            LocalDateTime.now()
        );
    }

    private HotbillResult createEmptySyncResult() {
        return new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );
    }
}
