package com.billing.hotbill.impl.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("HotbillException을 처리하고 적절한 에러 응답을 반환해야 한다")
    void handleHotbillException_ShouldReturnErrorResponse() {
        // Given
        HotbillException exception = new HotbillException(
            ErrorCode.CONTRACT_NOT_FOUND,
            "계약번호 CTR001을 찾을 수 없습니다"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHotbillException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CONTRACT_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("계약번호 CTR001을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("INVALID_STATUS 예외를 400 상태코드로 반환해야 한다")
    void handleInvalidStatus_ShouldReturn400() {
        // Given
        HotbillException exception = new HotbillException(ErrorCode.INVALID_STATUS);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHotbillException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_STATUS");
    }

    @Test
    @DisplayName("CALCULATION_NOT_COMPLETED 예외를 409 상태코드로 반환해야 한다")
    void handleCalculationNotCompleted_ShouldReturn409() {
        // Given
        HotbillException exception = new HotbillException(ErrorCode.CALCULATION_NOT_COMPLETED);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHotbillException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CALCULATION_NOT_COMPLETED");
    }

    @Test
    @DisplayName("EXTERNAL_SYSTEM_ERROR 예외를 502 상태코드로 반환해야 한다")
    void handleExternalSystemError_ShouldReturn502() {
        // Given
        HotbillException exception = new HotbillException(
            ErrorCode.EXTERNAL_SYSTEM_ERROR,
            "과금자료 시스템 연동 실패"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHotbillException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("EXTERNAL_SYSTEM_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("과금자료 시스템 연동 실패");
    }

    @Test
    @DisplayName("일반 Exception을 처리하고 500 상태코드를 반환해야 한다")
    void handleGeneralException_ShouldReturn500() {
        // Given
        Exception exception = new RuntimeException("예상치 못한 오류");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).contains("예상치 못한 오류");
    }

    @Test
    @DisplayName("IllegalArgumentException을 처리하고 400 상태코드를 반환해야 한다")
    void handleIllegalArgumentException_ShouldReturn400() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("잘못된 파라미터");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo("잘못된 파라미터");
    }

    @Test
    @DisplayName("ErrorResponse는 타임스탬프를 포함해야 한다")
    void errorResponse_ShouldIncludeTimestamp() {
        // Given
        HotbillException exception = new HotbillException(ErrorCode.CONTRACT_NOT_FOUND);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHotbillException(exception);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
