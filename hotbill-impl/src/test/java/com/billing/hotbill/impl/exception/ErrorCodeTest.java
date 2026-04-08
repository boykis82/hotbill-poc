package com.billing.hotbill.impl.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErrorCode 테스트
 */
@DisplayName("ErrorCode 테스트")
class ErrorCodeTest {

    @Test
    @DisplayName("CONTRACT_NOT_FOUND 에러코드가 존재해야 한다")
    void contractNotFound_ShouldExist() {
        // When
        ErrorCode errorCode = ErrorCode.CONTRACT_NOT_FOUND;

        // Then
        assertThat(errorCode).isNotNull();
        assertThat(errorCode.getCode()).isEqualTo("CONTRACT_NOT_FOUND");
        assertThat(errorCode.getMessage()).isEqualTo("계약을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("INVALID_STATUS 에러코드가 존재해야 한다")
    void invalidStatus_ShouldExist() {
        // When
        ErrorCode errorCode = ErrorCode.INVALID_STATUS;

        // Then
        assertThat(errorCode).isNotNull();
        assertThat(errorCode.getCode()).isEqualTo("INVALID_STATUS");
        assertThat(errorCode.getMessage()).isEqualTo("유효하지 않은 상태입니다");
    }

    @Test
    @DisplayName("CALCULATION_NOT_COMPLETED 에러코드가 존재해야 한다")
    void calculationNotCompleted_ShouldExist() {
        // When
        ErrorCode errorCode = ErrorCode.CALCULATION_NOT_COMPLETED;

        // Then
        assertThat(errorCode).isNotNull();
        assertThat(errorCode.getCode()).isEqualTo("CALCULATION_NOT_COMPLETED");
        assertThat(errorCode.getMessage()).isEqualTo("요금계산이 완료되지 않았습니다");
    }

    @Test
    @DisplayName("SETTLEMENT_NOT_COMPLETED 에러코드가 존재해야 한다")
    void settlementNotCompleted_ShouldExist() {
        // When
        ErrorCode errorCode = ErrorCode.SETTLEMENT_NOT_COMPLETED;

        // Then
        assertThat(errorCode).isNotNull();
        assertThat(errorCode.getCode()).isEqualTo("SETTLEMENT_NOT_COMPLETED");
        assertThat(errorCode.getMessage()).isEqualTo("해지정산이 완료되지 않았습니다");
    }

    @Test
    @DisplayName("EXTERNAL_SYSTEM_ERROR 에러코드가 존재해야 한다")
    void externalSystemError_ShouldExist() {
        // When
        ErrorCode errorCode = ErrorCode.EXTERNAL_SYSTEM_ERROR;

        // Then
        assertThat(errorCode).isNotNull();
        assertThat(errorCode.getCode()).isEqualTo("EXTERNAL_SYSTEM_ERROR");
        assertThat(errorCode.getMessage()).isEqualTo("외부 시스템 연동 오류");
    }

    @Test
    @DisplayName("모든 ErrorCode는 코드와 메시지를 가져야 한다")
    void allErrorCodes_ShouldHaveCodeAndMessage() {
        // When & Then
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertThat(errorCode.getCode()).isNotBlank();
            assertThat(errorCode.getMessage()).isNotBlank();
        }
    }

    @Test
    @DisplayName("총 5개의 에러코드가 존재해야 한다")
    void shouldHaveFiveErrorCodes() {
        // When
        ErrorCode[] errorCodes = ErrorCode.values();

        // Then
        assertThat(errorCodes).hasSize(5);
    }
}
