package com.billing.hotbill.impl.orchestrator;

import com.billing.hotbill.api.dto.ContractInfo;
import com.billing.hotbill.api.dto.HotbillRequest;
import com.billing.hotbill.api.dto.HotbillResult;
import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import com.billing.hotbill.impl.orchestrator.template.HotbillTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HotbillOrchestrator 테스트")
class HotbillOrchestratorTest {

    @Mock
    private HotbillTemplate hotbillTemplate;

    private HotbillOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new HotbillOrchestrator(hotbillTemplate);
    }

    @Test
    @DisplayName("executeHotbill()은 HotbillTemplate.executeHotbill()을 호출해야 한다")
    void executeHotbill_should_delegate_to_template() {
        // Given
        HotbillRequest request = createTestRequest();
        HotbillResult expectedResult = createTestResult();

        when(hotbillTemplate.executeHotbill(request)).thenReturn(expectedResult);

        // When
        HotbillResult actualResult = orchestrator.executeHotbill(request);

        // Then
        verify(hotbillTemplate).executeHotbill(request);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("executeHotbill()은 template의 결과를 그대로 반환해야 한다")
    void executeHotbill_should_return_template_result() {
        // Given
        HotbillRequest request = createTestRequest();
        HotbillResult expectedResult = createTestResult();

        when(hotbillTemplate.executeHotbill(any())).thenReturn(expectedResult);

        // When
        HotbillResult actualResult = orchestrator.executeHotbill(request);

        // Then
        assertThat(actualResult).isSameAs(expectedResult);
    }

    @Test
    @DisplayName("executeHotbill()은 request를 그대로 template에 전달해야 한다")
    void executeHotbill_should_pass_request_to_template() {
        // Given
        HotbillRequest request = createTestRequest();
        HotbillResult result = createTestResult();

        when(hotbillTemplate.executeHotbill(request)).thenReturn(result);

        // When
        orchestrator.executeHotbill(request);

        // Then
        verify(hotbillTemplate).executeHotbill(request);
        verify(hotbillTemplate, never()).executeHotbill(argThat(r -> r != request));
    }

    private HotbillRequest createTestRequest() {
        ContractInfo contract = new ContractInfo("CTR001", ContractType.INTERNET);

        return new HotbillRequest(
            HotbillUsecase.SIMPLE_INQUIRY,
            Arrays.asList(contract),
            LocalDateTime.now()
        );
    }

    private HotbillResult createTestResult() {
        return new HotbillResult(
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );
    }
}
