package com.billing.hotbill.impl.client;

import com.billing.hotbill.api.enums.HotbillUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ContentChargeClient 테스트
 */
@ExtendWith(MockitoExtension.class)
class ContentChargeClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ContentChargeClient contentChargeClient;

    private static final String CONTENT_API_URL = "http://localhost:8080/api/content/charge";

    @BeforeEach
    void setUp() {
        contentChargeClient = new ContentChargeClient(restTemplate, CONTENT_API_URL);
    }

    @Test
    void requestChargeDataAsync_REST_API_호출_테스트() {
        // Given
        List<String> contractNumbers = List.of("CTR001", "CTR002");
        HotbillUsecase usecase = HotbillUsecase.TERMINATION_INQUIRY;

        // When
        contentChargeClient.requestChargeDataAsync(contractNumbers, usecase);

        // Then
        verify(restTemplate, times(1)).exchange(
            eq(CONTENT_API_URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Void.class)
        );
    }

    @Test
    void requestChargeDataAsync_여러_계약_일괄_요청_테스트() {
        // Given
        List<String> contractNumbers = List.of("CTR001", "CTR002", "CTR003", "CTR004", "CTR005");
        HotbillUsecase usecase = HotbillUsecase.SIMPLE_INQUIRY;

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // When
        contentChargeClient.requestChargeDataAsync(contractNumbers, usecase);

        // Then
        verify(restTemplate).exchange(
            eq(CONTENT_API_URL),
            eq(HttpMethod.POST),
            requestCaptor.capture(),
            eq(Void.class)
        );

        // 요청 바디 검증
        HttpEntity<?> capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> requestBody = (Map<String, Object>) capturedRequest.getBody();

        @SuppressWarnings("unchecked")
        List<String> capturedContractNumbers = (List<String>) requestBody.get("contractNumbers");

        assertEquals(5, capturedContractNumbers.size());
        assertEquals(contractNumbers, capturedContractNumbers);
        assertEquals(usecase.name(), requestBody.get("usecase"));
    }

    @Test
    void requestChargeDataAsync_빈_계약번호_리스트_처리() {
        // Given
        List<String> contractNumbers = List.of();
        HotbillUsecase usecase = HotbillUsecase.PORT_OUT;

        // When
        contentChargeClient.requestChargeDataAsync(contractNumbers, usecase);

        // Then
        verify(restTemplate, times(1)).exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Void.class)
        );
    }

    @Test
    void requestChargeDataAsync_비동기_즉시_반환() {
        // Given
        List<String> contractNumbers = List.of("CTR001");
        HotbillUsecase usecase = HotbillUsecase.FORCED_TERMINATION;

        // When - 예외가 발생하지 않아야 함
        assertDoesNotThrow(() ->
            contentChargeClient.requestChargeDataAsync(contractNumbers, usecase)
        );

        // Then - API 호출 확인
        verify(restTemplate, times(1)).exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(Void.class)
        );
    }
}
