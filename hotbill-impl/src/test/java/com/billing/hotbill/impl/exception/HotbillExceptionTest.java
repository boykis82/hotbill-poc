package com.billing.hotbill.impl.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HotbillException 테스트
 */
@DisplayName("HotbillException 테스트")
class HotbillExceptionTest {

    @Test
    @DisplayName("ErrorCode와 메시지로 예외를 생성할 수 있어야 한다")
    void createException_WithErrorCodeAndMessage() {
        // Given
        ErrorCode errorCode = ErrorCode.CONTRACT_NOT_FOUND;
        String message = "계약을 찾을 수 없습니다";

        // When
        HotbillException exception = new HotbillException(errorCode, message);

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("ErrorCode만으로 예외를 생성할 수 있어야 한다")
    void createException_WithErrorCodeOnly() {
        // Given
        ErrorCode errorCode = ErrorCode.INVALID_STATUS;

        // When
        HotbillException exception = new HotbillException(errorCode);

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("원인 예외와 함께 생성할 수 있어야 한다")
    void createException_WithCause() {
        // Given
        ErrorCode errorCode = ErrorCode.EXTERNAL_SYSTEM_ERROR;
        String message = "외부 시스템 연동 오류";
        Throwable cause = new RuntimeException("Connection timeout");

        // When
        HotbillException exception = new HotbillException(errorCode, message, cause);

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("RuntimeException을 상속해야 한다")
    void shouldExtendRuntimeException() {
        // Given
        HotbillException exception = new HotbillException(ErrorCode.CONTRACT_NOT_FOUND);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
