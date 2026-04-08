package com.billing.hotbill.api.dto;

import com.billing.hotbill.api.enums.ContractType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContractInfoTest {

    @Test
    void 계약정보_객체생성_테스트() {
        // given
        String contractNumber = "CTR001";
        ContractType contractType = ContractType.MOBILE_PHONE;

        // when
        ContractInfo contractInfo = new ContractInfo(contractNumber, contractType);

        // then
        assertNotNull(contractInfo);
        assertEquals(contractNumber, contractInfo.getContractNumber());
        assertEquals(contractType, contractInfo.getContractType());
    }

    @Test
    void 계약정보_필드가_null이면_안됨() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new ContractInfo(null, ContractType.MOBILE_PHONE);
        });

        assertThrows(NullPointerException.class, () -> {
            new ContractInfo("CTR001", null);
        });
    }
}
