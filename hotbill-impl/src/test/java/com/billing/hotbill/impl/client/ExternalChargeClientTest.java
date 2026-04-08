package com.billing.hotbill.impl.client;

import com.billing.hotbill.api.enums.HotbillUsecase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExternalChargeClient 인터페이스 테스트
 */
class ExternalChargeClientTest {

    @Test
    void 인터페이스_메서드_시그니처_확인() {
        // Given
        class TestClient implements ExternalChargeClient {
            @Override
            public void requestChargeDataAsync(List<String> contractNumbers, HotbillUsecase usecase) {
                // 테스트용 구현
            }
        }

        // When
        ExternalChargeClient client = new TestClient();

        // Then
        assertNotNull(client);
        assertDoesNotThrow(() ->
            client.requestChargeDataAsync(
                List.of("CTR001", "CTR002"),
                HotbillUsecase.TERMINATION_INQUIRY
            )
        );
    }

    @Test
    void requestChargeDataAsync_메서드는_계약번호_리스트와_usecase를_받는다() {
        // Given
        class TestClient implements ExternalChargeClient {
            private List<String> receivedContractNumbers;
            private HotbillUsecase receivedUsecase;

            @Override
            public void requestChargeDataAsync(List<String> contractNumbers, HotbillUsecase usecase) {
                this.receivedContractNumbers = contractNumbers;
                this.receivedUsecase = usecase;
            }

            public List<String> getReceivedContractNumbers() {
                return receivedContractNumbers;
            }

            public HotbillUsecase getReceivedUsecase() {
                return receivedUsecase;
            }
        }

        TestClient client = new TestClient();
        List<String> contractNumbers = List.of("CTR001", "CTR002");
        HotbillUsecase usecase = HotbillUsecase.SIMPLE_INQUIRY;

        // When
        client.requestChargeDataAsync(contractNumbers, usecase);

        // Then
        assertEquals(contractNumbers, client.getReceivedContractNumbers());
        assertEquals(usecase, client.getReceivedUsecase());
    }
}
