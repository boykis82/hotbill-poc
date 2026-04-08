package com.billing.hotbill.api.dto;

import com.billing.hotbill.api.enums.ContractType;
import com.billing.hotbill.api.enums.HotbillUsecase;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotbillRequestTest {

    @Test
    void Hotbill요청_객체생성_테스트() {
        // given
        HotbillUsecase usecase = HotbillUsecase.SIMPLE_INQUIRY;
        List<ContractInfo> contracts = Arrays.asList(
            new ContractInfo("CTR001", ContractType.MOBILE_PHONE),
            new ContractInfo("CTR002", ContractType.INTERNET)
        );
        LocalDateTime requestDateTime = LocalDateTime.now();

        // when
        HotbillRequest request = new HotbillRequest(usecase, contracts, requestDateTime);

        // then
        assertNotNull(request);
        assertEquals(usecase, request.getUsecase());
        assertEquals(2, request.getContracts().size());
        assertEquals(requestDateTime, request.getRequestDateTime());
    }

    @Test
    void getContractNumbers_메서드_테스트() {
        // given
        List<ContractInfo> contracts = Arrays.asList(
            new ContractInfo("CTR001", ContractType.MOBILE_PHONE),
            new ContractInfo("CTR002", ContractType.INTERNET),
            new ContractInfo("CTR003", ContractType.IPTV)
        );
        HotbillRequest request = new HotbillRequest(
            HotbillUsecase.TERMINATION_INQUIRY,
            contracts,
            LocalDateTime.now()
        );

        // when
        List<String> contractNumbers = request.getContractNumbers();

        // then
        assertEquals(3, contractNumbers.size());
        assertEquals("CTR001", contractNumbers.get(0));
        assertEquals("CTR002", contractNumbers.get(1));
        assertEquals("CTR003", contractNumbers.get(2));
    }

    @Test
    void withContracts_메서드_새객체생성_테스트() {
        // given
        List<ContractInfo> originalContracts = Arrays.asList(
            new ContractInfo("CTR001", ContractType.MOBILE_PHONE),
            new ContractInfo("CTR002", ContractType.INTERNET)
        );
        HotbillRequest originalRequest = new HotbillRequest(
            HotbillUsecase.SIMPLE_INQUIRY,
            originalContracts,
            LocalDateTime.now()
        );

        List<ContractInfo> newContracts = List.of(
            new ContractInfo("CTR003", ContractType.IPTV)
        );

        // when
        HotbillRequest newRequest = originalRequest.withContracts(newContracts);

        // then
        assertNotSame(originalRequest, newRequest);
        assertEquals(originalRequest.getUsecase(), newRequest.getUsecase());
        assertEquals(originalRequest.getRequestDateTime(), newRequest.getRequestDateTime());
        assertEquals(1, newRequest.getContracts().size());
        assertEquals("CTR003", newRequest.getContracts().get(0).getContractNumber());

        // 원본은 변경되지 않음
        assertEquals(2, originalRequest.getContracts().size());
    }

    @Test
    void 필수필드가_null이면_예외발생() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new HotbillRequest(null, List.of(), LocalDateTime.now());
        });

        assertThrows(NullPointerException.class, () -> {
            new HotbillRequest(HotbillUsecase.SIMPLE_INQUIRY, null, LocalDateTime.now());
        });

        assertThrows(NullPointerException.class, () -> {
            new HotbillRequest(HotbillUsecase.SIMPLE_INQUIRY, List.of(), null);
        });
    }
}
