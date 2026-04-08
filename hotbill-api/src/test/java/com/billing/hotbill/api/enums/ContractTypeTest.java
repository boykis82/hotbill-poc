package com.billing.hotbill.api.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContractTypeTest {

    @Test
    void 모든_계약유형이_존재해야_한다() {
        // Given & When
        ContractType[] types = ContractType.values();

        // Then
        assertEquals(3, types.length, "3개의 계약 유형이 존재해야 함");

        assertNotNull(ContractType.MOBILE_PHONE);
        assertNotNull(ContractType.INTERNET);
        assertNotNull(ContractType.IPTV);
    }

    @Test
    void 각_계약유형은_설명을_가져야_한다() {
        // Given & When & Then
        assertEquals("이동전화", ContractType.MOBILE_PHONE.getDescription());
        assertEquals("인터넷", ContractType.INTERNET.getDescription());
        assertEquals("IPTV", ContractType.IPTV.getDescription());
    }
}
